package com.zoewave.probase.ashbike.features.main.navigation


import kotlinx.serialization.Serializable

// ✅ Shared Logic: Both Phone and Watch know these screens exist.
@Serializable
sealed interface AshBikeDestination {
    @Serializable data object Home : AshBikeDestination
    @Serializable data object RideHistory : AshBikeDestination
    @Serializable data object Settings : AshBikeDestination
}