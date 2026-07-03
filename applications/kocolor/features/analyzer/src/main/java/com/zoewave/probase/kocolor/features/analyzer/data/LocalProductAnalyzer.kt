package com.zoewave.probase.kocolor.features.analyzer.data

import android.graphics.Bitmap
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.features.readers.ocr.data.LocalOcrEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalProductAnalyzer @Inject constructor(
    private val ocrEngine: LocalOcrEngine
) {

    suspend fun analyze(bitmaps: List<Bitmap>): CosmeticItem {
        val combinedText = ocrEngine.extractTextFromBitmaps(bitmaps)
        return heuristicGuess(combinedText)
    }

    suspend fun extractText(bitmap: Bitmap): String {
        return ocrEngine.extractText(bitmap)
    }

    /**
     * Extracts a palette of dominant colors from the bitmap.
// ... (rest of the file remains similar but recognizer is removed)
     * Samples different regions to give the user better options.
     */
    fun extractColorPalette(bitmap: Bitmap): List<String> {
        val width = bitmap.width
        val height = bitmap.height
        
        // Sample points: Center, Center-Top, Center-Bottom, Left-Mid, Right-Mid
        val points = listOf(
            Pair(width / 2, height / 2),
            Pair(width / 2, height / 3),
            Pair(width / 2, (height * 2) / 3),
            Pair(width / 3, height / 2),
            Pair((width * 2) / 3, height / 2)
        )

        val palette = mutableSetOf<String>()
        val radius = (width * 0.05f).toInt().coerceAtLeast(5)

        for (point in points) {
            val (centerX, centerY) = point
            var rTotal = 0L
            var gTotal = 0L
            var bTotal = 0L
            var count = 0

            for (x in (centerX - radius)..(centerX + radius)) {
                for (y in (centerY - radius)..(centerY + radius)) {
                    if (x in 0 until width && y in 0 until height) {
                        val pixel = bitmap.getPixel(x, y)
                        rTotal += android.graphics.Color.red(pixel)
                        gTotal += android.graphics.Color.green(pixel)
                        bTotal += android.graphics.Color.blue(pixel)
                        count++
                    }
                }
            }

            if (count > 0) {
                val hex = String.format("#%02X%02X%02X", (rTotal / count).toInt(), (gTotal / count).toInt(), (bTotal / count).toInt())
                palette.add(hex)
            }
        }

        return palette.toList().take(5)
    }

    /**
     * Extracts a price value from the bitmap using local OCR.
     * Looks for patterns like "$24.00", "24.99", etc.
     */
    suspend fun extractPrice(bitmap: Bitmap): Double? {
        val text = extractText(bitmap)
        val regex = Regex("""\$?\s?(\d+\.\d{2})""")
        return regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun heuristicGuess(text: String): CosmeticItem {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        
        // 1. Guess Brand (Often in top 3 lines or all caps)
        val brand = guessBrand(lines)
        
        // 2. Guess Name (Often the longest prominent line near the top)
        val name = guessName(lines, brand)
        
        // 3. Guess Volume
        val volume = extractVolume(text)
        
        // 4. Guess Category based on keywords
        val micro = guessMicroCategory(text)

        return CosmeticItem(
            name = name ?: "Captured Product",
            brand = brand ?: "Detected Brand",
            macroCategory = micro.macro,
            microCategory = micro,
            volume = volume,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun guessBrand(lines: List<String>): String? {
        val knownBrands = listOf("CHANEL", "DIOR", "NARS", "MAC", "ESTEE LAUDER", "LOREAL", "MAYBELLINE")
        for (line in lines.take(5)) {
            val upper = line.uppercase()
            if (knownBrands.any { upper.contains(it) }) return line
        }
        return lines.firstOrNull()?.take(20)
    }

    private fun guessName(lines: List<String>, brand: String?): String? {
        val filtered = lines.filter { it != brand }
        return filtered.firstOrNull { it.length > 5 } ?: filtered.firstOrNull()
    }

    private fun extractVolume(text: String): String? {
        val regex = Regex("""(\d+\.?\d*)\s?(ml|g|oz|fl\.?\s?oz)""", RegexOption.IGNORE_CASE)
        return regex.find(text)?.value
    }

    private fun guessMicroCategory(text: String): MicroCategory {
        val lower = text.lowercase()
        return when {
            lower.contains("foundation") || lower.contains("tint") -> MicroCategory.FOUNDATION
            lower.contains("concealer") -> MicroCategory.CONCEALER
            lower.contains("lipstick") || lower.contains("lip") -> MicroCategory.LIPSTICK
            lower.contains("eye") || lower.contains("palette") -> MicroCategory.EYESHADOW
            lower.contains("serum") -> MicroCategory.SERUM
            lower.contains("moisturizer") || lower.contains("cream") -> MicroCategory.MOISTURIZER
            else -> MicroCategory.OTHER
        }
    }
}
