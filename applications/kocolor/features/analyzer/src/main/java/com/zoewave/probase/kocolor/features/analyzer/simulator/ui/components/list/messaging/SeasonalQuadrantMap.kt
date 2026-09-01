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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.analyzer.R

@Composable
fun SeasonalQuadrantMap(
    season: String, // Source of truth for season label
    undertoneScore: Float, // X-Axis: Cool (-1.0) to Warm (1.0)
    hairLuminance: Float, 
    eyeLuminance: Float,   
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Balanced square grid
    ) {
        // 1. The Grid & Data Point
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val quadW = canvasW / 2f
            val quadH = canvasH / 2f
            val border = 2.dp.toPx() // Minimal border/gap between quadrants

            // --- DRAW QUADRANTS WITH GRADIENTS (INCREASED INTENSITY) ---
            
            // Summer (Top Left): Cool / Light
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFBBDEFB), Color(0xFFF8BBD0)),
                    start = Offset(0f, 0f),
                    end = Offset(quadW, quadH)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(quadW - border/2, quadH - border/2)
            )

            // Spring (Top Right): Warm / Light
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFEB3B)),
                    start = Offset(quadW, 0f),
                    end = Offset(canvasW, quadH)
                ),
                topLeft = Offset(quadW + border/2, 0f),
                size = Size(quadW - border/2, quadH - border/2)
            )

            // Winter (Bottom Left): Cool / Deep
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF64B5F6), Color(0xFF1A237E)),
                    start = Offset(0f, quadH),
                    end = Offset(quadW, canvasH)
                ),
                topLeft = Offset(0f, quadH + border/2),
                size = Size(quadW - border/2, quadH - border/2)
            )

            // Autumn (Bottom Right): Warm / Deep
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFB74D), Color(0xFFBF360C)),
                    start = Offset(quadW, quadH),
                    end = Offset(canvasW, canvasH)
                ),
                topLeft = Offset(quadW + border/2, quadH + border/2),
                size = Size(quadW - border/2, quadH - border/2)
            )

            // --- DRAW HIGH-VISIBILITY GRAPH LINES ---
            val lineColor = Color.White.copy(alpha = 0.8f)
            drawLine(
                color = lineColor,
                start = Offset(quadW, 0f),
                end = Offset(quadW, canvasH),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = lineColor,
                start = Offset(0f, quadH),
                end = Offset(canvasW, quadH),
                strokeWidth = 2.dp.toPx()
            )

            // --- PLOT INDICATOR (Continuous Real-Time Coordinate Mapping) ---
            val normX = ((undertoneScore + 1f) / 2f).coerceIn(0.05f, 0.95f)
            val normY = ((hairLuminance * 0.5f + eyeLuminance * 0.5f)).coerceIn(0.05f, 0.95f)

            val plotX = normX * canvasW
            val plotY = (1f - normY) * canvasH 
            val userPoint = Offset(plotX, plotY)

            // High Contrast Indicator
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = 35f,
                center = userPoint
            )
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = userPoint
            )
            drawCircle(
                color = Color(0xFF6750A4),
                radius = 6f,
                center = userPoint
            )
        }

        // --- OVERLAY AXIS LABELS (HIGH VISIBILITY) ---
        val axisLabelDarkStyle = MaterialTheme.typography.labelMedium.copy(
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Black,

            fontSize = 12.sp
        )

        val axisLabelStyle = MaterialTheme.typography.labelMedium.copy(
            color = Color.Black.copy(alpha = 0.8f),
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
        )

        Text(stringResource(R.string.applications_kocolor_features_analyzer_light), style = axisLabelStyle, modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp))
        Text(stringResource(R.string.applications_kocolor_features_analyzer_deep), style = axisLabelDarkStyle, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
        Text(stringResource(R.string.applications_kocolor_features_analyzer_cool), style = axisLabelStyle, modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp))
        Text(stringResource(R.string.applications_kocolor_features_analyzer_warm), style = axisLabelStyle, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp))

        // --- OVERLAY QUADRANT LABELS (HIGH VISIBILITY) ---
        QuadrantLabelBox(
            title = "Summer",
            subtitle = "Cool/Light",
            modifier = Modifier.align(Alignment.TopStart),
            isDark = false
        )
        QuadrantLabelBox(
            title = "Spring",
            subtitle = "Warm/Light",
            modifier = Modifier.align(Alignment.TopEnd),
            isDark = false
        )
        QuadrantLabelBox(
            title = "Winter",
            subtitle = "Cool/Deep",
            modifier = Modifier.align(Alignment.BottomStart),
            isDark = true
        )
        QuadrantLabelBox(
            title = "Autumn",
            subtitle = "Warm/Deep",
            modifier = Modifier.align(Alignment.BottomEnd),
            isDark = true
        )
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
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontSize = 28.sp
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
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
