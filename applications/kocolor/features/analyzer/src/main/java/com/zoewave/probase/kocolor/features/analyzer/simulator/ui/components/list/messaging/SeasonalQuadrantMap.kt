package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            .aspectRatio(1.5f)
            .padding(vertical = 8.dp)
    ) {
        // 1. The Grid & Data Point
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 24.dp)
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val quadW = canvasW / 2f
            val quadH = canvasH / 2f
            val spacing = 4.dp.toPx()
            val radius = 12.dp.toPx()

            // --- DRAW QUADRANTS WITH GRADIENTS ---
            
            // Summer (Top Left): Cool / Light
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5))),
                topLeft = Offset(0f, 0f),
                size = Size(quadW - spacing, quadH - spacing),
                cornerRadius = CornerRadius(radius)
            )

            // Spring (Top Right): Warm / Light
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFF59D))),
                topLeft = Offset(quadW + spacing, 0f),
                size = Size(quadW - spacing, quadH - spacing),
                cornerRadius = CornerRadius(radius)
            )

            // Winter (Bottom Left): Cool / Deep
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFBBDEFB), Color(0xFF311B92))),
                topLeft = Offset(0f, quadH + spacing),
                size = Size(quadW - spacing, quadH - spacing),
                cornerRadius = CornerRadius(radius)
            )

            // Autumn (Bottom Right): Warm / Deep
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFFFE0B2), Color(0xFFBF360C))),
                topLeft = Offset(quadW + spacing, quadH + spacing),
                size = Size(quadW - spacing, quadH - spacing),
                cornerRadius = CornerRadius(radius)
            )

            // --- DRAW AXES ---
            val axisColor = Color.Gray.copy(alpha = 0.3f)
            drawLine(axisColor, Offset(canvasW / 2f, -12.dp.toPx()), Offset(canvasW / 2f, canvasH + 12.dp.toPx()), 2f)
            drawLine(axisColor, Offset(-12.dp.toPx(), canvasH / 2f), Offset(canvasW + 12.dp.toPx(), canvasH / 2f), 2f)

            // Plot the User's Coordinate
            val plotX = normalizedUndertone * canvasW
            val plotY = (1f - depthScore) * canvasH 
            val userPoint = Offset(plotX, plotY)

            // Glowing Indicator
            drawCircle(
                color = Color(0xFF6750A4).copy(alpha = 0.3f),
                radius = 24f,
                center = userPoint
            )
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = userPoint
            )
            drawCircle(
                color = Color(0xFF6750A4),
                radius = 7f,
                center = userPoint
            )
        }

        // --- LABELS ---
        val labelStyle = MaterialTheme.typography.labelSmall.copy(
            color = Color.Black.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            lineHeight = 12.sp
        )

        // Axis Labels
        Text("Light", style = labelStyle, modifier = Modifier.align(Alignment.TopCenter))
        Text("Deep", style = labelStyle, modifier = Modifier.align(Alignment.BottomCenter))
        Text("Cool", style = labelStyle, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
        Text("Warm", style = labelStyle, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))

        // Quadrant Names
        QuadrantLabel("SUMMER\n(Cool/Light)", Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 50.dp), labelStyle)
        QuadrantLabel("SPRING\n(Warm/Light)", Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 50.dp), labelStyle, TextAlign.End)
        QuadrantLabel("WINTER\n(Cool/Deep)", Modifier.align(Alignment.BottomStart).padding(bottom = 40.dp, start = 50.dp), labelStyle)
        QuadrantLabel("AUTUMN\n(Warm/Deep)", Modifier.align(Alignment.BottomEnd).padding(bottom = 40.dp, end = 50.dp), labelStyle, TextAlign.End)
    }
}

@Composable
private fun QuadrantLabel(text: String, modifier: Modifier, style: TextStyle, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun SeasonalQuadrantMapPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp), color = Color.White) {
            SeasonalQuadrantMap(
                undertoneScore = 0.2235f,
                hairLuminance = 0.5515f,
                eyeLuminance = 0.6121f
            )
        }
    }
}
