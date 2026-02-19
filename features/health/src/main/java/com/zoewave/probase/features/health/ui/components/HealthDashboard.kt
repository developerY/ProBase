package com.zoewave.probase.features.health.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState

// 1. Define the Tabs
private enum class HealthTab(val label: String, val icon: ImageVector) {
    Steps("Steps", Icons.Default.DateRange),   // Icon for Steps
    Calories("Energy", Icons.Default.Star),    // Icon for Calories
    Distance("Distance", Icons.Default.Place)  // Icon for Distance
}

@Composable
fun HealthDashboard(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // 2. Local State for the selected tab
    var currentTab by remember { mutableStateOf(HealthTab.Steps) }

    // 3. Use Scaffold for the Bottom Bar structure
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

        // 4. Content Area
        Column(
            modifier = Modifier
                .padding(innerPadding) // Respect the bottom bar height
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Common Header (Always Visible) ---
            HealthConnectionStatus(
                onEvent = onEvent,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- Action Button (Always Visible) ---
            Button(
                onClick = { onEvent(HealthEvent.WriteTestRide) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Test City Ride (4.5km)")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Tab Specific Content ---
            Text(
                text = "${currentTab.label} (Last 7 Days)",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(vertical = 8.dp)
            )

            // Switch content based on the selected tab
            when (currentTab) {
                HealthTab.Steps -> {
                    GenericWeeklyChart(
                        title = "Steps",
                        data = state.weeklySteps.mapValues { it.value.toDouble() },
                        color = MaterialTheme.colorScheme.primary,
                        formatValue = { v -> if (v > 999) "${(v / 1000).toInt()}k" else "${v.toInt()}" }
                    )
                }
                HealthTab.Calories -> {
                    GenericWeeklyChart(
                        title = "Calories (kcal)",
                        data = state.weeklyCalories,
                        color = Color(0xFFFF9800),
                        formatValue = { v -> "${v.toInt()}" }
                    )
                }
                HealthTab.Distance -> {
                    GenericWeeklyChart(
                        title = "Distance (km)",
                        data = state.weeklyDistance,
                        color = Color(0xFF03A9F4),
                        formatValue = { v -> String.format("%.1f", v / 1000) }
                    )
                }
            }
        }
    }
}