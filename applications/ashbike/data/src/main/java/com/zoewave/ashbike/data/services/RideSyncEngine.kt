package com.zoewave.ashbike.data.services

import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.RideLocationEntity

interface RideSyncEngine {
    /**
     * Packages up a completed ride and transmits it to paired devices.
     */
    suspend fun syncCompletedRide(ride: BikeRideEntity, locations: List<RideLocationEntity>)
}