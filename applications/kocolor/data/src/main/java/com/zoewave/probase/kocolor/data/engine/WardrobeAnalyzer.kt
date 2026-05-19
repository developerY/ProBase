package com.zoewave.probase.kocolor.data.engine

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import javax.inject.Inject

/**
 * Local processing framework for extracting dominant color signatures from garments.
 */
class WardrobeAnalyzer @Inject constructor() {

    /**
     * Extracts a comprehensive color signature from a garment image.
     */
    fun extractColorSignature(bitmap: Bitmap): GarmentColorSignature {
        val palette = Palette.from(bitmap).generate()
        
        val dominant = palette.dominantSwatch?.rgb?.toHex() ?: "#FFFFFF"
        val vibrant = palette.vibrantSwatch?.rgb?.toHex()
        val muted = palette.mutedSwatch?.rgb?.toHex()
        
        // Extract up to 5 prominent colors for a rich signature
        val secondaryColors = palette.swatches
            .sortedByDescending { it.population }
            .take(5)
            .map { it.rgb.toHex() }

        return GarmentColorSignature(
            dominantHex = dominant,
            vibrantHex = vibrant,
            mutedHex = muted,
            secondaryPalette = secondaryColors,
            allSwatches = palette.swatches.map { it.rgb.toHex() }
        )
    }

    fun calculateContrastLevel(swatches: List<String>): String {
        if (swatches.size < 2) return "LOW"
        
        val brightnesses = swatches.map { hex ->
            val color = try { android.graphics.Color.parseColor(hex) } catch (e: Exception) { 0 }
            (android.graphics.Color.red(color) * 0.299 + 
             android.graphics.Color.green(color) * 0.587 + 
             android.graphics.Color.blue(color) * 0.114) / 255.0
        }
        
        val min = brightnesses.minOrNull() ?: 0.0
        val max = brightnesses.maxOrNull() ?: 1.0
        val diff = max - min
        
        return when {
            diff > 0.6 -> "HIGH"
            diff > 0.3 -> "MEDIUM"
            else -> "LOW"
        }
    }

    private fun Int.toHex(): String = String.format("#%06X", 0xFFFFFF and this)
}

/**
 * Data signature representing the analytical color profile of a garment.
 */
data class GarmentColorSignature(
    val dominantHex: String,
    val vibrantHex: String? = null,
    val mutedHex: String? = null,
    val secondaryPalette: List<String> = emptyList(),
    val allSwatches: List<String> = emptyList()
)
