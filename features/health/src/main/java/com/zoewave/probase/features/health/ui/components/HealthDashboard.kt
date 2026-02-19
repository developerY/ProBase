package com.zoewave.probase.features.health.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 1. Define the 3 Main Tabs
private enum class HealthTab(val label: String, val icon: ImageVector) {
    Settings("Settings", Icons.Default.Settings), // Connection & Permissions
    Data("Overview", Icons.Default.DateRange),    // All Charts
    Sessions("Sessions", Icons.Default.List)      // List of Activities
}

@Composable
fun HealthDashboard(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(HealthTab.Data) } // Default to Data

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

        // Main Container
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (currentTab) {
                // --- TAB 1: SETTINGS ---
                HealthTab.Settings -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Health Connect Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Re-use your existing component
                        HealthConnectionStatus(onEvent = onEvent)

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Debug Tools",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use these tools to simulate data if you don't have a real device connected.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Move the Test Button here since it's a "Setup/Debug" action
                        Button(
                            onClick = { onEvent(HealthEvent.WriteTestRide) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Test City Ride (4.5km)")
                        }
                    }
                }

                // --- TAB 2: DATA OVERVIEW (Charts) ---
                HealthTab.Data -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Last 7 Days Activity",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 16.dp)
                        )

                        // Stack all 3 charts
                        GenericWeeklyChart(
                            title = "Steps",
                            data = state.weeklySteps.mapValues { it.value.toDouble() },
                            color = MaterialTheme.colorScheme.primary,
                            formatValue = { v -> if (v > 999) "${(v / 1000).toInt()}k" else "${v.toInt()}" }
                        )

                        GenericWeeklyChart(
                            title = "Calories (kcal)",
                            data = state.weeklyCalories,
                            color = Color(0xFFFF9800),
                            formatValue = { v -> "${v.toInt()}" }
                        )

                        GenericWeeklyChart(
                            title = "Distance (km)",
                            data = state.weeklyDistance,
                            color = Color(0xFF03A9F4),
                            formatValue = { v -> String.format("%.1f", v / 1000) }
                        )
                    }
                }

                // --- TAB 3: SESSIONS (List) ---
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
                                    SessionCard(session)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Helper for Session List ---
@Composable
private fun SessionCard(session: ExerciseSessionRecord) {
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