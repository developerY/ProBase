package com.zoewave.probase.kocolor.data.util

import kotlin.math.max
import kotlin.math.min

object ColorScience {

    data class Hsl(val h: Float, val s: Float, val l: Float)

    fun rgbToHsl(rInt: Int, gInt: Int, bInt: Int): Hsl {
        val r = rInt / 255f
        val g = gInt / 255f
        val b = bInt / 255f

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

        return Hsl(h, s, l)
    }

    fun hexToRgb(hex: String): Triple<Int, Int, Int>? {
        return try {
            val color = if (hex.startsWith("#")) hex.substring(1) else hex
            val r = color.substring(0, 2).toInt(16)
            val g = color.substring(2, 4).toInt(16)
            val b = color.substring(4, 6).toInt(16)
            Triple(r, g, b)
        } catch (e: Exception) {
            null
        }
    }

    fun determineTemperature(hsl: Hsl): String {
        if (hsl.s < 0.08f) return "NEUTRAL"
        return if (hsl.h in 0.0f..80.0f || hsl.h in 320.0f..360.0f) "WARM" else "COOL"
    }

    fun mapToSeasonalPalette(hsl: Hsl, temp: String): String {
        return when (temp) {
            "WARM" -> if (hsl.l > 0.5f && hsl.s > 0.4f) "SPRING" else "AUTUMN"
            "COOL" -> if (hsl.l > 0.5f && hsl.s < 0.6f) "SUMMER" else "WINTER"
            else -> if (hsl.l > 0.5f) "SUMMER" else "WINTER"
        }
    }
}
