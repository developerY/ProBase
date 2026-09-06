package com.zoewave.probase.kocolor.fashionista.scoring

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ColorHarmonicsEvaluator @Inject constructor() {

    fun evaluate(blueprint: StyleBlueprint): Float {
        val palette = blueprint.recommendedPalette
        if (palette.isEmpty()) return 75.0f

        val hslList = palette.mapNotNull { parseToHsl(it) }
        if (hslList.isEmpty()) return 75.0f
        if (hslList.size == 1) return 85.0f

        var pairHarmonyScore = 0f
        var pairCount = 0

        for (i in hslList.indices) {
            for (j in i + 1 until hslList.size) {
                val hsl1 = hslList[i]
                val hsl2 = hslList[j]

                val hue1 = hsl1[0]
                val hue2 = hsl2[0]
                val distance = calculateHueDistance(hue1, hue2)

                val isNeutral1 = isNeutralColor(hsl1)
                val isNeutral2 = isNeutralColor(hsl2)

                var deltaScore = 0f

                when {
                    // Monochromatic / Analogous: 0° - 30°
                    distance in 0f..30f -> {
                        deltaScore += 2.0f
                    }
                    // Complementary: 150° - 210°
                    distance in 150f..210f -> {
                        deltaScore += 1.5f
                    }
                    // Clash Penalty: 70° - 110°
                    distance in 70f..110f -> {
                        // Neutral Exception: If either color is black, white, pale, or muted, neutrals do not clash
                        if (isNeutral1 || isNeutral2) {
                            deltaScore += 0.5f // Neutral pairing credit
                        } else {
                            deltaScore -= 2.0f // Unmitigated clash penalty
                        }
                    }
                    else -> {
                        // Triadic or soft transition
                        deltaScore += 0.5f
                    }
                }

                pairHarmonyScore += deltaScore
                pairCount++
            }
        }

        val avgPairHarmony = if (pairCount > 0) pairHarmonyScore / pairCount else 0f

        // Map baseline score (78.0) + weighted pair harmony to 0 - 100 scale
        return (78.0f + avgPairHarmony * 10.0f).coerceIn(0.0f, 100.0f)
    }

    /**
     * Shortest angular distance between two hues on 360-degree color wheel.
     */
    private fun calculateHueDistance(hue1: Float, hue2: Float): Float {
        val diff = abs(hue1 - hue2)
        return minOf(diff, 360f - diff)
    }

    /**
     * Neutral Exception Check:
     * Lightness < 0.2 (black/dark), Lightness > 0.8 (white/pale), or Saturation < 0.2 (gray/muted).
     */
    private fun isNeutralColor(hsl: FloatArray): Boolean {
        val saturation = hsl[1]
        val lightness = hsl[2]
        return lightness < 0.2f || lightness > 0.8f || saturation < 0.2f
    }

    private fun parseToHsl(hex: String): FloatArray? {
        return try {
            val colorInt = Color.parseColor(hex)
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(colorInt, hsl)
            hsl
        } catch (e: Exception) {
            null
        }
    }
}
