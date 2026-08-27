package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
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
    val outerwearItem: BlueprintItem? = null,
    val recommendedPalette: List<String> = emptyList(),
    val isComplete: Boolean = false
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
        topItem = clothes.find { it.category.equals("TOPS", ignoreCase = true) || it.category.equals("DRESSES", ignoreCase = true) }?.toBlueprintItem(),
        bottomItem = clothes.find { it.category.equals("BOTTOMS", ignoreCase = true) || it.category.equals("DRESSES", ignoreCase = true) }?.toBlueprintItem(),
        shoeItem = clothes.find { it.category.equals("SHOES", ignoreCase = true) }?.toBlueprintItem(),
        outerwearItem = clothes.find { it.category.equals("OUTERWEAR", ignoreCase = true) }?.toBlueprintItem(),
        recommendedPalette = recommendedPalette,
        isComplete = true // FashionAdvice is always a result
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
    palette: List<String>,
    isComplete: Boolean = false
): VisualBlueprintData {
    // 🛠️ Highly Adaptive Rehydration: Preserve distinct items across available slots
    
    // 1. COSMETICS: Broad category matching + Keyword Fallback
    val eyes = cosmetics.find { it.macroCategory == MacroCategory.EYES }
        ?: cosmetics.find { it.name.contains("eye", true) }
        
    val cheeks = cosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
        ?: cosmetics.find { it.microCategory == MicroCategory.BLUSH }
        ?: cosmetics.find { it.macroCategory == MacroCategory.COMPLEXION && (it.name.contains("blush", true) || it.name.contains("powder", true)) }
        
    val lips = cosmetics.find { it.macroCategory == MacroCategory.LIPS }
        ?: cosmetics.find { it.name.contains("lip", true) }
        
    val nails = cosmetics.find { it.macroCategory == MacroCategory.NAILS }
        ?: cosmetics.find { it.name.contains("nail", true) || it.name.contains("lacquer", true) }

    // 2. CLOTHING: Identify primary pieces with greedy category matching
    val top = clothing.find { it.category == ClothingCategory.TOPS }
        ?: clothing.find { it.category == ClothingCategory.ACTIVEWEAR && (it.name.contains("tank", true) || it.name.contains("tee", true) || it.name.contains("top", true)) }
        ?: clothing.find { it.category == ClothingCategory.DRESSES }
        
    val bottom = clothing.find { it.category == ClothingCategory.BOTTOMS }
        ?: clothing.find { it.category == ClothingCategory.ACTIVEWEAR && (it.name.contains("pants", true) || it.name.contains("shorts", true) || it.name.contains("legging", true)) }
        ?: if (top?.category == ClothingCategory.DRESSES) top else null
        
    val shoes = clothing.find { it.category == ClothingCategory.SHOES }
    
    val outerwear = clothing.find { it.category == ClothingCategory.OUTERWEAR }
        ?: clothing.find { it.category == ClothingCategory.ACTIVEWEAR && (it.name.contains("shirt", true) || it.name.contains("jacket", true) || it.name.contains("coat", true)) }

    // 🛠️ Fallback rehydration: ensure all returned clothing items are visible somewhere
    val usedIds = listOfNotNull(top?.internalId, bottom?.internalId, shoes?.internalId, outerwear?.internalId)
    val remainingClothing = clothing.filter { it.internalId !in usedIds }
    
    val finalTop = top ?: remainingClothing.firstOrNull()
    val finalBottom = bottom ?: remainingClothing.drop(1).firstOrNull()
    
    // 🛠️ Same for cosmetics: assign "left behind" items to appropriate slots
    val usedCids = listOfNotNull(eyes?.internalId, cheeks?.internalId, lips?.internalId, nails?.internalId)
    val remainingCosmetics = cosmetics.filter { it.internalId !in usedCids }
    
    val finalEyes = eyes ?: remainingCosmetics.find { it.macroCategory == MacroCategory.PREP } // Often eye cream
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
