package com.zoewave.probase.features.readers.ocr.data

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zoewave.probase.features.readers.ocr.domain.model.BoxPanel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalOcrEngine @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val marketingFluff = hashSetOf(
        "new", "improved", "superior", "clinically", "proven",
        "advanced", "technology", "breakthrough", "dermatologist",
        "recommended", "guaranteed", "instant", "miracle", "introducing"
    )

    /**
     * Processes multiple box panels in parallel and applies panel-specific filtering.
     */
    suspend fun extractCategorizedText(bitmaps: Map<BoxPanel, Bitmap>): Map<BoxPanel, String> = coroutineScope {
        val startTime = System.currentTimeMillis()

        // Process all panels in parallel
        val deferredResults = bitmaps.map { (panel, bitmap) ->
            async {
                panel to processSinglePanel(panel, bitmap)
            }
        }

        val results = deferredResults.awaitAll().toMap()
        
        val duration = System.currentTimeMillis() - startTime
        Log.d("LocalOcrEngine", "Categorized OCR completed in ${duration}ms")
        
        return@coroutineScope results
    }

    /**
     * Processes a single box panel with its specific filtering rules.
     */
    suspend fun processSinglePanel(panel: BoxPanel, bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            
            when (panel) {
                BoxPanel.FRONT -> processFrontPanel(result, image.height)
                BoxPanel.INFO -> processInfoPanel(result)
                BoxPanel.INGREDIENTS, BoxPanel.DIRECTIONS -> result.text
            }

        } catch (e: Exception) {
            Log.e("LocalOcrEngine", "OCR processing failed for panel: $panel", e)
            ""
        }
    }

    private fun processFrontPanel(visionText: Text, imageHeight: Int): String {
        val cleanedTextBuilder = StringBuilder()
        var extractedVolume: String? = null

        // 1. Identify the Hero Text (The block with the largest physical height)
        val heroBlock = visionText.textBlocks.maxByOrNull { it.boundingBox?.height() ?: 0 }

        for (block in visionText.textBlocks) {
            val box: Rect = block.boundingBox ?: continue
            val text = block.text
            val textLower = text.lowercase()

            // Calculate relative vertical position (0.0 is top, 1.0 is bottom)
            val relativeTop = box.top.toFloat() / imageHeight.toFloat()

            // --- RULE 1: The "Starburst" Filter (Top 15% of the bottle) ---
            if (relativeTop < 0.15f) {
                val words = textLower.split("\\s+".toRegex())
                val hasFluff = words.any { marketingFluff.contains(it) }
                // If it's at the very top AND contains marketing words, destroy it.
                if (hasFluff && block != heroBlock) {
                    continue 
                }
            }

            // --- RULE 2: Stop-Word Density Filter (Middle 65%) ---
            if (relativeTop in 0.15f..0.80f) {
                val words = textLower.split("\\s+".toRegex())
                val fluffCount = words.count { marketingFluff.contains(it) }
                
                // If a block is densely packed with marketing buzzwords, drop it
                // (Unless it's the Hero block, which we mathematically protect)
                if (fluffCount >= 2 && block != heroBlock) {
                    continue
                }
            }

            // --- RULE 3: The Weight & Volume Filter (Bottom 20%) ---
            if (relativeTop > 0.80f) {
                // Check for standard cosmetic volume regex (e.g., "FL OZ", "mL", "Net Wt")
                val volumeRegex = Regex("""(\d+(\.\d+)?\s*(fl\s*oz|ml|g|net\s*wt))""", RegexOption.IGNORE_CASE)
                val match = volumeRegex.find(text)
                
                if (match != null) {
                    extractedVolume = match.value
                    // We don't append this to the main builder; we isolate it.
                    continue 
                }
                
                // The bottom 20% is usually company addresses and barcodes. 
                // If it's not volume/weight, it's safe to drop to keep the payload pristine.
                if (block != heroBlock) continue
            }

            // If the block survived the spatial gauntlet, append it
            cleanedTextBuilder.append(text).append("\n")
        }

        // Append the perfectly extracted volume to the very end for consistent LLM parsing
        extractedVolume?.let {
            cleanedTextBuilder.append("\n[VOLUME: $it]")
        }

        return cleanedTextBuilder.toString().trim()
    }

    private fun processInfoPanel(visionText: Text): String {
        val cleanedTextBuilder = StringBuilder()

        for (block in visionText.textBlocks) {
            val textLower = block.text.lowercase()
            val words = textLower.split("\\s+".toRegex())
            val fluffCount = words.count { marketingFluff.contains(it) }

            // Lighter filter for Info panel: only drop extremely high density fluff
            if (fluffCount > 3) {
                continue
            }

            cleanedTextBuilder.append(block.text).append("\n")
        }

        return cleanedTextBuilder.toString().trim()
    }

    suspend fun extractText(bitmap: Bitmap): String {
        val startTime = System.currentTimeMillis()
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            val duration = System.currentTimeMillis() - startTime
            Log.d("LocalOcrEngine", "OCR successful in ${duration}ms")
            Log.d("LocalOcrEngine", "RAW TEXT: ${result.text}")
            result.text
        } catch (e: Exception) {
            Log.e("LocalOcrEngine", "OCR processing failed", e)
            ""
        }
    }
    
    suspend fun extractTextFromBitmaps(bitmaps: List<Bitmap>): String = coroutineScope {
        bitmaps.map { async { extractText(it) } }
            .awaitAll()
            .joinToString("\n")
    }
}
