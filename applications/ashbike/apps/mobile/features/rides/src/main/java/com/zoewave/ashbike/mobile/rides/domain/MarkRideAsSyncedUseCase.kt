package com.zoewave.ashbike.mobile.rides.domain

import com.zoewave.probase.ashbike.database.BikeRideRepo
import javax.inject.Inject

/**
 * Encapsulates the business logic for marking a bike ride as synced in the local database.
 */
class MarkRideAsSyncedUseCase @Inject constructor(
    private val bikeRideRepo: BikeRideRepo
) {
    suspend operator fun invoke(rideId: String, healthConnectId: String?) {
        bikeRideRepo.markRideAsSyncedToHealthConnect(rideId, healthConnectId)
    }
}
