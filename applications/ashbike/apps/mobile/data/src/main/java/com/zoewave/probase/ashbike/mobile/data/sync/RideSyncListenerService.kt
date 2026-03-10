package com.zoewave.probase.ashbike.mobile.data.sync

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.BikeRideRepo
import com.zoewave.probase.ashbike.database.RideLocationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class RideSyncListenerService : WearableListenerService() {

    // Hilt perfectly injects your Room DB repository!
    @Inject lateinit var repo: BikeRideRepo

    private val gson = GsonBuilder()
        .serializeSpecialFloatingPointValues()
        .create()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        Log.d(TAG, "🟢 PHONE LISTENER WOKE UP! Incoming data events count: ${dataEvents.count}")

        for (event in dataEvents) {
            // We only care about new or updated data
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                Log.d(TAG, "🔍 PHONE inspecting incoming path: $path")

                // Check if this matches the exact path the watch used to pitch the data
                if (path != null && path.startsWith("/completed_ride/")) {
                    Log.d(TAG, "📥 PHONE caught a ride payload! Extracting DataMap...")

                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val dataMap = dataMapItem.dataMap

                    // 1. Extract the String (Ride Info) and the Asset (Massive Location Array)
                    val rideJson = dataMap.getString("ride_data")
                    val locationAsset = dataMap.getAsset("location_data") // 🚀 NOW FETCHED AS AN ASSET

                    if (rideJson != null && locationAsset != null) {

                        // 2. Move into the Coroutine to download and parse the Asset safely
                        serviceScope.launch {
                            try {
                                Log.d(TAG, "📦 PHONE downloading Asset bytes from Play Services...")

                                // Request the raw file stream from Play Services
                                val assetInputStream = Wearable.getDataClient(this@RideSyncListenerService)
                                    .getFdForAsset(locationAsset)
                                    .await()
                                    .inputStream

                                // Convert the byte stream back into our massive JSON String
                                val locationsJson = assetInputStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }

                                if (locationsJson != null) {
                                    // 3. Deserialize the data back into your Room Entities
                                    val rideEntity = gson.fromJson(rideJson, BikeRideEntity::class.java)

                                    val listType = object : TypeToken<List<RideLocationEntity>>() {}.type
                                    val locationEntities: List<RideLocationEntity> = gson.fromJson(locationsJson, listType)

                                    Log.d(TAG, "💾 PHONE successfully deserialized Ride ID: ${rideEntity.rideId}. Saving to Room DB...")

                                    // 4. Save it straight to the phone's database!
                                    repo.insertRideWithLocations(rideEntity, locationEntities)
                                    Log.i(TAG, "✅ PHONE Room DB save complete for Ride ID: ${rideEntity.rideId}")

                                    // ---------------------------------------------------------
                                    // THE NEW ACKNOWLEDGEMENT PITCH
                                    // ---------------------------------------------------------
                                    Log.d(TAG, "📤 PHONE pitching ACK back to watch for Ride ID: ${rideEntity.rideId}")
                                    val dataClient = Wearable.getDataClient(this@RideSyncListenerService)
                                    val ackRequest = PutDataMapRequest.create("/sync_ack/${rideEntity.rideId}")
                                    ackRequest.dataMap.putLong("timestamp", System.currentTimeMillis())

                                    val putDataReq = ackRequest.asPutDataRequest().setUrgent()
                                    dataClient.putDataItem(putDataReq).await()
                                    Log.i(TAG, "🚀 PHONE successfully fired ACK over Bluetooth!")

                                } else {
                                    Log.e(TAG, "❌ PHONE failed to read bytes from the location Asset!")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ PHONE failed to parse, save, or ACK incoming ride data", e)
                            }
                        }
                    } else {
                        Log.e(TAG, "❌ PHONE received missing ride JSON or location Asset from watch!")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Prevent memory leaks when the OS puts this service back to sleep
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "AshBikeSync"
    }
}