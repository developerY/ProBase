package com.zoewave.probase.ashbike.features.main.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AshBikeDestination {

    @Serializable
    data object Home : AshBikeDestination
    @Serializable
    data object Trips : AshBikeDestination
    @Serializable
    data object Settings : AshBikeDestination
    // data class Settings(val cardToExpand: String? = null) : AshBikeDestination
    @Serializable
    data class RideDetail(val rideId: String) : AshBikeDestination
}