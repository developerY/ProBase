package com.zoewave.probase.ashbike.wear.features.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun WearSpeedometer(
    currentSpeed: Float,
    maxSpeed: Float = 40f,
    modifier: Modifier = Modifier,
    indicatorBrush: Brush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF81C784), // Green
            Color(0xFFFFF176), // Yellow
            Color(0xFFFF5252)  // Red
        )
    ),
    trackColor: Color = Color.DarkGray.copy(alpha = 0.3f)
) {
    // This allows us to measure and draw text directly onto the Canvas
    val textMeasurer = rememberTextMeasurer()

    // Reduced padding to push the thicker gauge to the absolute edge of the watch
    Box(modifier = modifier.aspectRatio(1f).padding(2.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            // 1. Thicker Gauge
            val strokeWidthPx = 20.dp.toPx()
            val radius = (min(w, h) - strokeWidthPx) / 2

            // 2. Wrap all the way around (Bottom-Left to Bottom-Right)
            val startAngle = 135f
            val sweepAngle = 270f

            // Draw Background Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Draw Active Gradient Arc
            val speedFraction = (currentSpeed / maxSpeed).coerceIn(0f, 1f)
            val activeSweep = sweepAngle * speedFraction

            if (currentSpeed > 0) {
                drawArc(
                    brush = indicatorBrush,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }

            // 3. Draw Ticks and Numbers
            val innerRadius = radius - (strokeWidthPx / 2)
            val tickStep = 2 // Minor tick every 2 km/h
            val majorTickStep = 10 // Number every 10 km/h

            for (i in 0..maxSpeed.toInt() step tickStep) {
                val fraction = i / maxSpeed
                val angleRad = Math.toRadians((startAngle + fraction * sweepAngle).toDouble())

                val isMajor = i % majorTickStep == 0
                val tickLength = if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                val tickStroke = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                // Calculate tick line coordinates (pointing inward from the track)
                val startTickRadius = innerRadius - 2.dp.toPx()
                val endTickRadius = startTickRadius - tickLength

                val startX = center.x + startTickRadius * cos(angleRad).toFloat()
                val startY = center.y + startTickRadius * sin(angleRad).toFloat()
                val endX = center.x + endTickRadius * cos(angleRad).toFloat()
                val endY = center.y + endTickRadius * sin(angleRad).toFloat()

                // Draw the physical tick mark
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickStroke
                )

                // 4. Draw the Numbers on Major Ticks
                if (isMajor) {
                    val textRadius = endTickRadius - 12.dp.toPx() // Push text further inside
                    val textX = center.x + textRadius * cos(angleRad).toFloat()
                    val textY = center.y + textRadius * sin(angleRad).toFloat()

                    val textLayoutResult = textMeasurer.measure(
                        text = i.toString(),
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Draw the text perfectly centered on its coordinate
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = textX - textLayoutResult.size.width / 2,
                            y = textY - textLayoutResult.size.height / 2
                        )
                    )
                }
            }

            // 5. Draw Indicator Dot
            val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            val dotX = center.x + radius * cos(currentAngleRad).toFloat()
            val dotY = center.y + radius * sin(currentAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidthPx / 2.5f,
                center = Offset(dotX, dotY)
            )
        }
    }
}