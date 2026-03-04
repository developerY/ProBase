package com.zoewave.probase.ashbike.mobile.data.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.zoewave.ashbike.data.services.RideTrackingEngine
import com.zoewave.ashbike.model.bike.LocationPoint
import com.zoewave.probase.core.data.repository.sensor.heart.HeartRateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton
import com.google.android.gms.location.LocationRequest as GmsLocationRequest

@Singleton
class MobileLocationBleEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val heartRateRepository: HeartRateRepository
) : RideTrackingEngine {

    // --- Coroutines ---
    // Needed to convert the standard Flow into a StateFlow
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationProcessingThread: HandlerThread? = null
    private var backgroundLooper: Looper? = null

    override val currentHeartRate: StateFlow<Int> = heartRateRepository.heartRate
        .stateIn(
            scope = engineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    override val currentLocation: StateFlow<LocationPoint?> = _currentLocation.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                // Because this is a phone, GMS provides EVERYTHING in one object!
                _currentLocation.value = LocationPoint(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = if (loc.hasAltitude()) loc.altitude.toFloat() else null,
                    timestamp = loc.time
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startRide(intervalMs: Long, minIntervalMs: Long) {
        // Spin up the exact background thread we deleted from the service
        locationProcessingThread = HandlerThread("LocationProcessingThread").apply { start() }
        backgroundLooper = locationProcessingThread?.looper

        val request = GmsLocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(minIntervalMs)
            .setMaxUpdateDelayMillis(intervalMs)
            .build()

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            fusedLocationClient.requestLocationUpdates(request, locationCallback, backgroundLooper)
            Log.d("AshBike", "Mobile Engine started GPS tracking.")
        } catch (e: SecurityException) {
            Log.e("AshBike", "Location permission missing", e)
        }
    }

    override fun stopRide() {
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Safely kill the background thread
        locationProcessingThread?.quitSafely()
        locationProcessingThread = null
        backgroundLooper = null

        _currentLocation.value = null
        Log.d("AshBike", "Mobile Engine powered down.")
    }
}