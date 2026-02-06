package com.zoewave.probase.ashbike.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


import androidx.navigation3.runtime.NavEntry
import com.zoewave.ashbike.mobile.home.ui.HomeViewModel
import com.zoewave.ashbike.mobile.rides.ui.components.RideDetailViewModel
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination

// Import your feature screens here (HomeRoute, TripsUIRoute, etc.)

@Composable
fun AshBikeNavEntryProvider(
    key: AshBikeDestination,
    navigateTo: (AshBikeDestination) -> Unit,
    homeViewModel: HomeViewModel
): NavEntry<out AshBikeDestination> {
    return NavEntry(key) {
        when (key) {
            is AshBikeDestination.Home -> {
                // Return Home Screen Composable
            }
            is AshBikeDestination.Trips -> {
                // ✅ Hilt will now find the ViewModelStore provided by the Decorator
                val vm: RideDetailViewModel = hiltViewModel()
                // Return Trips Screen Composable
            }
            is AshBikeDestination.RideDetail -> {
                // Return RideDetail Screen Composable
            }
            is AshBikeDestination.Settings -> {
                // Return Settings Screen Composable
            }
        }
    }
}