package com.zoewave.probase.ashbike.features.main.service

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.model.bike.BikeRideInfo
import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.ashbike.model.bike.RideState
import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.BikeRideRepo
import com.zoewave.probase.ashbike.database.RideLocationEntity
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import com.zoewave.probase.ashbike.database.repository.UserProfileRepository
import com.zoewave.probase.ashbike.features.main.usecase.CalculateCaloriesUseCase
import com.zoewave.probase.ashbike.features.main.usecase.UserStats
import com.zoewave.probase.core.data.repository.sensor.heart.HeartRateRepository
import com.zoewave.probase.core.model.location.GpsFix
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max
import kotlin.random.Random
import com.google.android.gms.location.LocationRequest as GmsLocationRequest

@AndroidEntryPoint
class BikeForegroundService : LifecycleService() {

    // --- Injections ---
    @Inject lateinit var repo: BikeRideRepo
    @Inject lateinit var calculateCaloriesUseCase: CalculateCaloriesUseCase
    @Inject lateinit var userProfileRepository: UserProfileRepository
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var bikeRepository: BikeRepository
    @Inject lateinit var heartRateRepository: HeartRateRepository

    // --- System & Hardware ---
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationProcessingThread: HandlerThread
    private lateinit var backgroundLooper: Looper
    private lateinit var notificationManager: BikeNotificationManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val binder = LocalBinder()

    // --- Data Flows ---
    private lateinit var currentEnergyLevelState: StateFlow<LocationEnergyLevel>
    private lateinit var userStatsFlow: Flow<UserStats>
    private val _rideInfo = MutableStateFlow(getInitialRideInfo())
    val rideInfo = _rideInfo.asStateFlow()

    // --- Session State (Mutable) ---
    // 1. Continuous (Freeride / Passive)
    private var continuousSessionStartTimeMillis: Long = 0L
    private var continuousDistanceMeters: Float = 0f
    private var continuousCaloriesBurned: Float = 0f
    private var continuousMaxSpeedKph: Double = 0.0
    private var continuousElevationGainMeters: Double = 0.0
    private var continuousElevationLossMeters: Double = 0.0

    // 2. Formal (Active Recording)
    private var currentFormalRideId: String? = null
    private val formalRideTrackPoints = mutableListOf<Location>()
    private var formalRideSegmentStartTimeMillis: Long = 0L
    private var formalRideSegmentStartOffsetDistanceMeters: Float = 0f
    private var formalRideSegmentMaxSpeedKph: Double = 0.0
    private var currentFormalRideHighestCalories: Int = 0
    private var formalRideElevationGainMeters: Double = 0.0
    private var formalRideElevationLossMeters: Double = 0.0

    private var caloriesCalculationJob: Job? = null
    private var currentActualGpsIntervalMillis: Long = 0L
    private var isDemoModeActive = false

    // --- Lifecycle ---

    inner class LocalBinder : Binder() {
        fun getService(): BikeForegroundService = this@BikeForegroundService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating Service...")

        // 1. Hardware Init
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationProcessingThread = HandlerThread("LocationProcessingThread").apply { start() }
        backgroundLooper = locationProcessingThread.looper
        notificationManager = BikeNotificationManager(this)

        // 2. WakeLock (Prevents CPU sleep during rides)
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AshBike:RecordingWakelock")

        // 3. Data Init
        continuousSessionStartTimeMillis = System.currentTimeMillis()

        currentEnergyLevelState = appSettingsRepository.gpsAccuracyFlow
            .stateIn(lifecycleScope, SharingStarted.Eagerly, LocationEnergyLevel.BALANCED)

        userStatsFlow = userProfileRepository.weightFlow.map { weightString ->
            UserStats(0f, weightString.toFloatOrNull() ?: 70f)
        }

        setupObservers()
        startOrRestartCalorieCalculation(isFormalRideActive = false)
    }

    private fun setupObservers() {
        // Heart Rate
        lifecycleScope.launch {
            heartRateRepository.heartRate.collect { bpm ->
                if (_rideInfo.value.heartbeat != bpm) {
                    _rideInfo.value = _rideInfo.value.copy(heartbeat = bpm)
                }
            }
        }

        // GPS Accuracy Settings
        lifecycleScope.launch {
            combine(currentEnergyLevelState, appSettingsRepository.longRideEnabledFlow) { level, isLong ->
                Pair(level, isLong)
            }.collect { (level, isLongRide) ->
                val isRiding = _rideInfo.value.rideState == RideState.Riding
                val interval = if (isRiding) level.activeRideIntervalMillis else level.passiveTrackingIntervalMillis
                val minInterval = if (isRiding) level.activeRideMinUpdateIntervalMillis else level.passiveTrackingMinUpdateIntervalMillis

                startLocationUpdates(interval, minInterval, isLongRide)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.action?.let { action ->
            Log.d(TAG, "Received Action: $action")
            when (action) {
                ACTION_START_RIDE -> startFormalRide()
                ACTION_STOP_RIDE -> stopAndFinalizeFormalRide()
                ACTION_RESET_DASHBOARD -> resetDashboardData()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationProcessingThread.quitSafely()
        wakeLock?.takeIf { it.isHeld }?.release()
    }

    // --- Core Logic ---

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(interval: Long, minInterval: Long, isLongRide: Boolean) {
        var actualInterval = if (isLongRide) interval * 2 else interval
        var actualMinInterval = if (isLongRide) minInterval * 2 else minInterval

        // Safety clamps
        actualInterval = max(actualInterval, MIN_ALLOWED_GPS_INTERVAL_MS)
        actualMinInterval = max(actualMinInterval, MIN_ALLOWED_GPS_INTERVAL_MS)
        currentActualGpsIntervalMillis = actualInterval

        val request = GmsLocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, actualInterval)
            .setMinUpdateIntervalMillis(actualMinInterval)
            .setMaxUpdateDelayMillis(actualInterval)
            .build()

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            fusedLocationClient.requestLocationUpdates(request, locationCallback, backgroundLooper)
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing", e)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { updateRideInfo(it) }
        }
    }

    private fun updateRideInfo(location: Location) {
        val speedKph = location.speed * 3.6
        val isFormalRide = _rideInfo.value.rideState == RideState.Riding
        var currentRidePath: List<GpsFix> = emptyList()

        // 1. Continuous (Freeride) Calculations
        continuousMaxSpeedKph = max(continuousMaxSpeedKph, speedKph)
        val prevLoc = _rideInfo.value.location

        if (prevLoc != null && (prevLoc.latitude != 0.0 || prevLoc.longitude != 0.0)) {
            val lastLoc = Location("").apply { latitude = prevLoc.latitude; longitude = prevLoc.longitude; altitude = _rideInfo.value.elevation }

            if (location.accuracy <= MAX_ACCURACY_THRESHOLD_METERS) {
                val dist = location.distanceTo(lastLoc)
                if (dist >= MIN_DISTANCE_THRESHOLD_METERS) {
                    continuousDistanceMeters += dist
                }
                if (location.hasAltitude() && lastLoc.hasAltitude()) {
                    val altChange = location.altitude - lastLoc.altitude
                    if (altChange > 0) {
                        continuousElevationGainMeters += altChange
                        if (isFormalRide) formalRideElevationGainMeters += altChange
                    } else if (altChange < 0) {
                        continuousElevationLossMeters += -altChange
                        if (isFormalRide) formalRideElevationLossMeters += -altChange
                    }
                }
            }
        }

        // 2. Determine Display Values
        val displayDistanceKm: Float
        val displayCalories: Int
        val displayDuration: String
        val displayMaxSpeed: Double
        val displayAverageSpeed: Double // ✅ We will calculate this now

        if (isFormalRide) {
            // --- ACTIVE MODE ---
            formalRideTrackPoints.add(location)

            val segmentDistMeters = continuousDistanceMeters - formalRideSegmentStartOffsetDistanceMeters
            val segmentDurationMillis = System.currentTimeMillis() - formalRideSegmentStartTimeMillis
            val segmentDurationSeconds = segmentDurationMillis / 1000.0

            displayDistanceKm = segmentDistMeters / 1000f
            displayCalories = _rideInfo.value.caloriesBurned
            displayDuration = formatDuration(segmentDurationMillis)

            formalRideSegmentMaxSpeedKph = max(formalRideSegmentMaxSpeedKph, speedKph)
            displayMaxSpeed = formalRideSegmentMaxSpeedKph

            displayAverageSpeed = if (segmentDurationSeconds > 0 && segmentDistMeters > 0) {
                (segmentDistMeters / segmentDurationSeconds) * 3.6
            } else {
                0.0
            }

            // Map path for UI
            currentRidePath = formalRideTrackPoints.map { loc ->
                GpsFix(loc.latitude, loc.longitude, loc.time, if(loc.hasAltitude()) loc.altitude else null, if(loc.hasSpeed()) loc.speed else null, if(loc.hasAccuracy()) loc.accuracy else null)
            }
        } else {
            // --- PASSIVE MODE (Freeride) ---
            val sessionDurationMillis = System.currentTimeMillis() - continuousSessionStartTimeMillis
            val sessionDurationSeconds = sessionDurationMillis / 1000.0

            displayDistanceKm = continuousDistanceMeters / 1000f
            displayCalories = continuousCaloriesBurned.toInt()
            displayDuration = formatDuration(sessionDurationMillis)
            displayMaxSpeed = continuousMaxSpeedKph

            displayAverageSpeed = if (sessionDurationSeconds > 0 && continuousDistanceMeters > 0) {
                (continuousDistanceMeters / sessionDurationSeconds) * 3.6
            } else {
                0.0
            }
        }

        // 3. Update State
        val newInfo = _rideInfo.value.copy(
            location = LatLng(location.latitude, location.longitude),
            currentSpeed = speedKph,
            averageSpeed = displayAverageSpeed,
            currentTripDistance = displayDistanceKm,
            caloriesBurned = displayCalories,
            rideDuration = displayDuration,
            maxSpeed = displayMaxSpeed,
            elevation = location.altitude,
            elevationGain = if(isFormalRide) formalRideElevationGainMeters else continuousElevationGainMeters,
            elevationLoss = if(isFormalRide) formalRideElevationLossMeters else continuousElevationLossMeters,
            heading = if (location.hasBearing()) location.bearing else _rideInfo.value.heading,
            ridePath = currentRidePath,

            // This triggers the LaunchedEffect in your UI to flash the icon.
            lastGpsUpdateTime = System.currentTimeMillis(),
            gpsUpdateIntervalMillis = currentActualGpsIntervalMillis

        )

        _rideInfo.value = newInfo

        // 4. Update External Consumers
        lifecycleScope.launch { bikeRepository.updateRideInfo(newInfo) } // Update Glass/Repository

        if (isFormalRide) {
            notificationManager.updateNotification(BikeNotificationManager.NOTIFICATION_ID, newInfo)
        }
    }

    private fun startFormalRide() {
        if (_rideInfo.value.rideState == RideState.Riding) return

        Log.i(TAG, "Starting Active Ride")
        wakeLock?.acquire(10*60*60*1000L /*10 hours*/)

        // Reset Formal Counters
        currentFormalRideId = UUID.randomUUID().toString()
        formalRideSegmentStartTimeMillis = System.currentTimeMillis()
        formalRideTrackPoints.clear()
        formalRideSegmentStartOffsetDistanceMeters = continuousDistanceMeters
        formalRideSegmentMaxSpeedKph = 0.0
        currentFormalRideHighestCalories = 0
        formalRideElevationGainMeters = 0.0
        formalRideElevationLossMeters = 0.0

        val newState = _rideInfo.value.copy(
            rideState = RideState.Riding,
            currentTripDistance = 0f,
            caloriesBurned = 0,
            rideDuration = "00:00",
            maxSpeed = 0.0,
            ridePath = emptyList()
        )

        _rideInfo.value = newState
        lifecycleScope.launch { bikeRepository.updateRideInfo(newState) }

        // Start Notification & High Accuracy GPS
        startForeground(BikeNotificationManager.NOTIFICATION_ID, notificationManager.buildNotification(newState))

        lifecycleScope.launch {
            val level = currentEnergyLevelState.first()
            startLocationUpdates(level.activeRideIntervalMillis, level.activeRideMinUpdateIntervalMillis, appSettingsRepository.longRideEnabledFlow.first())
        }

        startOrRestartCalorieCalculation(true)
    }

    private fun stopAndFinalizeFormalRide() {
        val rideId = currentFormalRideId
        if (_rideInfo.value.rideState != RideState.Riding || rideId == null) return

        Log.i(TAG, "Stopping Active Ride: $rideId")

        // 1. Prepare Data
        val finalDist = continuousDistanceMeters - formalRideSegmentStartOffsetDistanceMeters
        val duration = System.currentTimeMillis() - formalRideSegmentStartTimeMillis
        val avgSpeed = if (duration > 0 && finalDist > 0) (finalDist / (duration/1000.0)) * 3.6 else 0.0

        val rideEntity = BikeRideEntity(
            rideId = rideId,
            startTime = formalRideSegmentStartTimeMillis,
            endTime = System.currentTimeMillis(),
            totalDistance = finalDist,
            averageSpeed = avgSpeed.toFloat(),
            maxSpeed = formalRideSegmentMaxSpeedKph.toFloat(),
            startLat = formalRideTrackPoints.firstOrNull()?.latitude ?: 0.0,
            startLng = formalRideTrackPoints.firstOrNull()?.longitude ?: 0.0,
            endLat = formalRideTrackPoints.lastOrNull()?.latitude ?: 0.0,
            endLng = formalRideTrackPoints.lastOrNull()?.longitude ?: 0.0,
            elevationGain = formalRideElevationGainMeters.toFloat(),
            elevationLoss = formalRideElevationLossMeters.toFloat(),
            caloriesBurned = currentFormalRideHighestCalories,
            isHealthDataSynced = false,
            weatherCondition = _rideInfo.value.bikeWeatherInfo?.conditionDescription
        )

        val locEntities = formalRideTrackPoints.map { loc ->
            RideLocationEntity(
                // id is skipped (defaults to 0, auto-generated by Room)
                rideId = rideId,
                timestamp = loc.time,
                lat = loc.latitude,
                lng = loc.longitude,
                // Handle nullable Float? properly
                elevation = if (loc.hasAltitude()) loc.altitude.toFloat() else null
            )
        }
        lifecycleScope.launch {
            // 2. Save DB
            withContext(Dispatchers.IO) {
                repo.insertRideWithLocations(rideEntity, locEntities)
            }
            // 3. ✅ IMMEDIATE RESET (Stop + Zero)
            resetDashboardData()
        }
    }

    private fun resetDashboardData() {
        Log.i(TAG, "Resetting Dashboard to Zero (Passive Mode)")

        // 1. Zero out counters
        continuousSessionStartTimeMillis = System.currentTimeMillis()
        continuousDistanceMeters = 0f
        continuousCaloriesBurned = 0f
        continuousMaxSpeedKph = 0.0
        continuousElevationGainMeters = 0.0
        continuousElevationLossMeters = 0.0

        formalRideTrackPoints.clear()

        // 2. Stop Foreground / Release WakeLock
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(BikeNotificationManager.NOTIFICATION_ID)
        wakeLock?.takeIf { it.isHeld }?.release()

        // 3. Reset StateFlow (But keep location/connection)
        val resetState = getInitialRideInfo().copy(
            rideState = RideState.NotStarted,
            location = _rideInfo.value.location,
            isBikeConnected = _rideInfo.value.isBikeConnected,
            heartbeat = _rideInfo.value.heartbeat,
            ridePath = emptyList()
        )

        _rideInfo.value = resetState
        lifecycleScope.launch { bikeRepository.updateRideInfo(resetState) }

        // 4. Return GPS to Passive Mode
        lifecycleScope.launch {
            val level = currentEnergyLevelState.first()
            startLocationUpdates(level.passiveTrackingIntervalMillis, level.passiveTrackingMinUpdateIntervalMillis, appSettingsRepository.longRideEnabledFlow.first())
        }

        startOrRestartCalorieCalculation(false)
    }

    // --- Helpers ---

    private fun startOrRestartCalorieCalculation(isFormalRideActive: Boolean) {
        caloriesCalculationJob?.cancel()

        // Define inputs for UseCase
        val distanceFlow = _rideInfo.map {
            if(isFormalRideActive) (continuousDistanceMeters - formalRideSegmentStartOffsetDistanceMeters) / 1000f
            else continuousDistanceMeters / 1000f
        }
        val speedFlow = _rideInfo.map { it.currentSpeed.toFloat() }

        caloriesCalculationJob = lifecycleScope.launch {
            calculateCaloriesUseCase(distanceFlow, speedFlow, userStatsFlow).collect { cal ->
                if (isFormalRideActive) {
                    currentFormalRideHighestCalories = cal.toInt()
                    _rideInfo.value = _rideInfo.value.copy(caloriesBurned = currentFormalRideHighestCalories)
                } else {
                    continuousCaloriesBurned = cal
                    if (_rideInfo.value.rideState != RideState.Riding) {
                        _rideInfo.value = _rideInfo.value.copy(caloriesBurned = cal.toInt())
                    }
                }
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val s = (millis / 1000) % 60
        val m = (millis / (1000 * 60)) % 60
        val h = (millis / (1000 * 60 * 60))
        return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }

    // --- Demo Mode ---
    fun toggleDemoMode() {
        isDemoModeActive = !isDemoModeActive
        val current = _rideInfo.value
        _rideInfo.value = current.copy(
            isBikeConnected = isDemoModeActive,
            batteryLevel = if (isDemoModeActive) Random.nextInt(20, 90) else null
        )
    }

    companion object {
        const val TAG = "BikeForegroundService"
        private const val PKG_PREFIX = "com.zoewave.probase.ashbike.features.main.service."
        const val ACTION_START_RIDE = PKG_PREFIX + "action.START_RIDE"
        const val ACTION_STOP_RIDE = PKG_PREFIX + "action.STOP_RIDE"
        const val ACTION_RESET_DASHBOARD = PKG_PREFIX + "action.RESET_DASHBOARD"

        private const val MAX_ACCURACY_THRESHOLD_METERS = 30f
        private const val MIN_DISTANCE_THRESHOLD_METERS = 5f
        private const val MIN_ALLOWED_GPS_INTERVAL_MS = 1000L

        fun getInitialRideInfo() = BikeRideInfo(
            location = null, currentSpeed = 0.0, averageSpeed = 0.0, maxSpeed = 0.0,
            currentTripDistance = 0f, totalTripDistance = null, remainingDistance = null,
            elevationGain = 0.0, elevationLoss = 0.0, caloriesBurned = 0,
            rideDuration = "00:00", settings = persistentMapOf(), heading = 0f,
            elevation = 0.0, isBikeConnected = false, batteryLevel = null, motorPower = null,
            rideState = RideState.NotStarted, bikeWeatherInfo = null, heartbeat = null,
            gpsUpdateIntervalMillis = 0L, ridePath = emptyList()
        )
    }
}