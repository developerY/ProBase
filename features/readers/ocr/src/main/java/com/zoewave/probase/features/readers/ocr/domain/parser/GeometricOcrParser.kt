package com.zoewave.probase.features.readers.ocr.domain.parser

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

data class StructuredElement(
    val text: String,
    val isBold: Boolean,
    val isColumnStart: Boolean,
    val boundingBox: Rect?
)

data class StructuredTextLine(
    val text: String,
    val isHeader: Boolean,
    val relativeTop: Float,
    val elements: List<StructuredElement>,
    val boundingBox: Rect?
) {
    /**
     * The "Trademark Booster": Rank legal identities higher.
     * Brands legally mark their identity with registered trademark (®) or trademark (™) symbols.
     */
    val hasTrademark: Boolean
        get() = text.contains("®") || 
                text.contains("™") || 
                text.contains("(R)", ignoreCase = true) || 
                text.contains("(TM)", ignoreCase = true)

    /**
     * The "Gravity" Heuristic: Rank visual prominence by height and vertical placement.
     * Important text (Brand, Name) is typically large and at the top.
     * Marketing fluff is penalized the further down the package it appears.
     */
    val prominenceScore: Float
        get() {
            val height = boundingBox?.height() ?: 0
            val baseScore = height.toFloat() * (1.0f - relativeTop)
            
            // If it has a trademark, apply a 1.5x multiplier to guarantee it wins
            return if (hasTrademark) baseScore * 1.5f else baseScore
        }
}

/**
 * GeometricOcrParser: Infers typographical hierarchy using bounding box geometry.
 * Uses O(N) single-pass extraction + O(1) mathematical flagging.
 */
object GeometricOcrParser {

    private const val HEADER_RATIO = 1.5f
    private const val BOLD_RATIO = 1.3f
    private const val COLUMN_GAP_RATIO = 2.0f

    /**
     * Converts raw ML Kit text into structured lines with geometric metadata.
     * @param visionText The raw result from ML Kit
     * @param imageHeight The height of the original bitmap to calculate relative vertical placement.
     */
    fun parse(visionText: Text, imageHeight: Int): List<StructuredTextLine> {
        val allLines = visionText.textBlocks.flatMap { it.lines }
        if (allLines.isEmpty()) return emptyList()

        // 1. Pre-calculate Averages for the panel context
        val lineHeights = allLines.mapNotNull { it.boundingBox?.height() }
        val averageLineHeight = lineHeights.average().toFloat()
        
        android.util.Log.d("GeometricOcrParser", "Panel Stats: Avg Height: ${"%.2f".format(averageLineHeight)}, Total Lines: ${allLines.size}")

        val allElements = allLines.flatMap { it.elements }
        val averageWordRatio = allElements.mapNotNull { element ->
            val box = element.boundingBox ?: return@mapNotNull null
            box.width().toFloat() / box.height().toFloat()
        }.average().toFloat()

        // Calculate average gap between words on the same line
        val allGaps = mutableListOf<Int>()
        allLines.forEach { line ->
            for (i in 0 until line.elements.size - 1) {
                val currentRight = line.elements[i].boundingBox?.right ?: continue
                val nextLeft = line.elements[i + 1].boundingBox?.left ?: continue
                val gap = nextLeft - currentRight
                if (gap > 0) allGaps.add(gap)
            }
        }
        val averageGap = if (allGaps.isNotEmpty()) allGaps.average().toFloat() else 0f

        // 2. Build structured output with mathematical flagging
        return allLines.map { line ->
            val lineBox = line.boundingBox
            val isHeader = (lineBox?.height() ?: 0) > (averageLineHeight * HEADER_RATIO)
            
            // Calculate relative vertical position (Gravity calculation)
            val relativeTop = if (lineBox != null && imageHeight > 0) {
                lineBox.top.toFloat() / imageHeight.toFloat()
            } else 0f

            val structuredElements = line.elements.mapIndexed { index, element ->
                val box = element.boundingBox
                
                // Infer Bolding: Is this word unusually wide for its height?
                val ratio = if (box != null && box.height() > 0) {
                    box.width().toFloat() / box.height().toFloat()
                } else 0f
                val isBold = ratio > (averageWordRatio * BOLD_RATIO)

                // Infer Columns: Is there an unusually large gap before this word?
                var isColumnStart = false
                if (index > 0) {
                    val prevRight = line.elements[index - 1].boundingBox?.right ?: 0
                    val currentLeft = box?.left ?: 0
                    val gap = currentLeft - prevRight
                    if (gap > (averageGap * COLUMN_GAP_RATIO)) {
                        isColumnStart = true
                    }
                }

                StructuredElement(
                    text = element.text,
                    isBold = isBold,
                    isColumnStart = isColumnStart,
                    boundingBox = box
                )
            }

            StructuredTextLine(
                text = line.text,
                isHeader = isHeader,
                relativeTop = relativeTop,
                elements = structuredElements,
                boundingBox = lineBox
            )
        }
    }

    /**
     * Identifies the top two identity candidates (Brand and Product Name)
     * using the Gravity-weighted Prominence Score.
     */
    fun extractIdentityCandidates(lines: List<StructuredTextLine>): Pair<String, String> {
        val sortedByProminence = lines.sortedByDescending { it.prominenceScore }
        
        val brand = sortedByProminence.getOrNull(0)?.text ?: "Detected Brand"
        val productName = sortedByProminence.getOrNull(1)?.text ?: "Captured Product"
        
        return brand to productName
    }
}
