package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Tracks user-specific usage history for a garment.
 * Decoupled from the canonical [ClothingItemEntity] to maintain data integrity
 * during catalog updates.
 */
@Entity(tableName = "clothing_usage")
data class ClothingUsageEntity(
    @PrimaryKey val productId: String, // Maps to ClothingItemEntity.remoteId or id
    val useCount: Long = 0,
    val lastUsedTimestamp: Long? = null
)
