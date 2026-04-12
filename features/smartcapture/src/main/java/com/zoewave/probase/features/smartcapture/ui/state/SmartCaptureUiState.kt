package com.zoewave.probase.features.smartcapture.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.core.model.tasks.SmartTaskDraft

@Immutable
sealed interface SmartCaptureUiState {
    object Idle : SmartCaptureUiState
    object Loading : SmartCaptureUiState
    data class Success(val draft: SmartTaskDraft) : SmartCaptureUiState
    data class Error(val message: String) : SmartCaptureUiState
}
