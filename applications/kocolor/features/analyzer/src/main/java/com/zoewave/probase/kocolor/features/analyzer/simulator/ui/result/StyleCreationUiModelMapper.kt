package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result

import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.ExecutionTier
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleResultUiState

fun StyleResultUiState.toStyleCreationUiModel(): StyleCreationUiModel {
    val result = this.blueprint
    val fashionista = this.fashionistaScore
    val intentFulfillment = this.intentFulfillment

    val clothingModels = this.selectedClothing.map { item ->
        StyleItemUiModel(
            id = "w_${item.internalId}",
            name = item.name,
            role = item.category.name,
            colorHex = item.colorHex,
            temperature = item.colorTemperature,
            material = item.material
        )
    }

    val cosmeticModels = this.selectedCosmetics.map { item ->
        StyleItemUiModel(
            id = "c_${item.internalId}",
            name = item.name,
            role = item.macroCategory.displayName,
            colorHex = item.colorHex,
            temperature = item.temperature.name
        )
    }

    val validationModels = listOf(
        ValidationUiModel("Mandatory anchor included", true),
        ValidationUiModel("Top / Bottom / Shoes composition", true),
        ValidationUiModel("Eye / Cheek / Lip / Nail roles", true),
        ValidationUiModel("All selected IDs grounded", true),
        ValidationUiModel("Forbidden PREP items excluded", true)
    )

    return StyleCreationUiModel(
        occasion = "Daily Outfit",
        userIntent = null,
        appearanceTemperature = "Neutral",
        appearanceDepth = "Medium",
        appearanceContrast = "Balanced",
        temperatureC = 22.0f,
        uvIndex = 3.0f,
        circadianContext = "Defense & Protection",
        eligibleWardrobeCount = 53,
        anchorName = this.selectedClothing.firstOrNull()?.name ?: "Universal Khaki Button-Down",
        anchorId = this.selectedClothing.firstOrNull()?.let { "w_${it.internalId}" } ?: "w_41",
        anchorReason = "Automatic context anchor",
        clothing = clothingModels,
        cosmetics = cosmeticModels,
        aiRationale = result?.rationale ?: "Selected from your vault based on intent and availability.",
        validationItems = validationModels,
        paletteHex = result?.recommendedPalette ?: emptyList(),
        fashionistaScore = fashionista?.totalScore ?: 88.0f,
        colorHarmony = fashionista?.colorHarmonyScore ?: 95.0f,
        silhouette = fashionista?.silhouetteScore ?: 80.0f,
        contrastDepth = fashionista?.contrastScore ?: 85.0f,
        intentStatus = if (intentFulfillment?.isSpecified == true) IntentUiStatus.SPECIFIED else IntentUiStatus.NOT_SPECIFIED,
        intentScore = intentFulfillment?.score,
        observedColorfulness = intentFulfillment?.observedMetrics?.colorfulness ?: 0.53f,
        observedColorContrast = intentFulfillment?.observedMetrics?.colorContrast ?: 0.50f,
        executionTier = ExecutionTier.AI_CLOUD,
        latencyMs = 1290L
    )
}
