package com.zoewave.probase.features.health.ui.components.charts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate

@Preview(showBackground = true)
@Composable
fun StackedWeeklyChartPreview() {
    val today = LocalDate.now()
    val mockData = mutableMapOf<String, List<ChartSegment>>()

    // Define standard colors for activities
    val colorBiking = Color(0xFF4CAF50) // Green
    val colorWalking = Color(0xFF03A9F4) // Blue
    val colorOther = Color(0xFF9E9E9E)   // Gray

    for (i in 6 downTo 0) {
        val dateStr = today.minusDays(i.toLong()).toString()

        // Create mock segments for the day
        val segments = mutableListOf<ChartSegment>()

        // Randomly add walking distance (e.g., normal daily movement)
        segments.add(ChartSegment((1000..3000).random().toDouble(), colorWalking, "Walking"))

        // Randomly add biking distance to some days
        if (i % 2 == 0) {
            segments.add(ChartSegment((4000..10000).random().toDouble(), colorBiking, "Biking"))
        }

        // Add some random unclassified distance
        segments.add(ChartSegment((500..1500).random().toDouble(), colorOther, "Other"))

        mockData[dateStr] = segments
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            StackedWeeklyChart(
                title = "Distance by Activity (km)",
                data = mockData,
                formatValue = { v -> String.format("%.1f", v / 1000) }
            )
        }
    }
}