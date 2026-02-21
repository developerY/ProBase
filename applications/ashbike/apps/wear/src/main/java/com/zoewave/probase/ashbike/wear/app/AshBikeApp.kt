package com.zoewave.probase.ashbike.wear.app

import com.zoewave.probase.ashbike.wear.ui.navigation.AshBikeRoute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.wear.compose.material3.AppScaffold
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiEvent
import com.zoewave.probase.ashbike.wear.presentation.screens.ride.BikeUiState
import com.zoewave.probase.ashbike.wear.ui.navigation.ashBikeWearNavEntryProvider

@Composable
fun AshBikeApp(
    uiState: BikeUiState,
    onEvent: (BikeUiEvent) -> Unit
) {
    // 1. THE SOURCE OF TRUTH (The Back Stack)
    // In Nav3, the back stack is a standard Compose mutable list.
    // We initialize it with the main ActiveRide screen.
    val backStack = remember {
        mutableStateListOf<AshBikeRoute>(AshBikeRoute.ActiveRide)
    }

    // Helper to push a new screen onto the stack
    fun navigateTo(destination: AshBikeRoute) {
        backStack.add(destination)
    }

    // Helper to handle "Back" (Swipe Right on Wear OS)
    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
    }

    // 2. THE WEAR APP SCAFFOLD
    // This is required for Material 3 Wear OS. It automatically draws the curved
    // TimeText at the top and handles scroll indicators natively.
    AppScaffold {

        // 3. THE NAV 3 DISPLAY FOR WEAR OS
        // This container reads the backStack and automatically handles the
        // hardware "Swipe-to-Dismiss" gesture to pop screens.
        WearNavDisplay(
            backStack = backStack,
            onPop = { navigateBack() }, // Triggered automatically when the user swipes right
            entryProvider = { key ->

                // 4. DELEGATE ROUTING
                // Keep the App container clean by moving the 'when' statement
                // to a dedicated entry provider function.
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