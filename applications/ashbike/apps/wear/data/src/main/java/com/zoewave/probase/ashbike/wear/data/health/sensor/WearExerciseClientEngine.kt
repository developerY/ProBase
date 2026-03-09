package com.zoewave.probase.ashbike.wear.data.health.sensor

import android.os.Build
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
import androidx.health.services.client.data.LocationAvailability
import com.zoewave.ashbike.data.services.RideTrackingEngine
import com.zoewave.ashbike.model.bike.LocationPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class WearExerciseClientEngine @Inject constructor(
    private val exerciseClient: ExerciseClient
) : RideTrackingEngine {

    private val TAG = "AshBikeDebug"

    private val _currentHeartRate = MutableStateFlow(0)
    override val currentHeartRate: StateFlow<Int> = _currentHeartRate.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    override val currentLocation: StateFlow<LocationPoint?> = _currentLocation.asStateFlow()
    private var latestWatchSpeedMps: Float? = null

    /**
     * The callback that receives batched hardware updates from the watch's sensor chip.
     */
    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val metrics = update.latestMetrics
            Log.d(TAG, "🟢 onExerciseUpdateReceived fired! State: ${update.exerciseStateInfo.state}")

            // 1. Extract Heart Rate
            val hrData = metrics.getData(DataType.HEART_RATE_BPM)
            if (hrData.isNotEmpty()) {
                _currentHeartRate.value = hrData.last().value.toInt()
            }

            // 2. EXTRACT SPEED (This was missing!)
            // Health Services provides speed in meters per second (m/s)
            val speedData = metrics.getData(DataType.SPEED)
            if (speedData.isNotEmpty()) {
                // Grab the newest speed value and save it to our variable
                latestWatchSpeedMps = speedData.last().value.toFloat()
                Log.d(TAG, "⚡ SPEED Received: $latestWatchSpeedMps m/s")
            } else {
                Log.d(TAG, "⚡ SPEED Data: Empty in this batch")
            }

            // 3. Extract Location
            val locationData = metrics.getData(DataType.LOCATION)
            if (locationData.isNotEmpty()) {
                // Grab the wrapper
                val latestDataPoint = locationData.last()
                // Grab the spatial coordinates from inside the wrapper
                val latestLoc = latestDataPoint.value
                Log.d(TAG, "📍 LOCATION Received: Lat/Lng: ${latestLoc.latitude}, ${latestLoc.longitude}")

                // Convert the watch's boot-relative time to a standard Unix Epoch timestamp
                val bootTimeOffset = System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime()
                val locationTimestamp = bootTimeOffset + latestDataPoint.timeDurationFromBoot.toMillis()

                _currentLocation.value = LocationPoint(
                    latitude = latestLoc.latitude,
                    longitude = latestLoc.longitude,
                    altitude = latestLoc.altitude?.toFloat(),
                    timestamp = locationTimestamp,
                    // Now, this actually contains the real speed instead of 'null'!
                    speed = latestWatchSpeedMps,
                    bearing = latestLoc.bearing.toFloat()
                )
            } else {
                Log.d(TAG, "📍 LOCATION Data: Empty in this batch")
            }
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            // Optional: Handle auto-laps if you configure them
        }

        override fun onRegistered() {
            Log.d("AshBike", "ExerciseClient successfully registered.")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.e("AshBike", "Failed to register ExerciseClient", throwable)
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            Log.d(TAG, "📡 AVAILABILITY CHANGED | DataType: ${dataType.name} | Availability: $availability")

            if (dataType == DataType.LOCATION) {
                // Notice we dropped the 'is' keyword because these are static instances, not subclasses!
                when (availability) {
                    LocationAvailability.ACQUIRED_UNTETHERED -> Log.d(TAG, "📡 GPS is ACQUIRED (Using Watch Hardware/Emulator)")
                    LocationAvailability.ACQUIRED_TETHERED -> Log.d(TAG, "📡 GPS is ACQUIRED (Tethered to Phone - THIS MIGHT BE OUR BUG!)")
                    LocationAvailability.ACQUIRING -> Log.d(TAG, "📡 GPS is ACQUIRING (Searching for satellites or mock data...)")
                    LocationAvailability.UNAVAILABLE -> Log.w(TAG, "📡 GPS is UNAVAILABLE (Hardware blocked or missing permissions)")
                    LocationAvailability.NO_GNSS -> Log.w(TAG, "📡 GPS is NO_GNSS (On-device location toggle is OFF in emulator settings)")
                    LocationAvailability.UNKNOWN -> Log.w(TAG, "📡 GPS is UNKNOWN")
                    else -> Log.w(TAG, "📡 GPS Availability Unmapped: $availability")
                }
            }
        }
    }

    // ✅ ADDED THE MISSING INTERVAL PARAMETERS HERE
    override fun startRide(intervalMs: Long, minIntervalMs: Long) {
        Log.d(TAG, "🚀 startRide() called. Requesting Health Services...")
        // 1. Define what we are doing.
        // Notice we ignore the intervalMs because Health Services handles its
        // own battery optimizations based on ExerciseType.BIKING.
        val config = ExerciseConfig(
            exerciseType = ExerciseType.BIKING,
            dataTypes = setOf(
                DataType.HEART_RATE_BPM,
                DataType.LOCATION,
                DataType.DISTANCE_TOTAL,
                DataType.SPEED
            ),
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = true
        )

        // 2. Register the callback to start receiving data
        exerciseClient.setUpdateCallback(exerciseUpdateCallback)

        // 3. Command the hardware to start the tracking session
        exerciseClient.startExerciseAsync(config)
        Log.d(TAG, "🚀 startRide() complete")
    }

    override fun stopRide() {
        Log.d(TAG, "🛑 stopRide() called")
        // Command the hardware to shut down the GPS and sensors to save battery
        exerciseClient.endExerciseAsync()

        // Clear the callback so we stop receiving ghost data
        exerciseClient.clearUpdateCallbackAsync(exerciseUpdateCallback)

        // Reset flows
        _currentHeartRate.value = 0
        _currentLocation.value = null
    }
}