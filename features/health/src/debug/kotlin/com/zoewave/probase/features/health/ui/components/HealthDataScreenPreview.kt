package com.zoewave.probase.features.health.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.features.health.ui.HealthUiState
import java.time.LocalDate

@Preview(showBackground = true, name = "Health Data Light", heightDp = 1000)
@Preview(showBackground = true, name = "Health Data Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HealthDataScreenPreview() {
    // 1. Generate mock data for the last 7 days
    val today = LocalDate.now()
    val mockSteps = mutableMapOf<String, Long>()
    val mockDistance = mutableMapOf<String, Double>() // Meters
    val mockCalories = mutableMapOf<String, Double>() // Kcal

    for (i in 6 downTo 0) {
        val date = today.minusDays(i.toLong()).toString()
        // Random-ish realistic data
        mockSteps[date] = (3000..12000).random().toLong()
        mockDistance[date] = (2000..10000).random().toDouble()
        mockCalories[date] = (1800..3200).random().toDouble()
    }

    // Apply specific "spike" values to verify chart scaling
    val spikeDate = today.minusDays(2).toString()
    mockSteps[spikeDate] = 15000L
    mockDistance[spikeDate] = 12500.0

    // 2. Create the Mock State
    val mockState = HealthUiState.Success(
        sessions = emptyList(), // Can be empty for this preview
        weeklySteps = mockSteps,
        weeklyDistance = mockDistance,
        weeklyCalories = mockCalories
    )

    MaterialTheme {
        Surface {
            HealthDataScreen(
                state = mockState,
                onEvent = {} // No-op for preview
            )
        }
    }
}