package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface

@Composable
fun SeasonalQuadrantMap(
    undertoneScore: Float, // X-Axis: Cool (-1.0) to Warm (1.0)
    hairLuminance: Float, 
    eyeLuminance: Float,   
    modifier: Modifier = Modifier
) {
    // Calculate Depth for Y-Axis: 0.0 (Dark) to 1.0 (Light)
    val depthScore = ((hairLuminance + eyeLuminance) / 2f).coerceIn(0f, 1f)
    
    // Normalize undertone to 0.0 - 1.0 for the Canvas (assuming -1.0 to 1.0 range)
    val normalizedUndertone = ((undertoneScore + 1f) / 2f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .padding(vertical = 16.dp)
    ) {
        // 1. The Labels
        val labelStyle = MaterialTheme.typography.labelSmall.copy(
            color = Color.Gray.copy(alpha = 0.7f),
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text("SUMMER\n(Cool/Light)", style = labelStyle, modifier = Modifier.align(Alignment.TopStart))
        Text("SPRING\n(Warm/Light)", style = labelStyle, textAlign = TextAlign.End, modifier = Modifier.align(Alignment.TopEnd))
        Text("WINTER\n(Cool/Deep)", style = labelStyle, modifier = Modifier.align(Alignment.BottomStart))
        Text("AUTUMN\n(Warm/Deep)", style = labelStyle, textAlign = TextAlign.End, modifier = Modifier.align(Alignment.BottomEnd))

        // 2. The Grid & Data Point
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, bottom = 32.dp, start = 16.dp, end = 16.dp) // Inset the grid from labels
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f

            // Draw Quadrant Crosshairs
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(centerX, 0f),
                end = Offset(centerX, canvasH),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, centerY),
                end = Offset(canvasW, centerY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Plot the User's Coordinate
            val plotX = normalizedUndertone * canvasW
            val plotY = (1f - depthScore) * canvasH 
            val userPoint = Offset(plotX, plotY)

            // Glowing Indicator
            drawCircle(
                color = Color(0xFF6750A4).copy(alpha = 0.2f), // Brand purple glow
                radius = 24f,
                center = userPoint
            )
            drawCircle(
                color = Color(0xFF6750A4), // Solid center
                radius = 8f,
                center = userPoint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SeasonalQuadrantMapPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SeasonalQuadrantMap(
                undertoneScore = 0.2235f,
                hairLuminance = 0.1515f,
                eyeLuminance = 0.2121f
            )
        }
    }
}

