package com.zoewave.probase.features.health.hydration.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Preview(showBackground = true)
@Composable
private fun WavyLiquidEnginePreview() {
    WavyLiquidEngine(progress = 0.5f)
}

@Composable
fun WavyLiquidEngine(progress: Float, modifier: Modifier = Modifier) {
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

    Canvas(modifier = modifier.fillMaxSize().alpha(0.2f)) {
        val height = size.height
        val width = size.width
        val baseLine = height * (1f - progress)

        // 1. Background Liquid Depth
        drawWaterLayer(
            width = width,
            height = height,
            baseLine = baseLine,
            phase = phase,
            waveHeight = 12.dp.toPx(),
            frequency = 1.2f,
            brush = Brush.verticalGradient(listOf(Color(0xFFBBDEFB), Color(0xFF64B5F6)))
        )

        // 2. Mid-level Refraction
        drawWaterLayer(
            width = width,
            height = height,
            baseLine = baseLine,
            phase = -phase * 0.7f,
            waveHeight = 16.dp.toPx(),
            frequency = 0.8f,
            brush = Brush.verticalGradient(listOf(Color(0x8090CAF9), Color(0x802196F3)))
        )

        // 3. Specular Surface Edge
        val surfacePath = Path()
        surfacePath.moveTo(0f, baseLine)
        for (x in 0..width.toInt() step 5) {
            val y = baseLine + sin((x / width * 2.0 * Math.PI) + phase).toFloat() * 10.dp.toPx()
            surfacePath.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = surfacePath,
            color = Color.White.copy(alpha = 0.4f),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private fun DrawScope.drawWaterLayer(
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
    for (x in 0..width.toInt() step 10) {
        val y = baseLine + sin((x / width * frequency * Math.PI) + phase).toFloat() * waveHeight
        path.lineTo(x.toFloat(), y)
    }
    path.lineTo(width, height)
    path.lineTo(0f, height)
    path.close()
    drawPath(path, brush = brush)
}
