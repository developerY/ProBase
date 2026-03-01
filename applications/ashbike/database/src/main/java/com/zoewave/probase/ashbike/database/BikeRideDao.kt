package com.zoewave.probase.ashbike.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeRideDao {

    // ========================================================================
    // 1. INSERTS (Creating Data)
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: BikeRideEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocations(locations: List<RideLocationEntity>)


    // ========================================================================
    // 2. MOBILE QUERIES (Reactive Streams)
    // Uses Flow: Best for Mobile phones where UI needs to instantly react
    // to DB changes without manual refreshes.
    // ========================================================================

    /**
     * Fetches all rides WITH their heavy GPS location lists.
     * Reactive Flow: Updates UI automatically if a ride is added or deleted.
     * Best for: Mobile Phone 'Ride History' list maps.
     */
    @Transaction
    @Query("SELECT * FROM bike_rides_table ORDER BY startTime DESC")
    fun getAllRidesWithLocations(): Flow<List<RideWithLocations>>

    /**
     * Fetches a single ride WITH its heavy GPS location list.
     * Reactive Flow: Updates UI automatically if the current ride is modified.
     */
    @Transaction
    @Query("SELECT * FROM bike_rides_table WHERE rideId = :id")
    fun getRideWithLocations(id: String): Flow<RideWithLocations?>


    // ========================================================================
    // 3. WEAR OS & PERFORMANCE QUERIES (One-Shot & Lightweight)
    // Uses Suspend/Basic Entities: Prevents watch memory crashes from massive
    // GPS lists and stops battery drain from constant Flow invalidation.
    // ========================================================================

    /**
     * Lightweight Reactive Fetch (For Wear OS History Pager)
     * Fetches ONLY the base ride stats. Ignores the heavy locations table.
     */
    @Query("SELECT * FROM bike_rides_table ORDER BY startTime DESC")
    fun getAllRidesBasic(): Flow<List<BikeRideEntity>>

    /**
     * One-Shot Heavy Fetch (For Wear OS Map Route)
     * Fetches the heavy relation (stats + GPS list) for exactly ONE ride.
     * Uses 'suspend' instead of 'Flow' so it fetches once and stops listening.
     */
    @Transaction
    @Query("SELECT * FROM bike_rides_table WHERE rideId = :id")
    suspend fun getRideWithLocationsSuspend(id: String): RideWithLocations?

    /**
     * One-Shot Lightweight Fetch (Mobile/Wear OS Alternative)
     * Gets all basic ride info just ONE TIME (no reactive stream).
     * * Use Case: Great for background sync workers, checking if the database
     * is empty on startup, or explicit "pull-to-refresh" buttons where
     * you don't want to incur the overhead of an active Flow.
     */
    @Query("SELECT * FROM bike_rides_table ORDER BY startTime DESC")
    suspend fun getAllRidesBasicOnce(): List<BikeRideEntity>

    /**
     * One-Shot Heavy Fetch (Mobile/Wear OS Alternative)
     * Fetches all rides WITH their heavy GPS location lists ONE TIME.
     * * Use Case: Ideal for bulk data exports, one-time bulk syncs to Health Connect,
     * or scenarios where you need all the data at once but the UI does not
     * need to react to real-time database changes.
     */
    @Transaction
    @Query("SELECT * FROM bike_rides_table ORDER BY startTime DESC")
    suspend fun getAllRidesWithLocationsOnce(): List<RideWithLocations>


    // ========================================================================
    // 4. UPDATES & HEALTH CONNECT SYNC
    // ========================================================================

    /** Updates the user's custom notes for a specific ride. */
    @Query("UPDATE bike_rides_table SET notes = :notes WHERE rideId = :rideId")
    suspend fun updateNotes(rideId: String, notes: String)

    /** Marks a ride as synced to Health Connect and stores the Health Connect ID. */
    @Query("UPDATE bike_rides_table SET isHealthDataSynced = 1, healthConnectRecordId = :healthConnectId WHERE rideId = :rideId")
    suspend fun markRideAsSyncedToHealthConnect(rideId: String, healthConnectId: String?)

    /** * Gets the count of rides that are not yet synced to Health Connect.
     * Returns a Flow to power a dynamic "Unsynced Rides: X" badge in the UI.
     */
    @Query("SELECT COUNT(*) FROM bike_rides_table WHERE isHealthDataSynced = 0")
    fun getUnsyncedRidesCount(): Flow<Int>


    // ========================================================================
    // 5. DELETIONS
    // ========================================================================

    /** Deletes a single ride. (Locations should auto-delete via foreign key cascade) */
    @Query("DELETE FROM bike_rides_table WHERE rideId = :id")
    suspend fun deleteRide(id: String)

    /** Wipes the entire rides table. */
    @Query("DELETE FROM bike_rides_table")
    suspend fun deleteAllBikeRides()
}