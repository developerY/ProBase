package com.zoewave.probase.ashbike.database.repository

// com.zoewave.probase.ashbike.database.repository.BikeRideRepoImpl.kt

import androidx.annotation.WorkerThread
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.probase.ashbike.database.BikeRideDao
import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.BikeRideRepo
import com.zoewave.probase.ashbike.database.RideLocationEntity
import com.zoewave.probase.ashbike.database.mapper.toBikeRide
import com.zoewave.probase.ashbike.database.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class BikeRideRepoImpl @Inject constructor(
    private val bikeRideDao: BikeRideDao
) : BikeRideRepo {

    override fun getAllRidesWithLocations() = bikeRideDao.getAllRidesWithLocations()
    override fun getRideWithLocations(id: String) = bikeRideDao.getRideWithLocations(id)

    @WorkerThread
    override suspend fun insert(ride: BikeRide) {
        bikeRideDao.insertRide(ride.toEntity())
    }

    @WorkerThread
    override suspend fun delete(ride: BikeRide) {
        bikeRideDao.deleteRide(ride.rideId)
    }

    @WorkerThread
    override suspend fun deleteById(rideId: String) {
        bikeRideDao.deleteRide(rideId)
    }

    @WorkerThread
    override suspend fun deleteAll() {
        // you’ll need to add this to BikeRideDao:
        // @Query("DELETE FROM rides") suspend fun deleteAllRides()
        bikeRideDao.deleteAllBikeRides()
    }

    @WorkerThread
    override suspend fun insertRideWithLocations(
        ride: BikeRideEntity,
        locations: List<RideLocationEntity>
    ) {
        bikeRideDao.insertRide(ride)
        bikeRideDao.insertLocations(locations)
    }

    /** New: update just the notes text */
    @WorkerThread
    override suspend fun updateRideNotes(rideId: String, notes: String) {
        bikeRideDao.updateNotes(rideId, notes)
    }

    /** Marks a ride as synced to Health Connect and stores the Health Connect ID. */
    @WorkerThread
    override suspend fun markRideAsSyncedToHealthConnect(rideId: String, healthConnectId: String?) {
        bikeRideDao.markRideAsSyncedToHealthConnect(rideId, healthConnectId)
    }

    /** Gets the count of rides that are not yet synced to Health Connect. */
    @WorkerThread // Added @WorkerThread for consistency, though Flow might not strictly need it here
    override fun getUnsyncedRidesCount(): Flow<Int> {
        return bikeRideDao.getUnsyncedRidesCount()
    }

    /** * OPTIMIZED FOR WEAR OS HISTORY LIST
     * Uses the lightweight DAO method to prevent OOM crashes on the watch.
     */
    @WorkerThread
    override fun getAllRides(): Flow<List<BikeRide>> =
        bikeRideDao
            .getAllRidesBasic() // <-- Use the new lightweight query!
            .map { list ->
                list.map { it.toBikeRide() }
            }

    /** * OPTIMIZED FOR WEAR OS MAP
     * Fetches the heavy relation and ACTUALLY MAPS the locations array!
     */
    @WorkerThread
    override suspend fun getRideById(rideId: String): BikeRide? {
        // 1. Fetch the heavy relational data using the new suspend DAO method
        val relation = bikeRideDao.getRideWithLocationsSuspend(rideId) ?: return null

        // 2. Convert the base entity to your domain model
        val baseRide = relation.bikeRideEnt.toBikeRide()

        // 3. Attach the mapped locations so the Canvas has data to draw!
        return baseRide.copy(
            locations = relation.locations.map { locEntity ->
                com.zoewave.ashbike.model.bike.LocationPoint(
                    latitude = locEntity.lat,
                    longitude = locEntity.lng,
                    altitude = locEntity.elevation?.toFloat(),
                    timestamp = locEntity.timestamp
                )
            }
        )
    }
}
