package com.zoewave.probase.ashbike.wear.ui.navigation

import kotlinx.serialization.Serializable

// All AshBike screens must implement this sealed interface
@Serializable
sealed interface AshBikeRoute {

    @Serializable
    data object Home : AshBikeRoute      // The main speedometer screen

    @Serializable
    data object Rides : AshBikeRoute     // The list of past ride cards

    @Serializable
    data object Settings : AshBikeRoute  // The settings menu

    @Serializable
    data object ActiveRide : AshBikeRoute    // The ride detail screen
}