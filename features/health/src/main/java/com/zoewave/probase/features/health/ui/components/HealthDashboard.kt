package com.zoewave.probase.features.health.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class HealthTab(val label: String, val icon: ImageVector) {
    Settings("Settings", Icons.Default.Settings),
    Data("Overview", Icons.Default.DateRange),
    Sessions("Sessions", Icons.Default.List)
}

@Composable
fun HealthDashboard(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(HealthTab.Data) }

    // 1. State to track which session is clicked (null = no dialog)
    var selectedSession by remember { mutableStateOf<ExerciseSessionRecord?>(null) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                HealthTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (currentTab) {
                HealthTab.Settings -> { /* ... same as before ... */
                    HealthConnectionStatus(onEvent = onEvent)
                }

                HealthTab.Data -> { /* ... same as before ... */
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        GenericWeeklyChart("Steps", state.weeklySteps.mapValues { it.value.toDouble() }, MaterialTheme.colorScheme.primary) { "${it.toInt()}" }
                        GenericWeeklyChart("Calories", state.weeklyCalories, Color(0xFFFF9800)) { "${it.toInt()}" }
                        GenericWeeklyChart("Distance", state.weeklyDistance, Color(0xFF03A9F4)) { String.format("%.1f", it / 1000) }
                    }
                }

                HealthTab.Sessions -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Recent Sessions",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (state.sessions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No recent activities found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.sessions) { session ->
                                    SessionCard(
                                        session = session,
                                        onClick = { selectedSession = session } // <--- Handle Click
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Show Dialog if a session is selected
        if (selectedSession != null) {
            SessionDetailDialog(
                session = selectedSession!!,
                onDismiss = { selectedSession = null }
            )
        }
    }
}

// --- Clickable Card ---
@Composable
private fun SessionCard(
    session: ExerciseSessionRecord,
    onClick: () -> Unit // <--- New Parameter
) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm").withZone(ZoneId.systemDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // <--- Make it clickable
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.title ?: "Workout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatter.format(session.startTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!session.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session.notes!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- The Detail Dialog ---
@Composable
fun SessionDetailDialog(
    session: ExerciseSessionRecord,
    onDismiss: () -> Unit
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

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}