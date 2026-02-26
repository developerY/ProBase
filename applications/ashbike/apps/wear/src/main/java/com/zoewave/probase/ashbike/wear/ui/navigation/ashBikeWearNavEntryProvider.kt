package com.zoewave.probase.ashbike.wear.ui.navigation

import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.ashbike.wear.features.rides.ui.RideDetailRoute

fun ashBikeWearNavEntryProvider(
    key: AshBikeRoute,
    navigateTo: (AshBikeRoute) -> Unit
): NavEntry<AshBikeRoute> {
    return NavEntry(key) {
        when (key) {
            // 1. The Root Pager
            is AshBikeRoute.HomePager -> {
                AshBikeWearPager(
                    // If the user taps a ride on Page 1, trigger this Nav3 routing action
                    onNavigateToRideDetail = { rideId ->
                        navigateTo(AshBikeRoute.RideDetail(rideId))
                    }
                )
            }

            // 2. The Drill-Down Detail Screen
            is AshBikeRoute.RideDetail -> {
                RideDetailRoute(
                    rideId = key.rideId,
                    onNavigateBack = { navigateTo(AshBikeRoute.HomePager) }
                )
            }
        }
    }
}