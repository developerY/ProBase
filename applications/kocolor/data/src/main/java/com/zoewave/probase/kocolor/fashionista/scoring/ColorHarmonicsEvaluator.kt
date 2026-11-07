package com.zoewave.probase.kocolor.fashionista.scoring

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import com.zoewave.probase.kocolor.data.util.KeywordMatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ColorHarmonicsEvaluator @Inject constructor() {

    fun evaluate(blueprint: StyleBlueprint, context: StyleRequestContext, resolvedClothing: List<ClothingItem>): Float {
        val clothingItems = resolvedClothing
        if (clothingItems.isEmpty()) return 0f

        val palette = blueprint.recommendedPalette
        if (palette.isEmpty()) return 50.0f

        val hslList = palette.mapNotNull { parseToHsl(it) }
        val labList = palette.mapNotNull { parseToLab(it) }

        if (hslList.isEmpty() || labList.isEmpty()) return 50.0f
        
        val minL = labList.minOfOrNull { it[0] } ?: 0f
        val maxL = labList.maxOfOrNull { it[0] } ?: 0f
        val lSpan = maxL - minL

        val isWarmContext = KeywordMatcher.containsWholeWord(context.appearanceProfile.undertone, "warm") ||
            KeywordMatcher.containsWholeWord(context.appearanceProfile.undertone, "golden") ||
            KeywordMatcher.containsWholeWord(context.appearanceProfile.undertone, "peach")
            
        val isCoolContext = KeywordMatcher.containsWholeWord(context.appearanceProfile.undertone, "cool") ||
            KeywordMatcher.containsWholeWord(context.appearanceProfile.undertone, "pink") ||
            KeywordMatcher.containsWholeWord(context.appearanceProfile.undertone, "blue")

        // Score modifiers widened to +/- 50 to meet the [0, 100] spread
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
                    distance in 0f..30f -> deltaScore += 2.0f
                    // Complementary: 150° - 210°
                    distance in 150f..210f -> deltaScore += 1.5f
                    // Clash Penalty: 70° - 110°
                    distance in 70f..110f -> {
                        // Neutral Exception
                        if (isNeutral1 || isNeutral2) {
                            deltaScore += 0.5f 
                        } else {
                            deltaScore -= 2.0f // Unmitigated clash penalty
                        }
                    }
                    else -> deltaScore += 0.5f
                }
                pairHarmonyScore += deltaScore
                pairCount++
            }
        }

        val avgPairHarmony = if (pairCount > 0) pairHarmonyScore / pairCount else 0f
        var modifier = avgPairHarmony * 25.0f

        // Single chroma tonal evaluation
        if (hslList.filter { !isNeutralColor(it) }.size <= 1) {
            if (lSpan < 15f) {
                modifier += 5f // Flat neutral (scores ~55)
            } else if (lSpan in 40f..60f) {
                modifier += 35f // Exceptional single-color tonal separation
            } else {
                modifier += 15f
            }
        } else {
            // Apply scale reduction so poor combinations properly hit the low end of the spectrum
            modifier = modifier * 1.5f - 40.0f
        }
        
        // Context undertone alignment bonus
        var warmCount = 0
        var coolCount = 0
        resolvedClothing.forEach { item ->
            val temp = item.colorTemperature ?: "Neutral"
            if (KeywordMatcher.containsWholeWord(temp, "warm")) warmCount++
            if (KeywordMatcher.containsWholeWord(temp, "cool")) coolCount++
        }
        
        if (isWarmContext && warmCount > coolCount) modifier += 15f
        if (isCoolContext && coolCount > warmCount) modifier += 15f
        if (isWarmContext && coolCount > warmCount + 1) modifier -= 15f
        if (isCoolContext && warmCount > coolCount + 1) modifier -= 15f

        // Map baseline score (50.0) + weighted pair harmony/undertone to 0 - 100 scale
        return (50.0f + modifier).coerceIn(0.0f, 100.0f)
    }

    private fun calculateHueDistance(hue1: Float, hue2: Float): Float {
        val diff = abs(hue1 - hue2)
        return minOf(diff, 360f - diff)
    }

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
    
    private fun parseToLab(hex: String): FloatArray? {
        return try {
            val colorInt = Color.parseColor(hex)
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(colorInt, lab)
            floatArrayOf(lab[0].toFloat(), lab[1].toFloat(), lab[2].toFloat())
        } catch (e: Exception) {
            null
        }
    }
}
