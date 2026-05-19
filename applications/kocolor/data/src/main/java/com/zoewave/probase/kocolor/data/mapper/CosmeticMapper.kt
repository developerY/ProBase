package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.CosmeticItem

fun CosmeticItemEntity.toModel(): CosmeticItem = CosmeticItem(
    id = id,
    name = name,
    brand = brand,
    category = category,
    colorHex = colorHex,
    shadeName = shadeName,
    imageUrl = imageUrl,
    price = price,
    volume = volume,
    usageCount = usageCount,
    openedDate = openedDate,
    paoMonths = paoMonths,
    timestamp = timestamp,
    amountRemaining = amountRemaining,
    amountPerUse = amountPerUse
)

fun CosmeticItem.toEntity(): CosmeticItemEntity = CosmeticItemEntity(
    id = id,
    name = name,
    brand = brand,
    category = category,
    colorHex = colorHex,
    shadeName = shadeName,
    imageUrl = imageUrl,
    price = price,
    volume = volume,
    usageCount = usageCount,
    openedDate = openedDate,
    paoMonths = paoMonths,
    timestamp = timestamp,
    amountRemaining = amountRemaining,
    amountPerUse = amountPerUse
)
