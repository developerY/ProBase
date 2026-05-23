package com.zoewave.probase.seaweed.features.affordability.ui

import androidx.compose.runtime.Immutable
import com.zoewave.probase.core.model.tasks.SmartTaskDraft

@Immutable
sealed interface AffordabilityUiState {
    data class Idle(
        val capturedUri: String? = null,
        val userComment: String = ""
    ) : AffordabilityUiState
    
    data class Loading(
        val message: String = "Analyzing...",
        val logs: List<String> = emptyList()
    ) : AffordabilityUiState
    
    data class Success(
        val draft: SmartTaskDraft,
        val affordabilityAdvice: String,
        val engineUsed: String,
        val diagnostics: List<String> = emptyList()
    ) : AffordabilityUiState
    
    data class Error(
        val message: String,
        val diagnostics: List<String> = emptyList()
    ) : AffordabilityUiState
}
