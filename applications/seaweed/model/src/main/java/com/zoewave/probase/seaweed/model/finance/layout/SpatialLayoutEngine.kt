package com.zoewave.probase.seaweed.model.finance.layout

import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger
import com.zoewave.probase.seaweed.model.finance.vision.NormalizedRect
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.tan

data class OcrLayoutElement(
    val id: String,
    val text: String,
    val boxNormalizedOcr: NormalizedRect,
    val rotationDegrees: Float
) {
    val centerX: Float get() = boxNormalizedOcr.x + (boxNormalizedOcr.width / 2f)
    val centerY: Float get() = boxNormalizedOcr.y + (boxNormalizedOcr.height / 2f)
    val height: Float get() = boxNormalizedOcr.height
}

data class ReconstructedLine(
    val lineId: String,
    val elements: List<OcrLayoutElement>
)

object SpatialLayoutEngine {

    /**
     * Reconstructs physical receipt lines taking into account curved thermal paper rotation.
     * Uses radian-converted vector projection to test if elements fall on the same baseline ray.
     */
    fun groupIntoLines(elements: List<OcrLayoutElement>, docId: String = "GLOBAL"): List<ReconstructedLine> {
        if (elements.isEmpty()) return emptyList()

        val sortedElements = elements.sortedBy { it.centerY }
        val lines = mutableListOf<MutableList<OcrLayoutElement>>()

        for (element in sortedElements) {
            var addedToLine = false
            for (line in lines) {
                val anchor = line.last()

                // Clamp rotation angle: if |theta| < 2.5, treat as 0; clamp to +-10.0
                val rawAngle = anchor.rotationDegrees
                val clampedAngle = when {
                    abs(rawAngle) < 2.5f -> 0f
                    rawAngle > 10.0f -> 10.0f
                    rawAngle < -10.0f -> -10.0f
                    else -> rawAngle
                }

                // Convert anchor rotation to radians
                val thetaAnchorRad = Math.toRadians(clampedAngle.toDouble())

                // Project baseline ray from anchor to candidate's X coordinate
                val projectedY = anchor.centerY + (element.centerX - anchor.centerX) * tan(thetaAnchorRad)

                // Compare Y drift
                val yDiff = abs(projectedY - element.centerY)
                val maxAllowedDrift = 0.75f * max(anchor.height, element.height)

                // Calculate vertical bounding-box overlap
                val topA = anchor.boxNormalizedOcr.y
                val botA = anchor.boxNormalizedOcr.y + anchor.boxNormalizedOcr.height
                val topB = element.boxNormalizedOcr.y
                val botB = element.boxNormalizedOcr.y + element.boxNormalizedOcr.height

                val overlapTop = maxOf(topA, topB)
                val overlapBot = minOf(botA, botB)
                val overlapHeight = (overlapBot - overlapTop).coerceAtLeast(0f)
                val minHeight = minOf(anchor.height, element.height).coerceAtLeast(0.001f)
                val overlapRatio = overlapHeight / minHeight

                // Compare rotation angle drift
                val angleDiff = abs(anchor.rotationDegrees - element.rotationDegrees)

                if ((yDiff <= maxAllowedDrift || overlapRatio >= 0.40f) && angleDiff <= 12.0f) {
                    line.add(element)
                    addedToLine = true
                    break
                }
            }

            if (!addedToLine) {
                lines.add(mutableListOf(element))
            }
        }

        val reconstructed = lines.mapIndexed { index, line ->
            ReconstructedLine(
                lineId = "L${index + 1}",
                elements = line.sortedBy { it.centerX }
            )
        }

        PipelineGearLogger.d(PipelineGear.GEAR_3_LAYOUT_OCR, docId) {
            "Extracted ${elements.size} OcrElements, radian vector grouped into ${reconstructed.size} lines"
        }

        return reconstructed
    }
}
