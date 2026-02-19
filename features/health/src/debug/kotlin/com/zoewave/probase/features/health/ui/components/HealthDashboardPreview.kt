package com.zoewave.probase.features.health.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.features.health.ui.HealthUiState
import java.time.LocalDate

@Preview(name = "Light Mode", showBackground = true, heightDp = 800)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 800)
@Composable
fun HealthDashboardPreview() {
    // 1. Generate Mock Data (Last 7 Days)
    val today = LocalDate.now()
    val mockSteps = mutableMapOf<String, Long>()
    val mockDistance = mutableMapOf<String, Double>()
    val mockCalories = mutableMapOf<String, Double>()

    for (i in 6 downTo 0) {
        val date = today.minusDays(i.toLong()).toString()
        mockSteps[date] = (3000..12000).random().toLong()
        mockDistance[date] = (2000..10000).random().toDouble()
        mockCalories[date] = (1500..3000).random().toDouble()
    }

    // Add a "spike" to make the chart interesting
    val spikeDate = today.minusDays(2).toString()
    mockSteps[spikeDate] = 15000L

    // 2. Create Mock State
    val mockState = HealthUiState.Success(
        sessions = emptyList(),
        weeklySteps = mockSteps,
        weeklyDistance = mockDistance,
        weeklyCalories = mockCalories
    )

    // 3. Render
    MaterialTheme {
        Surface {
            HealthDashboard(
                state = mockState,
                onEvent = {}, // No-op for preview
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}