package com.zoewave.probase.ashbike.wear.features.rides.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zoewave.ashbike.model.bike.BikeRide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun RideHistoryCard(
    ride: BikeRide,
    onRideClick: () -> Unit,
    onDeleteClick: (BikeRide) -> Unit,
    onForceSyncClick: (BikeRide) -> Unit,
    modifier: Modifier = Modifier
) {
    // Cache the formatter and the resulting string
    val formatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateString = remember(ride.startTime) { formatter.format(Date(ride.startTime)) }

    val durationSeconds = remember(ride.startTime, ride.endTime) {
        ((ride.endTime - ride.startTime) / 1000).coerceAtLeast(0)
    }

    Card(
        onClick = onRideClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Header Row: Date + (Duration & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.caption1,//.titleMedium,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${durationSeconds}s",
                        style = MaterialTheme.typography.caption2,//.bodyExtraSmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete Button
                    Button(
                        onClick = { onDeleteClick(ride) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Ride",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stats Body - Formatted to 1 decimal place to handle Floats cleanly
            Text(
                text = "Distance: ${String.format(Locale.getDefault(), "%.1f", ride.totalDistance)} km",
                style = MaterialTheme.typography.caption2,//.bodySmall,
                color = Color.LightGray
            )
            Text(
                text = "Avg: ${String.format(Locale.getDefault(), "%.1f", ride.averageSpeed)} km/h",
                style = MaterialTheme.typography.caption2,//.bodySmall,
                color = Color.LightGray
            )
            Text(
                text = "Max: ${String.format(Locale.getDefault(), "%.1f", ride.maxSpeed)} km/h",
                style = MaterialTheme.typography.caption2,//.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sync UI Logic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (ride.isAcknowledged) {
                    CompactChip(
                        onClick = { /* Disabled visually, already synced */ },
                        label = { Text("Synced", color = Color.White) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Acknowledged by Phone",
                                tint = Color.Green
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(
                            backgroundColor = Color.DarkGray
                        )
                    )
                } else {
                    CompactChip(
                        onClick = { onForceSyncClick(ride) },
                        label = { Text("Tap to Sync", color = Color.White) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Force Sync",
                                tint = Color(0xFF64B5F6)
                            )
                        },
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------

private fun getMockRide(isAcknowledged: Boolean): BikeRide {
    return BikeRide(
        rideId = UUID.randomUUID().toString(),
        startTime = System.currentTimeMillis() - 60000,
        endTime = System.currentTimeMillis(),
        totalDistance = 12.4f,
        averageSpeed = 22.1f,
        maxSpeed = 35.6f,
        elevationGain = 120f,
        elevationLoss = 110f,
        caloriesBurned = 350,
        avgHeartRate = 145,
        maxHeartRate = 165,
        isHealthDataSynced = false,
        isAcknowledged = isAcknowledged,
        healthConnectRecordId = null,
        weatherCondition = "Sunny",
        rideType = "Fitness",
        notes = null,
        rating = null,
        bikeId = null,
        batteryStart = 100,
        batteryEnd = 85,
        startLat = 37.3697,
        startLng = -122.0821,
        endLat = 37.3697,
        endLng = -122.0821,
        locations = emptyList() // Empty list to satisfy the compiler for the preview
    )
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true
)
@Composable
fun PreviewRideHistoryCard_PendingSync() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            RideHistoryCard(
                ride = getMockRide(isAcknowledged = false), // Pending state
                onRideClick = {},
                onDeleteClick = {},
                onForceSyncClick = {}
            )
        }
    }
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true
)
@Composable
fun PreviewRideHistoryCard_Acknowledged() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            RideHistoryCard(
                ride = getMockRide(isAcknowledged = true), // Synced state
                onRideClick = {},
                onDeleteClick = {},
                onForceSyncClick = {}
            )
        }
    }
}