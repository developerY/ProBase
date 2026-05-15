package com.zoewave.probase.kocolor.db.data

import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.CosmeticCategory

object CosmeticDefaults {
    fun getDefaultCosmetics(): List<CosmeticItemEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // --- Face (Base & Coverage) ---
            CosmeticItemEntity(name = "Silk Primer", brand = "KoColor", category = CosmeticCategory.PRIMER, colorHex = "#F8F0E3", shadeName = "Translucent", timestamp = now),
            CosmeticItemEntity(name = "Cool Ivory", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#FAD4D4", shadeName = "Cool", timestamp = now),
            CosmeticItemEntity(name = "Warm Honey", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#F0C080", shadeName = "Warm", timestamp = now),
            CosmeticItemEntity(name = "Neutral Beige", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#EAD4B4", shadeName = "Neutral", timestamp = now),
            CosmeticItemEntity(name = "Perfect Hide", brand = "KoColor", category = CosmeticCategory.CONCEALER, colorHex = "#F5F5DC", shadeName = "Light", timestamp = now),
            CosmeticItemEntity(name = "Daily Glow", brand = "KoColor", category = CosmeticCategory.BB_CC_CREAM, colorHex = "#FFE4C4", shadeName = "Medium", timestamp = now),
            CosmeticItemEntity(name = "Stay All Day", brand = "KoColor", category = CosmeticCategory.SETTING_PRODUCT, colorHex = "#FFFFFF", shadeName = "Clear Spray", timestamp = now),

            // --- Cheeks (Color & Dimension) ---
            CosmeticItemEntity(name = "Rosy Cheek", brand = "KoColor", category = CosmeticCategory.BLUSH, colorHex = "#FFB6C1", shadeName = "Rose", timestamp = now),
            CosmeticItemEntity(name = "Peach Fuzz", brand = "KoColor", category = CosmeticCategory.BLUSH, colorHex = "#FFDAB9", shadeName = "Peach", timestamp = now),
            CosmeticItemEntity(name = "Sun Kissed", brand = "KoColor", category = CosmeticCategory.BRONZER, colorHex = "#A0522D", shadeName = "Bronze", timestamp = now),
            CosmeticItemEntity(name = "Sculpt & Shape", brand = "KoColor", category = CosmeticCategory.CONTOUR, colorHex = "#8B4513", shadeName = "Deep", timestamp = now),
            CosmeticItemEntity(name = "Golden Glow", brand = "KoColor", category = CosmeticCategory.HIGHLIGHTER, colorHex = "#FAFAD2", shadeName = "Gold", timestamp = now),

            // --- Eyes (Definition) ---
            CosmeticItemEntity(name = "Sahara Sands", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#C2B280", shadeName = "Sand", timestamp = now),
            CosmeticItemEntity(name = "Ocean Mist", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#0077BE", shadeName = "Mist", timestamp = now),
            CosmeticItemEntity(name = "Starlight Purple", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#4B0082", shadeName = "Starlight", timestamp = now),
            CosmeticItemEntity(name = "Midnight Black", brand = "KoColor", category = CosmeticCategory.EYELINER, colorHex = "#000000", shadeName = "Black", timestamp = now),
            CosmeticItemEntity(name = "Deep Forest", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#002B1B", shadeName = "Black Forest", timestamp = now),
            CosmeticItemEntity(name = "Ocean Depth", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#000033", shadeName = "Midnight Ocean", timestamp = now),
            CosmeticItemEntity(name = "Eternal Flame", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#1A0000", shadeName = "Black Fire", timestamp = now),
            CosmeticItemEntity(name = "Brow Frame", brand = "KoColor", category = CosmeticCategory.EYEBROW_PRODUCT, colorHex = "#4B3621", shadeName = "Dark Brown", timestamp = now),
            CosmeticItemEntity(name = "Lash Boost", brand = "KoColor", category = CosmeticCategory.FALSE_LASHES, colorHex = "#000000", shadeName = "Natural Volume", timestamp = now),

            // --- Lips (Color & Texture) ---
            CosmeticItemEntity(name = "Nude Silk", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#BC8E8E", shadeName = "Nude", timestamp = now),
            CosmeticItemEntity(name = "Berrie Bliss", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#C71585", shadeName = "Berrie & Pink", timestamp = now),
            CosmeticItemEntity(name = "Crimson Fire", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#B22222", shadeName = "Red", timestamp = now),
            CosmeticItemEntity(name = "Crystal Shine", brand = "KoColor", category = CosmeticCategory.LIP_GLOSS, colorHex = "#FFC0CB", shadeName = "Clear", timestamp = now),
            CosmeticItemEntity(name = "Define & Stay", brand = "KoColor", category = CosmeticCategory.LIP_LINER, colorHex = "#CD5C5C", shadeName = "Mauve", timestamp = now),
            CosmeticItemEntity(name = "Berry Stain", brand = "KoColor", category = CosmeticCategory.LIP_STAIN_TINT, colorHex = "#800000", shadeName = "Deep Berry", timestamp = now),
            CosmeticItemEntity(name = "Plump & Pout", brand = "KoColor", category = CosmeticCategory.LIP_PLUMPER, colorHex = "#FFE4E1", shadeName = "Volume Up", timestamp = now),

            // --- Tools & Accessories ---
            CosmeticItemEntity(name = "Master Blender", brand = "KoColor", category = CosmeticCategory.BRUSHES_SPONGES, colorHex = "#FF69B4", shadeName = "Pink Sponge", timestamp = now),
            CosmeticItemEntity(name = "Perfect Curl", brand = "KoColor", category = CosmeticCategory.EYELASH_CURLER, colorHex = "#C0C0C0", shadeName = "Silver", timestamp = now),
            CosmeticItemEntity(name = "Vanity Tower", brand = "KoColor", category = CosmeticCategory.ORGANIZERS, colorHex = "#FFFFFF", shadeName = "Acrylic", timestamp = now),
            CosmeticItemEntity(name = "Ruby Red", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#E0115F", shadeName = "Ruby", timestamp = now),
            CosmeticItemEntity(name = "Sky Blue", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#87CEEB", shadeName = "Sky", timestamp = now),
            CosmeticItemEntity(name = "Forest Green", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#228B22", shadeName = "Forest", timestamp = now)
        )
    }
}
