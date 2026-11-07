package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.data.usecase.ColorTelemetry
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.VisualBlueprintData
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.BlueprintItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure ID-to-UI Mapper.
 * Upstream validators and engines have already guaranteed completion and deduplication.
 */
@Singleton
class GreedyRehydrator @Inject constructor() {

    fun mapToVisualBlueprintData(
        aiSelectedClothingIds: List<String>,
        aiSelectedCosmeticIds: List<String>,
        inventory: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        palette: List<String> = emptyList(),
        fashionistaScore: Int,
        isComplete: Boolean = false
    ): VisualBlueprintData {
        // 1. Initial Resolution
        val resolvedClothing = resolveClothing(aiSelectedClothingIds, inventory)
        val resolvedCosmetics = resolveCosmetics(aiSelectedCosmeticIds, cosmetics)

        // 2. Slot Assignment
        val blueprint = assignToSlots(resolvedClothing, resolvedCosmetics, palette, isComplete)
        
        // 3. Return mapped UI state with provided upstream score
        return blueprint.copy(koColorScore = fashionistaScore)
    }

    private fun resolveClothing(ids: List<String>, inventory: List<ClothingItem>): List<ClothingItem> {
        return inventory.filter { item ->
            "w_${item.internalId}" in ids || item.remoteId in ids
        }
    }

    private fun resolveCosmetics(ids: List<String>, cosmetics: List<CosmeticItem>): List<CosmeticItem> {
        return cosmetics.filter { item ->
            "c_${item.internalId}" in ids || item.remoteId in ids
        }
    }

    private fun assignToSlots(
        clothing: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        palette: List<String>,
        isComplete: Boolean
    ): VisualBlueprintData {
        // Clothing slots
        val top = clothing.find { it.category == ClothingCategory.TOPS }
            ?: clothing.find { it.category == ClothingCategory.DRESSES }

        val bottom = clothing.find { it.category == ClothingCategory.BOTTOMS }
            ?: if (top?.category == ClothingCategory.DRESSES) top else null

        val shoes = clothing.find { it.category == ClothingCategory.SHOES }
        val outerwear = clothing.find { it.category == ClothingCategory.OUTERWEAR }

        // Cosmetic slots
        val eye = cosmetics.find { it.macroCategory == MacroCategory.EYES }
        val cheek = cosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
        val lip = cosmetics.find { it.macroCategory == MacroCategory.LIPS }
        val nail = cosmetics.find { it.macroCategory == MacroCategory.NAILS }
        val complexion = cosmetics.find { it.macroCategory == MacroCategory.COMPLEXION }

        return VisualBlueprintData(
            topItem = top?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            bottomItem = bottom?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            shoeItem = shoes?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            outerwearItem = outerwear?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            eyesItem = eye?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            cheeksItem = cheek?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            lipsItem = lip?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            nailsItem = nail?.let { BlueprintItem(it.internalId, it.name, it.colorHex, it.imageUrl) },
            recommendedPalette = palette,
            koColorScore = 85,
            isComplete = isComplete
        )
    }
}
