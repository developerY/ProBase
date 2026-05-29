package com.zoewave.probase.kocolor.features.color.util

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import com.zoewave.probase.kocolor.features.color.util.ColorScienceUtils.toHexString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local processing framework for extracting dominant color signatures from garments.
 */
@Singleton
class WardrobeAnalyzer @Inject constructor() {

    /**
     * Extracts a comprehensive color signature from a garment image.
     */
    fun extractColorSignature(bitmap: Bitmap): GarmentColorSignature {
        val palette = Palette.from(bitmap).generate()
        
        val dominant = palette.dominantSwatch?.rgb?.toHexString() ?: "#FFFFFF"
        val vibrant = palette.vibrantSwatch?.rgb?.toHexString()
        val muted = palette.mutedSwatch?.rgb?.toHexString()
        
        // Extract up to 5 prominent colors for a rich signature
        val secondaryColors = palette.swatches
            .sortedByDescending { it.population }
            .take(5)
            .map { it.rgb.toHexString() }

        return GarmentColorSignature(
            dominantHex = dominant,
            vibrantHex = vibrant,
            mutedHex = muted,
            secondaryPalette = secondaryColors,
            allSwatches = palette.swatches.map { it.rgb.toHexString() }
        )
    }

    fun calculateContrastLevel(swatches: List<String>): String {
        if (swatches.size < 2) return "LOW"
        
        val brightnesses = swatches.map { hex ->
            val rgb = ColorScienceUtils.hexToRgb(hex)
            if (rgb != null) {
                (rgb.first * 0.299 + rgb.second * 0.587 + rgb.third * 0.114) / 255.0
            } else 0.0
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
