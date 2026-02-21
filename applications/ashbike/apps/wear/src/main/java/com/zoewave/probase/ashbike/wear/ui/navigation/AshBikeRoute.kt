package com.zoewave.probase.ashbike.wear.ui.navigation

import kotlinx.serialization.Serializable

// All AshBike screens must implement this sealed interface
@Serializable
sealed interface AshBikeRoute {

    @Serializable
    data object ActiveRide : AshBikeRoute

    @Serializable
    data class RideDetail(val rideId: String) : AshBikeRoute

    @Serializable
    data object Summary : AshBikeRoute
}