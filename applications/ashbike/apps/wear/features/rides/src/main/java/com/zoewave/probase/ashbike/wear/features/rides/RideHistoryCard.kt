package com.zoewave.probase.ashbike.wear.features.rides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.zoewave.ashbike.model.bike.BikeRide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RideHistoryCard(
    ride: BikeRide,
    onRideClick: () -> Unit,
    onDeleteClick: (BikeRide) -> Unit,
    modifier: Modifier = Modifier
) {
    // Helper to format the timestamp (e.g., "Feb 21, 02:12")
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateString = formatter.format(Date(ride.startTime))

    // Helper to calculate duration in seconds (for the top right corner)
    val durationSeconds = ((ride.endTime - ride.startTime) / 1000).coerceAtLeast(0)

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
                    style = MaterialTheme.typography.titleMedium,//title3,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${durationSeconds}s",
                        style = MaterialTheme.typography.bodyExtraSmall,//.arcSmall,//.caption2,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Small Delete Button
                    Button(
                        onClick = { onDeleteClick(ride) },
                        // colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Ride",
                            tint = Color.Gray, // Keeping it subtle to match the UI
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stats Body
            Text(
                text = "Distance: ${ride.totalDistance} km",
                style = MaterialTheme.typography.bodySmall,//.body2,
                color = Color.LightGray
            )
            Text(
                text = "Avg: ${ride.averageSpeed} km/h",
                style = MaterialTheme.typography.bodySmall,//.body2,
                color = Color.LightGray
            )
            Text(
                text = "Max: ${ride.maxSpeed} km/h",
                style = MaterialTheme.typography.bodySmall,//.body2,
                color = Color.LightGray
            )
        }
    }
}