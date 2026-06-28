package com.zoewave.probase.features.health.nutrition.ui.ritual

import com.zoewave.probase.core.model.ritual.BeautyRoutine

sealed interface NutritionUiState {
    data object Loading : NutritionUiState
    data class Success(
        val routine: BeautyRoutine,
        val nextMetabolicWindow: String? = null
    ) : NutritionUiState
    data class Error(val message: String) : NutritionUiState
}

sealed interface NutritionUiEvent {
    data class ToggleStage(val stageId: String) : NutritionUiEvent
    data object Refresh : NutritionUiEvent
}
