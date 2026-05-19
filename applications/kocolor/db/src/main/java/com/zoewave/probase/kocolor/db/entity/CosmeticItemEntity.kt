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
    val imageUrl: String? = null,
    val notes: String? = null,
    val instructions: String? = null,
    val timestamp: Long,
    
    // --- Professional Inventory ---
    val batchCode: String? = null,
    val openedDate: Long? = null,
    val paoMonths: Int? = null,
    val expiryDate: Long? = null,
    val price: Double? = null,
    val volume: String? = null,

    // --- Usage & Consumption Engine ---
    val isOpened: Boolean = false,
    val isFinished: Boolean = false,
    val isArchived: Boolean = false,
    val usageCount: Int = 0,
    val amountRemaining: Double? = null,
    val amountPerUse: Double? = null
)
