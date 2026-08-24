package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SeasonalQuadrantMap(
    season: String,
    undertoneScore: Float,
    hairLuminance: Float,
    eyeLuminance: Float,
    modifier: Modifier = Modifier
) {
    val isWarm = season.contains("SPRING", ignoreCase = true) || season.contains("AUTUMN", ignoreCase = true)
    val isLight = season.contains("SPRING", ignoreCase = true) || season.contains("SUMMER", ignoreCase = true)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val quadW = canvasW / 2f
            val quadH = canvasH / 2f

            // 1. Define the uniform gap and corner radius for the "white space" look
            val gap = 7.dp.toPx() // This applies to all 4 sides of each box
            val corner = CornerRadius(18.dp.toPx(), 18.dp.toPx())

            // --- DRAW QUADRANTS WITH GRADIENTS ---

            // Summer (Top Left)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5)),
                    start = Offset(0f, 0f),
                    end = Offset(quadW, quadH)
                ),
                topLeft = Offset(gap, gap),
                size = Size(quadW - (gap * 2), quadH - (gap * 2)),
                cornerRadius = corner
            )

            // Spring (Top Right)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFF59D)),
                    start = Offset(quadW, 0f),
                    end = Offset(canvasW, quadH)
                ),
                topLeft = Offset(quadW + gap, gap),
                size = Size(quadW - (gap * 2), quadH - (gap * 2)),
                cornerRadius = corner
            )

            // Winter (Bottom Left)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFBBDEFB), Color(0xFF311B92)),
                    start = Offset(0f, quadH),
                    end = Offset(quadW, canvasH)
                ),
                topLeft = Offset(gap, quadH + gap),
                size = Size(quadW - (gap * 2), quadH - (gap * 2)),
                cornerRadius = corner
            )

            // Autumn (Bottom Right)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFE0B2), Color(0xFFBF360C)),
                    start = Offset(quadW, quadH),
                    end = Offset(canvasW, canvasH)
                ),
                topLeft = Offset(quadW + gap, quadH + gap),
                size = Size(quadW - (gap * 2), quadH - (gap * 2)),
                cornerRadius = corner
            )

            // --- PLOT INDICATOR ---
            val rawX = ((undertoneScore + 1f) / 2f).coerceIn(0f, 1f)
            val rawY = ((hairLuminance + eyeLuminance) / 2f).coerceIn(0f, 1f)

            // Clamp coordinates to correct quadrant
            val clampedX = if (isWarm) rawX.coerceIn(0.55f, 0.95f) else rawX.coerceIn(0.05f, 0.45f)
            val clampedY = if (isLight) rawY.coerceIn(0.55f, 0.95f) else rawY.coerceIn(0.05f, 0.45f)

            val plotX = clampedX * canvasW
            val plotY = (1f - clampedY) * canvasH
            val userPoint = Offset(plotX, plotY)

            // White Glowing Indicator
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = 30f,
                center = userPoint
            )
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = userPoint
            )
        }

        // --- OVERLAY AXIS LABELS ---
        val axisLabelStyle = MaterialTheme.typography.labelMedium.copy(
            color = Color.Black.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Text("Light", style = axisLabelStyle, modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp))
        Text("Deep", style = axisLabelStyle, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp))
        Text("Cool", style = axisLabelStyle, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
        Text("Warm", style = axisLabelStyle, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp))

        // --- OVERLAY QUADRANT LABELS ---
        QuadrantLabelBox("Summer", "Cool/Light", Modifier.align(Alignment.TopStart), false)
        QuadrantLabelBox("Spring", "Warm/Light", Modifier.align(Alignment.TopEnd), false)
        QuadrantLabelBox("Winter", "Cool/Deep", Modifier.align(Alignment.BottomStart), true)
        QuadrantLabelBox("Autumn", "Warm/Deep", Modifier.align(Alignment.BottomEnd), true)
    }
}

@Composable
private fun QuadrantLabelBox(title: String, subtitle: String, modifier: Modifier, isDark: Boolean) {
    val color = if (isDark) Color.White else Color.Black.copy(alpha = 0.8f)
    Column(
        modifier = modifier.fillMaxSize(0.5f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 24.sp
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SeasonalQuadrantMapPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp), color = Color.White) {
            SeasonalQuadrantMap(
                season = "NEUTRAL SPRING",
                undertoneScore = 0.2235f,
                hairLuminance = 0.5515f,
                eyeLuminance = 0.6121f
            )
        }
    }
}
