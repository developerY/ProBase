package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun DynamicWeatherIcon(
    conditions: List<LayeredWeatherCondition>,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherAnimations")
    
    // Efficient global pulse/translation values
    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SlowRotation"
    )
    
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Floating"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        conditions.forEachIndexed { index, condition ->
            val icon = when (condition) {
                LayeredWeatherCondition.SUNNY -> Icons.Rounded.WbSunny
                LayeredWeatherCondition.CLOUDY -> Icons.Rounded.Cloud
                LayeredWeatherCondition.RAINY -> Icons.Rounded.WaterDrop
                LayeredWeatherCondition.THUNDER -> Icons.Rounded.FlashOn
                LayeredWeatherCondition.WINDY -> Icons.Rounded.Air
            }

            val gradientColors = when (condition) {
                LayeredWeatherCondition.SUNNY -> listOf(Color(0xFFFFD600), Color(0xFFFFA000).copy(alpha = 0.6f))
                LayeredWeatherCondition.CLOUDY -> listOf(Color(0xFF90A4AE), Color(0xFF607D8B).copy(alpha = 0.6f))
                LayeredWeatherCondition.RAINY -> listOf(Color(0xFF2196F3), Color(0xFF1976D2).copy(alpha = 0.6f))
                LayeredWeatherCondition.THUNDER -> listOf(Color(0xFFFFC107), Color(0xFFFF8F00).copy(alpha = 0.6f))
                LayeredWeatherCondition.WINDY -> listOf(Color(0xFF81D4FA), Color(0xFF03A9F4).copy(alpha = 0.6f))
            }

            val offset = when {
                conditions.size > 1 -> {
                    if (index == 0) Offset(-30f, -10f) else Offset(30f, 10f)
                }
                else -> Offset.Zero
            }

            val size = if (conditions.size > 1) 80.dp else 100.dp

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        translationX = offset.x
                        translationY = offset.y + (if (condition != LayeredWeatherCondition.SUNNY) floatAnim else 0f)
                        rotationZ = if (condition == LayeredWeatherCondition.SUNNY) slowRotation else 0f
                        alpha = 0.99f
                    }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = gradientColors
                            ),
                            blendMode = BlendMode.SrcIn
                        )
                    },
                tint = Color.Unspecified
            )
        }
    }
}
