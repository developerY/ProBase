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
    trackColor: Color = Color.DarkGray.copy(alpha = 0.3f),
    // ✅ New color for ticks and numbers
    gaugeContentColor: Color = Color.LightGray
) {
    val textMeasurer = rememberTextMeasurer()

    // Padding 2.dp pushes it right to the edge
    Box(modifier = modifier.aspectRatio(1f).padding(2.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            // Thicker gauge
            val strokeWidthPx = 22.dp.toPx()
            // The radius lies dead center of the thick track
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
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt) // Butt cap for cleaner ends
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

            // 3. Draw Ticks and Numbers OVER the bar
            val tickStep = 2
            val majorTickStep = 10

            for (i in 0..maxSpeed.toInt() step tickStep) {
                val fraction = i / maxSpeed
                val angleRad = Math.toRadians((startAngle + fraction * sweepAngle).toDouble())

                val isMajor = i % majorTickStep == 0

                // ✅ Math change: Ticks are centered on the track radius
                // Major ticks are longer than the track width, minor are shorter
                val tickTotalLength = if (isMajor) strokeWidthPx + 6.dp.toPx() else strokeWidthPx - 4.dp.toPx()
                val tickStroke = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()

                val startTickRadius = radius + (tickTotalLength / 2)
                val endTickRadius = radius - (tickTotalLength / 2)

                val startX = center.x + startTickRadius * cos(angleRad).toFloat()
                val startY = center.y + startTickRadius * sin(angleRad).toFloat()
                val endX = center.x + endTickRadius * cos(angleRad).toFloat()
                val endY = center.y + endTickRadius * sin(angleRad).toFloat()

                // Draw tick using LightGray
                drawLine(
                    color = gaugeContentColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )

                // 4. Draw Numbers OVER the bar on major ticks
                if (isMajor) {
                    // ✅ Math change: Text is centered exactly on the track radius
                    val textRadius = radius
                    val textX = center.x + textRadius * cos(angleRad).toFloat()
                    val textY = center.y + textRadius * sin(angleRad).toFloat()

                    val textLayoutResult = textMeasurer.measure(
                        text = i.toString(),
                        style = TextStyle(
                            color = gaugeContentColor, // LightGray text
                            fontSize = 14.sp, // Slightly larger for readability over color
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Draw a subtle dark shadow behind text for contrast over the gradient
                    drawText(
                        textLayoutResult = textLayoutResult,
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(
                            x = (textX - textLayoutResult.size.width / 2) + 2f,
                            y = (textY - textLayoutResult.size.height / 2) + 2f
                        )
                    )

                    // Draw main text
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = textX - textLayoutResult.size.width / 2,
                            y = textY - textLayoutResult.size.height / 2
                        )
                    )
                }
            }

            // 5. Draw Indicator Dot (at the very edge of the active sweep)
            val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            // Push the dot to the outer edge of the track
            val dotRadius = radius + (strokeWidthPx / 2)
            val dotX = center.x + dotRadius * cos(currentAngleRad).toFloat()
            val dotY = center.y + dotRadius * sin(currentAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            // Add a small black outline to the dot for contrast
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