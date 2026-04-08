package com.zoewave.probase.goswift.mobile.hydration.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WavyWaterLevel(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "animatedProgress"
    )

    Box(modifier = modifier.fillMaxWidth().height(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val waterLevelY = height * (1f - animatedProgress)
            val waveHeight = 10.dp.toPx()
            val waveLength = width

            val path = Path().apply {
                moveTo(0f, waterLevelY)
                for (x in 0..width.toInt()) {
                    val relativeX = x / waveLength
                    val y = waterLevelY + waveHeight * sin(relativeX * 2 * Math.PI + waveOffset).toFloat()
                    lineTo(x.toFloat(), y)
                }
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = path,
                color = Color(0xFF2196F3).copy(alpha = 0.7f)
            )
            
            // Background water (slightly darker/different alpha)
            val bgPath = Path().apply {
                moveTo(0f, waterLevelY)
                for (x in 0..width.toInt()) {
                    val relativeX = x / waveLength
                    val y = waterLevelY + waveHeight * sin(relativeX * 2 * Math.PI - waveOffset + Math.PI/2).toFloat()
                    lineTo(x.toFloat(), y)
                }
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            
            drawPath(
                path = bgPath,
                color = Color(0xFF2196F3).copy(alpha = 0.4f)
            )
        }
    }
}
