package com.zoewave.probase.kocolor.db.data

import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.model.ClothingCategory

object ClothingDefaults {
    fun getDefaultClothing(): List<ClothingItemEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // Tops
            ClothingItemEntity(name = "Silk Blouse", brand = "KoColor", category = ClothingCategory.TOPS, colorHex = "#F5F5DC", timestamp = now),
            ClothingItemEntity(name = "Cotton T-Shirt", brand = "KoColor", category = ClothingCategory.TOPS, colorHex = "#FFFFFF", timestamp = now),
            ClothingItemEntity(name = "Cashmere Sweater", brand = "KoColor", category = ClothingCategory.TOPS, colorHex = "#808080", timestamp = now),

            // Bottoms
            ClothingItemEntity(name = "Straight Jeans", brand = "KoColor", category = ClothingCategory.BOTTOMS, colorHex = "#000080", timestamp = now),
            ClothingItemEntity(name = "Pencil Skirt", brand = "KoColor", category = ClothingCategory.BOTTOMS, colorHex = "#000000", timestamp = now),
            ClothingItemEntity(name = "Chino Pants", brand = "KoColor", category = ClothingCategory.BOTTOMS, colorHex = "#D2B48C", timestamp = now),

            // Dresses
            ClothingItemEntity(name = "Wrap Dress", brand = "KoColor", category = ClothingCategory.DRESSES, colorHex = "#FF0000", timestamp = now),
            ClothingItemEntity(name = "Summer Sundress", brand = "KoColor", category = ClothingCategory.DRESSES, colorHex = "#FFFFE0", timestamp = now),

            // Outerwear
            ClothingItemEntity(name = "Trench Coat", brand = "KoColor", category = ClothingCategory.OUTERWEAR, colorHex = "#F0E68C", timestamp = now),
            ClothingItemEntity(name = "Denim Jacket", brand = "KoColor", category = ClothingCategory.OUTERWEAR, colorHex = "#4682B4", timestamp = now),

            // Shoes
            ClothingItemEntity(name = "Leather Loafers", brand = "KoColor", category = ClothingCategory.SHOES, colorHex = "#4B3621", timestamp = now),
            ClothingItemEntity(name = "Running Sneakers", brand = "KoColor", category = ClothingCategory.SHOES, colorHex = "#FFFFFF", timestamp = now),
            ClothingItemEntity(name = "Ankle Boots", brand = "KoColor", category = ClothingCategory.SHOES, colorHex = "#000000", timestamp = now),

            // Accessories
            ClothingItemEntity(name = "Silk Scarf", brand = "KoColor", category = ClothingCategory.ACCESSORIES, colorHex = "#DA70D6", timestamp = now),
            ClothingItemEntity(name = "Leather Belt", brand = "KoColor", category = ClothingCategory.ACCESSORIES, colorHex = "#8B4513", timestamp = now)
        )
    }
}
