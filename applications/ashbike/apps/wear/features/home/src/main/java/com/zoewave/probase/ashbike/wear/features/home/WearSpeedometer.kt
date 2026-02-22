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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.graphics.Path
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
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.aspectRatio(1f).padding(12.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            // 1. Define the Tapering Thickness
            val minThicknessPx = 6.dp.toPx()  // Thin at 0 km/h
            val maxThicknessPx = 28.dp.toPx() // Thick at 40 km/h

            // The outer edge remains a perfect circle
            val radius = min(w, h) / 2

            val startAngle = 135f
            val sweepAngle = 270f

            // Helper function to build a tapered path
            fun buildTaperedPath(targetSweep: Float): Path {
                val path = Path()
                if (targetSweep <= 0f) return path

                // Use steps to calculate points along the curve smoothly
                val steps = (targetSweep / 2f).toInt().coerceAtLeast(1)

                // Draw Outer Curve (Forward)
                for (i in 0..steps) {
                    val fraction = i.toFloat() / steps
                    val angle = startAngle + fraction * targetSweep
                    val rad = Math.toRadians(angle.toDouble())
                    val x = center.x + radius * cos(rad).toFloat()
                    val y = center.y + radius * sin(rad).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                // Draw Inner Curve (Backward)
                for (i in steps downTo 0) {
                    val fraction = i.toFloat() / steps
                    val angle = startAngle + fraction * targetSweep

                    // Thickness is based on the fraction of the TOTAL 270 deg sweep
                    val totalFraction = (fraction * targetSweep) / sweepAngle
                    val currentThickness = minThicknessPx + totalFraction * (maxThicknessPx - minThicknessPx)

                    val innerRadius = radius - currentThickness
                    val rad = Math.toRadians(angle.toDouble())
                    val x = center.x + innerRadius * cos(rad).toFloat()
                    val y = center.y + innerRadius * sin(rad).toFloat()
                    path.lineTo(x, y)
                }
                path.close()
                return path
            }

            // 2. Draw Background Tapered Track
            drawPath(
                path = buildTaperedPath(sweepAngle),
                color = trackColor
            )

            // 3. Draw Active Gradient Tapered Track
            val speedFraction = (currentSpeed / maxSpeed).coerceIn(0f, 1f)
            val activeSweep = sweepAngle * speedFraction

            if (currentSpeed > 0) {
                drawPath(
                    path = buildTaperedPath(activeSweep),
                    brush = indicatorBrush
                )
            }

            // 4. Draw Cutouts and Contour Numbers
            val tickStep = 2
            val majorTickStep = 10

            for (i in 0..maxSpeed.toInt() step tickStep) {
                val fraction = i / maxSpeed
                val angleRad = Math.toRadians((startAngle + fraction * sweepAngle).toDouble())
                val isMajor = i % majorTickStep == 0

                // Calculate the exact thickness of the track at this specific angle
                val currentThickness = minThicknessPx + fraction * (maxThicknessPx - minThicknessPx)
                val innerRadius = radius - currentThickness

                // Draw Black Cutout Ticks
                val tickStroke = if (isMajor) 4.dp.toPx() else 1.5.dp.toPx()

                val startX = center.x + (innerRadius - 2.dp.toPx()) * cos(angleRad).toFloat()
                val startY = center.y + (innerRadius - 2.dp.toPx()) * sin(angleRad).toFloat()
                val endX = center.x + (radius + 2.dp.toPx()) * cos(angleRad).toFloat()
                val endY = center.y + (radius + 2.dp.toPx()) * sin(angleRad).toFloat()

                drawLine(
                    color = Color.Black,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Butt
                )

                // Draw Numbers dynamically contoured to the sloped inner edge
                if (isMajor && i > 0 && i < maxSpeed.toInt()) {
                    val textRadius = innerRadius - 14.dp.toPx() // Push text safely into the void
                    val textX = center.x + textRadius * cos(angleRad).toFloat()
                    val textY = center.y + textRadius * sin(angleRad).toFloat()

                    val textLayoutResult = textMeasurer.measure(
                        text = i.toString(),
                        style = TextStyle(
                            color = Color.White, // Crisp white fixes the "hard to see gray" issue
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = textX - textLayoutResult.size.width / 2,
                            y = textY - textLayoutResult.size.height / 2
                        )
                    )
                }
            }

            // 5. Draw Indicator Dot (Riding the center of the current thickness)
            val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            val activeThickness = minThicknessPx + speedFraction * (maxThicknessPx - minThicknessPx)
            val dotRadius = radius - (activeThickness / 2) // Perfect center line

            val dotX = center.x + dotRadius * cos(currentAngleRad).toFloat()
            val dotY = center.y + dotRadius * sin(currentAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = Color.Black,
                radius = 6.dp.toPx(),
                center = Offset(dotX, dotY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Small Watch"
)
@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Large Watch"
)
@Composable
fun WearSpeedometerPreview() {
    MaterialTheme {
        // Wrapping it in a Box with fillMaxSize to simulate the full watch face
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            WearSpeedometer(
                currentSpeed = 24f, // Set a test speed so the gauge lights up
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}