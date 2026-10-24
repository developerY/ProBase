package com.zoewave.probase.seaweed.model.finance.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CoordinateTransformMapperTest {

    @Test
    fun `projectOcrRectToRawQuad returns SingularOrInvalidTransform for singular matrix`() {
        val rect = NormalizedRect(0.1f, 0.1f, 0.2f, 0.05f)
        // Singular matrix (all zeros)
        val hNorm = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

        val result = CoordinateTransformMapper.projectOcrRectToRawQuad(rect, hNorm)

        assertTrue(result is ProjectionResult.SingularOrInvalidTransform)
        val error = result as ProjectionResult.SingularOrInvalidTransform
        assertTrue(error.reason.contains("Singular homography"))
    }

    @Test
    fun `projectOcrRectToRawQuad returns requiresViewportClipping for out of bounds mapping`() {
        val rect = NormalizedRect(0.9f, 0.9f, 0.2f, 0.2f)
        // Identity matrix, so rect maps to 0.9 to 1.1 (out of bounds > 1.0)
        val hNorm = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )

        val result = CoordinateTransformMapper.projectOcrRectToRawQuad(rect, hNorm)

        assertTrue(result is ProjectionResult.Success)
        val success = result as ProjectionResult.Success
        assertTrue(success.requiresViewportClipping)
        assertEquals(1.1f, success.quad.bottomRight.x, 0.0001f)
    }

    @Test
    fun `projectOcrRectToRawQuad maps projective transform accurately`() {
        val rect = NormalizedRect(0.1f, 0.1f, 0.1f, 0.1f)
        // Simple scaling + translation homography
        // Scale x by 2, y by 3, translate x by 0.1, y by 0.2
        val hNorm = floatArrayOf(
            2f, 0f, 0.1f,
            0f, 3f, 0.2f,
            0f, 0f, 1f
        )

        val result = CoordinateTransformMapper.projectOcrRectToRawQuad(rect, hNorm)

        assertTrue(result is ProjectionResult.Success)
        val success = result as ProjectionResult.Success
        assertFalse(success.requiresViewportClipping)

        // Expected TL: x = 2*0.1 + 0.1 = 0.3, y = 3*0.1 + 0.2 = 0.5
        assertEquals(0.3f, success.quad.topLeft.x, 0.0001f)
        assertEquals(0.5f, success.quad.topLeft.y, 0.0001f)

        // Expected BR: x = 2*0.2 + 0.1 = 0.5, y = 3*0.2 + 0.2 = 0.8
        assertEquals(0.5f, success.quad.bottomRight.x, 0.0001f)
        assertEquals(0.8f, success.quad.bottomRight.y, 0.0001f)
    }
}
