package com.zoewave.probase.ashbike.wear.data.health.sensor

import android.annotation.SuppressLint
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.zoewave.ashbike.data.services.RideTrackingEngine
import com.zoewave.ashbike.model.bike.LocationPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
//class WearEmulatorTrackingEngine @Inject constructor(
class WearEmulatorTrackingEngine constructor( // removeDI
    private val exerciseClient: ExerciseClient,
    private val fusedLocationClient: FusedLocationProviderClient
) : RideTrackingEngine {

    private val _currentHeartRate = MutableStateFlow(0)
    override val currentHeartRate: StateFlow<Int> = _currentHeartRate.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    override val currentLocation: StateFlow<LocationPoint?> = _currentLocation.asStateFlow()

    // 1. Health Services Callback (HR Only for the emulator)
    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val hrData = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
            if (hrData.isNotEmpty()) {
                _currentHeartRate.value = hrData.last().value.toInt()
            }
        }
        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}
        override fun onRegistered() {}
        override fun onRegistrationFailed(throwable: Throwable) {}
        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {}
    }

    // 2. Fused Location Callback (GPS/Speed for the emulator route)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                // Calculate km/h for the log just so we can read it easily
                val rawSpeedMs = if (loc.hasSpeed()) loc.speed else -1f
                val speedKmh = if (loc.hasSpeed()) loc.speed * 3.6f else -1f

                Log.d("AshBikeDebug", "📍 SENSOR TICK | Time: ${loc.time} | Lat/Lng: ${loc.latitude}, ${loc.longitude}")
                Log.d("AshBikeDebug", "📍 SENSOR SPEED | HasSpeed: ${loc.hasSpeed()} | m/s: $rawSpeedMs | km/h: $speedKmh")

                _currentLocation.value = LocationPoint(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = if (loc.hasAltitude()) loc.altitude.toFloat() else null,
                    timestamp = loc.time,
                    speed = if (loc.hasSpeed()) loc.speed else null,
                    bearing = if (loc.hasBearing()) loc.bearing else null
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startRide(intervalMs: Long, minIntervalMs: Long) {

        // 1. Intercept and override the service's request for testing
        val emulatorIntervalMs = 1000L

        Log.d("AshBikeDebug", "🚀 EMULATOR OVERRIDE | Service asked for $intervalMs but forcing $emulatorIntervalMs")

        // Start Health Services (Heart Rate ONLY, GPS disabled)
        val config = ExerciseConfig(
            exerciseType = ExerciseType.BIKING,
            dataTypes = setOf(DataType.HEART_RATE_BPM),
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = false
        )
        exerciseClient.setUpdateCallback(exerciseUpdateCallback)
        exerciseClient.startExerciseAsync(config)

        // 2. Pass the forced 1-second interval to FusedLocation
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, emulatorIntervalMs)
            .setMinUpdateIntervalMillis(emulatorIntervalMs)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun stopRide() {
        // Shut everything down
        exerciseClient.endExerciseAsync()
        exerciseClient.clearUpdateCallbackAsync(exerciseUpdateCallback)
        fusedLocationClient.removeLocationUpdates(locationCallback)

        _currentHeartRate.value = 0
        _currentLocation.value = null
    }
}