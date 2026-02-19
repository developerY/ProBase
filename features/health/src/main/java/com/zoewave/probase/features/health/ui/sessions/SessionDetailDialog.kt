package com.zoewave.probase.features.health.ui.sessions // Adjust based on your new package structure

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailDialog(
    session: ExerciseSessionRecord,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

    // Calculate Duration
    val duration = Duration.between(session.startTime, session.endTime)
    val durationString = "${duration.toMinutes()} min ${duration.seconds % 60} sec"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = session.title ?: "Workout Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Rows
                DetailRow("Date", dateFormatter.format(session.startTime))
                DetailRow("Start Time", timeFormatter.format(session.startTime))
                DetailRow("End Time", timeFormatter.format(session.endTime))
                DetailRow("Duration", durationString)

                if (!session.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Notes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(session.notes!!, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // ID (Useful for debugging sync)
                Text(
                    text = "ID: ${session.metadata.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Action Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // DELETE BUTTON
                    OutlinedButton(
                        onClick = {
                            onDelete(session.metadata.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }

                    // CLOSE BUTTON
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// --- Preview Section ---

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SessionDetailDialogPreview() {
    // 1. Create realistic mock times
    val endTime = Instant.now()
    val startTime = endTime.minus(Duration.ofMinutes(45).plusSeconds(30))

    // 2. Build the mock ExerciseSessionRecord
    val mockSession = ExerciseSessionRecord(
        startTime = startTime,
        startZoneOffset = ZoneOffset.UTC,
        endTime = endTime,
        endZoneOffset = ZoneOffset.UTC,
        exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
        title = "Test City Ride \uD83D\uDEB4",
        notes = "Simulated ride created via Debug Menu to test UI rendering.",
        metadata = Metadata.manualEntry(
            clientRecordId = "550e8400-e29b-41d4-a716-446655440000",
            device = Device(type = Device.TYPE_PHONE)
        )
    )

    MaterialTheme {
        SessionDetailDialog(
            session = mockSession,
            onDismiss = { /* No-op */ },
            onDelete = { /* No-op */ }
        )
    }
}