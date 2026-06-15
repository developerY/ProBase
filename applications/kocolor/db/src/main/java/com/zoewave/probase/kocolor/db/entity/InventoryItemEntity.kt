package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.core.model.ritual.InventoryMetadata

enum class InventoryType {
    FACE, HAIR, SHOES, CLOTHES
}

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: InventoryType,
    val originalUri: String,
    val clippedUri: String? = null,
    val metadata: InventoryMetadata? = null,
    val timestamp: Long = System.currentTimeMillis()
)
