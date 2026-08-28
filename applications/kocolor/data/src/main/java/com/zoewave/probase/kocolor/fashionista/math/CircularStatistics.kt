package com.zoewave.probase.kocolor.fashionista.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object CircularStatistics {

    /**
     * Calculates chroma-weighted circular mean hue angle in degrees [0.0, 360.0).
     */
    fun chromaWeightedCircularMeanHue(huesDeg: DoubleArray, chromas: DoubleArray): Double {
        if (huesDeg.isEmpty() || huesDeg.size != chromas.size) return 0.0

        var sumX = 0.0
        var sumY = 0.0
        var totalChroma = 0.0

        for (i in huesDeg.indices) {
            val chroma = chromas[i]
            // Only non-neutral colors (significant chroma) distort the hue vector
            if (chroma >= 10.0) {
                val rad = Math.toRadians(huesDeg[i])
                sumX += chroma * cos(rad)
                sumY += chroma * sin(rad)
                totalChroma += chroma
            }
        }

        if (totalChroma == 0.0) return 0.0

        var meanRad = atan2(sumY, sumX)
        var meanHue = Math.toDegrees(meanRad)
        if (meanHue < 0.0) meanHue += 360.0
        return meanHue
    }

    /**
     * Calculates circular variance in [0.0, 1.0] for a set of angles in degrees.
     */
    fun circularVariance(anglesDeg: DoubleArray): Double {
        if (anglesDeg.isEmpty()) return 0.0
        var sumX = 0.0
        var sumY = 0.0
        for (angle in anglesDeg) {
            val rad = Math.toRadians(angle)
            sumX += cos(rad)
            sumY += sin(rad)
        }
        val n = anglesDeg.size.toDouble()
        val meanResultantLength = Math.hypot(sumX, sumY) / n
        return (1.0 - meanResultantLength).coerceIn(0.0, 1.0)
    }
}
