package com.zoewave.probase.seaweed.model.finance.vision

import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class NormalizedPoint(val x: Float, val y: Float)

@Serializable
data class NormalizedRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Serializable
data class NormalizedQuad(
    val topLeft: NormalizedPoint,
    val topRight: NormalizedPoint,
    val bottomRight: NormalizedPoint,
    val bottomLeft: NormalizedPoint
)

sealed class ProjectionResult {
    data class Success(
        val quad: NormalizedQuad,
        val requiresViewportClipping: Boolean
    ) : ProjectionResult()

    data class SingularOrInvalidTransform(val reason: String) : ProjectionResult()
}

object CoordinateTransformMapper {
    fun projectOcrRectToRawQuad(
        ocrRect: NormalizedRect,
        hNorm: FloatArray, // 9 elements, row-major
        docId: String = "GLOBAL"
    ): ProjectionResult {
        if (hNorm.size != 9 || hNorm.any { !it.isFinite() }) {
            val res = ProjectionResult.SingularOrInvalidTransform("Invalid homography array")
            PipelineGearLogger.d(PipelineGear.GEAR_2_VISION_HOMOGRAPHY, docId) { "Transform failed: ${res.reason}" }
            return res
        }

        // Calculate determinant of 3x3 matrix
        val det = hNorm[0] * (hNorm[4] * hNorm[8] - hNorm[5] * hNorm[7]) -
                  hNorm[1] * (hNorm[3] * hNorm[8] - hNorm[5] * hNorm[6]) +
                  hNorm[2] * (hNorm[3] * hNorm[7] - hNorm[4] * hNorm[6])

        if (!det.isFinite() || abs(det) < 1e-7f) {
            val res = ProjectionResult.SingularOrInvalidTransform("Singular homography determinant: $det")
            PipelineGearLogger.d(PipelineGear.GEAR_2_VISION_HOMOGRAPHY, docId) { "Transform failed: ${res.reason}" }
            return res
        }

        val tl = mapPoint(ocrRect.x, ocrRect.y, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("TL vertex projection failed")
        val tr = mapPoint(ocrRect.x + ocrRect.width, ocrRect.y, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("TR vertex projection failed")
        val br = mapPoint(ocrRect.x + ocrRect.width, ocrRect.y + ocrRect.height, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("BR vertex projection failed")
        val bl = mapPoint(ocrRect.x, ocrRect.y + ocrRect.height, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("BL vertex projection failed")

        val points = listOf(tl, tr, br, bl)
        val outOfBounds = points.any { it.x < 0f || it.x > 1f || it.y < 0f || it.y > 1f }

        return ProjectionResult.Success(
            quad = NormalizedQuad(tl, tr, br, bl),
            requiresViewportClipping = outOfBounds
        )
    }

    private fun mapPoint(x: Float, y: Float, h: FloatArray): NormalizedPoint? {
        val w = h[6] * x + h[7] * y + h[8]
        if (!w.isFinite() || abs(w) < 1e-6f) return null

        val px = (h[0] * x + h[1] * y + h[2]) / w
        val py = (h[3] * x + h[4] * y + h[5]) / w

        if (!px.isFinite() || !py.isFinite()) return null

        return NormalizedPoint(px, py)
    }
}
