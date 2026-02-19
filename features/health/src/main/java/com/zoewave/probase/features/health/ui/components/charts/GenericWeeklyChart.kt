package com.zoewave.probase.features.health.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

/**
 * Reusable Chart Component
 */
@Composable
fun GenericWeeklyChart(
    title: String,
    data: Map<String, Double>,
    color: Color,
    formatValue: (Double) -> String
) {
    val maxVal = data.values.maxOrNull() ?: 1.0
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

            if (data.values.sum() == 0.0) {
                Text("No data", style = MaterialTheme.typography.bodySmall)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    sortedData.forEach { (dateStr, value) ->
                        val dayLabel = try {
                            LocalDate.parse(dateStr).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        } catch (e: Exception) {
                            dateStr.takeLast(2)
                        }

                        val barHeightFraction = (value.toFloat() / maxVal.toFloat()).coerceAtLeast(0.02f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = formatValue(value),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(barHeightFraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
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