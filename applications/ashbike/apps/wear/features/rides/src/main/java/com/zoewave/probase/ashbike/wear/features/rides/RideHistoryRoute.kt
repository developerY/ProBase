package com.zoewave.probase.ashbike.wear.features.rides

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.probase.ashbike.wear.features.rides.ui.RideHistoryPage

// ==========================================
// 1. The Route (Handles State & Module Isolation)
// ==========================================

@Composable
fun RideHistoryRoute(
    viewModel: RidesViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    // Safely collecting domain models from the ViewModel
    val rides: List<BikeRide> by viewModel.ridesState.collectAsState()

    RideHistoryPage(
        rides = rides,
        onRideClick = { ride ->
            // Map the full BikeRide object to just its ID for the navigation graph
            onNavigateToDetail(ride.rideId)
        },
        onDeleteClick = { ride ->
            // Assuming your ViewModel takes the full object to delete it.
            // (If your ViewModel expects a String instead, use ride.rideId here too).
            viewModel.deleteRide(ride.rideId)
        }
    )
}