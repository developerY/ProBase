package com.zoewave.probase.ashbike.features.main.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AshBikeDestination {
    @Serializable data object Home : AshBikeDestination
    @Serializable data object RideHistory : AshBikeDestination
    @Serializable data object Settings : AshBikeDestination
}