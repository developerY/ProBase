package com.zoewave.probase.features.health.ui.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.health.ui.HealthUiState
import com.zoewave.probase.features.health.ui.components.charts.GenericWeeklyChart

@Composable
fun OverviewTab(
    state: HealthUiState.Success,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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