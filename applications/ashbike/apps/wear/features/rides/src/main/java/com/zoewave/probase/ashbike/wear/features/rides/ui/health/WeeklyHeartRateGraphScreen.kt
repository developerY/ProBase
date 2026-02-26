package com.zoewave.probase.ashbike.wear.features.rides.ui.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale



// ==========================================
// 2. The Graph Screen
// ==========================================
@Composable
fun WeeklyHeartRateGraphScreen(
    weeklyData: List<DailyHeartRate>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Heart Rate",
                style = MaterialTheme.typography.caption1,
                color = Color(0xFFE53935) // Material Red
            )
            Spacer(modifier = Modifier.height(16.dp))

            // The Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up the remaining vertical space
                    .padding(horizontal = 8.dp)
            ) {
                // Background reference lines (Optional, for 100 & 150 bpm)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y150 = size.height - ((150f - 40f) / (200f - 40f) * size.height)
                    val y100 = size.height - ((100f - 40f) / (200f - 40f) * size.height)

                    drawLine(
                        color = Color.DarkGray.copy(alpha = 0.5f),
                        start = Offset(0f, y150),
                        end = Offset(size.width, y150),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.DarkGray.copy(alpha = 0.5f),
                        start = Offset(0f, y100),
                        end = Offset(size.width, y100),
                        strokeWidth = 2f
                    )
                }

                // The Actual Bars
                HrBarChartCanvas(data = weeklyData)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // X-Axis Labels (M, T, W, T, F, S, S)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEach { day ->
                    Text(
                        // Extracts just the first letter (e.g., "M" for Monday)
                        text = day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.caption3,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 3. The Canvas Drawing Logic
// ==========================================
@Composable
fun HrBarChartCanvas(data: List<DailyHeartRate>, modifier: Modifier = Modifier.fillMaxSize()) {
    // Define the boundaries of our graph
    val maxGraphHr = 200f // The ceiling of the graph
    val minGraphHr = 40f  // The floor (resting HR baseline)

    Canvas(modifier = modifier) {
        val barWidth = 8.dp.toPx()
        // Divide the width evenly among the 7 days
        val stepX = size.width / 7f

        data.forEachIndexed { index, day ->
            // Center the bar within its allocated slot
            val xOffset = (index * stepX) + (stepX / 2f)

            // Normalize the HR values to fit inside the Canvas height (0.0 to 1.0)
            val heightRange = maxGraphHr - minGraphHr
            val maxHrRatio = ((day.maxHr - minGraphHr) / heightRange).coerceIn(0f, 1f)
            val avgHrRatio = ((day.avgHr - minGraphHr) / heightRange).coerceIn(0f, 1f)

            // Calculate the actual Y pixel coordinates (0 is top, size.height is bottom)
            val bottomY = size.height
            val maxHrY = size.height - (maxHrRatio * size.height)
            val avgHrY = size.height - (avgHrRatio * size.height)

            // 1. Draw Max HR (Background Red Bar)
            drawLine(
                color = Color(0xFFE53935).copy(alpha = 0.4f),
                start = Offset(xOffset, bottomY),
                end = Offset(xOffset, maxHrY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )

            // 2. Draw Avg HR (Foreground White Bar)
            drawLine(
                color = Color.White,
                start = Offset(xOffset, bottomY),
                end = Offset(xOffset, avgHrY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

// ==========================================
// 4. Preview
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Weekly HR Canvas Graph"
)
@Composable
fun WeeklyHeartRateGraphScreenPreview() {
    val dummyData = listOf(
        DailyHeartRate(DayOfWeek.MONDAY, 135, 165),
        DailyHeartRate(DayOfWeek.TUESDAY, 142, 172),
        DailyHeartRate(DayOfWeek.WEDNESDAY, 128, 155),
        DailyHeartRate(DayOfWeek.THURSDAY, 130, 158),
        DailyHeartRate(DayOfWeek.FRIDAY, 145, 178),
        DailyHeartRate(DayOfWeek.SATURDAY, 150, 182),
        DailyHeartRate(DayOfWeek.SUNDAY, 60, 95) // Rest day
    )

    MaterialTheme {
        WeeklyHeartRateGraphScreen(weeklyData = dummyData)
    }
}