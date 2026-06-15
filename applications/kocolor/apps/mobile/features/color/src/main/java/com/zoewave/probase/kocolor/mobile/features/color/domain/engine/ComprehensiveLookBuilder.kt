package com.zoewave.probase.kocolor.mobile.features.color.domain.engine

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.HarmonizedLook
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComprehensiveLookBuilder @Inject constructor() {

    /**
     * Cross-references a garment against available cosmetics to build harmonized looks.
     */
    fun buildLook(garment: ClothingItem, availableCosmetics: List<CosmeticItem>): HarmonizedLook {
        // Filter cosmetics by category for specific roles
        val lipOptions = availableCosmetics.filter { it.microCategory.macro == com.zoewave.probase.core.model.ritual.MacroCategory.LIPS }
        val eyeOptions = availableCosmetics.filter { it.microCategory.macro == com.zoewave.probase.core.model.ritual.MacroCategory.EYES }
        val cheekOptions = availableCosmetics.filter { it.microCategory.macro == com.zoewave.probase.core.model.ritual.MacroCategory.DIMENSION }

        // In a production engine, we would match based on seasonalPalette and colorTemperature
        // For now, we take the most relevant items
        return HarmonizedLook(
            targetGarment = garment,
            recommendedLip = lipOptions.maxByOrNull { it.usageCount } ?: lipOptions.firstOrNull(),
            recommendedEye = eyeOptions.maxByOrNull { it.usageCount } ?: eyeOptions.firstOrNull(),
            recommendedCheek = cheekOptions.maxByOrNull { it.usageCount } ?: cheekOptions.firstOrNull()
        )
    }
}
