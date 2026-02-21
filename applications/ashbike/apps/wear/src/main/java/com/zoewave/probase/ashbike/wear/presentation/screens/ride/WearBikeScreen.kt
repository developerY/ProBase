package com.zoewave.probase.ashbike.wear.presentation.screens.ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun WearBikeScreen(
    uiState: BikeUiState,
    onEvent: (BikeUiEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    // The state for the scrollable list.
    // In a real app, you might hoist this to AppScaffold so the rotary crown works globally.
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // 1. HEADER
        item {
            Text(
                text = "AshBike",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 2. MAIN CONTENT (Dependent on Tracking State)
        if (!uiState.isTracking) {
            // --- IDLE STATE ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onEvent(BikeUiEvent.StartRide) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Ride"
                    )
                }
            }
            item {
                Text(
                    text = "Ready to Ride",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // --- TRACKING STATE ---

            // Speed Metric (Primary Focus)
            item {
                Text(
                    text = String.format("%.1f", uiState.currentSpeedMph),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (uiState.isPaused) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground
                )
            }
            item {
                Text(
                    text = "mph",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Secondary Metrics (Heart Rate & Distance)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "${uiState.heartRateBpm} bpm",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = String.format("%.1f mi", uiState.distanceMiles),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 3. PLAYBACK CONTROLS
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isPaused) {
                        Button(onClick = { onEvent(BikeUiEvent.ResumeRide) }) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                        }
                    } else {
                        Button(
                            onClick = { onEvent(BikeUiEvent.PauseRide) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                        }
                    }

                    Button(
                        onClick = { onEvent(BikeUiEvent.StopRide) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
            }

            // 4. NAVIGATION DELEGATION
            // If we have an active ride ID, we can navigate forward to the detail screen
            uiState.activeRideId?.let { rideId ->
                item {
                    CompactButton(
                        onClick = { onNavigateToDetail(rideId) },
                        icon = {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Details")
                        },
                        label = {
                            Text("Live Map / Details")
                        }
                    )
                }
            }
        }

        // 5. ERROR HANDLING
        uiState.errorMessage?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}