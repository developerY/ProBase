package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MakeupSuggestion
import com.zoewave.probase.core.model.ritual.SuggestedPiece

enum class ResultTab { FACE, CLOTHES, NAILS }

/**
 * Common data model used to render the visual "Blueprint" views (Face, Hand, Clothing).
 * This allows these views to be reused in the Simulator and the History Detail screen.
 */
data class VisualBlueprintData(
    val eyesItem: BlueprintItem? = null,
    val cheeksItem: BlueprintItem? = null,
    val lipsItem: BlueprintItem? = null,
    val nailsItem: BlueprintItem? = null,
    val topItem: BlueprintItem? = null,
    val bottomItem: BlueprintItem? = null,
    val shoeItem: BlueprintItem? = null,
    val recommendedPalette: List<String> = emptyList()
)

data class BlueprintItem(
    val id: Long?,
    val name: String,
    val colorHex: String,
    val imageUrl: String? = null
)

/**
 * Mapper for FashionAdvice (used in History/Collection Detail)
 */
fun FashionAdvice.toVisualBlueprintData(): VisualBlueprintData {
    val cosmetics = makeupSuggestions
    val clothes = outfitSuggestions.flatMap { it.suggestedItems }
    
    return VisualBlueprintData(
        eyesItem = cosmetics.find { it.category.equals("Eyes & Brows", ignoreCase = true) || it.category.equals("EYES", ignoreCase = true) }?.toBlueprintItem(),
        cheeksItem = cosmetics.find { it.category.equals("Color & Dimension", ignoreCase = true) || it.category.equals("DIMENSION", ignoreCase = true) }?.toBlueprintItem(),
        lipsItem = cosmetics.find { it.category.equals("Lips", ignoreCase = true) || it.category.equals("LIPS", ignoreCase = true) }?.toBlueprintItem(),
        nailsItem = cosmetics.find { it.category.equals("Nails", ignoreCase = true) || it.category.equals("NAILS", ignoreCase = true) }?.toBlueprintItem(),
        topItem = clothes.find { it.category.equals("TOPS", ignoreCase = true) || it.category.equals("OUTERWEAR", ignoreCase = true) }?.toBlueprintItem(),
        bottomItem = clothes.find { it.category.equals("BOTTOMS", ignoreCase = true) }?.toBlueprintItem(),
        shoeItem = clothes.find { it.category.equals("SHOES", ignoreCase = true) }?.toBlueprintItem(),
        recommendedPalette = recommendedPalette
    )
}

private fun MakeupSuggestion.toBlueprintItem() = BlueprintItem(
    id = productId,
    name = suggestedProductName ?: category,
    colorHex = recommendedColors.firstOrNull() ?: "#FFFFFF", // Standard fallback for invalid suggestions
    imageUrl = suggestedProductImageUrl
)

private fun SuggestedPiece.toBlueprintItem() = BlueprintItem(
    id = null,
    name = name,
    colorHex = colorHex, // Now mandatory in the model
    imageUrl = imageUrl
)

/**
 * Mapper for raw lists (used in StyleSimulatorUiState)
 */
fun mapToVisualBlueprintData(
    cosmetics: List<CosmeticItem>,
    clothing: List<ClothingItem>,
    palette: List<String>
): VisualBlueprintData {
    return VisualBlueprintData(
        eyesItem = cosmetics.find { it.macroCategory == MacroCategory.EYES }?.toBlueprintItem(),
        cheeksItem = cosmetics.find { it.macroCategory == MacroCategory.DIMENSION }?.toBlueprintItem(),
        lipsItem = cosmetics.find { it.macroCategory == MacroCategory.LIPS }?.toBlueprintItem(),
        nailsItem = cosmetics.find { it.macroCategory == MacroCategory.NAILS }?.toBlueprintItem(),
        topItem = clothing.find { it.category == ClothingCategory.TOPS }?.toBlueprintItem(),
        bottomItem = clothing.find { it.category == ClothingCategory.BOTTOMS }?.toBlueprintItem(),
        shoeItem = clothing.find { it.category == ClothingCategory.SHOES }?.toBlueprintItem(),
        recommendedPalette = palette
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
