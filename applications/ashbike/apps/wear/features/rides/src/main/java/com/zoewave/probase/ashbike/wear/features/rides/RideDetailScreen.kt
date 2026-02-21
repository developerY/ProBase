package com.zoewave.probase.ashbike.wear.features.rides


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard

@Composable
fun RideDetailScreen(
    rideId: String,
    uiState: BikeUiState, // The hoisted state
    onEvent: (BikeUiEvent) -> Unit, // The hoisted event callback
    onDeleteSuccess: () -> Unit // Navigation callback to pop the screen
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        // 1. HEADER
        item {
            Text(
                text = "Ride Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Display the strongly-typed ID passed safely from Nav3
        item {
            Text(
                text = "ID: $rideId",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // 2. DATA CARD
        // TitleCard is a native Wear OS component designed for displaying grouped information
        item {
            Spacer(modifier = Modifier.height(8.dp))
            TitleCard(
                onClick = { /* Optional: Expand to see more details */ },
                title = {
                    Text(
                        text = if (uiState.isTracking) "Live Stats" else "Summary",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                // Using standard string formatting to keep numbers clean
                Text(
                    text = "Distance: ${String.format("%.1f", uiState.distanceMiles)} mi\n" +
                            "Speed: ${String.format("%.1f", uiState.currentSpeedMph)} mph\n" +
                            "Heart Rate: ${uiState.heartRateBpm} bpm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. ACTION BUTTONS
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // In a real scenario, this might trigger a 'BikeUiEvent.DeleteRide(rideId)'
            // For now, we simulate success by simply popping the Nav3 backstack.
            Button(
                onClick = { onDeleteSuccess() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Ride"
                )
            }
        }

        item {
            Text(
                text = "Discard",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}