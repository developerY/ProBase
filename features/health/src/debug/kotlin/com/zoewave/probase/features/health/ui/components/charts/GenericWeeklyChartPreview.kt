package com.zoewave.probase.features.health.ui.components.charts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GenericWeeklyChartPreview() {
    // 1. Generate Mock Dates (Last 7 Days)
    val today = LocalDate.now()
    val mockSteps = mutableMapOf<String, Double>()
    val mockDistance = mutableMapOf<String, Double>()
    val emptyData = mutableMapOf<String, Double>()

    for (i in 6 downTo 0) {
        val dateStr = today.minusDays(i.toLong()).toString()

        // Mock Steps: Random between 2000 and 12000
        mockSteps[dateStr] = (2000..12000).random().toDouble()

        // Mock Distance: Random between 1.5km and 15.0km
        mockDistance[dateStr] = (1500..15000).random().toDouble()

        // Empty Data
        emptyData[dateStr] = 0.0
    }

    // Add a specific spike to Steps to test the relative scaling
    val spikeDate = today.minusDays(2).toString()
    mockSteps[spikeDate] = 18500.0

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- 1. Steps Chart (Large Numbers / Blue) ---
                GenericWeeklyChart(
                    title = "Steps",
                    data = mockSteps,
                    color = Color(0xFF03A9F4), // Light Blue
                    formatValue = { v ->
                        if (v > 999) String.format("%.1fk", v / 1000) else "${v.toInt()}"
                    }
                )

                // --- 2. Distance Chart (Decimals / Orange) ---
                GenericWeeklyChart(
                    title = "Distance (km)",
                    data = mockDistance,
                    color = Color(0xFFFF9800), // Orange
                    formatValue = { v ->
                        String.format("%.1f", v / 1000) // Assuming input is meters, convert to km
                    }
                )

                // --- 3. Empty Chart State ---
                GenericWeeklyChart(
                    title = "Calories (kcal) - Empty",
                    data = emptyData,
                    color = Color(0xFFE91E63), // Pink
                    formatValue = { v -> "${v.toInt()}" }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}