package com.zoewave.probase.ashbike.wear.ui.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed interface AshBikeRoute {
    @Serializable
    data object HomePager : AshBikeRoute // The single root destination

    @Serializable
    data class RideDetail(val rideId: String) : AshBikeRoute // ✅ Depth navigation
}