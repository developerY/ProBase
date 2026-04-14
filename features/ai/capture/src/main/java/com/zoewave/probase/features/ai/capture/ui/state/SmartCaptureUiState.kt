package com.zoewave.probase.features.ai.capture.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.core.model.tasks.SmartTaskDraft

@Immutable
sealed interface SmartCaptureUiState {
    object Idle : SmartCaptureUiState
    
    data class Loading(
        val logs: List<String> = emptyList(),
        val isUsingCloud: Boolean = false,
        val networkSpeed: String? = null // Optional: e.g. "LTE", "WiFi"
    ) : SmartCaptureUiState
    
    data class Success(
        val draft: SmartTaskDraft,
        val engineUsed: String,
        val diagnostics: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    ) : SmartCaptureUiState
    
    data class Error(
        val message: String,
        val diagnostics: List<String> = emptyList()
    ) : SmartCaptureUiState
}
