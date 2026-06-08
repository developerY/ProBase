package com.zoewave.probase.features.health.nutrition.ui

import com.zoewave.probase.features.health.nutrition.data.NutritionRoutine

sealed interface NutritionUiState {
    data object Loading : NutritionUiState
    data class Success(
        val routine: NutritionRoutine,
        val nextMetabolicWindow: String? = null
    ) : NutritionUiState
    data class Error(val message: String) : NutritionUiState
}

sealed interface NutritionUiEvent {
    data class ToggleStage(val stageId: String) : NutritionUiEvent
    data object Refresh : NutritionUiEvent
}
