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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.probase.ashbike.wear.features.rides.RidesEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.zoewave.ashbike.wear.rides.R.string as RidesR

// 1. The Card only takes the data it needs and a single event dispatcher
@Composable
fun RideHistoryCard(
    ride: BikeRide,
    onEvent: (RidesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateString = remember(ride.startTime) { formatter.format(Date(ride.startTime)) }

    val durationStr = remember(ride.startTime, ride.endTime) {
        val mins = ((ride.endTime - ride.startTime) / 60000)
        mins
    }

    Card(
        onClick = { onEvent(RidesEvent.OnRideClick(ride.rideId)) }, // Dispatches the event!
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically//.Baseline
            ) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.title3,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                /* 🚀 THE DELETE BUTTON
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_delete_ride_description),
                    tint = MaterialTheme.colors.error, // Makes it red!
                    modifier = Modifier
                        .size(24.dp)
                        // padding increases the touch target size slightly
                        .padding(2.dp)
                        .clickable {
                            // onDeleteClick(ride.id)
                        }
                )*/

                Text(
                    text = if (durationStr <= 0) stringResource(RidesR.applications_ashbike_apps_wear_features_rides_duration_less_than_one_minute) else stringResource(RidesR.applications_ashbike_apps_wear_features_rides_duration_minutes_format, durationStr),
                    style = MaterialTheme.typography.caption1,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hero Metric: Distance
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", ride.totalDistance / 1000f),
                    style = MaterialTheme.typography.display3,
                    color = Color(0xFF64B5F6)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_distance_unit_km),
                    style = MaterialTheme.typography.body2,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_avg_speed_format, ride.averageSpeed),
                    style = MaterialTheme.typography.caption1,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(RidesR.applications_ashbike_apps_wear_features_rides_max_speed_format, ride.maxSpeed),
                    style = MaterialTheme.typography.caption1,
                    color = Color.Gray
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------

private fun getMockRide(): BikeRide {
    return BikeRide(
        rideId = UUID.randomUUID().toString(),
        startTime = System.currentTimeMillis() - (1000 * 60 * 45), // 45 mins ago
        endTime = System.currentTimeMillis(),
        totalDistance = 18.4f,
        averageSpeed = 22.1f,
        maxSpeed = 35.6f,
        elevationGain = 120f,
        elevationLoss = 110f,
        caloriesBurned = 350,
        avgHeartRate = 145,
        maxHeartRate = 165,
        isHealthDataSynced = false,
        isAcknowledged = false,
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
        locations = emptyList()
    )
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true
)
@Composable
fun PreviewRideHistoryCard() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            RideHistoryCard(
                ride = getMockRide(),
                onEvent = {},
            )
        }
    }
}