package com.zoewave.ashbike.mobile.rides.ui.components.health

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RideSparkLine(
    dataPoints: List<Float>, // Pass a list of speeds or elevations
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF007AFF), // Your brand blue
    fillColor: Color = Color(0xFF007AFF).copy(alpha = 0.2f)
) {
    if (dataPoints.isEmpty()) return

    // Downsample data if too large (optimization)
    val points = remember(dataPoints) {
        if (dataPoints.size > 100) {
            val step = dataPoints.size / 100
            dataPoints.filterIndexed { index, _ -> index % step == 0 }
        } else dataPoints
    }

    val maxVal = points.maxOrNull() ?: 1f
    val minVal = points.minOrNull() ?: 0f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Safety check
        if (maxVal == minVal) return@Canvas

        val stepX = width / (points.size - 1)
        val path = Path()

        // 1. Calculate Path
        points.forEachIndexed { i, value ->
            // Normalize value to fit height (Invert Y because canvas 0,0 is top-left)
            val normalizedY = height - ((value - minVal) / (maxVal - minVal) * height)
            val x = i * stepX

            if (i == 0) path.moveTo(x, normalizedY)
            else path.lineTo(x, normalizedY)
        }

        // 2. Draw Fill (Gradient below line)
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height) // Bottom right corner
        fillPath.lineTo(0f, height)    // Bottom left corner
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // 3. Draw Line (Stroke)
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}