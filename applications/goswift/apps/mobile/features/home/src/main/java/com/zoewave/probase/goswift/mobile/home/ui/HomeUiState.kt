package com.zoewave.probase.goswift.mobile.home.ui

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val currentCaffeineMg: Int,
        val nextDoseRecommendation: String,
        val sleepQualityImpact: String,
        val sleepDuration: String = "Unknown",
        val exerciseMinutes: Int = 0
    ) : HomeUiState
}

sealed interface HomeUiEvent {
    object Refresh : HomeUiEvent
}
