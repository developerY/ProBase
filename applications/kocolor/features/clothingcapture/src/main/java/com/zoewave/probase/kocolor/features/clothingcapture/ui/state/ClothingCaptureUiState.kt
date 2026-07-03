package com.zoewave.probase.kocolor.features.clothingcapture.ui.state

import com.zoewave.probase.core.model.ritual.ClothingItem

sealed interface ClothingCaptureUiState {
    data class Idle(
        val capturedUris: List<String> = emptyList(),
        val currentStep: ClothingCaptureStep = ClothingCaptureStep.FRONT,
        val extractedColorHex: String? = null,
        val manualPrice: Double? = null
    ) : ClothingCaptureUiState

    data class Analyzing(
        val capturedUris: List<String>,
        val progress: String = "Initializing AI..."
    ) : ClothingCaptureUiState

    data class ColorConfirmation(
        val capturedUris: List<String>,
        val suggestedColors: List<String>,
        val selectedColorHex: String
    ) : ClothingCaptureUiState

    data class PriceConfirmation(
        val capturedUris: List<String>,
        val detectedPrice: Double
    ) : ClothingCaptureUiState

    data class Review(
        val capturedUris: List<String>,
        val labelsOcr: String = "",
        val manualColorHex: String? = null,
        val price: Double? = null
    ) : ClothingCaptureUiState

    data class FinalReview(
        val item: ClothingItem
    ) : ClothingCaptureUiState

    data class Success(
        val item: ClothingItem
    ) : ClothingCaptureUiState

    data class AiAnalyzing(
        val capturedUris: List<String>,
        val progress: String = "Identifying with Gemini..."
    ) : ClothingCaptureUiState

    data class Error(
        val message: String
    ) : ClothingCaptureUiState
}

enum class ClothingCaptureStep(
    val label: String,
    val isSkippable: Boolean = false
) {
    FRONT("Front View"),
    BACK("Back View"),
    LABEL("Label / Care Side", isSkippable = true),
    PRICE("Scan Price Tag", isSkippable = true),
    COLOR("Product Color", isSkippable = true);

    companion object {
        val ALL = entries
    }
}
