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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// ==========================================
// 1. The Route (Connects to ViewModel)
// ==========================================
@Composable
fun RideDetailRoute(
    rideId: String,
    viewModel: RidesViewModel = hiltViewModel(), // Assuming you use the same VM or a specific one
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
// 2. The Stateless UI (Great for Previews)
// ==========================================
@Composable
fun RideDetailPage(
    ride: BikeRide,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    val dateString = dateFormatter.format(Date(ride.startTime))
    val timeString = timeFormatter.format(Date(ride.startTime))
    val durationMins = ((ride.endTime - ride.startTime) / 60000).coerceAtLeast(0)

    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        // --- Header ---
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = dateString, style = MaterialTheme.typography.titleMedium)//.title3)
                Text(text = timeString, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // --- Core Stats Card ---
        item {
            TitleCard(
                onClick = { /* Do nothing or expand */ },
                title = { Text("Performance") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricColumn(label = "Dist", value = "${ride.totalDistance} km")
                    MetricColumn(label = "Time", value = "${durationMins} m")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricColumn(label = "Avg Spd", value = "${ride.averageSpeed} km/h")
                    MetricColumn(label = "Max Spd", value = "${ride.maxSpeed} km/h")
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

        // --- Destructive Action ---
        item { Spacer(modifier = Modifier.height(16.dp)) }
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
                label = {
                    Text("Delete Ride", color = Color.White)
                },
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
        Text(text = value, style = MaterialTheme.typography.bodyMedium)//.body1)
    }
}