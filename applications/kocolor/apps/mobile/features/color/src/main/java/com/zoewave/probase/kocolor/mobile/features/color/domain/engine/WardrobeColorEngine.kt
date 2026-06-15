package com.zoewave.probase.kocolor.mobile.features.color.domain.engine

import android.graphics.Bitmap
import com.zoewave.probase.kocolor.mobile.features.color.util.ColorScienceUtils
import com.zoewave.probase.kocolor.mobile.features.color.util.WardrobeAnalyzer
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrator for the local Wardrobe Color Engine pipeline.
 * Handles the full flow from image pre-processing to fashion signature generation.
 */
@Singleton
class WardrobeColorEngine @Inject constructor(
    private val analyzer: WardrobeAnalyzer
) {

    /**
     * Processes a garment image and generates a rich color signature for a ClothingItem.
     */
    fun processGarment(bitmap: Bitmap, baseItem: ClothingItem): ClothingItem {
        // 1. Palette Extraction: Local analysis using Android Palette API
        val signature = analyzer.extractColorSignature(bitmap)
        
        // 2. Signature Generation: Map raw colors to fashion intelligence
        val (temperature, seasonalPalette) = analyzeColorIntelligence(signature.dominantHex)
        val contrast = analyzer.calculateContrastLevel(signature.allSwatches)
        val group = "${temperature.name} ${seasonalPalette.name}"

        return baseItem.copy(
            dominantHex = signature.dominantHex,
            vibrantHex = signature.vibrantHex,
            mutedHex = signature.mutedHex,
            paletteHexes = signature.secondaryPalette,
            colorTemperature = temperature.name,
            seasonalPalette = seasonalPalette.name,
            contrastLevel = contrast,
            koColorGroup = group,
            colorHex = signature.dominantHex
        )
    }

    private fun analyzeColorIntelligence(hex: String): Pair<Undertone, SeasonalType> {
        val rgb = ColorScienceUtils.hexToRgb(hex) ?: return Undertone.NEUTRAL to SeasonalType.UNKNOWN
        val hsl = ColorScienceUtils.rgbToHsl(rgb.first, rgb.second, rgb.third)
        
        val tempStr = ColorScienceUtils.determineTemperature(hsl)
        val seasonStr = ColorScienceUtils.mapToSeasonalPalette(hsl, tempStr)
        
        val temperature = try { Undertone.valueOf(tempStr) } catch (e: Exception) { Undertone.NEUTRAL }
        val seasonalPalette = try { SeasonalType.valueOf(seasonStr) } catch (e: Exception) { SeasonalType.UNKNOWN }
        
        return temperature to seasonalPalette
    }
}
