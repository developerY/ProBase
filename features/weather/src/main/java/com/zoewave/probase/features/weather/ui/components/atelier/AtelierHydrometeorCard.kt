package com.zoewave.probase.features.weather.ui.components.atelier

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.weather.ui.components.backgrounds.Raindrop
import com.zoewave.probase.features.weather.ui.components.snow.Snowflake
import kotlin.math.roundToInt

@Preview(showBackground = true, backgroundColor = 0xFFF9F7F2)
@Composable
fun AtelierHydrometeorCardRainPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AtelierHydrometeorCard(
                label = "Rain Volume",
                value = 2.5,
                isRain = true,
                isActive = true
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F7F2)
@Composable
fun AtelierHydrometeorCardSnowPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AtelierHydrometeorCard(
                label = "Snow Volume",
                value = 1.2,
                isRain = false,
                isActive = true
            )
        }
    }
}

@Composable
fun AtelierHydrometeorCard(
    label: String,
    value: Double,
    isRain: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hydrometeor_anim")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Pattern Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(if (isRain) Color(0xFFE1F5FE).copy(alpha = 0.3f) else Color(0xFFF3E5F5).copy(alpha = 0.3f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (isRain) {
                        val spacing = 12.dp.toPx()
                        for (i in -10..20) {
                            val x = i * spacing + (if (isActive) offset else 0f)
                            drawLine(
                                color = Color(0xFF2196F3).copy(alpha = 0.2f),
                                start = Offset(x, 0f),
                                end = Offset(x - 20.dp.toPx(), size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    } else {
                        // Snow Pattern
                        val spacing = 20.dp.toPx()
                        for (i in 0..10) {
                            for (j in 0..5) {
                                val x = i * spacing + (if (isActive) offset * 0.5f else 0f)
                                val y = j * spacing + (if (isActive) offset else 0f)
                                drawCircle(
                                    color = Color(0xFF90CAF9).copy(alpha = 0.3f),
                                    radius = 2.dp.toPx(),
                                    center = Offset(x % size.width, y % size.height)
                                )
                            }
                        }
                    }
                }
            }

            // Value Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${"%.1f".format(value)} mm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
