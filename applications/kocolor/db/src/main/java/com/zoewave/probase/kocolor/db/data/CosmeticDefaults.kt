package com.zoewave.probase.kocolor.db.data

import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.CosmeticCategory

object CosmeticDefaults {
    fun getDefaultCosmetics(): List<CosmeticItemEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // Lipsticks
            CosmeticItemEntity(name = "Nude Silk", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#BC8E8E", shadeName = "Nude", timestamp = now),
            CosmeticItemEntity(name = "Berrie Bliss", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#C71585", shadeName = "Berrie & Pink", timestamp = now),
            CosmeticItemEntity(name = "Crimson Fire", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#B22222", shadeName = "Red", timestamp = now),

            // foundations
            CosmeticItemEntity(name = "Cool Ivory", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#FAD4D4", shadeName = "Cool", timestamp = now),
            CosmeticItemEntity(name = "Warm Honey", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#F0C080", shadeName = "Warm", timestamp = now),
            CosmeticItemEntity(name = "Neutral Beige", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#EAD4B4", shadeName = "Neutral", timestamp = now),

            // Mascaras
            CosmeticItemEntity(name = "Deep Forest", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#002B1B", shadeName = "Black Forest", timestamp = now),
            CosmeticItemEntity(name = "Ocean Depth", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#000033", shadeName = "Midnight Ocean", timestamp = now),
            CosmeticItemEntity(name = "Eternal Flame", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#1A0000", shadeName = "Black Fire", timestamp = now),

            // Blush
            CosmeticItemEntity(name = "Rosy Cheek", brand = "KoColor", category = CosmeticCategory.BLUSH, colorHex = "#FFB6C1", shadeName = "Rose", timestamp = now),
            CosmeticItemEntity(name = "Peach Fuzz", brand = "KoColor", category = CosmeticCategory.BLUSH, colorHex = "#FFDAB9", shadeName = "Peach", timestamp = now),
            CosmeticItemEntity(name = "Berry Bloom", brand = "KoColor", category = CosmeticCategory.BLUSH, colorHex = "#D02090", shadeName = "Berry", timestamp = now),

            // Eyeshadow
            CosmeticItemEntity(name = "Sahara Sands", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#C2B280", shadeName = "Sand", timestamp = now),
            CosmeticItemEntity(name = "Ocean Mist", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#0077BE", shadeName = "Mist", timestamp = now),
            CosmeticItemEntity(name = "Starlight Purple", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#4B0082", shadeName = "Starlight", timestamp = now),

            // Eyeliner
            CosmeticItemEntity(name = "Midnight Black", brand = "KoColor", category = CosmeticCategory.EYELINER, colorHex = "#000000", shadeName = "Black", timestamp = now),
            CosmeticItemEntity(name = "Coffee Bean", brand = "KoColor", category = CosmeticCategory.EYELINER, colorHex = "#4B3621", shadeName = "Brown", timestamp = now),
            CosmeticItemEntity(name = "Slate Gray", brand = "KoColor", category = CosmeticCategory.EYELINER, colorHex = "#708090", shadeName = "Slate", timestamp = now),

            // Nail Polish
            CosmeticItemEntity(name = "Ruby Red", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#E0115F", shadeName = "Ruby", timestamp = now),
            CosmeticItemEntity(name = "Sky Blue", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#87CEEB", shadeName = "Sky", timestamp = now),
            CosmeticItemEntity(name = "Forest Green", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#228B22", shadeName = "Forest", timestamp = now)
        )
    }
}
