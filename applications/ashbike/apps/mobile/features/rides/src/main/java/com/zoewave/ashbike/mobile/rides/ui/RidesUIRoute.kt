package com.zoewave.ashbike.mobile.rides.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.zoewave.ashbike.mobile.rides.ui.components.BikeTripsCompose
import com.zoewave.ashbike.mobile.rides.ui.components.ErrorScreen
import com.zoewave.ashbike.mobile.rides.ui.components.LoadingScreen
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthSideEffect
import com.zoewave.probase.features.health.ui.HealthUiState
import com.zoewave.probase.features.health.ui.HealthViewModel
import kotlinx.coroutines.launch

@Composable
fun RidesUIRoute(
    modifier: Modifier = Modifier,
    navTo: (String) -> Unit,
    // Ask Hilt for both viewmodels
    ridesViewModel: RidesViewModel = hiltViewModel(),
    healthViewModel: HealthViewModel = hiltViewModel(),
) {
    // 1. Collect state from BOTH ViewModels
    val tripsUiState by ridesViewModel.uiState.collectAsState()
    val healthUiState by healthViewModel.uiState.collectAsState()

    // 2. Observe the set of already-synced IDs from HealthViewModel
    val syncedIds by healthViewModel.syncedIds.collectAsState() // This line was present in an earlier version you showed, ensure it's still needed or remove if not.

    // 2. Setup Snackbar for showing errors from HealthViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 3. Setup permissions launcher for Health Connect permissions
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = healthViewModel.permissionsLauncher,
        onResult = { healthViewModel.onEvent(HealthEvent.LoadHealthData) }
    )

    // 4. Handle side effects from BOTH ViewModels

    val lifecycleOwner =
        androidx.lifecycle.compose.LocalLifecycleOwner.current // Get the lifecycle owner for the check

    // Effect handler for HealthViewModel
    LaunchedEffect(Unit) { // Changed key from (healthUiState, snackbarHostState) to Unit for consistency if only collecting side effects
        // Error handling for healthUiState can be separate if needed, or kept if healthUiState changes should re-trigger collection logic
        if (healthUiState is HealthUiState.Error) { // This will only be checked when the LaunchedEffect initially runs or if its keys change.
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = (healthUiState as HealthUiState.Error).message
                )
            }
        }

        healthViewModel.sideEffect.collect { effect ->
            when (effect) {
                is HealthSideEffect.LaunchPermissions -> {
                    // CRITICAL: This check is necessary to prevent this TripsUIRoute,
                    // which shares HealthViewModel with other screens (e.g., Settings),
                    // from attempting to launch the Health Connect permissions dialog
                    // when it's not the active, resumed screen. Without this, if TripsUIRoute
                    // is in the backstack but still composed, it can interfere with the
                    // foreground screen's attempt to launch the same permissions dialog,
                    // causing the dialog to not appear or behave unpredictably.
                    if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        Log.d(
                            "TripsUIRoute",
                            "LaunchPermissions side effect received IN TRIPS UI (RESUMED). Launching its own permissionsLauncher."
                        )
                        permissionsLauncher.launch(effect.permissions)
                    } else {
                        Log.d(
                            "TripsUIRoute",
                            "LaunchPermissions side effect received IN TRIPS UI (NOT RESUMED - Current state: ${lifecycleOwner.lifecycle.currentState}). Ignoring."
                        )
                    }
                }

                is HealthSideEffect.BikeRideSyncedToHealth -> {
                    Log.d(
                        "TripsUIRoute",
                        "BikeRideSyncedToHealth side effect received. Marking ride ${effect.rideId} as synced."
                    )
                    ridesViewModel.markRideAsSyncedInLocalDb(
                        rideId = effect.rideId,
                        healthConnectId = effect.healthConnectId
                    )
                }
            }
        }
    }

    // Effect handler for TripsViewModel
    LaunchedEffect(Unit) {
        ridesViewModel.sideEffect.collect { effect ->
            when (effect) {
                is TripsSideEffect.RequestHealthConnectSync -> {
                    Log.d(
                        "TripsUIRoute",
                        "Sync requested for rideId: ${effect.rideId}. Sending HealthEvent.Insert to HealthViewModel."
                    )
                    healthViewModel.onEvent(
                        { } as HealthEvent
                        /*HealthEvent.Insert(
                            rideId = effect.rideId,
                            records = effect.records
                        )*/
                    )
                }
            }
        }
    }

    // Render based on your trips state
    when (val state = tripsUiState) {
        RidesUIState.Loading -> LoadingScreen()

        is RidesUIState.Error -> ErrorScreen(
            errorMessage = state.message,
            onRetry = { ridesViewModel.onEvent(RidesEvent.OnRetry) }
        )

        is RidesUIState.Success -> {
            // Note the simplified parameters passed to BikeTripsCompose
            BikeTripsCompose(
                modifier = modifier,
                bikeRides = state.rides, // This is now List<BikeRideUiModel>
                bikeEvent = ridesViewModel::onEvent,
                syncedIds = syncedIds,
                healthEvent = healthViewModel::onEvent,
                onDeleteClick = { rideId ->
                    ridesViewModel.onEvent(RidesEvent.DeleteItem(rideId))
                },
                onSyncClick = { rideId ->
                    // This triggers the new side effect flow in TripsViewModel
                    ridesViewModel.onEvent(RidesEvent.SyncRide(rideId))
                },
                healthUiState = healthUiState,
                navTo = navTo
            )
        }
    }
}
