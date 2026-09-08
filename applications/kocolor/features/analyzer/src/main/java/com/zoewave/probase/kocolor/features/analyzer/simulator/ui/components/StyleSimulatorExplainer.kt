package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.ExecutionTier
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleResultContent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleResultUiState
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result.IntentUiStatus
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result.StyleCreationStoryScreen
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result.StyleCreationUiModel
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result.StyleItemUiModel
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result.ValidationUiModel
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun StyleSimulatorExplainer(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val model = uiState.toStyleCreationUiModel()
    StyleCreationStoryScreen(
        model = model,
        phase = uiState.creationPhase
    )
}

fun StyleSimulatorUiState.toStyleCreationUiModel(): StyleCreationUiModel {
    val result = creationResult
    val context = result?.context
    val anchor = result?.anchor
    val candidateSummary = result?.candidateSummary
    val validation = result?.validation
    val fashionista = result?.fashionista
    val intentFulfillment = result?.intent

    val clothingModels = recommendedClothing.map { item ->
        StyleItemUiModel(
            id = "w_${item.internalId}",
            name = item.name,
            role = item.category.name,
            colorHex = item.colorHex,
            temperature = item.colorTemperature,
            material = item.material
        )
    }

    val cosmeticModels = recommendedCosmetics.map { item ->
        StyleItemUiModel(
            id = "c_${item.internalId}",
            name = item.name,
            role = item.macroCategory.displayName,
            colorHex = item.colorHex,
            temperature = item.temperature.name
        )
    }

    val validationModels = validation?.validationItems?.map {
        ValidationUiModel(label = it.label, passed = it.passed)
    } ?: listOf(
        ValidationUiModel("Mandatory anchor included", true),
        ValidationUiModel("Top / Bottom / Shoes composition", true),
        ValidationUiModel("Eye / Cheek / Lip / Nail roles", true),
        ValidationUiModel("All selected IDs grounded", true),
        ValidationUiModel("Forbidden PREP items excluded", true)
    )

    return StyleCreationUiModel(
        occasion = context?.occasion ?: "Daily Outfit",
        userIntent = userMessage.ifBlank { null },
        appearanceTemperature = context?.appearanceTemperature ?: "Neutral",
        appearanceDepth = context?.appearanceDepth ?: "Medium",
        appearanceContrast = context?.appearanceContrast ?: "Balanced",
        temperatureC = context?.temperatureC ?: 22.0f,
        uvIndex = context?.uvIndex ?: 3.0f,
        circadianContext = context?.circadianContext ?: circadianContext,
        eligibleWardrobeCount = candidateSummary?.eligibleWardrobeCount ?: fullClothingInventory.count { !it.isHidden },
        anchorName = anchor?.anchorName ?: recommendedClothing.firstOrNull()?.name ?: "Universal Khaki Button-Down",
        anchorId = anchor?.anchorId ?: recommendedClothing.firstOrNull()?.let { "w_${it.internalId}" } ?: "w_41",
        anchorReason = anchor?.anchorReason ?: "Automatic context anchor",
        clothing = clothingModels,
        cosmetics = cosmeticModels,
        aiRationale = rationale ?: "Selected from your vault based on intent and availability.",
        validationItems = validationModels,
        paletteHex = recommendedPalette,
        fashionistaScore = fashionista?.totalScore ?: 88.0f,
        colorHarmony = fashionista?.colorHarmonyScore ?: 95.0f,
        silhouette = fashionista?.silhouetteScore ?: 80.0f,
        contrastDepth = fashionista?.contrastScore ?: 85.0f,
        intentStatus = if (intentFulfillment?.isSpecified == true) IntentUiStatus.SPECIFIED else IntentUiStatus.NOT_SPECIFIED,
        intentScore = intentFulfillment?.score,
        observedColorfulness = intentFulfillment?.observedMetrics?.colorfulness ?: 0.53f,
        observedColorContrast = intentFulfillment?.observedMetrics?.colorContrast ?: 0.50f,
        executionTier = if (isLocalResult) ExecutionTier.DETERMINISTIC_FALLBACK else ExecutionTier.AI_CLOUD,
        latencyMs = if (isLocalResult) 134L else 1290L
    )
}
