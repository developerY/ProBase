package com.zoewave.probase.ashbike.database

import androidx.room3.Embedded
import androidx.room3.Relation

data class RideWithLocations(
    @Embedded val bikeRideEnt: BikeRideEntity,
    @Relation(
        parentColumns = ["rideId"],
        entityColumns = ["rideId"],//"rideOwnerId"
        entity = RideLocationEntity::class
    )
    val locations: List<RideLocationEntity>
)
