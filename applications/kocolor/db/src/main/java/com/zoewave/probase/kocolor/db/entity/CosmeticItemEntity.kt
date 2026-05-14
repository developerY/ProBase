package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.kocolor.model.CosmeticCategory

@Entity(tableName = "cosmetic_items")
data class CosmeticItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val category: CosmeticCategory,
    val colorHex: String? = null,
    val shadeName: String? = null,
    val notes: String? = null,
    val timestamp: Long
)
