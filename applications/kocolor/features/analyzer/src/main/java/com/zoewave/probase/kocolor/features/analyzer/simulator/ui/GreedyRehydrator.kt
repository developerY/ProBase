package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.data.color.ColorHarmonyEngine
import com.zoewave.probase.kocolor.data.usecase.ColorTelemetry
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.BlueprintItem
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.VisualBlueprintData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Final interceptor before AI Blueprint data hits the ViewModel/UI.
 * Guarantees the "user is always correct" invariant via Anchor Fail-Safes,
 * resolves ambiguous categories using keyword fallbacks, and calculates the post-synthesis Fashionista Score.
 */
@Singleton
class GreedyRehydrator @Inject constructor(
    private val colorHarmonyEngine: ColorHarmonyEngine
) {

    fun mapToVisualBlueprintData(
        aiSelectedClothingIds: List<String>,
        aiSelectedCosmeticIds: List<String>,
        inventory: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        activeClothingAnchors: List<ClothingItem> = emptyList(),
        activeCosmeticAnchors: List<CosmeticItem> = emptyList(),
        palette: List<String> = emptyList(),
        telemetry: ColorTelemetry = ColorTelemetry(),
        isComplete: Boolean = false
    ): VisualBlueprintData {
        // 1. Initial Resolution & Deduplication by Category (Never allow 2 Bottoms or 2 Tops)
        var resolvedClothing = resolveClothing(aiSelectedClothingIds, inventory).distinctBy { it.category }
        var resolvedCosmetics = resolveCosmetics(aiSelectedCosmeticIds, cosmetics).distinctBy { it.macroCategory }

        // 2. Keyword Fallback (Resolving ambiguous/generic categories)
        resolvedClothing = applyClothingKeywordFallbacks(resolvedClothing)

        // 3. Anchor Fail-Safe (Re-injecting any omitted LOCKED/FORCED anchors)
        resolvedClothing = enforceClothingFailSafe(resolvedClothing, activeClothingAnchors).distinctBy { it.category }
        resolvedCosmetics = enforceCosmeticFailSafe(resolvedCosmetics, activeCosmeticAnchors).distinctBy { it.macroCategory }

        // 4. Calculate Post-Synthesis Fashionista Score (0-100)
        val fashionistaScore = colorHarmonyEngine.calculateFashionistaScore(
            finalOutfit = resolvedClothing,
            finalCosmetics = resolvedCosmetics,
            telemetry = telemetry
        )

        // 5. Slot Assignment & Inventory Fallback for Shoes
        val blueprint = assignToSlots(resolvedClothing, resolvedCosmetics, inventory, palette, isComplete)
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

    private fun applyClothingKeywordFallbacks(items: List<ClothingItem>): List<ClothingItem> {
        return items.map { item ->
            if (item.category == ClothingCategory.ACTIVEWEAR || item.category == ClothingCategory.OTHER) {
                when {
                    item.name.contains("tank", ignoreCase = true) || item.name.contains("tee", ignoreCase = true) || item.name.contains("top", ignoreCase = true) ->
                        item.copy(category = ClothingCategory.TOPS)
                    item.name.contains("pant", ignoreCase = true) || item.name.contains("short", ignoreCase = true) || item.name.contains("legging", ignoreCase = true) ->
                        item.copy(category = ClothingCategory.BOTTOMS)
                    item.name.contains("jacket", ignoreCase = true) || item.name.contains("coat", ignoreCase = true) || item.name.contains("windshirt", ignoreCase = true) ->
                        item.copy(category = ClothingCategory.OUTERWEAR)
                    else -> item
                }
            } else item
        }
    }

    private fun enforceClothingFailSafe(
        resolved: List<ClothingItem>,
        anchors: List<ClothingItem>
    ): List<ClothingItem> {
        val resolvedInternalIds = resolved.map { it.internalId }.toSet()
        val missingAnchors = anchors.filter { it.internalId !in resolvedInternalIds }
        return resolved + missingAnchors
    }

    private fun enforceCosmeticFailSafe(
        resolved: List<CosmeticItem>,
        anchors: List<CosmeticItem>
    ): List<CosmeticItem> {
        val resolvedInternalIds = resolved.map { it.internalId }.toSet()
        val missingAnchors = anchors.filter { it.internalId !in resolvedInternalIds }
        return resolved + missingAnchors
    }

    private fun assignToSlots(
        clothing: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        inventory: List<ClothingItem>,
        palette: List<String>,
        isComplete: Boolean
    ): VisualBlueprintData {
        // Clothing slots (strictly 1 item per category)
        val deduplicatedClothing = clothing.distinctBy { it.category }

        val top = deduplicatedClothing.find { it.category == ClothingCategory.TOPS }
            ?: deduplicatedClothing.find { it.category == ClothingCategory.DRESSES }
            ?: inventory.find { it.category == ClothingCategory.TOPS }

        val bottom = deduplicatedClothing.find { it.category == ClothingCategory.BOTTOMS }
            ?: if (top?.category == ClothingCategory.DRESSES) top else inventory.find { it.category == ClothingCategory.BOTTOMS }

        val shoes = deduplicatedClothing.find { it.category == ClothingCategory.SHOES }
            ?: inventory.find { it.category == ClothingCategory.SHOES }

        val outerwear = deduplicatedClothing.find { it.category == ClothingCategory.OUTERWEAR }

        // Cosmetic slots
        val eyes = cosmetics.find { it.macroCategory == MacroCategory.EYES }
            ?: cosmetics.find { it.name.contains("eye", true) }
        val cheeks = cosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
            ?: cosmetics.find { it.microCategory == MicroCategory.BLUSH }
            ?: cosmetics.find { it.macroCategory == MacroCategory.COMPLEXION && (it.name.contains("blush", true) || it.name.contains("powder", true)) }
        val lips = cosmetics.find { it.macroCategory == MacroCategory.LIPS }
            ?: cosmetics.find { it.name.contains("lip", true) }
        val nails = cosmetics.find { it.macroCategory == MacroCategory.NAILS }
            ?: cosmetics.find { it.name.contains("nail", true) || it.name.contains("lacquer", true) }

        return VisualBlueprintData(
            eyesItem = eyes?.toBlueprintItem(),
            cheeksItem = cheeks?.toBlueprintItem(),
            lipsItem = lips?.toBlueprintItem(),
            nailsItem = nails?.toBlueprintItem(),
            topItem = top?.toBlueprintItem(),
            bottomItem = bottom?.toBlueprintItem(),
            shoeItem = shoes?.toBlueprintItem(),
            outerwearItem = outerwear?.toBlueprintItem(),
            recommendedPalette = palette,
            isComplete = isComplete
        )
    }

    private fun CosmeticItem.toBlueprintItem() = BlueprintItem(
        id = internalId,
        name = name,
        colorHex = colorHex,
        imageUrl = imageUrl
    )

    private fun ClothingItem.toBlueprintItem() = BlueprintItem(
        id = internalId,
        name = name,
        colorHex = colorHex,
        imageUrl = imageUrl
    )
}
