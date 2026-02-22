package com.zoewave.probase.ashbike.wear.ui.navigation

import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.ashbike.wear.features.home.WearHomeScreen
import com.zoewave.probase.ashbike.wear.features.home.WearMenuScreen
import com.zoewave.probase.ashbike.wear.features.rides.WearRidesScreen
import com.zoewave.probase.ashbike.wear.features.settings.WearSettingsScreen


// Note: This is NOT a @Composable function. It's a simple factory.
fun ashBikeWearNavEntryProvider(
    key: AshBikeRoute,
    navigateTo: (AshBikeRoute) -> Unit
): NavEntry<AshBikeRoute> {
    return NavEntry(key) {
        // The Composable content lives inside this lambda
        when (key) {
            AshBikeRoute.Home -> {
                WearHomeScreen(
                    onNavigateToMenu = { navigateTo(AshBikeRoute.Menu) }
                )
            }

            AshBikeRoute.Rides -> {
                WearRidesScreen()
            }

            AshBikeRoute.Settings -> {
                WearSettingsScreen()
            }
            AshBikeRoute.Menu -> {
                WearMenuScreen(
                    onNavigateToRides = { navigateTo(AshBikeRoute.Rides) },
                    onNavigateToSettings = { navigateTo(AshBikeRoute.Settings) }
                )
            }

            AshBikeRoute.ActiveRide -> {} //TODO()
        }
    }
}