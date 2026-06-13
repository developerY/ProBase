package com.zoewave.probase.kocolor.features.boxcapture.ui.state

import com.zoewave.probase.kocolor.model.CosmeticItem

sealed interface BoxCaptureUiState {
    data class Idle(
        val capturedUris: List<String> = emptyList(),
        val currentStep: CaptureStep = CaptureStep.FRONT
    ) : BoxCaptureUiState

    data class Analyzing(
        val capturedUris: List<String>,
        val progress: String = "Initializing AI..."
    ) : BoxCaptureUiState

    data class Success(
        val item: CosmeticItem
    ) : BoxCaptureUiState

    data class Error(
        val message: String
    ) : BoxCaptureUiState
}

enum class CaptureStep(val label: String) {
    FRONT("Front Side"),
    BACK("Back Side"),
    LEFT("Left Side"),
    RIGHT("Right Side"),
    TOP("Top Side"),
    BOTTOM("Bottom Side"),
    INGREDIENTS("Ingredients List")
}
