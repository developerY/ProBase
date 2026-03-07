package com.zoewave.probase.ashbike.wear.features.rides.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.probase.ashbike.wear.features.rides.RidesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ==========================================
// 1. The Route (Connects to ViewModel)
// ==========================================
@Composable
fun RideDetailRoute(
    rideId: String,
    viewModel: RidesViewModel = hiltViewModel(),
    onWearRideMapRoute: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Fetch the specific ride based on the ID passed from the Navigation 3 graph
    // (You'll need a function in your ViewModel like `getRideById(rideId)`)
    var ride by remember { mutableStateOf<BikeRide?>(null) }

    // LaunchedEffect provides the coroutine scope
    LaunchedEffect(key1 = rideId) {
        // This suspends until the DB returns the ride
        ride = viewModel.getRide(rideId)
    }

    ride?.let { safeRide ->
        // This block ONLY executes if ride is not null.
        // safeRide is a guaranteed non-null BikeRide.
        RideDetailPage(
            ride = safeRide,
            onWearRideMapRoute = onWearRideMapRoute,
            onForceSyncClick = {
                // Assuming you have a function in your VM that calls your WearRideSyncEngine
                // viewModel.syncRideToPhone(safeRide)
            },
            onDeleteClick = {
                viewModel.deleteRide(safeRide.rideId)
                onNavigateBack()
            }
        )
    } ?: run {
        // The ?: (Elvis operator) handles the 'else' (null) case
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
    }
}

// ==========================================
// 2. The Stateless UI
// ==========================================
@Composable
fun RideDetailPage(
    ride: BikeRide,
    onWearRideMapRoute: () -> Unit,
    onForceSyncClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
                onClick = onWearRideMapRoute,
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
                onClick = { },
                title = { Text("Environment") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricColumn(label = "Gain", value = "+${ride.elevationGain} m")
                    MetricColumn(label = "Loss", value = "-${ride.elevationLoss} m")
                }
            }
        }

        // --- Health Metrics (Conditionally Rendered) ---
        if (ride.avgHeartRate != null || ride.caloriesBurned > 0) {
            item {
                TitleCard(
                    onClick = { },
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

        // --- THE NEW SYNC ACTION ---
        item {
            if (ride.isAcknowledged) {
                Chip(
                    onClick = { /* Disabled visually, already synced */ },
                    colors = ChipDefaults.secondaryChipColors(backgroundColor = Color.DarkGray),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Synced to Phone",
                            tint = Color.Green
                        )
                    },
                    label = { Text("Synced to Phone", color = Color.White) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Chip(
                    onClick = onForceSyncClick,
                    colors = ChipDefaults.primaryChipColors(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Force Sync",
                            tint = Color(0xFF64B5F6) // Light Blue
                        )
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
                onClick = onDeleteClick,
                colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xFFB3261E)), // Material Red
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
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

// Helper function to generate a realistic mock ride for the previews
private fun getMockRide(isAcknowledged: Boolean): BikeRide {
    return BikeRide(
        rideId = UUID.randomUUID().toString(),
        startTime = System.currentTimeMillis() - (1000 * 60 * 45), // 45 mins ago
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
        startLat = 37.3697,
        startLng = -122.0821,
        endLat = 37.3697,
        endLng = -122.0821,
        locations = emptyList() // Empty list to satisfy the compiler
    )
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Pending Sync"
)
@Composable
fun PreviewRideDetailPage_PendingSync() {
    MaterialTheme {
        RideDetailPage(
            ride = getMockRide(isAcknowledged = false), // State: Trapped on Watch
            onWearRideMapRoute = {},
            onForceSyncClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Acknowledged"
)
@Composable
fun PreviewRideDetailPage_Acknowledged() {
    MaterialTheme {
        RideDetailPage(
            ride = getMockRide(isAcknowledged = true), // State: Saved on Phone
            onWearRideMapRoute = {},
            onForceSyncClick = {},
            onDeleteClick = {}
        )
    }
}