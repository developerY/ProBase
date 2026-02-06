package com.zoewave.ashbike.mobile.rides.ui

////import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zoewave.ashbike.mobile.rides.ui.components.BikeTripsCompose
import com.zoewave.ashbike.mobile.rides.ui.components.ErrorScreen
import com.zoewave.ashbike.mobile.rides.ui.components.LoadingScreen


/**
 * The stateless, presentational composable that is responsible for the UI layout.
 */
@Composable
fun RidesScreen(
    modifier: Modifier = Modifier,
    ridesUiState: RidesUIState,
    snackbarHostState: SnackbarHostState,
    onDeleteClick: (String) -> Unit,
    onSyncClick: (String) -> Unit,
    onRetry: () -> Unit,
    navTo: (String) -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when (val state = ridesUiState) {
            is RidesUIState.Loading -> LoadingScreen()//modifier = Modifier.padding(innerPadding))
            is RidesUIState.Error -> ErrorScreen(
                //modifier = Modifier.padding(innerPadding),
                errorMessage = state.message,
                onRetry = onRetry
            )

            is RidesUIState.Success -> {
                /*
                modifier      = modifier,
                bikeRides     = (uiState as TripsUIState.Success).bikeRides,
                syncedIds     = syncedIds,
                bikeEvent       = { tripsViewModel.onEvent(it) },
                healthEvent   = { healthViewModel.onEvent(it) },
                bikeToHealthConnectRecords = { tripsViewModel.buildHealthConnectRecordsForRide(it) },
                healthUiState = healthUiState,
                navTo         = navTo
                 */
                BikeTripsCompose(
                    modifier = Modifier.padding(innerPadding),
                    bikeRides = state.rides,
                    onDeleteClick = onDeleteClick,
                    onSyncClick = onSyncClick,
                    navTo = navTo,
                    bikeEvent = TODO(),
                    syncedIds = TODO(),
                    healthEvent = TODO(),
                    healthUiState = TODO()
                )
            }
        }
    }
}
/*
@Preview
@Composable
fun TripsScreenPreview() {
    val tripsUiState = TripsUIState.Success(
        rides = listOf(
            //BikeRideUiModel(id = "1", name = "Morning Ride", duration = "1h 30m", distance = "25 km"),
            //BikeRideUiModel(id = "2", name = "Evening Commute", duration = "45m", distance = "10 km")
        )
    )
    val snackbarHostState = SnackbarHostState()
    TripsScreen(
        tripsUiState = tripsUiState,
        snackbarHostState = snackbarHostState,
        onDeleteClick = {},
        onSyncClick = {},
        onRetry = {},
        navTo = {})
}
*/