package com.zoewave.probase.ashbike.wear.data.sync

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import com.zoewave.probase.ashbike.database.BikeRideRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RideAckListenerService : WearableListenerService() {

    @Inject lateinit var repo: BikeRideRepo
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Log.d(TAG, "⌚ WATCH ACK LISTENER WOKE UP!")

        for (event in dataEvents) {
            val path = event.dataItem.uri.path
            Log.d(TAG, "🔍 WATCH inspecting incoming path: $path")

            if (event.type == DataEvent.TYPE_CHANGED && path?.startsWith("/sync_ack/") == true) {
                // The path looks like: /sync_ack/ride_12345
                val rideId = path.substringAfterLast("/")
                Log.d(TAG, "📥 WATCH caught ACK for Ride ID: $rideId! Marking as synced...")

                serviceScope.launch {
                    try {
                        // NOTE: You will need to add this simple update function to your DAO/Repo
                        // e.g., @Query("UPDATE bike_rides SET isSynced = 1 WHERE rideId = :id")
                        repo.markRideAsAcknowledged(rideId)

                        Log.i(TAG, "✅ WATCH successfully marked Ride ID: $rideId as fully synced!")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ WATCH failed to update Room DB with ACK", e)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "AshBikeSync"
    }
}