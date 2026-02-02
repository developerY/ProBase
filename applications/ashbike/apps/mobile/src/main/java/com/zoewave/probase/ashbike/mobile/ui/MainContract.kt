package com.zoewave.probase.ashbike.mobile.ui

import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination


// 1. What the UI needs to render
data class MainUiState(
    val currentDestination: AshBikeDestination = AshBikeDestination.Home,
    val hasLocationPermission: Boolean = false,
    val showSettingsBadge: Boolean = false, // e.g., Profile Incomplete
    val unsyncedRidesCount: Int = 0
)

// 2. What the UI can ask the ViewModel to do
sealed interface MainUiEvent {
    data class OnTabSelected(val destination: AshBikeDestination) : MainUiEvent
    data class OnPermissionResult(val isGranted: Boolean) : MainUiEvent
    data object OnBackPressed : MainUiEvent
}