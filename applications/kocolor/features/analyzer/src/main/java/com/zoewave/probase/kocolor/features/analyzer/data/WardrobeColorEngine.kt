package com.zoewave.probase.kocolor.features.analyzer.data

import android.graphics.Bitmap
import android.graphics.Color
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone
import javax.inject.Inject

/**
 * Orchestrator for the local Wardrobe Color Engine pipeline.
 * Handles the full flow from image pre-processing to fashion signature generation.
 */
class WardrobeColorEngine @Inject constructor(
    private val analyzer: WardrobeAnalyzer
) {

    /**
     * Processes a garment image and generates a rich color signature for a ClothingItem.
     */
    fun processGarment(bitmap: Bitmap, baseItem: ClothingItem): ClothingItem {
        // 1. Pre-processing: Standardize image (Currently assuming bitmap is already prepared)
        val processedBitmap = bitmap // Placeholder for resizing/normalization logic
        
        // 2. Palette Extraction: Local analysis using Android Palette API
        val signature = analyzer.extractColorSignature(processedBitmap)
        
        // 3. Signature Generation: Map raw colors to fashion intelligence
        val temperature = calculateColorTemperature(signature.dominantHex)
        val seasonalPalette = determineSeasonalPalette(signature.dominantHex, temperature)

        return baseItem.copy(
            dominantHex = signature.dominantHex,
            vibrantHex = signature.vibrantHex,
            mutedHex = signature.mutedHex,
            paletteHexes = signature.secondaryPalette,
            colorTemperature = temperature.name,
            seasonalPalette = seasonalPalette.name
        )
    }

    /**
     * Heuristic-based calculation of color temperature (WARM vs COOL).
     */
    private fun calculateColorTemperature(hex: String): Undertone {
        val color = try { Color.parseColor(hex) } catch (e: Exception) { return Undertone.NEUTRAL }
        
        val red = Color.red(color)
        val blue = Color.blue(color)
        
        return when {
            red > blue + 30 -> Undertone.WARM
            blue > red + 30 -> Undertone.COOL
            else -> Undertone.NEUTRAL
        }
    }

    /**
     * Heuristic-based determination of seasonal palette based on dominant color and temperature.
     */
    private fun determineSeasonalPalette(hex: String, temperature: Undertone): SeasonalType {
        val hsv = FloatArray(3)
        try { Color.colorToHSV(Color.parseColor(hex), hsv) } catch (e: Exception) { return SeasonalType.UNKNOWN }
        
        val value = hsv[2] // Brightness/Lightness
        val saturation = hsv[1]

        return when (temperature) {
            Undertone.WARM -> {
                if (value > 0.6f && saturation > 0.5f) SeasonalType.SPRING else SeasonalType.AUTUMN
            }
            Undertone.COOL -> {
                if (value > 0.5f && saturation > 0.5f) SeasonalType.WINTER else SeasonalType.SUMMER
            }
            Undertone.NEUTRAL -> {
                if (value > 0.8f) SeasonalType.SPRING // Light neutral lean
                else if (value < 0.3f) SeasonalType.WINTER // Dark neutral lean
                else SeasonalType.AUTUMN // Earthy neutral lean
            }
            else -> SeasonalType.UNKNOWN
        }
    }
}
