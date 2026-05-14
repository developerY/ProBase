package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.kocolor.model.ClothingCategory

@Entity(tableName = "clothing_items")
data class ClothingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val category: ClothingCategory,
    val colorHex: String? = null,
    val size: String? = null,
    val material: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    val timestamp: Long
)
