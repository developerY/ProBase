package com.zoewave.probase.kocolor.data.model

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity

/**
 * Domain model combining a canonical garment with its user-specific usage history.
 */
data class ClothingWithUsage(
    val garment: ClothingItem,
    val usage: ClothingUsageEntity?
)
