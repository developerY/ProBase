package com.zoewave.probase.goswift.mobile.nutrition.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zoewave.probase.goswift.mobile.nutrition.ui.MealLog
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CalorieBubbleContainer(
    meals: List<MealLog>,
    modifier: Modifier = Modifier,
    targetCalories: Double = 2500.0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbleBobbing")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val currentTotal = meals.sumOf { it.calories }
    val progress = (currentTotal / targetCalories).toFloat().coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(modifier = modifier.aspectRatio(1f).padding(16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2, height / 2)
            val containerRadius = size.minDimension / 2
            val strokeWidth = 4.dp.toPx()

            // Draw glass container
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.2f),
                radius = containerRadius,
                center = center
            )
            drawCircle(
                color = Color.Gray.copy(alpha = 0.5f),
                radius = containerRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Draw energy "liquid" at bottom (arc representing fill)
            if (animatedProgress > 0) {
                drawArc(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFC107).copy(alpha = 0.3f),
                            Color(0xFFFF9800).copy(alpha = 0.5f)
                        )
                    ),
                    startAngle = 90f - (animatedProgress * 180f),
                    sweepAngle = animatedProgress * 360f,
                    useCenter = true,
                    topLeft = Offset(center.x - containerRadius, center.y - containerRadius),
                    size = size
                )
            }

            // Draw bubbles for each meal
            meals.take(10).forEachIndexed { index, meal ->
                val baseSize = 40.dp.toPx()
                val bubbleSize = (meal.calories / 500.0 * baseSize).coerceIn(10.dp.toPx().toDouble(), 60.dp.toPx().toDouble()).toFloat()
                
                // Deterministic but "random" looking position inside container
                val angleOffset = (index * 137.5).toFloat() // Golden angle
                val radiusPos = containerRadius * 0.6f
                
                val bobbingX = 5.dp.toPx() * sin(time.toDouble() + index).toFloat()
                val bobbingY = 10.dp.toPx() * sin(time.toDouble() * 0.8 + index * 2).toFloat()

                val x = center.x + radiusPos * cos(angleOffset * Math.PI / 180.0).toFloat() + bobbingX
                val y = center.y + radiusPos * sin(angleOffset * Math.PI / 180.0).toFloat() + bobbingY

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFEB3B).copy(alpha = 0.8f),
                            Color(0xFFFF9800).copy(alpha = 0.4f)
                        ),
                        center = Offset(x, y),
                        radius = bubbleSize
                    ),
                    radius = bubbleSize,
                    center = Offset(x, y)
                )
                
                // Bubble highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = bubbleSize * 0.3f,
                    center = Offset(x - bubbleSize * 0.3f, y - bubbleSize * 0.3f)
                )
            }
        }
    }
}
