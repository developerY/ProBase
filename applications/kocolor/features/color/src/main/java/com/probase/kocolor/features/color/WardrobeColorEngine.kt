package com.probase.kocolor.features.color

import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.probase.kocolor.model.ClothingItem

class WardrobeColorEngine(context: Context) {
    private val analyzer = WardrobeAnalyzer(context)

    fun processGarment(id: String, imageUriStr: String): ClothingItem {
        val uri = Uri.parse(imageUriStr)
        val rawAnalysis = analyzer.analyzeImage(uri)

        if (rawAnalysis == null) {
            return ClothingItem(
                id = id, imageUri = imageUriStr, dominantHex = "#FFFFFF",
                vibrantHex = null, mutedHex = null, paletteHexes = emptyList(),
                koColorGroup = "Unknown", contrastLevel = "Low",
                colorTemperature = "NEUTRAL", seasonalPalette = "NEUTRAL"
            )
        }

        val hsl = FloatArray(3)
        try {
            val dominantIntColor = Color.parseColor(rawAnalysis.dominantHex)
            ColorScienceUtils.rgbToHsl(dominantIntColor, hsl)
        } catch (e: Exception) {
            hsl[0] = 0f; hsl[1] = 0f; hsl[2] = 1f
        }

        val temperature = ColorScienceUtils.determineTemperature(hsl)
        val contrast = ColorScienceUtils.calculateContrastLevel(rawAnalysis.allSwatches)
        val seasonalClassification = ColorScienceUtils.mapToSeasonalPalette(hsl, temperature)

        return ClothingItem(
            id = id, imageUri = imageUriStr, dominantHex = rawAnalysis.dominantHex,
            vibrantHex = rawAnalysis.vibrantHex, mutedHex = rawAnalysis.mutedHex,
            paletteHexes = rawAnalysis.allSwatches, koColorGroup = seasonalClassification,
            contrastLevel = contrast, colorTemperature = temperature, seasonalPalette = seasonalClassification
        )
    }
}
