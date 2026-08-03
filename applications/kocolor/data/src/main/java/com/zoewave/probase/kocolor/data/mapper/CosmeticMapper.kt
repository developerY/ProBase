package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity

fun CosmeticItemEntity.toModel(): CosmeticItem = CosmeticItem(
    id = id,
    name = name,
    brand = brand,
    macroCategory = macroCategory,
    microCategory = microCategory,
    formulation = formulation,
    chemistryBase = chemistryBase,
    finish = finish,
    coverage = coverage,
    temperature = temperature,
    colorHex = colorHex,
    colorFamily = colorFamily,
    shadeName = shadeName,
    imageUrl = imageUrl,
    notes = notes,
    instructions = instructions,
    price = price,
    volume = volume,
    usageCount = usageCount,
    openedDate = openedDate,
    paoMonths = paoMonths,
    timestamp = timestamp,
    amountRemaining = amountRemaining,
    amountPerUse = amountPerUse,
    isOpened = isOpened,
    isFinished = isFinished,
    isArchived = isArchived,
    
    // Algorithmic Insights
    heroIngredient = heroIngredient,
    skinCompatibility = skinCompatibility,
    containsFragrance = containsFragrance,
    ingredients = ingredients,
    allergens = allergens,
    
    // Sustainability
    ecoScore = ecoScore,
    isVegan = isVegan,
    isCrueltyFree = isCrueltyFree,
    recyclingInstructions = recyclingInstructions,
    
    // Context
    ritualPlacement = ritualPlacement,
    sourcePackId = sourcePackId,
    
    // FDA Safety
    fdaRecallStatus = fdaRecallStatus,
    fdaAdverseEventCount = fdaAdverseEventCount,
    fdaClinicalWarnings = fdaClinicalWarnings,
    fdaTopReactions = fdaTopReactions,
    fdaActiveIngredients = fdaActiveIngredients,
    isFdaChecked = isFdaChecked
)

/**
 * Maps the rich [CosmeticItem] model to a flat payload suitable for 
 * uploading to the Open Beauty Facts (OBF) community database.
 */
fun CosmeticItem.toObfPayload(): Map<String, String> {
    return mutableMapOf<String, String>().apply {
        put("product_name", name)
        put("brands", brand)
        put("barcode", batchCode ?: "")
        put("ingredients_text", ingredients.joinToString(", "))
        put("quantity", volume ?: "")
        put("categories", "${macroCategory.displayName}, ${microCategory.displayName}")
        
        // Technical facets flattened back to readable tags
        val tags = mutableListOf<String>()
        if (formulation != com.zoewave.probase.core.model.ritual.Formulation.UNKNOWN) tags.add(formulation.name.lowercase())
        if (finish != com.zoewave.probase.core.model.ritual.Finish.UNKNOWN) tags.add(finish.name.lowercase())
        if (chemistryBase != com.zoewave.probase.core.model.ritual.ChemistryBase.UNKNOWN) tags.add(chemistryBase.name.lowercase())
        
        if (tags.isNotEmpty()) {
            put("labels", tags.joinToString(", "))
        }

        if (isVegan == true) put("labels", (get("labels")?.let { "$it, " } ?: "") + "vegan")
        if (isCrueltyFree == true) put("labels", (get("labels")?.let { "$it, " } ?: "") + "cruelty-free")
    }
}

fun CosmeticItem.toEntity(): CosmeticItemEntity = CosmeticItemEntity(
    id = id,
    name = name,
    brand = brand,
    macroCategory = macroCategory,
    microCategory = microCategory,
    formulation = formulation,
    chemistryBase = chemistryBase,
    finish = finish,
    coverage = coverage,
    temperature = temperature,
    colorHex = colorHex,
    colorFamily = colorFamily,
    shadeName = shadeName,
    imageUrl = imageUrl,
    notes = notes,
    instructions = instructions,
    price = price,
    volume = volume,
    usageCount = usageCount,
    openedDate = openedDate,
    paoMonths = paoMonths,
    timestamp = timestamp,
    amountRemaining = amountRemaining,
    amountPerUse = amountPerUse,
    isOpened = isOpened,
    isFinished = isFinished,
    isArchived = isArchived,
    
    // Algorithmic Insights
    heroIngredient = heroIngredient,
    skinCompatibility = skinCompatibility,
    containsFragrance = containsFragrance,
    ingredients = ingredients,
    allergens = allergens,
    
    // Sustainability
    ecoScore = ecoScore,
    isVegan = isVegan,
    isCrueltyFree = isCrueltyFree,
    recyclingInstructions = recyclingInstructions,
    
    // Context
    ritualPlacement = ritualPlacement,
    sourcePackId = sourcePackId,
    
    // FDA Safety
    fdaRecallStatus = fdaRecallStatus,
    fdaAdverseEventCount = fdaAdverseEventCount,
    fdaClinicalWarnings = fdaClinicalWarnings,
    fdaTopReactions = fdaTopReactions,
    fdaActiveIngredients = fdaActiveIngredients,
    isFdaChecked = isFdaChecked
)
