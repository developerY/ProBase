package com.zoewave.probase.features.health.core.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.zoewave.probase.core.model.health.SleepSessionData
import com.zoewave.probase.features.health.core.R
import com.zoewave.probase.features.health.core.ui.HealthEvent
import com.zoewave.probase.features.health.core.ui.HealthUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SessionsTab(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSession by remember { mutableStateOf<ExerciseSessionRecord?>(null) }
    val sessions = state.sessions
    val sleepSessions = state.sleepSessions

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- 1. Debug Tools (Relocated here) ---
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.features_health_debug_tools_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.features_health_debug_tools_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { onEvent(HealthEvent.WriteTestRide) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.features_health_action_add_test_ride))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onEvent(HealthEvent.SeedData) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.features_health_core_action_seed_data))
                }
            }
        }

        // --- 2. Exercise Sessions Section ---
        item {
            Text(
                text = "Exercise Sessions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (sessions.isEmpty()) {
            item {
                Text("No exercise activities found.", color = Color.Gray)
            }
        } else {
            items(sessions) { session ->
                SessionCard(
                    session = session,
                    onClick = { selectedSession = session }
                )
            }
        }

        // --- 3. Sleep Sessions Section ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sleep Sessions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (sleepSessions.isEmpty()) {
            item {
                Text("No sleep sessions found.", color = Color.Gray)
            }
        } else {
            items(sleepSessions) { sleep ->
                SleepSessionCard(sleep = sleep)
            }
        }
        
        // --- 4. Daily Metrics (Summary) ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Daily Activity Summaries",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        val dates = (state.weeklySteps.keys + state.weeklyCalories.keys + state.weeklyDistance.keys + state.weeklyHydration.keys).sortedDescending()
        
        if (dates.isEmpty()) {
            item {
                Text("No daily activity data found.", color = Color.Gray)
            }
        } else {
            items(dates) { date ->
                DailyMetricCard(
                    date = date,
                    steps = state.weeklySteps[date],
                    calories = state.weeklyCalories[date],
                    distance = state.weeklyDistance[date],
                    hydration = state.weeklyHydration[date]
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // Show Dialog if a session is selected
    if (selectedSession != null) {
        SessionDetailDialog(
            session = selectedSession!!,
            onDismiss = { selectedSession = null },
            onDelete = { uid ->
                onEvent(HealthEvent.DeleteSession(uid))
            }
        )
    }
}

@Composable
fun SleepSessionCard(sleep: SleepSessionData) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm").withZone(ZoneId.systemDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sleep.title ?: "Sleep",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatter.format(sleep.startTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            sleep.notes?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            sleep.duration?.let {
                Text(
                    text = "Duration: ${it.toHours()}h ${it.toMinutesPart()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DailyMetricCard(date: String, steps: Long?, calories: Double?, distance: Double?, hydration: Double? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                steps?.let { Text("Steps: $it", style = MaterialTheme.typography.bodySmall) }
                calories?.let { Text("Cals: ${it.toInt()}kcal", style = MaterialTheme.typography.bodySmall) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                distance?.let { Text("Dist: ${String.format("%.1f", it / 1000)}km", style = MaterialTheme.typography.bodySmall) }
                hydration?.let { Text("Water: ${String.format("%.1f", it)}L", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
