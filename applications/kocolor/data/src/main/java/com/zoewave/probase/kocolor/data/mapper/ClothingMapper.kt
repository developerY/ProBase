package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity

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
    sourcePackId = sourcePackId,
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
    sourcePackId = sourcePackId,
    usageCount = usageCount
)
