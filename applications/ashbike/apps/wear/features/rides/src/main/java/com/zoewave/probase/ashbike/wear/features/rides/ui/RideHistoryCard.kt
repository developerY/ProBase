package com.zoewave.probase.ashbike.wear.features.rides.ui
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun RideHistoryCard(
    ride: BikeRide,
    onRideClick: () -> Unit,
    onDeleteClick: (BikeRide) -> Unit,
    onForceSyncClick: (BikeRide) -> Unit, // 👈 New sync callback
    modifier: Modifier = Modifier
) {
    // 1. PERFORMANCE: Cache the formatter and the resulting string
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
                    style = MaterialTheme.typography.title2,//.titleMedium,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${durationSeconds}s",
                        style = MaterialTheme.typography.caption2,//.bodyExtraSmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete Button (48dp touch target, 20dp visual)
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

            // Stats Body
            Text(
                text = "Distance: ${ride.totalDistance} km",
                style = MaterialTheme.typography.display1,//.bodySmall,
                color = Color.LightGray
            )
            Text(
                text = "Avg: ${ride.averageSpeed} km/h",
                style = MaterialTheme.typography.display1,//.bodySmall,
                color = Color.LightGray
            )
            Text(
                text = "Max: ${ride.maxSpeed} km/h",
                style = MaterialTheme.typography.display1,//.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. THE NEW SYNC UI LOGIC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (ride.isAcknowledged) {
                    // State: Confirmed by Phone
                    CompactChip(
                        onClick = { /* Disabled visually, already synced */ },
                        label = { Text("Synced", color = Color.White) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Synced to Phone",
                                tint = Color.Green
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(
                            backgroundColor = Color.DarkGray
                        )
                    )
                } else {
                    // State: Stuck on Watch
                    CompactChip(
                        onClick = { onForceSyncClick(ride) },
                        label = { Text("Tap to Sync", color = Color.White) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Force Sync",
                                tint = Color(0xFF64B5F6) // Light Blue
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

