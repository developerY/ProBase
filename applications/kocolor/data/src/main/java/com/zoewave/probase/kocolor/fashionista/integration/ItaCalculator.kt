package com.zoewave.probase.kocolor.fashionista.integration

import com.zoewave.probase.kocolor.fashionista.color.LabColor
import kotlin.math.atan2

object ItaCalculator {

    /**
     * Calculates Individual Typology Angle (ITA):
     * ITA = [arctan((L* - 50) / b*)] * (180 / PI)
     */
    fun calculateIta(skinLab: LabColor): Double {
        if (skinLab.b == 0.0) return 0.0
        val rad = atan2(skinLab.l - 50.0, skinLab.b)
        return Math.toDegrees(rad)
    }
}
