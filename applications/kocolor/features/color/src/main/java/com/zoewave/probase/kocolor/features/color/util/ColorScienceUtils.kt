package com.zoewave.probase.kocolor.features.color.util

import kotlin.math.max
import kotlin.math.min

object ColorScienceUtils {

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

    fun Int.toHexString(): String = String.format("#%06X", 0xFFFFFF and this)

    /**
     * Calculates the distance between two colors in RGB space.
     */
    fun calculateDistance(hex1: String, hex2: String): Double {
        val rgb1 = hexToRgb(hex1) ?: return Double.MAX_VALUE
        val rgb2 = hexToRgb(hex2) ?: return Double.MAX_VALUE
        
        return kotlin.math.sqrt(
            Math.pow((rgb1.first - rgb2.first).toDouble(), 2.0) +
            Math.pow((rgb1.second - rgb2.second).toDouble(), 2.0) +
            Math.pow((rgb1.third - rgb2.third).toDouble(), 2.0)
        )
    }

    /**
     * Returns the complementary color of a given hex color.
     */
    fun getComplementary(hex: String): String {
        val rgb = hexToRgb(hex) ?: return "#FFFFFF"
        val compR = 255 - rgb.first
        val compG = 255 - rgb.second
        val compB = 255 - rgb.third
        return String.format("#%02X%02X%02X", compR, compG, compB)
    }

    /**
     * Returns a list of hex colors representing the analogous harmony.
     */
    fun getAnalogous(hex: String): List<String> {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(hex), hsv)
        
        val h1 = (hsv[0] + 30) % 360
        val h2 = (hsv[0] + 330) % 360
        
        return listOf(
            hsvToHex(h1, hsv[1], hsv[2]),
            hsvToHex(h2, hsv[1], hsv[2])
        )
    }

    /**
     * Returns a list of hex colors representing the triadic harmony.
     */
    fun getTriadic(hex: String): List<String> {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(hex), hsv)
        
        val h1 = (hsv[0] + 120) % 360
        val h2 = (hsv[0] + 240) % 360
        
        return listOf(
            hsvToHex(h1, hsv[1], hsv[2]),
            hsvToHex(h2, hsv[1], hsv[2])
        )
    }

    /**
     * Returns a list of hex colors representing the monochromatic harmony.
     */
    fun getMonochromatic(hex: String): List<String> {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(hex), hsv)
        
        return listOf(
            hsvToHex(hsv[0], (hsv[1] * 0.7f).coerceIn(0f, 1f), (hsv[2] * 0.7f).coerceIn(0f, 1f)),
            hsvToHex(hsv[0], (hsv[1] * 0.3f).coerceIn(0f, 1f), (hsv[2] * 1.2f).coerceIn(0f, 1f))
        )
    }

    private fun hsvToHex(h: Float, s: Float, v: Float): String {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
        return String.format("#%06X", 0xFFFFFF and argb)
    }
}
