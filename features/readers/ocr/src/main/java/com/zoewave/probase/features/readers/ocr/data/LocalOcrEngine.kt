package com.zoewave.probase.features.readers.ocr.data

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zoewave.probase.features.readers.ocr.domain.model.BoxPanel
import com.zoewave.probase.features.readers.ocr.domain.parser.GeometricOcrParser
import com.zoewave.probase.features.readers.ocr.domain.parser.IngredientParser
import com.zoewave.probase.features.readers.ocr.domain.parser.StructuredTextLine
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
            val rawText = result.text
            
            // Apply Geometric Parsing to identify headers/bolding with Gravity and Symmetry Heuristics
            val structuredLines = GeometricOcrParser.parse(result, image.height, image.width)

            val cleanedText = when (panel) {
                BoxPanel.FRONT -> processFrontPanel(structuredLines, image.height)
                BoxPanel.INFO -> processInfoPanel(structuredLines)
                BoxPanel.INGREDIENTS -> processIngredientsPanel(rawText)
                BoxPanel.DIRECTIONS -> rawText
            }

            Log.d("LocalOcrEngine", "--- [9a Machine Vision Cleanup: $panel] ---")
            Log.d("LocalOcrEngine", "RAW OCR OUTPUT:\n$rawText")
            Log.d("LocalOcrEngine", "CLEANED ARMOR PAYLOAD:\n$cleanedText")
            Log.d("LocalOcrEngine", "-------------------------------------------")

            cleanedText

        } catch (e: Exception) {
            Log.e("LocalOcrEngine", "OCR processing failed for panel: $panel", e)
            ""
        }
    }

    private fun processIngredientsPanel(rawText: String): String {
        val parsed = IngredientParser.parse(rawText)
        val builder = StringBuilder()
        
        if (parsed.active.isNotEmpty()) {
            builder.append("[ACTIVE INGREDIENTS]:\n")
            parsed.active.forEach { builder.append("- $it\n") }
            builder.append("\n")
        }
        
        if (parsed.inactive.isNotEmpty()) {
            builder.append("[INACTIVE INGREDIENTS]:\n")
            parsed.inactive.forEach { builder.append("- $it\n") }
        }
        
        return builder.toString().trim()
    }

    private fun processFrontPanel(lines: List<StructuredTextLine>, imageHeight: Int): String {
        val cleanedTextBuilder = StringBuilder()
        var extractedVolume: String? = null

        // Calculate avg height again locally for the tag, or I can pass it from Parser.
        // Let's just use the Line metadata for now.
        val avgHeight = lines.mapNotNull { it.boundingBox?.height() }.average()

        for (line in lines) {
            val box: Rect = line.boundingBox ?: continue
            val text = line.text
            val textLower = text.lowercase()
            val height = box.height()

            // Calculate relative vertical position (0.0 is top, 1.0 is bottom)
            val relativeTop = box.top.toFloat() / imageHeight.toFloat()

            // --- RULE 1: The "Starburst" Filter (Top 10% of the bottle) ---
            if (relativeTop < 0.10f) {
                val words = textLower.split("\\s+".toRegex())
                val hasFluff = words.any { marketingFluff.contains(it) }
                // Only destroy if it's fluff AND NOT a geometric header
                if (hasFluff && !line.isHeader && words.size < 3) {
                    continue 
                }
            }

            // --- RULE 2: Stop-Word Density Filter (Middle 80%) ---
            if (relativeTop in 0.10f..0.90f) {
                val words = textLower.split("\\s+".toRegex())
                val fluffCount = words.count { marketingFluff.contains(it) }
                
                // Drop only if it's marketing speak AND not a header
                if (fluffCount >= 4 && !line.isHeader) {
                    continue
                }
            }

            // --- RULE 3: The Weight & Volume Filter (Bottom 10%) ---
            if (relativeTop > 0.90f) {
                val volumeRegex = Regex("""(\d+(\.\d+)?\s*(fl\s*oz|ml|g|net\s*wt))""", RegexOption.IGNORE_CASE)
                val match = volumeRegex.find(text)
                
                if (match != null) {
                    extractedVolume = match.value
                    continue 
                }
                
                if (!line.isHeader && text.length < 5) continue
            }

            // High-fidelity Logging for Debugging
            val ratio = if (avgHeight > 0) height / avgHeight else 1.0
            val pScore = line.prominenceScore
            val boostTag = if (line.hasTrademark) "B" else ""
            val centerTag = if (line.relativeTop > 0.5f && line.relativeCenterX in 0.35f..0.65f) "C" else ""
            val sinkTag = if (line.isSentenceCase) "S" else ""
            val typeTag = if (line.isHeader) "H" else "N"
            
            val tag = "[$boostTag$centerTag$sinkTag$typeTag(h=$height, r=${"%.1f".format(ratio)}, p=${"%.0f".format(pScore)})] "
            cleanedTextBuilder.append(tag).append(text).append("\n")
        }

        extractedVolume?.let {
            cleanedTextBuilder.append("\n[VOLUME: $it]")
        }

        return cleanedTextBuilder.toString().trim()
    }

    private fun processInfoPanel(lines: List<StructuredTextLine>): String {
        val cleanedTextBuilder = StringBuilder()

        for (line in lines) {
            val textLower = line.text.lowercase()
            val words = textLower.split("\\s+".toRegex())
            val fluffCount = words.count { marketingFluff.contains(it) }

            // Protect headers and bold terms in info panels
            val hasBoldTerms = line.elements.any { it.isBold }
            
            if (fluffCount > 3 && !line.isHeader && !hasBoldTerms) {
                continue
            }

            cleanedTextBuilder.append(line.text).append("\n")
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

    suspend fun extractOcrElements(
        documentId: String,
        pageNumber: Int,
        cleanedOcrBitmap: Bitmap,
        homographyNorm: FloatArray = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
    ): com.zoewave.probase.features.readers.ocr.data.model.OcrExtractionResult {
        val widthPx = cleanedOcrBitmap.width.coerceAtLeast(1)
        val heightPx = cleanedOcrBitmap.height.coerceAtLeast(1)

        val visionText: com.google.mlkit.vision.text.Text = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val inputImage = InputImage.fromBitmap(cleanedOcrBitmap, 0)
            recognizer.process(inputImage)
                .addOnSuccessListener { result ->
                    if (cont.isActive) cont.resumeWith(Result.success(result))
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWith(Result.failure(e))
                }
        }

        val extractedElements = mutableListOf<com.zoewave.probase.seaweed.model.finance.validation.OcrElement>()
        var elementCounter = 1
        var lineCounter = 1
        var singularFailures = 0

        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val lineId = String.format("P%02d_L%04d", pageNumber, lineCounter++)
                val lineAngleDeg = computeRotationDegrees(line.angle, line.cornerPoints)

                // Emit line-level or element-level tokens with full spatial geometry
                val lineElements = if (line.elements.isNotEmpty()) line.elements else emptyList()
                if (lineElements.isEmpty()) continue

                for (element in lineElements) {
                    val rect = element.boundingBox ?: continue
                    val rawText = element.text.trim()
                    if (rawText.isEmpty()) continue

                    val elementId = String.format("P%02d_T%04d", pageNumber, elementCounter++)
                    val normX = (rect.left.toFloat() / widthPx).coerceIn(0f, 1f)
                    val normY = (rect.top.toFloat() / heightPx).coerceIn(0f, 1f)
                    val normW = (rect.width().toFloat() / widthPx).coerceIn(0.001f, 1f - normX)
                    val normH = (rect.height().toFloat() / heightPx).coerceIn(0.001f, 1f - normY)

                    val normRect = com.zoewave.probase.seaweed.model.finance.vision.NormalizedRect(
                        x = normX,
                        y = normY,
                        width = normW,
                        height = normH
                    )

                    val projectedQuad = when (
                        val proj = com.zoewave.probase.seaweed.model.finance.vision.CoordinateTransformMapper.projectOcrRectToRawQuad(normRect, homographyNorm, documentId)
                    ) {
                        is com.zoewave.probase.seaweed.model.finance.vision.ProjectionResult.Success -> proj.quad
                        is com.zoewave.probase.seaweed.model.finance.vision.ProjectionResult.SingularOrInvalidTransform -> {
                            singularFailures++
                            null
                        }
                    }

                    val elemAngleDeg = computeRotationDegrees(element.angle, element.cornerPoints)
                        .takeIf { it != 0f } ?: lineAngleDeg

                    val region = when {
                        normY < 0.22f -> "HEADER"
                        normY > 0.82f -> "FOOTER"
                        else -> "BODY"
                    }

                    extractedElements += com.zoewave.probase.seaweed.model.finance.validation.OcrElement(
                        id = elementId,
                        pageNumber = pageNumber,
                        rawText = rawText,
                        normalizedText = rawText.uppercase(),
                        provenance = com.zoewave.probase.seaweed.model.finance.validation.ExtractionProvenance.MLKIT_OCR_V2,
                        boxNormalizedOcr = normRect,
                        boxNormalizedRawQuad = projectedQuad,
                        ocrConfidence = element.confidence ?: 0.99f,
                        rotationDegrees = elemAngleDeg,
                        lineId = lineId,
                        region = region
                    )
                }
            }
        }

        com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger.d(com.zoewave.probase.seaweed.model.finance.common.PipelineGear.GEAR_2_VISION_HOMOGRAPHY, documentId) {
            "Page $pageNumber homography projection completed: mapped=${extractedElements.size - singularFailures}, singularFailures=$singularFailures"
        }

        val minTextY = extractedElements.minOfOrNull { it.boxNormalizedOcr.y } ?: 0f
        val maxTextY = extractedElements.maxOfOrNull { it.boxNormalizedOcr.y + it.boxNormalizedOcr.height } ?: 1f
        val textHeightSpan = (maxTextY - minTextY).coerceAtLeast(0.05f)

        val finalElements = extractedElements.map { el ->
            val relY = (el.boxNormalizedOcr.y - minTextY) / textHeightSpan
            val relativeRegion = when {
                relY <= 0.25f -> "HEADER"
                relY >= 0.80f -> "FOOTER"
                else -> "BODY"
            }
            el.copy(region = relativeRegion)
        }

        com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger.d(com.zoewave.probase.seaweed.model.finance.common.PipelineGear.GEAR_3_LAYOUT_OCR, documentId) {
            val firstId = finalElements.firstOrNull()?.id ?: "NONE"
            val lastId = finalElements.lastOrNull()?.id ?: "NONE"
            "ML Kit OCR extracted ${finalElements.size} elements ($firstId..$lastId) from ${widthPx}x${heightPx} bitmap"
        }

        return com.zoewave.probase.features.readers.ocr.data.model.OcrExtractionResult(
            pageNumber = pageNumber,
            imageWidthPx = widthPx,
            imageHeightPx = heightPx,
            elements = finalElements,
            singularTransformCount = singularFailures
        )
    }

    private fun computeRotationDegrees(mlKitAngle: Float, corners: Array<android.graphics.Point>?): Float {
        if (mlKitAngle.isFinite() && mlKitAngle != 0f) return mlKitAngle
        if (corners != null && corners.size >= 2) {
            val dx = (corners[1].x - corners[0].x).toDouble()
            val dy = (corners[1].y - corners[0].y).toDouble()
            if (dx != 0.0 || dy != 0.0) {
                return Math.toDegrees(kotlin.math.atan2(dy, dx)).toFloat()
            }
        }
        return 0f
    }
}
