package com.zoewave.probase.features.ar.facelab.domain

import android.graphics.*
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceRenderingEngine {

    private val makeupPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // Lip landmarks outer and inner for "hollow" fill
    private val outerLipIndices = listOf(61, 185, 40, 39, 37, 0, 267, 269, 270, 409, 291, 375, 321, 405, 314, 17, 84, 181, 91, 146)
    private val innerLipIndices = listOf(78, 191, 80, 81, 82, 13, 312, 311, 310, 415, 308, 324, 318, 402, 317, 14, 87, 178, 88, 95)

    fun drawMakeup(
        canvas: Canvas,
        result: FaceLandmarkerResult,
        colorHex: String,
        category: String,
        width: Int,
        height: Int,
        inputWidth: Int,
        inputHeight: Int
    ) {
        val baseColor = try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.RED
        }

        // Apply some transparency for natural look (Alpha: 0x88 = ~136/255)
        makeupPaint.color = (baseColor and 0x00FFFFFF) or (0x88 shl 24)

        // Calculate scaling and offsets for FILL_CENTER
        val scale = Math.max(width.toFloat() / inputWidth, height.toFloat() / inputHeight)
        val displayWidth = inputWidth * scale
        val displayHeight = inputHeight * scale
        val offsetX = (width - displayWidth) / 2f
        val offsetY = (height - displayHeight) / 2f

        result.faceLandmarks().forEach { faceLandmarks ->
            if (category.contains("Lip", ignoreCase = true)) {
                drawLips(canvas, faceLandmarks, displayWidth, displayHeight, offsetX, offsetY)
            } else if (category.contains("Blush", ignoreCase = true) || category.contains("Cheek", ignoreCase = true)) {
                drawBlush(canvas, faceLandmarks, displayWidth, displayHeight, offsetX, offsetY)
            }
        }
    }

    private fun drawLips(
        canvas: Canvas,
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        displayWidth: Float,
        displayHeight: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        val path = Path().apply {
            fillType = Path.FillType.EVEN_ODD
        }
        
        // Outer loop
        outerLipIndices.forEachIndexed { index, landmarkIndex ->
            val landmark = landmarks[landmarkIndex]
            val x = landmark.x() * displayWidth + offsetX
            val y = landmark.y() * displayHeight + offsetY
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        
        // Inner loop (will be "hollowed out" due to EVEN_ODD if mouth is open)
        innerLipIndices.forEachIndexed { index, landmarkIndex ->
            val landmark = landmarks[landmarkIndex]
            val x = landmark.x() * displayWidth + offsetX
            val y = landmark.y() * displayHeight + offsetY
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        
        canvas.drawPath(path, makeupPaint)
    }

    private fun drawBlush(
        canvas: Canvas,
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        displayWidth: Float,
        displayHeight: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        // Cheeks: Left 234, Right 454
        val leftCheek = landmarks[234]
        val rightCheek = landmarks[454]
        
        val radius = displayWidth * 0.08f // Slightly larger blush
        
        // Use a radial gradient for a more natural blush look
        val blushAlpha = 0x44 // Subtle alpha for blush
        val blushColor = (makeupPaint.color and 0x00FFFFFF) or (blushAlpha shl 24)
        
        val blushPaint = Paint(makeupPaint).apply {
            color = blushColor
        }
        
        // Left
        canvas.drawCircle(
            leftCheek.x() * displayWidth + offsetX,
            leftCheek.y() * displayHeight + offsetY,
            radius,
            blushPaint
        )
        
        // Right
        canvas.drawCircle(
            rightCheek.x() * displayWidth + offsetX,
            rightCheek.y() * displayHeight + offsetY,
            radius,
            blushPaint
        )
    }
}
