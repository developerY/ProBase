package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.Formality
import com.zoewave.probase.core.model.ritual.ColorFamily
import com.zoewave.probase.core.model.ritual.InventorySource

@Entity(tableName = "clothing_items")
data class ClothingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val category: ClothingCategory,
    val formality: Formality = Formality.CASUAL,
    val colorHex: String,
    val colorFamily: ColorFamily = ColorFamily.UNKNOWN,
    val size: String? = null,
    val material: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    
    // --- Wardrobe Color Engine Metadata ---
    val dominantHex: String? = null,
    val vibrantHex: String? = null,
    val mutedHex: String? = null,
    val paletteHexes: List<String> = emptyList(),
    val colorTemperature: String? = null,
    val seasonalPalette: String? = null,
    val contrastLevel: String? = null,
    val koColorGroup: String? = null,
    val sourceType: InventorySource = InventorySource.USER_SCAN,
    val sourceName: String? = null,
    val sourcePackId: String? = null,
    val parentItemId: String? = null,
    val isHidden: Boolean = false,

    // --- Usage & Performance ---
    val usageCount: Int = 0
)
