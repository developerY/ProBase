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

            // Foundations
            CosmeticItemEntity(name = "Cool Ivory", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#FAD4D4", shadeName = "Cool", timestamp = now),
            CosmeticItemEntity(name = "Warm Honey", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#F0C080", shadeName = "Warm", timestamp = now),
            CosmeticItemEntity(name = "Neutral Beige", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#EAD4B4", shadeName = "Neutral", timestamp = now),

            // Mascaras
            CosmeticItemEntity(name = "Deep Forest", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#002B1B", shadeName = "Black Forest", timestamp = now),
            CosmeticItemEntity(name = "Ocean Depth", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#000033", shadeName = "Midnight Ocean", timestamp = now),
            CosmeticItemEntity(name = "Eternal Flame", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#1A0000", shadeName = "Black Fire", timestamp = now)
        )
    }
}
