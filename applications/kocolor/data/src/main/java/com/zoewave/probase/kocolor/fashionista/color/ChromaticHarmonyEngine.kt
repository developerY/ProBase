package com.zoewave.probase.kocolor.fashionista.color

import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import com.zoewave.probase.kocolor.fashionista.math.CircularStatistics
import com.zoewave.probase.kocolor.fashionista.math.Distance
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ChromaticHarmonyEngine @Inject constructor() {

    fun evaluate(colorsHex: List<String>): FeatureValue {
        if (colorsHex.isEmpty()) {
            return FeatureValue(value = 0.0, availability = 0.0)
        }

        val lchColors = colorsHex.map { ColorSpaceConverter.hexToLCh(it) }
        val chromas = lchColors.map { it.c }.toDoubleArray()
        val hues = lchColors.map { it.h }.toDoubleArray()

        // 1. Hue Dispersion Score
        val meanHue = CircularStatistics.chromaWeightedCircularMeanHue(hues, chromas)
        val hueVar = CircularStatistics.circularVariance(hues)
        val hueHarmonyScore = (1.0 - hueVar * 0.5).coerceIn(0.0, 1.0)

        // 2. Pairwise Delta E 00 Score
        var deltaESum = 0.0
        var pairs = 0
        val labColors = colorsHex.map { ColorSpaceConverter.hexToLab(it) }

        for (i in 0 until labColors.size - 1) {
            for (j in i + 1 until labColors.size) {
                val c1 = labColors[i]
                val c2 = labColors[j]
                val dE = Distance.ciede2000(c1.l, c1.a, c1.b, c2.l, c2.a, c2.b)
                deltaESum += dE
                pairs++
            }
        }

        val avgDeltaE = if (pairs > 0) deltaESum / pairs else 0.0
        // Optimal aesthetic delta E range in fashion is typically 15-45
        val deltaEScore = when {
            avgDeltaE in 10.0..50.0 -> 0.9
            avgDeltaE < 10.0 -> 0.75 // Very low contrast
            else -> 0.6 // High contrast / potentially clashing
        }

        val finalScore = (hueHarmonyScore * 0.5 + deltaEScore * 0.5).coerceIn(0.0, 1.0)
        val availability = if (colorsHex.size >= 2) 1.0 else 0.5

        return FeatureValue(value = finalScore, availability = availability)
    }
}
