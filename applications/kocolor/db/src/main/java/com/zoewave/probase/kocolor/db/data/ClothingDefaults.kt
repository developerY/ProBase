package com.zoewave.probase.kocolor.db.data

import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.model.ClothingCategory

object ClothingDefaults {
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
                imageUrl = "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=400&q=80"
            ),
            ClothingItemEntity(
                name = "Cotton T-Shirt", 
                brand = "KoColor", 
                category = ClothingCategory.TOPS, 
                colorHex = "#FFFFFF", 
                price = 45.0,
                material = "Organic Cotton",
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=400&q=80"
            ),
            ClothingItemEntity(
                name = "Cashmere Sweater", 
                brand = "KoColor", 
                category = ClothingCategory.TOPS, 
                colorHex = "#808080", 
                price = 250.0,
                material = "Cashmere",
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=400&q=80"
            ),

            // Bottoms
            ClothingItemEntity(
                name = "Straight Jeans", 
                brand = "KoColor", 
                category = ClothingCategory.BOTTOMS, 
                colorHex = "#000080", 
                price = 150.0,
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&q=80"
            ),
            ClothingItemEntity(
                name = "Chino Pants", 
                brand = "KoColor", 
                category = ClothingCategory.BOTTOMS, 
                colorHex = "#D2B48C", 
                price = 95.0,
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1473966968600-fa804b86d30b?w=400&q=80"
            ),

            // Dresses (Map to TOPS or OTHER if removed, but user requested clean categorization)
            ClothingItemEntity(
                name = "Wrap Dress", 
                brand = "KoColor", 
                category = ClothingCategory.OTHER, 
                colorHex = "#FF0000", 
                price = 180.0,
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1539008886427-464469802c0b?w=400&q=80"
            ),

            // Outerwear (Map to TOPS)
            ClothingItemEntity(
                name = "Trench Coat", 
                brand = "KoColor", 
                category = ClothingCategory.TOPS,
                colorHex = "#F0E68C", 
                price = 450.0,
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=400&q=80"
            ),

            // Shoes
            ClothingItemEntity(
                name = "Leather Loafers", 
                brand = "KoColor", 
                category = ClothingCategory.SHOES, 
                colorHex = "#4B3621", 
                price = 195.0,
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=400&q=80"
            ),
            // Accessories
            ClothingItemEntity(
                name = "Leather Bag",
                brand = "KoColor",
                category = ClothingCategory.ACCESSORIES,
                colorHex = "#4B3621",
                price = 220.0,
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=400&q=80"
            ),
            ClothingItemEntity(
                name = "Gold Watch",
                brand = "KoColor",
                category = ClothingCategory.ACCESSORIES,
                colorHex = "#FFD700",
                price = 350.0,
                timestamp = now,
                imageUrl = "https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=400&q=80"
            )
        )
    }
}
