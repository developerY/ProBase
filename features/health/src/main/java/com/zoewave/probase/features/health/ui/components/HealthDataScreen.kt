package com.zoewave.probase.features.health.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HealthDataScreen(
    weeklySteps: Map<String, Long>,
    weeklyDistance: Map<String, Double>,
    weeklyCalories: Map<String, Double>,
    onManagePermissionsClick: () -> Unit // <--- 1. Add Lambda
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // Make screen scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Header
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

        // 1. Steps Chart
        GenericWeeklyChart(
            title = "Steps",
            data = weeklySteps.mapValues { it.value.toDouble() },
            color = MaterialTheme.colorScheme.primary,
            formatValue = { v -> if (v > 999) "${(v / 1000).toInt()}k" else "${v.toInt()}" }
        )

        // 2. Calories Chart
        GenericWeeklyChart(
            title = "Calories (kcal)",
            data = weeklyCalories,
            color = Color(0xFFFF9800), // Orange
            formatValue = { v -> "${v.toInt()}" }
        )

        // 3. Distance Chart
        GenericWeeklyChart(
            title = "Distance (km)",
            data = weeklyDistance,
            color = Color(0xFF03A9F4), // Blue
            formatValue = { v -> String.format("%.1f", v / 1000) } // Convert meters to km
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onManagePermissionsClick,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text("Manage Permissions")
        }
    }
}