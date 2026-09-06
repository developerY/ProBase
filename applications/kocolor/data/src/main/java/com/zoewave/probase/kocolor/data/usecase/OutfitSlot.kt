package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory

enum class OutfitSlot {
    TOP,
    BOTTOM,
    SHOES,
    OUTERWEAR;

    companion object {
        fun fromCategory(category: ClothingCategory): OutfitSlot? {
            return when (category) {
                ClothingCategory.TOPS, ClothingCategory.DRESSES, ClothingCategory.ACTIVEWEAR -> TOP
                ClothingCategory.BOTTOMS -> BOTTOM
                ClothingCategory.SHOES -> SHOES
                ClothingCategory.OUTERWEAR -> OUTERWEAR
                else -> null
            }
        }
    }
}
