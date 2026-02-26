package com.zoewave.probase.ashbike.wear.features.rides.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

// ==========================================
// 2. The Main Screen Composable
// ==========================================
@Composable
fun WeeklyHeartRateScreen(
    weeklyData: List<DailyHeartRate>,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        // Top Header
        item {
            ListHeader {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Rate",
                        tint = Color(0xFFE53935), // Material Red
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "7-Day Heart Rate", style = MaterialTheme.typography.title3)
                }
            }
        }

        // Loop through the 7 days of data
        items(weeklyData) { dayData ->
            HeartRateDayRow(data = dayData)
        }
    }
}

// ==========================================
// 3. The Individual Day Card
// ==========================================
@Composable
fun HeartRateDayRow(data: DailyHeartRate) {
    Card(
        onClick = { /* In the future, tap to see hourly breakdown */ },
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color(0xFF1E1E1E), // Deep dark gray
            endBackgroundColor = Color(0xFF1E1E1E)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Day Label (e.g., "Mon", "Tue")
            Text(
                text = data.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.title3,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Right Side: Heart Rate Stats
            Column(horizontalAlignment = Alignment.End) {
                // Average HR
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "Avg: ", style = MaterialTheme.typography.caption2, color = Color.Gray)
                    Text(text = "${data.avgHr}", style = MaterialTheme.typography.body1, color = Color.White)
                }

                // Max HR
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "Max: ", style = MaterialTheme.typography.caption2, color = Color.Gray)
                    // Highlighting the max in red for visual pop
                    Text(text = "${data.maxHr}", style = MaterialTheme.typography.caption1, color = Color(0xFFE53935))
                }
            }
        }
    }
}

// ==========================================
// 4. The Preview
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Weekly HR Data"
)
@Composable
fun WeeklyHeartRateScreenPreview() {
    // Generate some realistic-looking dummy data for the preview
    val dummyData = listOf(
        DailyHeartRate(DayOfWeek.MONDAY, 135, 165),
        DailyHeartRate(DayOfWeek.TUESDAY, 142, 172),
        DailyHeartRate(DayOfWeek.WEDNESDAY, 128, 155),
        DailyHeartRate(DayOfWeek.THURSDAY, 130, 158),
        DailyHeartRate(DayOfWeek.FRIDAY, 145, 178),
        DailyHeartRate(DayOfWeek.SATURDAY, 150, 182),
        DailyHeartRate(DayOfWeek.SUNDAY, 115, 140) // Rest day!
    )

    MaterialTheme {
        WeeklyHeartRateScreen(weeklyData = dummyData)
    }
}