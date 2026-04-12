package com.zoewave.probase.features.smartcapture.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.features.smartcapture.domain.SmartTask

@Immutable
data class SmartCaptureUiState(
    val isProcessing: Boolean = false,
    val capturedTask: SmartTask? = null,
    val errorMessage: String? = null
)
