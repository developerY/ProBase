package com.zoewave.probase.goswift.wear.hydration.ui

sealed interface HydrationUiState {
    object Loading : HydrationUiState
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
    object Refresh : HydrationUiEvent
}
