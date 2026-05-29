package com.zoewave.probase.kocolor.db.data

import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.*

object CosmeticDefaults {
    fun getDefaultCosmetics(): List<CosmeticItemEntity> {
        val now = System.currentTimeMillis()
        val month = 30L * 24 * 60 * 60 * 1000
        
        return listOf(
            // --- Skincare & Prep ---
            CosmeticItemEntity(
                name = "Silk Primer", 
                brand = "KoColor", 
                macroCategory = MacroCategory.PREP,
                microCategory = MicroCategory.PRIMER,
                formulation = Formulation.LIQUID,
                chemistryBase = ChemistryBase.SILICONE,
                finish = Finish.SATIN,
                colorHex = "#F8F0E3", 
                shadeName = "Translucent", 
                timestamp = now,
                price = 28.0,
                paoMonths = 12,
                usageCount = 45,
                isOpened = true,
                openedDate = now - (3 * month),
                imageUrl = "https://images.unsplash.com/photo-1596704017254-9b121068fb31?w=400&q=80"
            ),
            
            // --- Complexion ---
            CosmeticItemEntity(
                name = "Cool Ivory", 
                brand = "KoColor", 
                macroCategory = MacroCategory.COMPLEXION,
                microCategory = MicroCategory.FOUNDATION,
                formulation = Formulation.LIQUID,
                chemistryBase = ChemistryBase.WATER,
                finish = Finish.NATURAL,
                coverage = Coverage.MEDIUM,
                colorHex = "#FAD4D4", 
                shadeName = "Cool", 
                timestamp = now,
                price = 42.0,
                paoMonths = 24,
                usageCount = 120,
                isOpened = true,
                openedDate = now - (23 * month),
                imageUrl = "https://images.unsplash.com/photo-1625093742435-6fa192b6fb10?w=400&q=80",
                volume = "42ml",
                amountRemaining = 14.7,
                amountPerUse = 0.35
            ),
            CosmeticItemEntity(
                name = "Perfect Hide", 
                brand = "KoColor", 
                macroCategory = MacroCategory.COMPLEXION,
                microCategory = MicroCategory.CONCEALER,
                formulation = Formulation.CREAM,
                chemistryBase = ChemistryBase.SILICONE,
                finish = Finish.MATTE,
                coverage = Coverage.FULL,
                colorHex = "#F5F5DC", 
                shadeName = "Light", 
                timestamp = now
            ),

            // --- Color & Dimension ---
            CosmeticItemEntity(
                name = "Rosy Cheek", 
                brand = "KoColor", 
                macroCategory = MacroCategory.DIMENSION,
                microCategory = MicroCategory.BLUSH,
                formulation = Formulation.POWDER,
                finish = Finish.RADIANT,
                colorHex = "#FFB6C1", 
                shadeName = "Rose", 
                timestamp = now
            ),
            CosmeticItemEntity(
                name = "Golden Glow", 
                brand = "KoColor", 
                macroCategory = MacroCategory.DIMENSION,
                microCategory = MicroCategory.HIGHLIGHTER,
                formulation = Formulation.POWDER,
                finish = Finish.GLITTER,
                colorHex = "#FAFAD2", 
                shadeName = "Gold", 
                timestamp = now
            ),

            // --- Eyes & Brows ---
            CosmeticItemEntity(
                name = "Midnight Black", 
                brand = "KoColor", 
                macroCategory = MacroCategory.EYES,
                microCategory = MicroCategory.EYELINER,
                formulation = Formulation.PENCIL,
                finish = Finish.MATTE,
                colorHex = "#000000", 
                shadeName = "Black", 
                timestamp = now
            ),
            CosmeticItemEntity(
                name = "Deep Forest", 
                brand = "KoColor", 
                macroCategory = MacroCategory.EYES,
                microCategory = MicroCategory.MASCARA,
                formulation = Formulation.LIQUID,
                finish = Finish.MATTE,
                colorHex = "#002B1B", 
                shadeName = "Black Forest", 
                timestamp = now
            ),

            // --- Lips ---
            CosmeticItemEntity(
                name = "Crimson Fire", 
                brand = "KoColor", 
                macroCategory = MacroCategory.LIPS,
                microCategory = MicroCategory.LIPSTICK,
                formulation = Formulation.STICK,
                finish = Finish.MATTE,
                colorHex = "#B22222", 
                shadeName = "Red", 
                timestamp = now
            ),
            CosmeticItemEntity(
                name = "Crystal Shine", 
                brand = "KoColor", 
                macroCategory = MacroCategory.LIPS,
                microCategory = MicroCategory.LIP_GLOSS,
                formulation = Formulation.LIQUID,
                finish = Finish.GLOSSY,
                colorHex = "#FFC0CB", 
                shadeName = "Clear", 
                timestamp = now
            )
        )
    }
}
