package com.zoewave.probase.kocolor.data.color

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.features.ai.firebase.models.Appearance
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ColorHarmonyEngine @Inject constructor() {

    /**
     * Calculates a continuous compatibility score (0.0 to 1.0) between an anchor and a candidate garment.
     */
    suspend fun calculateCompatibility(
        anchor: HSL,
        candidate: HSL,
        appearance: Appearance
    ): Float {
        var score = 0f
        
        // 1. Hue Relationship (Weighted at 40%)
        val hueDistance = abs(anchor.h - candidate.h)
        val hueScore = when {
            hueDistance < 15f || hueDistance > 345f -> 1.0f // Monochromatic
            abs(hueDistance - 180f) < 15f -> 0.9f    // Complementary
            abs(hueDistance - 30f) < 15f || abs(hueDistance - 330f) < 15f -> 0.8f // Analogous
            else -> 0.5f
        }
        score += hueScore * 0.4f

        // 2. Perceptual Clash Prevention (Delta E stub - Weighted at 20%)
        // Real implementation would use CIELAB, for now using HSL distance
        val perceptualScore = if (abs(anchor.l - candidate.l) > 0.1f || abs(anchor.h - candidate.h) > 20f) 1.0f else 0.4f
        score += perceptualScore * 0.2f

        // 3. Contrast Balancing (Weighted at 20%)
        // If user has high contrast appearance, favor high lightness delta
        val lightnessDelta = abs(anchor.l - candidate.l)
        val contrastScore = if (appearance.contrast.contains("High", ignoreCase = true)) {
            if (lightnessDelta > 0.4f) 1.0f else 0.4f
        } else {
            if (lightnessDelta < 0.4f) 1.0f else 0.6f
        }
        score += contrastScore * 0.2f

        // 4. Saturation & Temperature Alignment (Weighted at 20%)
        val saturationMatch = 1.0f - abs(anchor.s - candidate.s)
        score += saturationMatch * 0.2f

        return score.coerceIn(0.0f, 1.0f)
    }

    /**
     * Simple HEX to HSL conversion helper.
     */
    fun hexToHsl(hex: String): HSL {
        val color = try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.GRAY
        }
        val outHsl = FloatArray(3)
        ColorUtils.colorToHSL(color, outHsl)
        return HSL(outHsl[0], outHsl[1], outHsl[2])
    }
}
