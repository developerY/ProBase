package com.zoewave.probase.goswift.mobile.home.ui

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val currentCaffeineMg: Int,
        val nextDoseRecommendation: String,
        val sleepQualityImpact: String,
        val sleepDuration: String = "Unknown",
        val exerciseMinutes: Int = 0,
        val hydrationProgress: Double = 0.0
    ) : HomeUiState
}

sealed interface HomeUiEvent {
    object Refresh : HomeUiEvent
}
