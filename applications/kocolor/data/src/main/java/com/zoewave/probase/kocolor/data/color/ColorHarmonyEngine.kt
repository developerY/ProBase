package com.zoewave.probase.kocolor.data.color

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.ColorTelemetry
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class ColorHarmonyEngine @Inject constructor() {

    /**
     * Converts a HEX color to CIELAB L*C*h° color space using pure Kotlin math
     * to ensure unit tests run in JVM environments without needing Android runtime mocks.
     */
    fun hexToLCh(hex: String): LCh {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLongOrNull(16)?.toInt() ?: 0x808080

        val r = ((colorInt shr 16) and 0xFF) / 255f
        val g = ((colorInt shr 8) and 0xFF) / 255f
        val b = (colorInt and 0xFF) / 255f

        // Convert RGB to XYZ
        fun pivotRgb(c: Float): Double {
            return if (c > 0.04045f) Math.pow((c + 0.055) / 1.055, 2.4) else (c / 12.92)
        }

        val x = (pivotRgb(r) * 0.4124 + pivotRgb(g) * 0.3576 + pivotRgb(b) * 0.1805) / 0.95047
        val y = (pivotRgb(r) * 0.2126 + pivotRgb(g) * 0.7152 + pivotRgb(b) * 0.0722) / 1.00000
        val z = (pivotRgb(r) * 0.0193 + pivotRgb(g) * 0.1192 + pivotRgb(b) * 0.9505) / 1.08883

        fun pivotXyz(c: Double): Double {
            return if (c > 0.008856) Math.cbrt(c) else (7.787 * c) + (16.0 / 116.0)
        }

        val fx = pivotXyz(x)
        val fy = pivotXyz(y)
        val fz = pivotXyz(z)

        val l = ((116.0 * fy) - 16.0).coerceIn(0.0, 100.0).toFloat()
        val labA = (500.0 * (fx - fy)).toFloat()
        val labB = (200.0 * (fy - fz)).toFloat()

        val c = sqrt(labA * labA + labB * labB)
        var h = Math.toDegrees(atan2(labB.toDouble(), labA.toDouble())).toFloat()
        if (h < 0) h += 360f

        return LCh(l, c, h)
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
    fun calculateDeltaE(lch1: LCh, lch2: LCh): Float {
        val dL = lch1.l - lch2.l
        val dC = lch1.c - lch2.c
        val dH = abs(lch1.h - lch2.h)
        return sqrt(dL * dL + dC * dC + dH * dH)
    }
}
