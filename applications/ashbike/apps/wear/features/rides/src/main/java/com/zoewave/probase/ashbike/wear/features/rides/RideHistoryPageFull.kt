package com.zoewave.probase.ashbike.wear.features.rides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.tooling.preview.devices.WearDevices
import com.zoewave.ashbike.model.bike.BikeRide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 2. The UI Page (Stateless & Previewable)
// ==========================================
@Composable
fun RideHistoryPageFull(
    rides: List<BikeRide>, // ✅ Changed to expect BikeRide domain models
    onRideClick: (String) -> Unit
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // Crucial: Padding pushes the first/last items safely past the round screen curves
        contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- Header ---
        item {
            ListHeader {
                Text(
                    text = "Ride History",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium// title3
                )
            }
        }

        // --- Empty State ---
        if (rides.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No recorded rides yet.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Ride Cards ---
        items(rides, key = { it.rideId }) { ride ->
            TitleCard(
                onClick = { onRideClick(ride.rideId) },
                title = {
                    Text(
                        text = formatStartDate(ride.startTime),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                },
                // The "time" slot is natively top-right aligned in Wear OS TitleCards
                time = {
                    Text(
                        text = formatDuration(ride.startTime, ride.endTime),
                        color = MaterialTheme.colorScheme.primary,//.colors.primary,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Card Body matching your phone's history feed
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Distance: ${String.format(Locale.US, "%.1f", ride.totalDistance)} km",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Avg: ${String.format(Locale.US, "%.1f", ride.averageSpeed)} km/h",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Max: ${String.format(Locale.US, "%.1f", ride.maxSpeed)} km/h",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. Formatting Helpers
// ==========================================
private fun formatStartDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

private fun formatDuration(start: Long, end: Long): String {
    if (end <= start) return "0s"
    val diffSeconds = (end - start) / 1000
    val minutes = diffSeconds / 60
    val seconds = diffSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

// ==========================================
// 4. Previews (Test your scaling behavior!)
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "History List - Populated"
)


// ==========================================
// Previews (Test your scaling behavior!)
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "History List - Populated"
)
@Composable
fun RideHistoryPagePreview() {
    val dummyRides = listOf(
        BikeRide(
            rideId = "1",
            startTime = System.currentTimeMillis() - 14000,
            endTime = System.currentTimeMillis(),
            totalDistance = 0.3f,
            averageSpeed = 64.1f, // 2. Added 'f' to convert Double to Float
            maxSpeed = 37.1f,     // Added 'f'
            startLat = 0.0, startLng = 0.0, endLat = 0.0, endLng = 0.0,
            elevationGain = 0.0f, // Added 'f'
            elevationLoss = 0.0f, // Added 'f'
            caloriesBurned = 0,
            isHealthDataSynced = false,
            // 3. Added all missing required parameters
            avgHeartRate = null, maxHeartRate = null, healthConnectRecordId = null,
            weatherCondition = null, rideType = null, notes = null, rating = null,
            bikeId = null, batteryStart = null, batteryEnd = null,
            locations = emptyList()
        ),
        BikeRide(
            rideId = "2",
            startTime = System.currentTimeMillis() - 86400000 - 3600000,
            endTime = System.currentTimeMillis() - 86400000,
            totalDistance = 24.5f,
            averageSpeed = 24.5f, // Added 'f'
            maxSpeed = 41.2f,     // Added 'f'
            startLat = 0.0, startLng = 0.0, endLat = 0.0, endLng = 0.0,
            elevationGain = 120.0f, // Added 'f'
            elevationLoss = 110.0f, // Added 'f'
            caloriesBurned = 540,
            isHealthDataSynced = true,
            // 3. Added all missing required parameters
            avgHeartRate = 145, maxHeartRate = 165, healthConnectRecordId = "hc_123",
            weatherCondition = "Sunny", rideType = "Gravel", notes = "Morning spin", rating = 5,
            bikeId = "bike_01", batteryStart = 100, batteryEnd = 82,
            locations = emptyList()
        )
    )

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            RideHistoryPageFull(
                rides = dummyRides,
                onRideClick = {}
            )
        }
    }
}