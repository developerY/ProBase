package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.core.model.ritual.Provenance as ModelProvenance
import com.zoewave.probase.kocolor.db.entity.Provenance as DbProvenance

fun ClothingItemEntity.toModel(): ClothingItem = ClothingItem(
    id = id,
    name = name,
    brand = brand,
    category = category,
    formality = formality,
    colorHex = colorHex,
    colorFamily = colorFamily,
    size = size,
    material = material,
    price = price,
    imageUrl = imageUrl,
    notes = notes,
    timestamp = timestamp,
    dominantHex = dominantHex,
    vibrantHex = vibrantHex,
    mutedHex = mutedHex,
    paletteHexes = paletteHexes,
    colorTemperature = colorTemperature,
    seasonalPalette = seasonalPalette,
    contrastLevel = contrastLevel,
    koColorGroup = koColorGroup,
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
    blurhash = blurhash,
    searchTokens = searchTokens,
    
    usageCount = usageCount
)

fun ClothingItem.toEntity(): ClothingItemEntity = ClothingItemEntity(
    id = id,
    name = name,
    brand = brand,
    category = category,
    formality = formality,
    colorHex = colorHex,
    colorFamily = colorFamily,
    size = size,
    material = material,
    price = price,
    imageUrl = imageUrl,
    notes = notes,
    timestamp = timestamp,
    dominantHex = dominantHex,
    vibrantHex = vibrantHex,
    mutedHex = mutedHex,
    paletteHexes = paletteHexes,
    colorTemperature = colorTemperature,
    seasonalPalette = seasonalPalette,
    contrastLevel = contrastLevel,
    koColorGroup = koColorGroup,
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
    blurhash = blurhash,
    searchTokens = searchTokens,
    
    usageCount = usageCount
)
