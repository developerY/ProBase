package com.zoewave.probase.ashbike.wear.ui.navigation

import androidx.navigation3.runtime.NavEntry

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
                // Your RideDetailScreen logic goes here
                // e.g., RideDetailScreen(rideId = key.rideId)
            }
        }
    }
}