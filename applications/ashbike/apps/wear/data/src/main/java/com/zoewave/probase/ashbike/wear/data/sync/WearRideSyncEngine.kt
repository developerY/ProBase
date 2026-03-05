package com.zoewave.probase.ashbike.wear.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.zoewave.ashbike.data.services.RideSyncEngine
import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.RideLocationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await

import javax.inject.Inject

class WearRideSyncEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : RideSyncEngine {

    private val dataClient = Wearable.getDataClient(context)
    private val gson = Gson()

    override suspend fun syncCompletedRide(ride: BikeRideEntity, locations: List<RideLocationEntity>) {
        try {
            Log.d("AshBikeSync", "Packaging ride for transmission: ${ride.rideId}")

            // 1. Create a unique path for the DataItem
            val request = PutDataMapRequest.create("/completed_ride/${ride.rideId}")

            // 2. Serialize the data to JSON strings
            val rideJson = gson.toJson(ride)
            val locationsJson = gson.toJson(locations)

            // 3. Pack the DataMap
            request.dataMap.putString("ride_data", rideJson)
            request.dataMap.putString("location_data", locationsJson)

            // Adding a timestamp forces the DataClient to treat this as a "new"
            // event, even if the payload looks identical to a previous one.
            request.dataMap.putLong("timestamp", System.currentTimeMillis())

            // 4. Fire it over Bluetooth/Wi-Fi!
            val putDataReq = request.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataReq).await()

            Log.d("AshBikeSync", "Ride successfully beamed to Phone!")

        } catch (e: Exception) {
            Log.e("AshBikeSync", "Failed to sync ride to phone", e)
        }
    }
}