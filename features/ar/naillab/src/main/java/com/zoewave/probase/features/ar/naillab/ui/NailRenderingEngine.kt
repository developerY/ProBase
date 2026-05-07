package com.zoewave.probase.features.ar.naillab.ui

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

    fun drawNails(
        canvas: Canvas,
        result: HandLandmarkerResult,
        colorHex: String,
        finish: String,
        width: Int,
        height: Int
    ) {
        val colorInt = try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.RED
        }

        result.landmarks().forEach { handLandmarks ->
            // Fingertip indices: 4 (thumb), 8, 12, 16, 20
            val fingerTips = listOf(4, 8, 12, 16, 20)
            val fingerJoints = listOf(3, 7, 11, 15, 19)

            for (i in fingerTips.indices) {
                val tip = handLandmarks[fingerTips[i]]
                val joint = handLandmarks[fingerJoints[i]]

                val tipX = tip.x() * width
                val tipY = tip.y() * height
                val jointX = joint.x() * width
                val jointY = joint.y() * height

                drawSingleNail(canvas, tipX, tipY, jointX, jointY, colorInt, finish)
            }
        }
    }

    private fun drawSingleNail(
        canvas: Canvas,
        tipX: Float,
        tipY: Float,
        jointX: Float,
        jointY: Float,
        color: Int,
        finish: String
    ) {
        // Calculate vector from joint to tip
        val dx = tipX - jointX
        val dy = tipY - jointY
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (dist < 5f) return // Too small to draw

        // Estimate nail dimensions
        val nailLength = dist * 0.6f
        val nailWidth = dist * 0.4f

        // Calculate perpendicular vector for width
        val px = -dy / dist * nailWidth
        val py = dx / dist * nailWidth

        // Define nail path (elliptical shape centered near the tip)
        val path = Path()
        val centerX = tipX - dx * 0.2f
        val centerY = tipY - dy * 0.2f

        // Simple diamond/oval path for now
        path.moveTo(centerX + px, centerY + py)
        path.lineTo(tipX, tipY)
        path.lineTo(centerX - px, centerY - py)
        path.lineTo(centerX - dx * 0.4f, centerY - dy * 0.4f)
        path.close()

        // Apply finish-specific effects
        when (finish.uppercase()) {
            "MATTE" -> {
                nailPaint.color = color
                nailPaint.shader = null
                canvas.drawPath(path, nailPaint)
            }
            "GLOSSY" -> {
                nailPaint.color = color
                nailPaint.shader = null
                canvas.drawPath(path, nailPaint)

                // Add a specular highlight
                val highlightColor = Color.argb(150, 255, 255, 255)
                highlightPaint.shader = RadialGradient(
                    tipX - dx * 0.1f, tipY - dy * 0.1f, dist * 0.2f,
                    highlightColor, Color.TRANSPARENT, Shader.TileMode.CLAMP
                )
                canvas.drawPath(path, highlightPaint)
            }
            "METALLIC" -> {
                val darkerColor = shadeColor(color, -0.3f)
                val lighterColor = shadeColor(color, 0.3f)
                
                nailPaint.shader = LinearGradient(
                    jointX, jointY, tipX, tipY,
                    intArrayOf(darkerColor, lighterColor, darkerColor),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(path, nailPaint)
            }
            else -> {
                nailPaint.color = color
                nailPaint.shader = null
                canvas.drawPath(path, nailPaint)
            }
        }
    }

    private fun shadeColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = Math.min(255f, Math.max(0f, Color.red(color) * (1 + factor))).toInt()
        val g = Math.min(255f, Math.max(0f, Color.green(color) * (1 + factor))).toInt()
        val b = Math.min(255f, Math.max(0f, Color.blue(color) * (1 + factor))).toInt()
        return Color.argb(a, r, g, b)
    }
}
