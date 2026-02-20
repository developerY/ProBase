package com.zoewave.probase.features.health.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// 1. Define a data class for the segments
data class ChartSegment(
    val value: Double,
    val color: Color,
    val label: String // e.g., "Biking", "Walking"
)

@Composable
fun StackedWeeklyChart(
    title: String,
    data: Map<String, List<ChartSegment>>, // Date -> List of activities
    formatValue: (Double) -> String
) {
    // Find the maximum total value for a single day to scale the chart
    val maxDailyTotal = data.values.maxOfOrNull { daySegments ->
        daySegments.sumOf { it.value }
    } ?: 1.0

    val sortedData = data.toSortedMap()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (maxDailyTotal == 0.0) {
                Text("No data", style = MaterialTheme.typography.bodySmall)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    sortedData.forEach { (dateStr, segments) ->
                        val dayLabel = try {
                            LocalDate.parse(dateStr).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        } catch (e: Exception) {
                            dateStr.takeLast(2)
                        }

                        val dailyTotal = segments.sumOf { it.value }
                        val totalBarHeightFraction = (dailyTotal / maxDailyTotal).coerceAtLeast(0.02)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Total value label on top
                            Text(
                                text = formatValue(dailyTotal),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // The Stacked Bar
                            Column(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(totalBarHeightFraction.toFloat())
                                    .clip(RoundedCornerShape(4.dp)),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Draw each segment based on its percentage of the day's total
                                segments.forEach { segment ->
                                    if (segment.value > 0) {
                                        val segmentWeight = (segment.value / dailyTotal).toFloat()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(segmentWeight)
                                                .background(segment.color)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}