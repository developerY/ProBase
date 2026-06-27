package com.zoewave.probase.features.health.hydration.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Preview(showBackground = true)
@Composable
private fun WaterDropVisualPreview() {
    Box(modifier = Modifier.size(200.dp)) {
        WaterDropVisual(progress = 0.4f)
    }
}

@Preview(showBackground = true)
@Composable
private fun WaterDropLiquidContainerPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(Color.Gray).padding(16.dp)) {
            WaterDropLiquidContainer(progress = 0.7f)
        }
    }
}

val WaterDropShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    
    // Top Tip
    moveTo(w / 2f, 0f)
    
    // Elegant neck to belly (narrower)
    cubicTo(
        x1 = w * 0.5f, y1 = h * 0.1f,
        x2 = w * 0.1f, y2 = h * 0.4f,
        x3 = w * 0.1f, y3 = h * 0.65f
    )
    
    // Smoothly rounded base
    cubicTo(
        x1 = w * 0.1f, y1 = h * 0.98f,
        x2 = w * 0.9f, y2 = h * 0.98f,
        x3 = w * 0.9f, y3 = h * 0.65f
    )
    
    // Right side back to tip
    cubicTo(
        x1 = w * 0.9f, y1 = h * 0.4f,
        x2 = w * 0.5f, y2 = h * 0.1f,
        x3 = w / 2f, y3 = 0f
    )
    
    close()
}

@Composable
fun WaterDropVisual(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_flow")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val textMeasurer = rememberTextMeasurer()
    val markerStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 15.sp,
        color = Color.Black.copy(alpha = 0.45f),
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Outer Halo Glow (Not clipped)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outlinePath = WaterDropShape.createOutline(size, layoutDirection, this).let { (it as Outline.Generic).path }
            drawPath(
                path = outlinePath,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE1F5FE).copy(alpha = 0.5f), Color.Transparent),
                    center = center,
                    radius = size.width / 1.1f
                )
            )
        }

        // 2. The Water Drop Container (Clipped for Liquid)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(WaterDropShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val outlinePath = WaterDropShape.createOutline(size, layoutDirection, this).let { (it as Outline.Generic).path }
                
                // Glass Body Fill
                drawPath(
                    path = outlinePath,
                    color = Color.Black.copy(alpha = 0.06f)
                )

                // Bold Outline
                drawPath(
                    path = outlinePath,
                    color = Color(0xFF424242).copy(alpha = 0.25f),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Liquid Engine
            WavyLiquidEngine(progress = progress, phase = phase)
        }

        // 3. Time Markers (Drawn on top, NOT clipped so numbers stay whole)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Lowered top marker to 0.85f to avoid the narrow tip
            val markers = listOf(
                "08:00" to 0.22f,
                "12:00" to 0.50f,
                "16:00" to 0.72f,
                "20:00" to 0.88f
            )

            markers.forEach { (time, pos) ->
                val y = h * (1f - pos)
                val textLayoutResult = textMeasurer.measure(time, markerStyle)
                val textWidth = textLayoutResult.size.width
                val textHeight = textLayoutResult.size.height

                // Gauge line (Subtle)
                drawLine(
                    color = Color.Black.copy(alpha = 0.06f),
                    start = Offset(w * 0.25f, y),
                    end = Offset(w * 0.75f, y),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw Time Text centered
                drawText(
                    textMeasurer = textMeasurer,
                    text = time,
                    style = markerStyle,
                    topLeft = Offset(w / 2f - textWidth / 2f, y - textHeight / 2f)
                )
            }
        }

        // 4. Specular Highlight
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
            val w = size.width
            val h = size.height
            val highlightPath = Path().apply {
                moveTo(w * 0.45f, h * 0.15f)
                cubicTo(w * 0.35f, h * 0.3f, w * 0.25f, h * 0.5f, w * 0.3f, h * 0.65f)
            }
            drawPath(
                path = highlightPath,
                color = Color.White,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun WaterDropLiquidContainer(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Current Progress",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            color = Color.White.copy(alpha = 0.9f)
        )

        WaterDropVisual(
            progress = progress,
            modifier = Modifier.size(280.dp)
        )

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            color = Color.White
        )
    }
}

@Composable
fun WavyLiquidEngine(progress: Float, phase: Float) {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.8f)) {
        val height = size.height
        val width = size.width
        val baseLine = height * (1f - progress)

        // 1. Deep Layer
        drawWaterLayer(
            width = width,
            height = height,
            baseLine = baseLine,
            phase = phase,
            waveHeight = 10.dp.toPx(),
            frequency = 1.2f,
            brush = Brush.verticalGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0)))
        )

        // 2. Surface Layer
        drawWaterLayer(
            width = width,
            height = height,
            baseLine = baseLine,
            phase = -phase * 0.8f,
            waveHeight = 12.dp.toPx(),
            frequency = 0.9f,
            brush = Brush.verticalGradient(listOf(Color(0xCC64B5F6), Color(0xCC2196F3)))
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWaterLayer(
    width: Float,
    height: Float,
    baseLine: Float,
    phase: Float,
    waveHeight: Float,
    frequency: Float,
    brush: Brush
) {
    val path = Path()
    path.moveTo(0f, baseLine)
    for (x in 0..width.toInt() step 5) {
        val y = baseLine + sin((x / width * frequency * Math.PI) + phase).toFloat() * waveHeight
        path.lineTo(x.toFloat(), y)
    }
    path.lineTo(width, height)
    path.lineTo(0f, height)
    path.close()
    drawPath(path, brush = brush)
}
