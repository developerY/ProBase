package com.zoewave.probase.features.smartcapture.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.features.smartcapture.domain.TaskDraftState

@Immutable
sealed interface SmartCaptureUiState {
    object Idle : SmartCaptureUiState
    object Loading : SmartCaptureUiState
    data class Success(val draft: TaskDraftState) : SmartCaptureUiState
    data class Error(val message: String) : SmartCaptureUiState
}
