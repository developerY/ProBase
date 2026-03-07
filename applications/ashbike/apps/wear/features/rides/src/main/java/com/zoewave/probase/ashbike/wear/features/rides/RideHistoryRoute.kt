package com.zoewave.probase.ashbike.wear.features.rides

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.zoewave.probase.ashbike.wear.features.rides.ui.RideHistoryPage

@Composable
fun RideHistoryRoute(
    viewModel: RidesViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    // 1. Safely collect the MVI StateFlow we built
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. React to the exact state of the database
    when (val state = uiState) {
        is RidesUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is RidesUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, textAlign = TextAlign.Center)
            }
        }
        is RidesUiState.Success -> {
            RideHistoryPage(
                rides = state.rides,
                onEvent = { event ->
                    // 3. The Traffic Controller: Route navigation vs Business logic
                    when (event) {
                        is RidesEvent.OnRideClick -> onNavigateToDetail(event.rideId)
                        else -> viewModel.onEvent(event) // Pass Delete/Sync to ViewModel
                    }
                }
            )
        }
    }
}