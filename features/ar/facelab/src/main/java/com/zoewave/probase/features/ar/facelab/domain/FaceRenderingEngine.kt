package com.zoewave.probase.features.ar.facelab.domain

import android.graphics.*
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceRenderingEngine {

    private val makeupPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // Lip landmarks (outer)
    private val lipLandmarks = listOf(61, 146, 91, 181, 84, 17, 314, 405, 321, 375, 291, 409, 270, 269, 267, 0, 37, 39, 40, 185)

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

        // Apply some transparency for natural look
        makeupPaint.color = (baseColor and 0x00FFFFFF) or (0x99 shl 24)

        // Calculate scaling and offsets for FILL_CENTER
        val scale = Math.max(width.toFloat() / inputWidth, height.toFloat() / inputHeight)
        val displayWidth = inputWidth * scale
        val displayHeight = inputHeight * scale
        val offsetX = (width - displayWidth) / 2f
        val offsetY = (height - displayHeight) / 2f

        result.faceLandmarks().forEach { faceLandmarks ->
            if (category.contains("Lip", ignoreCase = true)) {
                drawLips(canvas, faceLandmarks, displayWidth, displayHeight, offsetX, offsetY)
            } else if (category.contains("Blush", ignoreCase = true)) {
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
        val path = Path()
        lipLandmarks.forEachIndexed { index, landmarkIndex ->
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
        // Simple blush on cheeks
        // Left cheek: around 234, Right cheek: around 454
        val leftCheek = landmarks[234]
        val rightCheek = landmarks[454]
        
        val radius = displayWidth * 0.05f
        
        // Left blush
        canvas.drawCircle(
            leftCheek.x() * displayWidth + offsetX,
            leftCheek.y() * displayHeight + offsetY,
            radius,
            makeupPaint
        )
        
        // Right blush
        canvas.drawCircle(
            rightCheek.x() * displayWidth + offsetX,
            rightCheek.y() * displayHeight + offsetY,
            radius,
            makeupPaint
        )
    }
}
