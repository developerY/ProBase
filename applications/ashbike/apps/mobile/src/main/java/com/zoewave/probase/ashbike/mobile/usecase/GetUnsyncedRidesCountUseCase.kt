package com.zoewave.probase.ashbike.mobile.usecase

import com.zoewave.probase.ashbike.database.BikeRideRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case to get the count of bike rides that have not yet been synced.
 */
class GetUnsyncedRidesCountUseCase @Inject constructor(
    private val bikeRideRepo: BikeRideRepo
) {
    operator fun invoke(): Flow<Int> {
        return bikeRideRepo.getAllRidesWithLocations()
            .map { ridesWithLocs ->
                ridesWithLocs.count { !it.bikeRideEnt.isHealthDataSynced }
            }
    }
}
