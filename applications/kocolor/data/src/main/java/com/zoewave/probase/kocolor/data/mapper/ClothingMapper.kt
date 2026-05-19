package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.model.ClothingItem

fun ClothingItemEntity.toModel(): ClothingItem = ClothingItem(
    id = id,
    name = name,
    brand = brand,
    category = category,
    colorHex = colorHex,
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
    koColorGroup = koColorGroup
)

fun ClothingItem.toEntity(): ClothingItemEntity = ClothingItemEntity(
    id = id,
    name = name,
    brand = brand,
    category = category,
    colorHex = colorHex,
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
    koColorGroup = koColorGroup
)
