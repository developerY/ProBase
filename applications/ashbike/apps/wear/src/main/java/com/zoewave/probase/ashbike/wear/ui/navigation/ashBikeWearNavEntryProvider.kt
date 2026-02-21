package com.zoewave.probase.ashbike.wear.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry

import com.zoewave.probase.ashbike.wear.presentation.screens.history.RideDetailScreen
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiEvent
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiState
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.WearBikeScreen

// import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiState
// import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiEvent

@Composable
fun ashBikeWearNavEntryProvider(
    key: AshBikeRoute,
    navigateTo: (AshBikeRoute) -> Unit,
    navigateBack: () -> Unit,
    uiState: BikeUiState,           // Assuming hoisted state
    onEvent: (BikeUiEvent) -> Unit  // Assuming hoisted events
): NavEntry<AshBikeRoute> {

    return NavEntry(key) {
        when (key) {
            is AshBikeRoute.ActiveRide -> {
                WearBikeScreen(
                    uiState = uiState,
                    onEvent = onEvent,
                    onNavigateToDetail = { rideId ->
                        navigateTo(AshBikeRoute.RideDetail(rideId))
                    }
                )
            }

            is AshBikeRoute.RideDetail -> {
                RideDetailScreen(
                    rideId = key.rideId,
                    uiState = uiState,
                    onEvent = onEvent,
                    onDeleteSuccess = { navigateBack() }
                )
            }

            is AshBikeRoute.Summary -> {
                // SummaryScreen(uiState, onEvent)
            }
        }
    }
}