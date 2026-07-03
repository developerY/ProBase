package com.zoewave.probase.kocolor.features.boxcapture.ui.state

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem

sealed interface BoxCaptureUiState {
    data class Idle(
        val capturedUris: List<String> = emptyList(),
        val currentStep: CaptureStep = CaptureStep.FRONT,
        val mode: CaptureMode = CaptureMode.BOX,
        val extractedColorHex: String? = null,
        val manualPrice: Double? = null
    ) : BoxCaptureUiState

    data class Analyzing(
        val capturedUris: List<String>,
        val progress: String = "Initializing AI...",
        val mode: CaptureMode = CaptureMode.BOX
    ) : BoxCaptureUiState

    data class ColorConfirmation(
        val capturedUris: List<String>,
        val suggestedColors: List<String>,
        val selectedColorHex: String,
        val mode: CaptureMode
    ) : BoxCaptureUiState

    data class PriceConfirmation(
        val capturedUris: List<String>,
        val detectedPrice: Double,
        val mode: CaptureMode
    ) : BoxCaptureUiState

    data class Review(
        val capturedUris: List<String>,
        val barcode: String?,
        val ingredientsOcr: String = "",
        val instructionsOcr: String = "",
        val mode: CaptureMode,
        val enrichmentData: CosmeticItem? = null,
        val manualColorHex: String? = null,
        val price: Double? = null
    ) : BoxCaptureUiState

    data class FinalReview(
        val item: CosmeticItem
    ) : BoxCaptureUiState

    data class Success(
        val item: CosmeticItem
    ) : BoxCaptureUiState

    data class AiAnalyzing(
        val capturedUris: List<String>,
        val progress: String = "Synthesizing with Gemini..."
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
    PRICE("Scan Price Tag", isSkippable = true),
    COLOR("Product Color", isSkippable = true),
    BARCODE("Barcode Scan");

    companion object {
        fun getStepsForMode(mode: CaptureMode): List<CaptureStep> {
            return when (mode) {
                CaptureMode.BOX -> listOf(FRONT, BACK, INGREDIENTS, INSTRUCTIONS, PRICE, COLOR, BARCODE)
                CaptureMode.PRODUCT -> listOf(FRONT, BACK, PRICE, COLOR, BARCODE)
            }
        }
    }
}
