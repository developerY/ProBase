package com.zoewave.probase.ashbike.wear.ui.navigation


// The parent interface for EVERYTHING in the Wear app
sealed interface AshBikeRoute {

    // --- ZONE 1: Core App Routes ---
    sealed interface Core : AshBikeRoute {
        data object HomePager : Core
        data class RideDetail(val rideId: String) : Core
        data object ActiveRide : Core
    }

    // --- ZONE 2: Experimental / Info Routes ---
    sealed interface Info : AshBikeRoute {
        data object FeatureHub : Info
        data object Weather : Info
        data object Elevation : Info
        data object HrGraph : Info
        data object RideMap : Info
    }
}