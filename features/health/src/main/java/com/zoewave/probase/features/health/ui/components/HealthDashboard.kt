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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState

@Composable
fun HealthDashboard(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Re-use Status Component ---
        HealthConnectionStatus(
            onEvent = onEvent,
            modifier = Modifier.padding(top = 16.dp)
        )

        // --- 2. Action Buttons ---
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

        // --- 2. Dashboard Charts ---
        Text(
            text = "Last 7 Days Activity",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(vertical = 8.dp)
        )

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

        Spacer(modifier = Modifier.height(32.dp))
    }
}