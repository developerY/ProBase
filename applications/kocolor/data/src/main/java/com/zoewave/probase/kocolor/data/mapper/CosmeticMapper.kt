package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.CosmeticItem

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
    colorHex = colorHex,
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
    
    // FDA Safety
    fdaRecallStatus = fdaRecallStatus,
    fdaAdverseEventCount = fdaAdverseEventCount,
    fdaClinicalWarnings = fdaClinicalWarnings,
    fdaTopReactions = fdaTopReactions,
    fdaActiveIngredients = fdaActiveIngredients,
    isFdaChecked = isFdaChecked
)

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
    colorHex = colorHex,
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
    
    // FDA Safety
    fdaRecallStatus = fdaRecallStatus,
    fdaAdverseEventCount = fdaAdverseEventCount,
    fdaClinicalWarnings = fdaClinicalWarnings,
    fdaTopReactions = fdaTopReactions,
    fdaActiveIngredients = fdaActiveIngredients,
    isFdaChecked = isFdaChecked
)
