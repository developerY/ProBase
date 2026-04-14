package com.zoewave.probase.photodo.features.smartadvice.ui

import com.zoewave.probase.photodo.features.smartadvice.domain.ProjectAdvice

sealed interface SmartAdviceUiState {
    data object Idle : SmartAdviceUiState
    data object Loading : SmartAdviceUiState
    data class Success(val advice: ProjectAdvice) : SmartAdviceUiState
    data class Error(val message: String) : SmartAdviceUiState
}
