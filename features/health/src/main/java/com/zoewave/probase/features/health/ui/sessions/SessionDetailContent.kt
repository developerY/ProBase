package com.zoewave.probase.features.health.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.data.service.health.ExerciseSessionData
import java.time.Duration

@Composable
fun SessionDetailContent(
    data: ExerciseSessionData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ID ${data.uid}", style = MaterialTheme.typography.titleLarge)

        Text(
            "Duration: ${formatDuration(data.totalActiveTime)}",
            style = MaterialTheme.typography.bodyLarge
        )
        data.totalDistance?.let {
            Text(
                "Distance: ${"%.2f".format(it.inFeet)} km",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        data.totalSteps?.let {
            Text("Steps: $it", style = MaterialTheme.typography.bodyLarge)
        }
        data.totalEnergyBurned?.let {
            Text(
                "Calories: ${"%.0f".format(it.inCalories)} kcal",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data.minHeartRate?.let { Text("Min HR: $it bpm") }
            data.avgHeartRate?.let { Text("Avg HR: $it bpm") }
            data.maxHeartRate?.let { Text("Max HR: $it bpm") }
        }
    }
}


private fun formatDuration(duration: Duration?): String {
    if (duration == null) return "--"
    val h = duration.toHours()
    val m = duration.toMinutes() % 60
    val s = duration.seconds % 60
    return buildString {
        if (h > 0) append("${h}h ")
        if (m > 0 || h > 0) append("${m}m ")
        append("${s}s")
    }
}
