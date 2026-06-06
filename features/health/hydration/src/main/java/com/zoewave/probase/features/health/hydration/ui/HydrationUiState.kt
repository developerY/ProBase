package com.zoewave.probase.features.health.hydration.ui

import androidx.health.connect.client.records.Record

sealed interface HydrationUiState {
    data object Loading : HydrationUiState
    data class Success(
        val dailyTotalLiters: Double,
        val targetLiters: Double,
        val recentLogs: List<HydrationLog>
    ) : HydrationUiState
}

data class HydrationLog(
    val id: String,
    val amountLiters: Double,
    val timestamp: Long
)

sealed interface HydrationUiEvent {
    data class AddWater(val liters: Double) : HydrationUiEvent
    data object Refresh : HydrationUiEvent
}
