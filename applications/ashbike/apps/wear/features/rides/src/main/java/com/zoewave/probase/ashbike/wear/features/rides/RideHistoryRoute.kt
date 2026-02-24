package com.zoewave.probase.ashbike.wear.features.rides

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.ashbike.model.bike.BikeRide

// ==========================================
// 1. The Route (Handles State & Module Isolation)
// ==========================================
@Composable
fun RideHistoryRoute(
    viewModel: RidesViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    // Now safely collecting domain models
    val rides: List<BikeRide> by viewModel.ridesState.collectAsState()

    RideHistoryPage(
        rides = rides,
        onRideClick = onNavigateToDetail
    )
}