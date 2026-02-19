package com.zoewave.probase.features.health.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ExerciseSessionRowPreview() {
    // 1. Setup mock data
    val mockStartTime = ZonedDateTime.now(ZoneId.systemDefault()).minusMinutes(45)
    val mockEndTime = ZonedDateTime.now(ZoneId.systemDefault())
    val mockUid = UUID.randomUUID().toString()

    // 2. Wrap in MaterialTheme and Surface to see proper colors and elevation drop-shadow
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ExerciseSessionRow(
                startTime = mockStartTime,
                endTime = mockEndTime,
                uid = mockUid,
                title = "Test City Ride \uD83D\uDEB4",
                onDetailsClick = { /* No-op for preview */ }
            )
        }
    }
}