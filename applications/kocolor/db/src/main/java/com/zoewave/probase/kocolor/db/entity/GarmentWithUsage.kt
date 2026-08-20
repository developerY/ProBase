package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

/**
 * A data-transfer object (POJO) that joins a canonical garment with its
 * user-specific usage history.
 */
data class GarmentWithUsage(
    @Embedded val garment: ClothingItemEntity,
    @Relation(
        parentColumns = ["remoteId"],
        entityColumns = ["productId"]
    )
    val usage: ClothingUsageEntity?
)
