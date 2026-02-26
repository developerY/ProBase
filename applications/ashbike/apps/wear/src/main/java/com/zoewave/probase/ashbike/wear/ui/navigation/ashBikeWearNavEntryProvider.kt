package com.zoewave.probase.ashbike.wear.ui.navigation

import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.ashbike.wear.features.rides.ui.RideDetailRoute
import com.zoewave.probase.ashbike.wear.features.rides.ui.dash.FeatureHubScreen
import com.zoewave.probase.ashbike.wear.features.rides.ui.graphs.ElevationProfileScreen
import com.zoewave.probase.ashbike.wear.features.rides.ui.health.WeeklyHeartRateGraphScreen
import com.zoewave.probase.ashbike.wear.features.rides.ui.maps.WearRideMapScreen
import com.zoewave.probase.ashbike.wear.features.rides.ui.weather.PreRideWeatherScreen

fun ashBikeWearNavEntryProvider(
    key: AshBikeRoute,
    navigateTo: (AshBikeRoute) -> Unit
): NavEntry<AshBikeRoute> {
    return NavEntry(key) {
        when (key) {
            // ==========================================
            // ZONE 1: Core App Destinations
            // ==========================================
            is AshBikeRoute.Core.HomePager -> {
                AshBikeWearPager(
                    onNavigateToRideDetail = { rideId ->
                        navigateTo(AshBikeRoute.Core.RideDetail(rideId))
                    },
                    // Add a button somewhere in the pager to open the hub
                    onNavigateToExperiments = {
                        navigateTo(AshBikeRoute.Info.FeatureHub)
                    }
                )
            }
            is AshBikeRoute.Core.RideDetail -> {
                RideDetailRoute(
                    rideId = key.rideId,
                    onNavigateBack = { navigateTo(AshBikeRoute.Core.HomePager) }
                )
            }
            is AshBikeRoute.Core.ActiveRide -> {
                // Future live tracking screen
            }

            // ==========================================
            // ZONE 2: Info / Experimental Destinations
            // ==========================================
            is AshBikeRoute.Info.FeatureHub -> {
                FeatureHubScreen(
                    onNavigateToWeather = { navigateTo(AshBikeRoute.Info.Weather) },
                    onNavigateToElevation = { navigateTo(AshBikeRoute.Info.Elevation) },
                    onNavigateToHrGraph = { navigateTo(AshBikeRoute.Info.HrGraph) },
                    onNavigateToMap = { navigateTo(AshBikeRoute.Info.RideMap) }
                )
            }
            is AshBikeRoute.Info.Weather -> {
                PreRideWeatherScreen(
                    weather = null,
                    // Pop back to the hub
                    onStartRideClick = { navigateTo(AshBikeRoute.Info.FeatureHub) }
                )
            }
            is AshBikeRoute.Info.Elevation -> ElevationProfileScreen(locations = emptyList())
            is AshBikeRoute.Info.HrGraph -> WeeklyHeartRateGraphScreen(weeklyData = emptyList())
            is AshBikeRoute.Info.RideMap -> WearRideMapScreen(locations = emptyList())
        }
    }
}