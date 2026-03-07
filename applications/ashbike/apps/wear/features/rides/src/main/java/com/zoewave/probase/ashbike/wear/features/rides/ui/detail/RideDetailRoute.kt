package com.zoewave.probase.ashbike.wear.features.rides.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.probase.ashbike.wear.features.rides.RidesEvent
import com.zoewave.probase.ashbike.wear.features.rides.RidesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ==========================================
// 1. The Route (Traffic Controller & Reactive State)
// ==========================================
@Composable
fun RideDetailRoute(
    rideId: String,
    viewModel: RidesViewModel = hiltViewModel(),
    onWearRideMapRoute: (String) -> Unit, // Navigation callback
    onNavigateBack: () -> Unit            // Navigation callback
) {
    // REACTIVE FIX: This Flow constantly watches the DB.
    // When the ACK arrives, this auto-updates and Compose redraws the chip!
    val ride by viewModel.getRideFlow(rideId).collectAsStateWithLifecycle(initialValue = null)

    ride?.let { safeRide ->
        RideDetailPage(
            ride = safeRide,
            onMapClick = { onWearRideMapRoute(safeRide.rideId) },
            onEvent = { event ->
                // MVI FIX: The Route intercepts the event, fires it to the VM, and handles navigation
                when (event) {
                    is RidesEvent.OnDeleteClick -> {
                        viewModel.onEvent(event)
                        onNavigateBack() // Pop back to history after deleting
                    }
                    else -> viewModel.onEvent(event) // Pass sync clicks straight to the VM
                }
            }
        )
    } ?: run {
        // The ?: (Elvis operator) handles the 'else' (null) case
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Much cleaner than text!
        }
    }
}

// ==========================================
// 2. The Stateless UI (Purely Data & Events)
// ==========================================
@Composable
fun RideDetailPage(
    ride: BikeRide,
    onMapClick: () -> Unit, // Pure UI navigation isolated from business logic
    onEvent: (RidesEvent) -> Unit, // MVI Dispatcher
    modifier: Modifier = Modifier
) {
    // PERFORMANCE FIX: Cache these heavy formatters so they don't rebuild on scroll
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val dateString = remember(ride.startTime) { dateFormatter.format(Date(ride.startTime)) }
    val timeString = remember(ride.startTime) { timeFormatter.format(Date(ride.startTime)) }
    val durationMins = remember(ride.startTime, ride.endTime) {
        ((ride.endTime - ride.startTime) / 60000).coerceAtLeast(0)
    }

    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        // --- Header ---
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = dateString, style = MaterialTheme.typography.titleMedium)
                Text(text = timeString, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // --- Core Stats Card ---
        item {
            TitleCard(
                onClick = onMapClick,
                title = { Text("Performance") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricColumn(label = "Dist", value = "${String.format(Locale.getDefault(), "%.1f", ride.totalDistance)} km")
                    MetricColumn(label = "Time", value = "${durationMins} m")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricColumn(label = "Avg Spd", value = "${String.format(Locale.getDefault(), "%.1f", ride.averageSpeed)} km/h")
                    MetricColumn(label = "Max Spd", value = "${String.format(Locale.getDefault(), "%.1f", ride.maxSpeed)} km/h")
                }
            }
        }

        // --- Elevation & Environment Card ---
        item {
            TitleCard(
                onClick = { /* Future Expansion */ },
                title = { Text("Environment") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricColumn(label = "Gain", value = "+${ride.elevationGain} m")
                    MetricColumn(label = "Loss", value = "-${ride.elevationLoss} m")
                }
            }
        }

        // --- Health Metrics ---
        if (ride.avgHeartRate != null || ride.caloriesBurned > 0) {
            item {
                TitleCard(
                    onClick = { /* Future Expansion */ },
                    title = { Text("Health") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricColumn(label = "Avg HR", value = "${ride.avgHeartRate ?: "--"} bpm")
                        MetricColumn(label = "Cals", value = "${ride.caloriesBurned} kcal")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // --- Sync Action (Reactive via Flow) ---
        item {
            if (ride.isAcknowledged) {
                Chip(
                    onClick = { /* Disabled visually */ },
                    colors = ChipDefaults.secondaryChipColors(backgroundColor = Color.DarkGray),
                    icon = {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Synced", tint = Color.Green)
                    },
                    label = { Text("Synced to Phone", color = Color.White) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Chip(
                    onClick = { onEvent(RidesEvent.OnForceSyncClick(ride)) }, // Dispatch!
                    colors = ChipDefaults.primaryChipColors(),
                    icon = {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = Color(0xFF64B5F6))
                    },
                    label = { Text("Tap to Sync", color = Color.White) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // --- Destructive Action ---
        item {
            Chip(
                onClick = { onEvent(RidesEvent.OnDeleteClick(ride.rideId)) }, // Dispatch!
                colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xFFB3261E)),
                icon = {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                },
                label = { Text("Delete Ride", color = Color.White) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Small helper composable for formatting the grid of stats
@Composable
private fun MetricColumn(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

// ==========================================
// 3. Previews
// ==========================================

private fun getMockRide(isAcknowledged: Boolean): BikeRide = BikeRide(
    rideId = UUID.randomUUID().toString(),
    startTime = System.currentTimeMillis() - (1000 * 60 * 45),
    endTime = System.currentTimeMillis(),
    totalDistance = 18.5f,
    averageSpeed = 24.6f,
    maxSpeed = 42.1f,
    elevationGain = 210f,
    elevationLoss = 205f,
    caloriesBurned = 520,
    avgHeartRate = 152,
    maxHeartRate = 178,
    isHealthDataSynced = false,
    isAcknowledged = isAcknowledged,
    healthConnectRecordId = null,
    weatherCondition = "Sunny",
    rideType = "Fitness",
    notes = null,
    rating = null,
    bikeId = null,
    batteryStart = 100,
    batteryEnd = 80,
    startLat = 37.3697, startLng = -122.0821,
    endLat = 37.3697, endLng = -122.0821,
    locations = emptyList()
)

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewRideDetailPage_PendingSync() {
    MaterialTheme {
        RideDetailPage(
            ride = getMockRide(isAcknowledged = false),
            onMapClick = {},
            onEvent = {}
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewRideDetailPage_Acknowledged() {
    MaterialTheme {
        RideDetailPage(
            ride = getMockRide(isAcknowledged = true),
            onMapClick = {},
            onEvent = {}
        )
    }
}