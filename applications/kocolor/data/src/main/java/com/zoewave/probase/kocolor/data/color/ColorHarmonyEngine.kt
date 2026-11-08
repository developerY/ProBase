package com.zoewave.probase.kocolor.data.color

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.usecase.ColorTelemetry
import com.zoewave.probase.kocolor.fashionista.color.ColorSpaceConverter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Singleton
class ColorHarmonyEngine @Inject constructor() {

    /**
     * Converts a HEX color to CIELAB L*C*h° color space via ColorSpaceConverter.
     */
    fun hexToLCh(hex: String): LCh {
        val lch = ColorSpaceConverter.hexToLCh(hex)
        return LCh(lch.l.toFloat(), lch.c.toFloat(), lch.h.toFloat())
    }

    /**
     * Calculates a composite color profile using chroma-weighted circular statistics for hues.
     * Low-chroma neutrals (C* < 10) bypass hue vector distortion but contribute to contrast.
     */
    fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile {
        if (items.isEmpty()) return CompositeColorProfile()

        var sumX = 0f
        var sumY = 0f
        var totalChromaWeight = 0f
        var minL = 100f
        var maxL = 0f

        items.forEach { item ->
            val lch = hexToLCh(item.colorHex)
            if (lch.l < minL) minL = lch.l
            if (lch.l > maxL) maxL = lch.l

            // Only factor into dominant hue if chroma is significant (non-neutral)
            if (lch.c >= 10f) {
                val rad = Math.toRadians(lch.h.toDouble())
                val weight = lch.c
                sumX += (weight * cos(rad)).toFloat()
                sumY += (weight * sin(rad)).toFloat()
                totalChromaWeight += weight
            }
        }

        val dominantHues = if (totalChromaWeight > 0f) {
            var meanRad = atan2(sumY.toDouble(), sumX.toDouble())
            var meanHue = Math.toDegrees(meanRad).toFloat()
            if (meanHue < 0) meanHue += 360f
            listOf(meanHue)
        } else {
            emptyList()
        }

        val contrastRange = (maxL - minL) / 100f

        return CompositeColorProfile(
            dominantHues = dominantHues,
            secondaryHues = emptyList(),
            temperatureDistribution = emptyMap(),
            contrastRange = contrastRange
        )
    }

    /**
     * Continuous compatibility scoring (0.0 to 1.0) between a candidate LCh color and a composite profile.
     */
    fun scoreCandidate(
        candidateLCh: LCh,
        composite: CompositeColorProfile,
        telemetry: ColorTelemetry
    ): Float {
        var score = 0.5f

        // 1. Hue Geometry Score (if dominant hue exists)
        if (composite.dominantHues.isNotEmpty()) {
            val domHue = composite.dominantHues.first()
            val hueDelta = abs(candidateLCh.h - domHue)
            val minHueDelta = Math.min(hueDelta, 360f - hueDelta)

            val hueHarmonyScore = when {
                minHueDelta <= 15f -> 1.0f  // Monochromatic / Analogous
                abs(minHueDelta - 180f) <= 20f -> 0.95f // Complementary
                abs(minHueDelta - 120f) <= 20f -> 0.85f // Triadic
                abs(minHueDelta - 30f) <= 15f -> 0.9f  // Analogous
                else -> 0.6f
            }
            score += hueHarmonyScore * 0.35f
        } else {
            score += 0.35f // Neutrals anchor bonus
        }

        // 2. Contrast Balancing Score
        val lightnessDelta = candidateLCh.l / 100f
        val contrastBonus = if (telemetry.contrastScore > 0.6f) {
            // User needs high contrast
            if (lightnessDelta > 0.5f) 0.25f else 0.1f
        } else {
            0.15f
        }
        score += contrastBonus

        return score.coerceIn(0.0f, 1.0f)
    }

    /**
     * Perceptual color distance using simplified Delta E metric.
     */




}
