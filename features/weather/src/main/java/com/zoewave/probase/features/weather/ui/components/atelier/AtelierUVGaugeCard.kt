package com.zoewave.probase.features.weather.ui.components.atelier

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AtelierUVGaugeCard(
    uvIndex: Double,
    modifier: Modifier = Modifier
) {
    val level = uvIndex.toInt()
    val levelText = when {
        level < 3 -> "Low"
        level < 6 -> "Moderate"
        level < 8 -> "High"
        level < 11 -> "Very High"
        else -> "Extreme"
    }
    val recommendation = when {
        level < 3 -> "Standard care"
        level < 8 -> "Broad-spectrum SPF 30+ recommended"
        else -> "Broad-spectrum SPF 50+ required"
    }

    Card(
        modifier = modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f).aspectRatio(2f)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val arcRadius = width / 2f
                    val strokeWidth = 20.dp.toPx()
                    
                    // Background Arc
                    drawArc(
                        color = Color.Black.copy(alpha = 0.05f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(0f, strokeWidth / 2f),
                        size = Size(width, width),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Gradient Arc
                    drawArc(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFFF8A65), Color(0xFFD32F2F))
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(0f, strokeWidth / 2f),
                        size = Size(width, width),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Needle
                    val angle = 180f + (uvIndex.coerceIn(0.0, 12.0) / 12.0 * 180f).toFloat()
                    val needleCenter = Offset(width / 2f, width / 2f + strokeWidth / 2f)
                    
                    rotate(degrees = angle + 90f, pivot = needleCenter) {
                        val path = Path().apply {
                            moveTo(needleCenter.x, needleCenter.y - arcRadius * 0.9f)
                            lineTo(needleCenter.x - 4.dp.toPx(), needleCenter.y)
                            lineTo(needleCenter.x + 4.dp.toPx(), needleCenter.y)
                            close()
                        }
                        drawPath(path, color = Color.Black.copy(alpha = 0.7f))
                    }
                    
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = needleCenter)
                    drawCircle(color = Color.Black.copy(alpha = 0.7f), radius = 3.dp.toPx(), center = needleCenter)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Level $level - $levelText",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
