package com.zoewave.ashbike.mobile.rides.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.health.connect.client.PermissionController
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
    ridesViewModel: RidesViewModel = hiltViewModel(),
    healthViewModel: HealthViewModel = hiltViewModel(),
) {
    // 1. Collect state from BOTH ViewModels
    val tripsUiState by ridesViewModel.uiState.collectAsState()
    val healthUiState by healthViewModel.uiState.collectAsState()

    // 2. Observe the set of already-synced IDs to update icons
    val syncedIds by healthViewModel.syncedIds.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // --- NEW: State for the Success Alert Dialog ---
    var showSyncSuccessDialog by remember { mutableStateOf(false) }

    // 3. Setup permissions launcher locally
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
        onResult = {
            // When user returns from permission screen, tell ViewModel to retry/load
            healthViewModel.onEvent(HealthEvent.LoadHealthData)
        }
    )

    // 4. Handle side effects from HealthViewModel
    LaunchedEffect(Unit) {
        healthViewModel.sideEffect.collect { effect ->
            when (effect) {
                is HealthSideEffect.LaunchPermissions -> {
                    // CRITICAL: Only launch permissions if this screen is actually visible (RESUMED).
                    // This prevents conflicts if HealthViewModel emits this while the user
                    // is on the "Settings" or "Health" tab.
                    if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        Log.d("RidesUIRoute", "Launching permissions request from Rides screen.")
                        permissionsLauncher.launch(effect.permissions)
                    } else {
                        Log.d("RidesUIRoute", "Ignoring permission request (Screen not RESUMED).")
                    }
                }

                is HealthSideEffect.BikeRideSyncedToHealth -> {
                    Log.d("RidesUIRoute", "Ride ${effect.rideId} synced. Updating local DB.")
                    ridesViewModel.markRideAsSyncedInLocalDb(
                        rideId = effect.rideId,
                        healthConnectId = effect.healthConnectId
                    )
                    // --- CHANGED: Trigger the Alert Dialog instead of Snackbar ---
                    showSyncSuccessDialog = true
                }
            }
        }
    }

    // 5. Handle side effects from RidesViewModel
    LaunchedEffect(Unit) {
        ridesViewModel.sideEffect.collect { effect ->
            when (effect) {
                // When RidesViewModel says "Sync this!", we forward it to HealthViewModel
                is TripsSideEffect.RequestHealthConnectSync -> {
                    Log.d("RidesUIRoute", "Forwarding sync request for ${effect.rideId} to HealthViewModel")
                    healthViewModel.onEvent(
                        HealthEvent.Insert(
                            rideId = effect.rideId,
                            records = effect.records
                        )
                    )
                }
            }
        }
    }

    // 6. Handle UI State Errors (Optional: Show Snackbar if Health fails)
    LaunchedEffect(healthUiState) {
        if (healthUiState is HealthUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = (healthUiState as HealthUiState.Error).message
                )
            }
        }
    }

    // 7. Render UI
    when (val state = tripsUiState) {
        RidesUIState.Loading -> LoadingScreen()

        is RidesUIState.Error -> ErrorScreen(
            errorMessage = state.message,
            onRetry = { ridesViewModel.onEvent(RidesEvent.OnRetry) }
        )

        is RidesUIState.Success -> {
            BikeTripsCompose(
                modifier = modifier,
                bikeRides = state.rides,
                bikeEvent = ridesViewModel::onEvent,
                syncedIds = syncedIds, // Pass the synced IDs here
                healthEvent = healthViewModel::onEvent,
                onDeleteClick = { rideId ->
                    ridesViewModel.onEvent(RidesEvent.DeleteItem(rideId))
                },
                onSyncClick = { rideId ->
                    // This triggers the side effect flow -> HealthViewModel
                    ridesViewModel.onEvent(RidesEvent.SyncRide(rideId))
                },
                healthUiState = healthUiState,
                navTo = navTo
            )
        }
    }

    /* --- NEW: Render the Alert Dialog when active ---
    if (showSyncSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSyncSuccessDialog = false },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
            title = { Text(text = "Sync Complete") },
            text = { Text(text = "Your ride has been successfully synced to Health Connect.") },
            confirmButton = {
                TextButton(
                    onClick = { showSyncSuccessDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }*/
}