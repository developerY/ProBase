package com.zoewave.probase.ashbike.wear.data.health.sensor

import android.annotation.SuppressLint
import android.os.Build
import android.os.Looper
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
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class WearEmulatorTrackingEngine @Inject constructor(
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
        // Start Health Services (Heart Rate ONLY, GPS disabled)
        val config = ExerciseConfig(
            exerciseType = ExerciseType.BIKING,
            dataTypes = setOf(DataType.HEART_RATE_BPM),
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = false
        )
        exerciseClient.setUpdateCallback(exerciseUpdateCallback)
        exerciseClient.startExerciseAsync(config)

        // Start Fused Location (Sees the Emulator Route perfectly)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(minIntervalMs)
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