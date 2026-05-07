package com.zoewave.probase.features.ar.naillab.domain

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class NailRenderingEngine {

    private val nailPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val highlightPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // Landmark indices for finger tips
    private val fingertipIndices = listOf(8, 12, 16, 20, 4)

    fun drawNails(
        canvas: Canvas,
        result: HandLandmarkerResult,
        colorHex: String,
        finish: String,
        width: Int,
        height: Int
    ) {
        val baseColor = try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.RED
        }

        result.landmarks().forEach { handLandmarks ->
            fingertipIndices.forEach { tipIndex ->
                val tip = handLandmarks[tipIndex]
                val prev = handLandmarks[tipIndex - 1] // Joint below tip

                val cx = tip.x() * width
                val cy = tip.y() * height
                
                // Calculate orientation/size based on bone segment
                val dx = (tip.x() - prev.x()) * width
                val dy = (tip.y() - prev.y()) * height
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                
                val nailWidth = distance * 0.4f
                val nailHeight = distance * 0.6f

                drawNailShape(canvas, cx, cy, nailWidth, nailHeight, baseColor, finish)
            }
        }
    }

    private fun drawNailShape(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        baseColor: Int,
        finish: String
    ) {
        val nailPath = Path().apply {
            addOval(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2, Path.Direction.CW)
        }

        when (finish.uppercase()) {
            "GLOSSY" -> {
                nailPaint.color = baseColor
                canvas.drawPath(nailPath, nailPaint)

                // Add glossy highlight
                val highlightGradient = RadialGradient(
                    cx - w * 0.2f, cy - h * 0.2f, w * 0.8f,
                    intArrayOf(Color.WHITE.withAlpha(180), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
                highlightPaint.shader = highlightGradient
                canvas.drawPath(nailPath, highlightPaint)
            }
            "METALLIC" -> {
                val metallicGradient = LinearGradient(
                    cx - w, cy - h, cx + w, cy + h,
                    intArrayOf(baseColor, Color.WHITE.withAlpha(100), baseColor),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                nailPaint.shader = metallicGradient
                canvas.drawPath(nailPath, nailPaint)
                nailPaint.shader = null
            }
            else -> { // MATTE
                nailPaint.color = baseColor
                nailPaint.shader = null
                canvas.drawPath(nailPath, nailPaint)
            }
        }
    }

    private fun Int.withAlpha(alpha: Int): Int {
        return (this and 0x00FFFFFF) or (alpha shl 24)
    }
}
