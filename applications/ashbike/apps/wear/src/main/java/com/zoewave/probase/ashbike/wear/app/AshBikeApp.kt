package com.zoewave.probase.ashbike.wear.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy // ✅ Wear OS swipe wrapper
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiEvent
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiState
import com.zoewave.probase.ashbike.wear.ui.navigation.AshBikeRoute
import com.zoewave.probase.ashbike.wear.ui.navigation.ashBikeWearNavEntryProvider

@Composable
fun AshBikeApp(
    uiState: BikeUiState,
    onEvent: (BikeUiEvent) -> Unit
) {
    // 1. THE SOURCE OF TRUTH
    val backStack = remember {
        mutableStateListOf<AshBikeRoute>(AshBikeRoute.ActiveRide)
    }

    fun navigateTo(destination: AshBikeRoute) {
        backStack.add(destination)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
    }

    AppScaffold {
        // 2. THE STANDARD NAV 3 DISPLAY
        NavDisplay(
            backStack = backStack,
            // ✅ Tell NavDisplay to use the Wear OS swipe-to-dismiss behavior
            sceneStrategy = SwipeDismissableSceneStrategy(),
            onBack = { navigateBack() },
            entryProvider = { key ->
                // 3. DELEGATE ROUTING
                ashBikeWearNavEntryProvider(
                    key = key,
                    navigateTo = { dest -> navigateTo(dest) },
                    navigateBack = { navigateBack() },
                    uiState = uiState,
                    onEvent = onEvent
                )
            }
        )
    }
}