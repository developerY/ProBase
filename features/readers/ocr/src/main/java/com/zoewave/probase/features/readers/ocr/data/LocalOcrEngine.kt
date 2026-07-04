package com.zoewave.probase.features.readers.ocr.data

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
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
        "recommended", "guaranteed", "instant", "miracle"
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
            val imageHeight = image.height

            val cleanedTextBuilder = StringBuilder()

            for (block in result.textBlocks) {
                val box = block.boundingBox
                val text = block.text.lowercase()

                // Apply aggressive filtering ONLY to the Front and Info panels
                if (panel == BoxPanel.FRONT || panel == BoxPanel.INFO) {
                    
                    // 1. Spatial Filter: Top 10% Starburst trap (common for "NEW!" stickers)
                    if (box != null && box.top < (imageHeight * 0.10) && text.contains("new")) {
                        continue // Drop it completely
                    }

                    // 2. Stop-Word Density Filter
                    val words = text.split("\\s+".toRegex())
                    val fluffCount = words.count { marketingFluff.contains(it) }

                    // Drop heavily saturated marketing blocks (e.g. "NEW IMPROVED CLINICALLY PROVEN FORMULA")
                    if (fluffCount > 2) {
                        continue 
                    }
                }

                // Ingredients and Directions bypass the filters to preserve pure data
                cleanedTextBuilder.append(block.text).append("\n")
            }

            cleanedTextBuilder.toString().trim()

        } catch (e: Exception) {
            Log.e("LocalOcrEngine", "OCR processing failed for panel: $panel", e)
            ""
        }
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
