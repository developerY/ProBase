package com.zoewave.probase.features.weather.ui.sun

import com.zoewave.probase.core.model.weather.EnvironmentalContext

sealed interface SunIntelligenceUiState {
    data object Loading : SunIntelligenceUiState
    data class Success(
        val context: EnvironmentalContext?,
        val reapplicationTimeRemaining: Long = 0, // in milliseconds
        val isTimerActive: Boolean = false
    ) : SunIntelligenceUiState
    data class Error(val message: String) : SunIntelligenceUiState
}

sealed interface SunIntelligenceEvent {
    data object Refresh : SunIntelligenceEvent
    data object ResetTimer : SunIntelligenceEvent
    data class ToggleTimer(val active: Boolean) : SunIntelligenceEvent
}
