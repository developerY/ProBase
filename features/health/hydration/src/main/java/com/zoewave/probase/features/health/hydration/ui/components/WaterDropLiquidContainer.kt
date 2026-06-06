package com.zoewave.probase.features.health.hydration.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sin

val WaterDropShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    
    moveTo(width / 2f, 0f)
    cubicTo(
        x1 = width * 0.1f, y1 = height * 0.4f,
        x2 = 0f, y2 = height * 0.7f,
        x3 = width / 2f, y3 = height
    )
    cubicTo(
        x1 = width, y1 = height * 0.7f,
        x2 = width * 0.9f, y2 = height * 0.4f,
        x3 = width / 2f, y3 = 0f
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

    Box(
        modifier = modifier
            .clip(WaterDropShape),
        contentAlignment = Alignment.Center
    ) {
        // Background Glow/Glass
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = Path().apply {
                    val w = size.width
                    val h = size.height
                    moveTo(w / 2f, 0f)
                    cubicTo(w * 0.1f, h * 0.4f, 0f, h * 0.7f, w / 2f, h)
                    cubicTo(w, h * 0.7f, w * 0.9f, h * 0.4f, w / 2f, 0f)
                    close()
                },
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                    center = center,
                    radius = size.width / 2f
                )
            )
        }

        // Liquid Engine
        WavyLiquidEngine(progress = progress, phase = phase)

        // Specular Highlight
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.3f)) {
            val w = size.width
            val h = size.height
            val highlightPath = Path().apply {
                moveTo(w * 0.45f, h * 0.15f)
                cubicTo(w * 0.35f, h * 0.3f, w * 0.25f, h * 0.5f, w * 0.3f, h * 0.65f)
            }
            drawPath(
                path = highlightPath,
                color = Color.White,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
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
