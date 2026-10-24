package com.zoewave.probase.seaweed.model.finance.layout

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
    fun groupIntoLines(elements: List<OcrLayoutElement>): List<ReconstructedLine> {
        if (elements.isEmpty()) return emptyList()

        val sortedElements = elements.sortedBy { it.centerY }
        val lines = mutableListOf<MutableList<OcrLayoutElement>>()

        for (element in sortedElements) {
            var addedToLine = false
            for (line in lines) {
                val anchor = line.last()

                // Convert anchor rotation to radians
                val thetaAnchorRad = Math.toRadians(anchor.rotationDegrees.toDouble())

                // Project baseline ray from anchor to candidate's X coordinate
                val projectedY = anchor.centerY + (element.centerX - anchor.centerX) * tan(thetaAnchorRad)

                // Compare Y drift
                val yDiff = abs(projectedY - element.centerY)
                val maxAllowedDrift = 0.45f * max(anchor.height, element.height)

                // Compare rotation angle drift
                val angleDiff = abs(anchor.rotationDegrees - element.rotationDegrees)

                if (yDiff <= maxAllowedDrift && angleDiff <= 8.0f) {
                    line.add(element)
                    addedToLine = true
                    break
                }
            }

            if (!addedToLine) {
                lines.add(mutableListOf(element))
            }
        }

        // Sort elements horizontally within each line
        return lines.mapIndexed { index, line ->
            ReconstructedLine(
                lineId = "L${index + 1}",
                elements = line.sortedBy { it.centerX }
            )
        }
    }
}
