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

    Box(modifier = modifier.aspectRatio(1f).padding(2.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            val strokeWidthPx = 22.dp.toPx()
            val radius = (min(w, h) - strokeWidthPx) / 2

            val startAngle = 135f
            val sweepAngle = 270f

            // 1. Draw Background Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
            )

            // 2. Draw Active Gradient Arc
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
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                )
            }

            // 3. Draw "Cutout" Ticks and Inner Numbers
            val tickStep = 2
            val majorTickStep = 10

            for (i in 0..maxSpeed.toInt() step tickStep) {
                val fraction = i / maxSpeed
                val angleRad = Math.toRadians((startAngle + fraction * sweepAngle).toDouble())

                val isMajor = i % majorTickStep == 0

                // THE CUTOUT TRICK:
                // Draw ticks exactly the width of the track, using Black to "slice" it
                val tickStroke = if (isMajor) 4.dp.toPx() else 1.5.dp.toPx()
                val startTickRadius = radius + (strokeWidthPx / 2)
                val endTickRadius = radius - (strokeWidthPx / 2)

                val startX = center.x + startTickRadius * cos(angleRad).toFloat()
                val startY = center.y + startTickRadius * sin(angleRad).toFloat()
                val endX = center.x + endTickRadius * cos(angleRad).toFloat()
                val endY = center.y + endTickRadius * sin(angleRad).toFloat()

                // Use Color.Black to create the negative space segments
                drawLine(
                    color = Color.Black,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Butt
                )

                // 4. Draw Numbers INSIDE the track
                // We only draw numbers for 10, 20, 30 to prevent UI clutter
                if (isMajor && i > 0 && i < maxSpeed.toInt()) {

                    // Push the text inward into the black background
                    val textRadius = radius - (strokeWidthPx / 2) - 14.dp.toPx()
                    val textX = center.x + textRadius * cos(angleRad).toFloat()
                    val textY = center.y + textRadius * sin(angleRad).toFloat()

                    val textLayoutResult = textMeasurer.measure(
                        text = i.toString(),
                        style = TextStyle(
                            color = Color.LightGray, // Safe to use here since background is black
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

            // 5. Draw Indicator Dot
            val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            val dotRadius = radius + (strokeWidthPx / 2)
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