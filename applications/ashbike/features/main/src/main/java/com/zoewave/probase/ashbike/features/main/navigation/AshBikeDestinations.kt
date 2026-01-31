package com.zoewave.probase.ashbike.features.main.navigation

import kotlinx.serialization.Serializable

// ✅ Nav3 uses standard Kotlin Serialization
@Serializable
sealed interface AshBikeDestination {
    @Serializable data object Home : AshBikeDestination
    @Serializable data object RideHistory : AshBikeDestination
    @Serializable data object Settings : AshBikeDestination
}