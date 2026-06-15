package com.zoewave.probase.kocolor.db.data

import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.core.model.ritual.ClothingCategory

object ClothingDefaults {
    private const val RES_PREFIX = "android.resource://com.zoewave.probase.kocolor.db/drawable/"

    fun getDefaultClothing(): List<ClothingItemEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // Tops
            ClothingItemEntity(
                name = "Silk Blouse", 
                brand = "KoColor", 
                category = ClothingCategory.TOPS, 
                colorHex = "#F5F5DC", 
                price = 120.0,
                material = "100% Silk",
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_1"
            ),
            ClothingItemEntity(
                name = "Cotton T-Shirt", 
                brand = "KoColor", 
                category = ClothingCategory.TOPS, 
                colorHex = "#FFFFFF", 
                price = 45.0,
                material = "Organic Cotton",
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_2"
            ),
            ClothingItemEntity(
                name = "Cashmere Sweater", 
                brand = "KoColor", 
                category = ClothingCategory.TOPS, 
                colorHex = "#808080", 
                price = 250.0,
                material = "Cashmere",
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_3"
            ),

            // Bottoms
            ClothingItemEntity(
                name = "Straight Jeans", 
                brand = "KoColor", 
                category = ClothingCategory.BOTTOMS, 
                colorHex = "#000080", 
                price = 150.0,
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_4"
            ),
            ClothingItemEntity(
                name = "Chino Pants", 
                brand = "KoColor", 
                category = ClothingCategory.BOTTOMS, 
                colorHex = "#D2B48C", 
                price = 95.0,
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_5"
            ),

            // Dresses
            ClothingItemEntity(
                name = "Wrap Dress", 
                brand = "KoColor", 
                category = ClothingCategory.OTHER, 
                colorHex = "#FF0000", 
                price = 180.0,
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_6"
            ),

            // Outerwear
            ClothingItemEntity(
                name = "Trench Coat", 
                brand = "KoColor", 
                category = ClothingCategory.TOPS,
                colorHex = "#F0E68C", 
                price = 450.0,
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_7"
            ),

            // Shoes
            ClothingItemEntity(
                name = "Leather Loafers", 
                brand = "KoColor", 
                category = ClothingCategory.SHOES, 
                colorHex = "#4B3621", 
                price = 195.0,
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_8"
            ),
            // Accessories
            ClothingItemEntity(
                name = "Leather Bag",
                brand = "KoColor",
                category = ClothingCategory.ACCESSORIES,
                colorHex = "#4B3621",
                price = 220.0,
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_9"
            ),
            ClothingItemEntity(
                name = "Gold Watch",
                brand = "KoColor",
                category = ClothingCategory.ACCESSORIES,
                colorHex = "#FFD700",
                price = 350.0,
                timestamp = now,
                imageUrl = "${RES_PREFIX}default_clothing_10"
            )
        )
    }
}
