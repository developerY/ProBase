package com.zoewave.probase.kocolor.features.boxcapture.ui.state

import com.zoewave.probase.core.model.ritual.CosmeticItem

sealed interface BoxCaptureUiState {
    data class Idle(
        val capturedUris: List<String> = emptyList(),
        val currentStep: CaptureStep = CaptureStep.FRONT,
        val mode: CaptureMode = CaptureMode.BOX
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

enum class CaptureMode {
    BOX, PRODUCT
}

enum class CaptureStep(val boxLabel: String, val productLabel: String = boxLabel) {
    FRONT("Front Side", "Front of Product"),
    BACK("Back Side", "Back of Product"),
    LEFT("Left Side"),
    RIGHT("Right Side"),
    TOP("Top Side"),
    BOTTOM("Bottom Side"),
    INGREDIENTS("Ingredients List");

    fun getLabel(mode: CaptureMode): String {
        return if (mode == CaptureMode.PRODUCT) productLabel else boxLabel
    }

    companion object {
        fun getStepsForMode(mode: CaptureMode): List<CaptureStep> {
            return when (mode) {
                CaptureMode.BOX -> entries
                CaptureMode.PRODUCT -> listOf(FRONT, BACK)
            }
        }
    }
}
