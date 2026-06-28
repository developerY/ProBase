package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.weather.R

@Preview(showBackground = true, backgroundColor = 0xFFF9F7F2)
@Composable
fun AtmosphericUVGaugeCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AtmosphericUVGaugeCard(uvIndex = 6.2)
        }
    }
}

@Composable
fun AtmosphericUVGaugeCard(
    uvIndex: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val level = uvIndex.toInt()
    val levelText = when {
        level < 3 -> stringResource(R.string.features_weather_uv_level_low)
        level < 6 -> stringResource(R.string.features_weather_uv_level_moderate)
        level < 8 -> stringResource(R.string.features_weather_uv_level_high)
        level < 11 -> stringResource(R.string.features_weather_uv_level_very_high)
        else -> stringResource(R.string.features_weather_uv_level_extreme)
    }
    val recommendation = when {
        level < 3 -> stringResource(R.string.features_weather_uv_rec_standard)
        level < 8 -> stringResource(R.string.features_weather_uv_rec_spf30)
        else -> stringResource(R.string.features_weather_uv_rec_spf50)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onClick() },
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
                    text = stringResource(R.string.features_weather_uv_level_format, level, levelText),
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
