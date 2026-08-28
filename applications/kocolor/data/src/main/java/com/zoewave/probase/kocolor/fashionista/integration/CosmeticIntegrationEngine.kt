package com.zoewave.probase.kocolor.fashionista.integration

import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.fashionista.color.ColorSpaceConverter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class CosmeticIntegrationEngine @Inject constructor() {

    /**
     * Calculates Michelson facial feature contrast:
     * Cf = |L_skin - L_feature| / (L_skin + L_feature)
     */
    fun calculateMichelsonContrast(skinL: Double, featureL: Double): Double {
        val sum = skinL + featureL
        return if (sum > 0.0) {
            abs(skinL - featureL) / sum
        } else {
            0.0
        }
    }

    fun evaluateCosmetics(cosmetics: List<CosmeticItem>): Double {
        if (cosmetics.isEmpty()) return 0.5
        val labColors = cosmetics.map { ColorSpaceConverter.hexToLab(it.colorHex) }
        val avgL = labColors.map { it.l }.average()
        return (avgL / 100.0).coerceIn(0.0, 1.0)
    }
}
