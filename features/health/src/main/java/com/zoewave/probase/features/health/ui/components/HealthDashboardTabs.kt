package com.zoewave.probase.features.health.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 1. Add "Activities" to the Tab Enum
private enum class HealthTabs(val label: String, val icon: ImageVector) {
    Steps("Steps", Icons.Default.DateRange),
    Calories("Energy", Icons.Default.Star),
    Distance("Distance", Icons.Default.Place),
    Activities("Activities", Icons.Default.List) // <--- NEW TAB
}

@Composable
fun HealthDashboardTabs(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(HealthTabs.Steps) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                HealthTabs.entries.forEach { tab ->
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

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Common Header ---
            HealthConnectionStatus(onEvent = onEvent)

            Spacer(modifier = Modifier.height(16.dp))

            // --- Switch Content ---
            if (currentTab == HealthTabs.Activities) {
                // Show List of Sessions
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Start).padding(vertical = 8.dp)
                )

                if (state.sessions.isEmpty()) {
                    Text("No recent activities found.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.sessions) { session ->
                            SessionCard(session)
                        }
                    }
                }
            } else {
                // Show Charts (Wrapped in Scroll because charts are tall)
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                ) {
                    // Action Button (Only show on chart tabs)
                    Button(
                        onClick = { onEvent(HealthEvent.WriteTestRide) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Test City Ride (4.5km)")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "${currentTab.label} (Last 7 Days)",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    when (currentTab) {
                        HealthTabs.Steps -> GenericWeeklyChart(
                            title = "Steps",
                            data = state.weeklySteps.mapValues { it.value.toDouble() },
                            color = MaterialTheme.colorScheme.primary,
                            formatValue = { v -> if (v > 999) "${(v / 1000).toInt()}k" else "${v.toInt()}" }
                        )
                        HealthTabs.Calories -> GenericWeeklyChart(
                            title = "Calories (kcal)",
                            data = state.weeklyCalories,
                            color = Color(0xFFFF9800),
                            formatValue = { v -> "${v.toInt()}" }
                        )
                        HealthTabs.Distance -> GenericWeeklyChart(
                            title = "Distance (km)",
                            data = state.weeklyDistance,
                            color = Color(0xFF03A9F4),
                            formatValue = { v -> String.format("%.1f", v / 1000) }
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}

// --- Helper Component for the List Item ---
@Composable
fun SessionCard(session: ExerciseSessionRecord) {
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