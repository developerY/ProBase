package com.zoewave.probase.kocolor.db.data

import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.CosmeticCategory

object CosmeticDefaults {
    fun getDefaultCosmetics(): List<CosmeticItemEntity> {
        val now = System.currentTimeMillis()
        val month = 30L * 24 * 60 * 60 * 1000
        
        return listOf(
            // --- Face (Base & Coverage) ---
            CosmeticItemEntity(
                name = "Silk Primer", 
                brand = "KoColor", 
                category = CosmeticCategory.PRIMER, 
                colorHex = "#F8F0E3", 
                shadeName = "Translucent", 
                timestamp = now,
                price = 28.0,
                paoMonths = 12,
                usageCount = 45,
                isOpened = true,
                openedDate = now - (3 * month)
            ),
            CosmeticItemEntity(
                name = "Cool Ivory", 
                brand = "KoColor", 
                category = CosmeticCategory.FOUNDATION, 
                colorHex = "#FAD4D4", 
                shadeName = "Cool", 
                timestamp = now,
                price = 42.0,
                paoMonths = 24,
                usageCount = 120,
                isOpened = true,
                openedDate = now - (23 * month) // Expiring soon!
            ),
            CosmeticItemEntity(
                name = "Warm Honey", 
                brand = "KoColor", 
                category = CosmeticCategory.FOUNDATION, 
                colorHex = "#F0C080", 
                shadeName = "Warm", 
                timestamp = now,
                price = 42.0,
                paoMonths = 24,
                usageCount = 10,
                isOpened = false
            ),
            CosmeticItemEntity(name = "Neutral Beige", brand = "KoColor", category = CosmeticCategory.FOUNDATION, colorHex = "#EAD4B4", shadeName = "Neutral", timestamp = now),
            /** Concealer: Covers imperfections and brightens under-eyes. */
            CosmeticItemEntity(name = "Perfect Hide", brand = "KoColor", category = CosmeticCategory.CONCEALER, colorHex = "#F5F5DC", shadeName = "Light", timestamp = now),
            /** BB/CC Cream: Lighter alternatives for coverage and skincare benefits. */
            CosmeticItemEntity(name = "Daily Glow", brand = "KoColor", category = CosmeticCategory.BB_CC_CREAM, colorHex = "#FFE4C4", shadeName = "Medium", timestamp = now),
            /** Setting Powder/Spray: Locks makeup in place. */
            CosmeticItemEntity(name = "Stay All Day", brand = "KoColor", category = CosmeticCategory.SETTING_PRODUCT, colorHex = "#FFFFFF", shadeName = "Clear Spray", timestamp = now),

            // --- Cheeks (Color & Dimension) ---
            /** Blush: Adds color to the cheeks (powder, cream, liquid). */
            CosmeticItemEntity(name = "Rosy Cheek", brand = "KoColor", category = CosmeticCategory.BLUSH, colorHex = "#FFB6C1", shadeName = "Rose", timestamp = now),
            CosmeticItemEntity(name = "Peach Fuzz", brand = "KoColor", category = CosmeticCategory.BLUSH, colorHex = "#FFDAB9", shadeName = "Peach", timestamp = now),
            /** Bronzer: Adds warmth for a sun-kissed look. */
            CosmeticItemEntity(name = "Sun Kissed", brand = "KoColor", category = CosmeticCategory.BRONZER, colorHex = "#A0522D", shadeName = "Bronze", timestamp = now),
            /** Contour: Creates shadows for sculpting. */
            CosmeticItemEntity(name = "Sculpt & Shape", brand = "KoColor", category = CosmeticCategory.CONTOUR, colorHex = "#8B4513", shadeName = "Deep", timestamp = now),
            /** Highlighter: Adds radiance to high points of the face. */
            CosmeticItemEntity(name = "Golden Glow", brand = "KoColor", category = CosmeticCategory.HIGHLIGHTER, colorHex = "#FAFAD2", shadeName = "Gold", timestamp = now),

            // --- Eyes (Definition) ---
            /** Eyeshadow: Adds color and depth to eyelids. */
            CosmeticItemEntity(name = "Sahara Sands", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#C2B280", shadeName = "Sand", timestamp = now),
            CosmeticItemEntity(name = "Ocean Mist", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#0077BE", shadeName = "Mist", timestamp = now),
            CosmeticItemEntity(name = "Starlight Purple", brand = "KoColor", category = CosmeticCategory.EYESHADOW, colorHex = "#4B0082", shadeName = "Starlight", timestamp = now),
            /** Eyeliner: Defines the lash line. */
            CosmeticItemEntity(name = "Midnight Black", brand = "KoColor", category = CosmeticCategory.EYELINER, colorHex = "#000000", shadeName = "Black", timestamp = now),
            /** Mascara: Volumizes, lengthens, or darkens lashes. */
            CosmeticItemEntity(name = "Deep Forest", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#002B1B", shadeName = "Black Forest", timestamp = now),
            CosmeticItemEntity(name = "Ocean Depth", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#000033", shadeName = "Midnight Ocean", timestamp = now),
            CosmeticItemEntity(name = "Eternal Flame", brand = "KoColor", category = CosmeticCategory.MASCARA, colorHex = "#1A0000", shadeName = "Black Fire", timestamp = now),
            /** Eyebrow Products: Pencils, gels, and powders to fill and shape brows. */
            CosmeticItemEntity(name = "Brow Frame", brand = "KoColor", category = CosmeticCategory.EYEBROW_PRODUCT, colorHex = "#4B3621", shadeName = "Dark Brown", timestamp = now),
            /** False Lashes: Adds volume and length. */
            CosmeticItemEntity(name = "Lash Boost", brand = "KoColor", category = CosmeticCategory.FALSE_LASHES, colorHex = "#000000", shadeName = "Natural Volume", timestamp = now),

            // --- Lips (Color & Texture) ---
            /** Lipstick: Provides pigmented color (matte, satin, cream). */
            CosmeticItemEntity(name = "Nude Silk", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#BC8E8E", shadeName = "Nude", timestamp = now),
            CosmeticItemEntity(name = "Berrie Bliss", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#C71585", shadeName = "Berrie & Pink", timestamp = now),
            CosmeticItemEntity(name = "Crimson Fire", brand = "KoColor", category = CosmeticCategory.LIPSTICK, colorHex = "#B22222", shadeName = "Red", timestamp = now),
            /** Lip Gloss: Adds shine. */
            CosmeticItemEntity(name = "Crystal Shine", brand = "KoColor", category = CosmeticCategory.LIP_GLOSS, colorHex = "#FFC0CB", shadeName = "Clear", timestamp = now),
            /** Lip Liner: Defines and prevents bleeding. */
            CosmeticItemEntity(name = "Define & Stay", brand = "KoColor", category = CosmeticCategory.LIP_LINER, colorHex = "#CD5C5C", shadeName = "Mauve", timestamp = now),
            /** Lip Stain/Tint: Provides long-lasting sheer color. */
            CosmeticItemEntity(name = "Berry Stain", brand = "KoColor", category = CosmeticCategory.LIP_STAIN_TINT, colorHex = "#800000", shadeName = "Deep Berry", timestamp = now),
            /** Lip Plumper: Temporarily increases lip volume. */
            CosmeticItemEntity(name = "Plump & Pout", brand = "KoColor", category = CosmeticCategory.LIP_PLUMPER, colorHex = "#FFE4E1", shadeName = "Volume Up", timestamp = now),

            // --- Tools & Accessories ---
            /** Brushes & Sponges: For application and blending. */
            CosmeticItemEntity(name = "Master Blender", brand = "KoColor", category = CosmeticCategory.BRUSHES_SPONGES, colorHex = "#FF69B4", shadeName = "Pink Sponge", timestamp = now),
            /** Eyelash Curler: Curls lashes. */
            CosmeticItemEntity(name = "Perfect Curl", brand = "KoColor", category = CosmeticCategory.EYELASH_CURLER, colorHex = "#C0C0C0", shadeName = "Silver", timestamp = now),
            /** Cosmetic Organizers: For storage. */
            CosmeticItemEntity(name = "Vanity Tower", brand = "KoColor", category = CosmeticCategory.ORGANIZERS, colorHex = "#FFFFFF", shadeName = "Acrylic", timestamp = now),
            CosmeticItemEntity(name = "Ruby Red", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#E0115F", shadeName = "Ruby", timestamp = now),
            CosmeticItemEntity(name = "Sky Blue", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#87CEEB", shadeName = "Sky", timestamp = now),
            CosmeticItemEntity(name = "Forest Green", brand = "KoColor", category = CosmeticCategory.NAIL_POLISH, colorHex = "#228B22", shadeName = "Forest", timestamp = now)
        )
    }
}
