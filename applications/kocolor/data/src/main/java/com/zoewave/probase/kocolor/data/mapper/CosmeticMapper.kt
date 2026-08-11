package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.core.model.ritual.Provenance as ModelProvenance
import com.zoewave.probase.kocolor.db.entity.Provenance as DbProvenance

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
    sourceType = sourceType,
    sourceName = sourceName,
    provenance = provenance?.let {
        ModelProvenance(
            packId = it.packId,
            packageVersion = it.packageVersion,
            schemaVersion = it.schemaVersion,
            publisher = it.publisher,
            packageHash = it.packageHash,
            installedAtTimestamp = it.installedAtTimestamp,
            verificationState = it.verificationState
        )
    },
    parentItemId = parentItemId,
    isHidden = isHidden,
    
    // Engine Enrichment
    calculatedChemistryPhase = calculatedChemistryPhase,
    calculatedCielabL = calculatedCielabL,
    calculatedCielabA = calculatedCielabA,
    calculatedCielabB = calculatedCielabB,
    calculatedHueAngle = calculatedHueAngle,
    calculatedBlurhash = calculatedBlurhash,
    isSiliconeFree = isSiliconeFree,
    isParabenFree = isParabenFree,
    isSulfateFree = isSulfateFree,
    heroActives = heroActives,
    calculatedUnitPrice = calculatedUnitPrice,
    searchTokens = searchTokens,
    
    // FDA Safety
    fdaRecallStatus = fdaRecallStatus,
    fdaAdverseEventCount = fdaAdverseEventCount,
    fdaClinicalWarnings = fdaClinicalWarnings,
    fdaTopReactions = fdaTopReactions,
    fdaActiveIngredients = fdaActiveIngredients,
    fdaDataVerified = fdaDataVerified
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
    sourceType = sourceType,
    sourceName = sourceName,
    provenance = provenance?.let {
        DbProvenance(
            packId = it.packId,
            packageVersion = it.packageVersion,
            schemaVersion = it.schemaVersion,
            publisher = it.publisher,
            packageHash = it.packageHash,
            installedAtTimestamp = it.installedAtTimestamp,
            verificationState = it.verificationState
        )
    },
    parentItemId = parentItemId,
    isHidden = isHidden,
    
    // Engine Enrichment
    calculatedChemistryPhase = calculatedChemistryPhase,
    calculatedCielabL = calculatedCielabL,
    calculatedCielabA = calculatedCielabA,
    calculatedCielabB = calculatedCielabB,
    calculatedHueAngle = calculatedHueAngle,
    calculatedBlurhash = calculatedBlurhash,
    isSiliconeFree = isSiliconeFree,
    isParabenFree = isParabenFree,
    isSulfateFree = isSulfateFree,
    heroActives = heroActives,
    calculatedUnitPrice = calculatedUnitPrice,
    searchTokens = searchTokens,
    
    // FDA Safety
    fdaRecallStatus = fdaRecallStatus,
    fdaAdverseEventCount = fdaAdverseEventCount,
    fdaClinicalWarnings = fdaClinicalWarnings,
    fdaTopReactions = fdaTopReactions,
    fdaActiveIngredients = fdaActiveIngredients,
    fdaDataVerified = fdaDataVerified
)
