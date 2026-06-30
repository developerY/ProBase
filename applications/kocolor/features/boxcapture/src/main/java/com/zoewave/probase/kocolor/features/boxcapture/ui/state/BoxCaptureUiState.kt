package com.zoewave.probase.kocolor.features.boxcapture.ui.state

import com.zoewave.probase.core.model.ritual.CosmeticItem

sealed interface BoxCaptureUiState {
    data class Idle(
        val capturedUris: List<String> = emptyList(),
        val currentStep: CaptureStep = CaptureStep.FRONT,
        val mode: CaptureMode = CaptureMode.BOX,
        val extractedColorHex: String? = null
    ) : BoxCaptureUiState

    data class Analyzing(
        val capturedUris: List<String>,
        val progress: String = "Initializing AI..."
    ) : BoxCaptureUiState

    data class ColorConfirmation(
        val capturedUris: List<String>,
        val suggestedColorHex: String,
        val mode: CaptureMode
    ) : BoxCaptureUiState

    data class Review(
        val capturedUris: List<String>,
        val barcode: String?,
        val ingredientsOcr: String = "",
        val instructionsOcr: String = "",
        val mode: CaptureMode,
        val enrichmentData: CosmeticItem? = null,
        val manualColorHex: String? = null
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

enum class CaptureStep(
    val label: String,
    val isSkippable: Boolean = false
) {
    FRONT("Front View"),
    BACK("Back / Info Side"),
    INGREDIENTS("Ingredients List"),
    INSTRUCTIONS("Instructions / Info", isSkippable = true),
    COLOR("Product Color", isSkippable = true),
    BARCODE("Barcode Scan");

    companion object {
        fun getStepsForMode(mode: CaptureMode): List<CaptureStep> {
            return when (mode) {
                CaptureMode.BOX -> listOf(FRONT, BACK, INGREDIENTS, INSTRUCTIONS, COLOR, BARCODE)
                CaptureMode.PRODUCT -> listOf(FRONT, BACK, COLOR, BARCODE)
            }
        }
    }
}
