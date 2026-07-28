package com.zoewave.probase.kocolor.features.colors.util

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.kocolor.features.colors.domain.model.HsvValue
import com.zoewave.probase.kocolor.features.colors.domain.model.LabValue
import com.zoewave.probase.kocolor.features.colors.domain.model.PantoneMatch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ColorScienceUtils {

    fun hexToRgb(hex: String): Triple<Int, Int, Int>? {
        return try {
            val color = Color.parseColor(if (hex.startsWith("#")) hex else "#$hex")
            Triple(Color.red(color), Color.green(color), Color.blue(color))
        } catch (e: Exception) {
            null
        }
    }

    fun rgbToLab(r: Int, g: Int, b: Int): LabValue {
        val outLab = DoubleArray(3)
        ColorUtils.RGBToLAB(r, g, b, outLab)
        return LabValue(outLab[0], outLab[1], outLab[2])
    }

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

    fun hexToHsv(hex: String): HsvValue? {
        return try {
            val color = Color.parseColor(if (hex.startsWith("#")) hex else "#$hex")
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            HsvValue(hsv[0], hsv[1], hsv[2])
        } catch (e: Exception) {
            null
        }
    }

    fun findNearestPantone(hex: String): PantoneMatch {
        // Mock lookup - In a real app, this would query a local database of 2000+ Pantone swatches
        // For demonstration, we'll return a match based on the hue
        val hsv = FloatArray(3)
        try {
            Color.colorToHSV(Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
        } catch (e: Exception) {
            return PantoneMatch("UNKNOWN", "Neutral Gray", 0.0)
        }
        
        return when {
            hsv[0] in 0f..20f -> PantoneMatch("18-1662", "Flame Scarlet", 1.2)
            hsv[0] in 200f..250f -> PantoneMatch("19-4052", "Classic Blue", 0.8)
            hsv[0] in 50f..70f -> PantoneMatch("13-0647", "Illuminating", 1.5)
            else -> PantoneMatch("19-0000", "Obsidian", 2.0)
        }
    }

    fun calculateDistance(hex1: String, hex2: String): Double {
        val rgb1 = hexToRgb(hex1) ?: return Double.MAX_VALUE
        val rgb2 = hexToRgb(hex2) ?: return Double.MAX_VALUE
        
        return sqrt(
            Math.pow((rgb1.first - rgb2.first).toDouble(), 2.0) +
            Math.pow((rgb1.second - rgb2.second).toDouble(), 2.0) +
            Math.pow((rgb1.third - rgb2.third).toDouble(), 2.0)
        )
    }

    fun getComplementary(hex: String): String {
        val rgb = hexToRgb(hex) ?: return "#FFFFFF"
        val compR = 255 - rgb.first
        val compG = 255 - rgb.second
        val compB = 255 - rgb.third
        return String.format("#%02X%02X%02X", compR, compG, compB)
    }

    fun getAnalogous(hex: String): List<String> {
        val hsv = FloatArray(3)
        try {
            Color.colorToHSV(Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
        } catch (e: Exception) { return emptyList() }
        
        val h1 = (hsv[0] + 30) % 360
        val h2 = (hsv[0] + 330) % 360
        
        return listOf(
            hsvToHex(h1, hsv[1], hsv[2]),
            hsvToHex(h2, hsv[1], hsv[2])
        )
    }

    fun getTriadic(hex: String): List<String> {
        val hsv = FloatArray(3)
        try {
            Color.colorToHSV(Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
        } catch (e: Exception) { return emptyList() }
        
        val h1 = (hsv[0] + 120) % 360
        val h2 = (hsv[0] + 240) % 360
        
        return listOf(
            hsvToHex(h1, hsv[1], hsv[2]),
            hsvToHex(h2, hsv[1], hsv[2])
        )
    }

    fun getMonochromatic(hex: String): List<String> {
        val hsv = FloatArray(3)
        try {
            Color.colorToHSV(Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
        } catch (e: Exception) { return emptyList() }
        
        return listOf(
            hsvToHex(hsv[0], (hsv[1] * 0.7f).coerceIn(0f, 1f), (hsv[2] * 0.7f).coerceIn(0f, 1f)),
            hsvToHex(hsv[0], (hsv[1] * 0.3f).coerceIn(0f, 1f), (hsv[2] * 1.2f).coerceIn(0f, 1f))
        )
    }

    private fun hsvToHex(h: Float, s: Float, v: Float): String {
        val argb = Color.HSVToColor(floatArrayOf(h, s, v))
        return String.format("#%06X", 0xFFFFFF and argb)
    }
}
