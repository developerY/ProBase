package com.zoewave.probase.ashbike.database

import com.zoewave.ashbike.model.bike.BikeRide
import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for Bike Ride data operations.
 * * Note on Architecture: This interface currently exposes a mix of pure Domain Models (BikeRide)
 * and Room Database Entities. Ideally, a repository should strictly expose the Domain Model
 * and handle all entity mapping internally. Raw entity methods have been grouped at the bottom.
 */
interface BikeRideRepo {

    // =========================================================================
    // REACTIVE STREAMS (UI OBSERVATION)
    // =========================================================================

    /** * Returns a continuous stream of all rides (lightweight summary data only, no GPS locations).
     * Ideal for populating the primary History list UI without causing memory overhead.
     */
    fun getAllRides(): Flow<List<BikeRide>>

    /** * Returns a continuous stream of a single ride mapped to the domain model.
     * Ideal for Detail screens so the UI instantly updates when background states
     * (like [isAcknowledged]) change.
     */
    fun getRideFlow(rideId: String): Flow<BikeRide?>


    // =========================================================================
    // ONE-SHOT READS
    // =========================================================================

    /** * Fetches a single ride by its ID (summary data only).
     */
    suspend fun getRideById(rideId: String): BikeRide?


    // =========================================================================
    // WRITES (INSERT, UPDATE, DELETE)
    // =========================================================================

    /** * Inserts a basic bike ride summary.
     */
    suspend fun insert(bikeRide: BikeRide)

    /** * Updates the user's personal notes for a specific ride.
     */
    suspend fun updateRideNotes(rideId: String, notes: String)

    /** * Deletes a specific ride using its domain object.
     */
    suspend fun delete(bikeRide: BikeRide)

    /** * Deletes a specific ride using its unique ID.
     */
    suspend fun deleteById(rideId: String)

    /** * Completely wipes the rides table. Handle with caution.
     */
    suspend fun deleteAll()


    // =========================================================================
    // SYNC & STATE MANAGEMENT
    // =========================================================================

    /** * Marks a ride as successfully caught and saved by the phone over the Bluetooth Data Layer.
     */
    suspend fun markRideAsAcknowledged(rideId: String)

    /** * Marks a ride as successfully pushed to Google Health Connect and stores the resulting remote ID.
     */
    suspend fun markRideAsSyncedToHealthConnect(rideId: String, healthConnectId: String?)

    /** * Emits the current count of rides that are pending transfer to Google Health Connect.
     */
    fun getUnsyncedRidesCount(): Flow<Int>

    // TODO: Refactor to use pure Domain Models.
    // Replace BikeRideEntity and RideWithLocations in these signatures with the BikeRide domain model.
    // Move all Room Entity mapping logic directly into BikeRideRepoImpl to stop leaking DB implementation details.
    // =========================================================================
    // ⚠️ RAW DATABASE ENTITY OPERATIONS (ARCHITECTURAL DEBT)
    // =========================================================================
    /**
     * TECHNICAL DEBT EXPLANATION:
     * A Repository's primary job is to act as a strict boundary between the data source (Room)
     * and the rest of the application (ViewModels, Use Cases). To maintain Clean Architecture,
     * this interface should only speak in pure Domain Models (e.g., [BikeRide]).
     *
     * Currently, these methods leak Room-specific objects ([BikeRideEntity], [RideLocationEntity],
     * and [RideWithLocations]) into the higher layers of the app. This tightly couples the UI
     * and Domain logic directly to the SQLite database structure.
     *
     * REFACTORING GOAL:
     * 1. Update these method signatures to accept/return the pure [BikeRide] domain model
     * (which already contains a `List<LocationPoint>`).
     * 2. Move the mapping logic (Domain <-> Entity) entirely inside the `BikeRideRepoImpl`.
     * 3. Once refactored, the ViewModels will never know that Room, SQLite, or these Entities exist.
     */

    /** * Returns a continuous stream of rides paired directly with their raw location entities.
     */
    fun getAllRidesWithLocations(): Flow<List<RideWithLocations>>

    /** * Returns a stream of a single ride paired directly with its raw location entities.
     */
    fun getRideWithLocations(rideId: String): Flow<RideWithLocations?>

    /** * Inserts a ride and its associated high-fidelity GPS points directly using raw database entities.
     */
    suspend fun insertRideWithLocations(
        ride: BikeRideEntity,
        locations: List<RideLocationEntity>
    )
}