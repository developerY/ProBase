package com.zoewave.probase.ashbike.mobile.data.sync

import android.util.Log
import com.zoewave.ashbike.data.services.RideSyncEngine
import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.RideLocationEntity

import javax.inject.Inject

class MobileRideSyncEngine @Inject constructor() : RideSyncEngine {

    override suspend fun syncCompletedRide(ride: BikeRideEntity, locations: List<RideLocationEntity>) {
        // Do absolutely nothing!
        // The phone is the final destination, so it doesn't need to transmit rides.
        Log.d("AshBikeSync", "Mobile app formal ride saved. No outgoing sync required.")
    }
}