package com.zoewave.probase.features.health.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.data.service.health.ExerciseSessionData
import com.zoewave.probase.features.health.R
import java.time.Duration

@Composable
fun SessionDetailContent(
    data: ExerciseSessionData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.features_health_session_id_format, data.uid), style = MaterialTheme.typography.titleLarge)

        Text(
            stringResource(R.string.features_health_session_duration_format, formatDuration(data.totalActiveTime)),
            style = MaterialTheme.typography.bodyLarge
        )
        data.totalDistance?.let {
            Text(
                stringResource(R.string.features_health_session_distance_format, "%.2f".format(it.inFeet)),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        data.totalSteps?.let {
            Text(stringResource(R.string.features_health_session_steps_format, it), style = MaterialTheme.typography.bodyLarge)
        }
        data.totalEnergyBurned?.let {
            Text(
                stringResource(R.string.features_health_session_calories_format, "%.0f".format(it.inCalories)),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data.minHeartRate?.let { Text(stringResource(R.string.features_health_session_min_hr_format, it)) }
            data.avgHeartRate?.let { Text(stringResource(R.string.features_health_session_avg_hr_format, it)) }
            data.maxHeartRate?.let { Text(stringResource(R.string.features_health_session_max_hr_format, it)) }
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
