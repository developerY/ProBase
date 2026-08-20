package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Tracks global wardrobe usage metrics.
 * Persists as a single-row table (metricsId = 0).
 */
@Entity(tableName = "global_rotation_metrics")
data class GlobalRotationMetricsEntity(
    @PrimaryKey val metricsId: Int = 0,
    val totalOutfitsCommitted: Long = 0,
    val lastOutfitTimestamp: Long = System.currentTimeMillis()
)
