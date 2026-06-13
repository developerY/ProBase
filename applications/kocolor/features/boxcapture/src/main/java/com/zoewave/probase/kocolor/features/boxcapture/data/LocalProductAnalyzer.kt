package com.zoewave.probase.kocolor.features.boxcapture.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zoewave.probase.kocolor.model.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalProductAnalyzer @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun analyze(bitmaps: List<Bitmap>): CosmeticItem {
        val allText = mutableListOf<String>()

        for (bitmap in bitmaps) {
            val image = InputImage.fromBitmap(bitmap, 0)
            try {
                val result = recognizer.process(image).await()
                allText.add(result.text)
            } catch (e: Exception) {
                // Skip failed images
            }
        }

        val combinedText = allText.joinToString("\n")
        return heuristicGuess(combinedText)
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
        // High fidelity brands often have short, distinct names
        val knownBrands = listOf("CHANEL", "DIOR", "NARS", "MAC", "ESTEE LAUDER", "LOREAL", "MAYBELLINE")
        for (line in lines.take(5)) {
            val upper = line.uppercase()
            if (knownBrands.any { upper.contains(it) }) return line
        }
        return lines.firstOrNull()?.take(20)
    }

    private fun guessName(lines: List<String>, brand: String?): String? {
        val filtered = lines.filter { it != brand }
        // Often the name is a multi-word line that isn't the brand
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
