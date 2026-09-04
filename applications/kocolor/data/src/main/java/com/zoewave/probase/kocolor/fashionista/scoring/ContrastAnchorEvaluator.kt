package com.zoewave.probase.kocolor.fashionista.scoring

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContrastAnchorEvaluator @Inject constructor() {

    fun evaluate(blueprint: StyleBlueprint, context: StyleRequestContext): Float {
        val palette = blueprint.recommendedPalette
        if (palette.isEmpty()) return 75.0f

        val lightnessList = palette.mapNotNull { parseLightness(it) }
        if (lightnessList.isEmpty()) return 75.0f

        val minLightness = lightnessList.minOrNull() ?: 0.5f
        val maxLightness = lightnessList.maxOrNull() ?: 0.5f
        val lightnessDelta = maxLightness - minLightness
        val averageLightness = lightnessList.average().toFloat()

        var scoreModifier = 0.0f

        val appearance = context.appearanceProfile
        val contrastProfile = appearance.contrast
        val depthProfile = appearance.depth

        // 1. Evaluate Outfit Contrast vs. User Contrast
        when {
            contrastProfile.contains("High", ignoreCase = true) -> {
                if (lightnessDelta > 0.6f) {
                    scoreModifier += 15.0f
                } else {
                    scoreModifier -= 10.0f
                }
            }
            contrastProfile.contains("Low", ignoreCase = true) || contrastProfile.contains("Muted", ignoreCase = true) -> {
                if (lightnessDelta < 0.4f) {
                    scoreModifier += 15.0f
                } else {
                    scoreModifier -= 10.0f
                }
            }
            else -> { // Balanced / Medium
                if (lightnessDelta in 0.3f..0.7f) {
                    scoreModifier += 15.0f
                }
            }
        }

        // 2. Evaluate Washout / Depth Penalties
        val isLightUser = depthProfile.contains("Light", ignoreCase = true)
        val isDeepUser = depthProfile.contains("Deep", ignoreCase = true)

        when {
            // Light Depth User + Dark Outfit: Overpowering Penalty (-15.0f)
            isLightUser && averageLightness < 0.25f -> {
                scoreModifier -= 15.0f
            }
            // Deep Depth User + Pale Outfit: Washout Penalty (-15.0f)
            isDeepUser && averageLightness > 0.75f -> {
                scoreModifier -= 15.0f
            }
            // Compatible Depth Match (+10.0f)
            else -> {
                scoreModifier += 10.0f
            }
        }

        return (70.0f + scoreModifier).coerceIn(0.0f, 100.0f)
    }

    private fun parseLightness(hex: String): Float? {
        return try {
            val colorInt = Color.parseColor(hex)
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(colorInt, hsl)
            hsl[2] // Lightness is index 2 in HSL (0.0 to 1.0)
        } catch (e: Exception) {
            null
        }
    }
}
