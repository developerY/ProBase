package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.BlueprintItem
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.VisualBlueprintData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Final interceptor before AI Blueprint data hits the ViewModel/UI.
 * Guarantees the "user is always correct" invariant via Anchor Fail-Safes,
 * resolves ambiguous categories using keyword fallbacks, and prevents Compose UI hangs.
 */
@Singleton
class GreedyRehydrator @Inject constructor() {

    fun mapToVisualBlueprintData(
        aiSelectedClothingIds: List<String>,
        aiSelectedCosmeticIds: List<String>,
        inventory: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        activeClothingAnchors: List<ClothingItem> = emptyList(),
        activeCosmeticAnchors: List<CosmeticItem> = emptyList(),
        palette: List<String> = emptyList(),
        isComplete: Boolean = false
    ): VisualBlueprintData {
        // 1. Initial Resolution
        var resolvedClothing = resolveClothing(aiSelectedClothingIds, inventory)
        var resolvedCosmetics = resolveCosmetics(aiSelectedCosmeticIds, cosmetics)

        // 2. Keyword Fallback (Resolving ambiguous/generic categories)
        resolvedClothing = applyClothingKeywordFallbacks(resolvedClothing)

        // 3. Anchor Fail-Safe (Re-injecting any omitted LOCKED/FORCED anchors)
        resolvedClothing = enforceClothingFailSafe(resolvedClothing, activeClothingAnchors)
        resolvedCosmetics = enforceCosmeticFailSafe(resolvedCosmetics, activeCosmeticAnchors)

        // 4. Slot Assignment & Greedy Rehydration
        return assignToSlots(resolvedClothing, resolvedCosmetics, palette, isComplete)
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

        // No item left behind for clothing
        val usedClothingIds = listOfNotNull(top?.internalId, bottom?.internalId, shoes?.internalId, outerwear?.internalId)
        val remainingClothing = clothing.filter { it.internalId !in usedClothingIds }

        val finalTop = top ?: remainingClothing.firstOrNull()
        val finalBottom = bottom ?: remainingClothing.drop(1).firstOrNull()

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

        // No item left behind for cosmetics
        val usedCosmeticIds = listOfNotNull(eyes?.internalId, cheeks?.internalId, lips?.internalId, nails?.internalId)
        val remainingCosmetics = cosmetics.filter { it.internalId !in usedCosmeticIds }

        val finalEyes = eyes ?: remainingCosmetics.find { it.macroCategory == MacroCategory.PREP }
        val finalLips = lips ?: remainingCosmetics.find { it.microCategory == MicroCategory.LIP_CARE }

        return VisualBlueprintData(
            eyesItem = (finalEyes ?: eyes)?.toBlueprintItem(),
            cheeksItem = cheeks?.toBlueprintItem(),
            lipsItem = (finalLips ?: lips)?.toBlueprintItem(),
            nailsItem = nails?.toBlueprintItem(),
            topItem = finalTop?.toBlueprintItem(),
            bottomItem = finalBottom?.toBlueprintItem(),
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
