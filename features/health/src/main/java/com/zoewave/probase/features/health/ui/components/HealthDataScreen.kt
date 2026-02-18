package com.zoewave.probase.features.health.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState

@Composable
fun HealthDataScreen(
    state: HealthUiState.Success, // ✅ ONLY State
    onEvent: (HealthEvent) -> Unit // ✅ ONLY Event Handler
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Access Granted",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF1B5E20)
            )
        }

        Text(
            text = "Last 7 Days Activity",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // --- Charts ---
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

        // --- Actions ---

        // 1. Write Test Data (Triggered via Event)
        Button(
            onClick = { onEvent(HealthEvent.WriteTestRide) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Test City Ride (4.5km)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Manage Permissions (Triggered via Event)
        OutlinedButton(
            onClick = { onEvent(HealthEvent.ManagePermissions) },
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text("Manage Permissions")
        }
    }
}