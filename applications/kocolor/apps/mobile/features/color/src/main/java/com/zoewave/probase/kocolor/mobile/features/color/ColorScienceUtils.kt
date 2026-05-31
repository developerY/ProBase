package com.zoewave.probase.kocolor.mobile.features.color

import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

object ColorScienceUtils {
    fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    fun rgbToHsl(color: Int, outHsl: FloatArray) {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f

        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val delta = max - min

        var h = 0f
        var s = 0f
        val l = (max + min) / 2f

        if (max != min) {
            s = if (l < 0.5f) delta / (max + min) else delta / (2f - max - min)
            h = when (max) {
                r -> (g - b) / delta + (if (g < b) 6 else 0)
                g -> (b - r) / delta + 2
                else -> (r - g) / delta + 4
            }
            h *= 60f
        }

        outHsl[0] = h
        outHsl[1] = s
        outHsl[2] = l
    }

    fun determineTemperature(hsl: FloatArray): String {
        val hue = hsl[0]
        val saturation = hsl[1]
        if (saturation < 0.08f) return "NEUTRAL"
        return if (hue in 0.0f..80.0f || hue in 320.0f..360.0f) "WARM" else "COOL"
    }

    fun calculateContrastLevel(paletteHexes: List<String>): String {
        if (paletteHexes.size < 2) return "Low"
        var minL = 1.0f
        var maxL = 0.0f
        val hsl = FloatArray(3)
        for (hex in paletteHexes) {
            try {
                val color = Color.parseColor(hex)
                rgbToHsl(color, hsl)
                val lightness = hsl[2]
                if (lightness < minL) minL = lightness
                if (lightness > maxL) maxL = lightness
            } catch (e: Exception) { continue }
        }
        val spread = maxL - minL
        return when {
            spread > 0.65f -> "High"
            spread > 0.35f -> "Medium"
            else -> "Low"
        }
    }

    fun mapToSeasonalPalette(hsl: FloatArray, temp: String): String {
        val light = hsl[2]
        val sat = hsl[1]
        return when (temp) {
            "WARM" -> if (light > 0.5f && sat > 0.4f) "SPRING" else "AUTUMN"
            "COOL" -> if (light > 0.5f && sat < 0.6f) "SUMMER" else "WINTER"
            else -> if (light > 0.5f) "SUMMER" else "WINTER"
        }
    }
}
