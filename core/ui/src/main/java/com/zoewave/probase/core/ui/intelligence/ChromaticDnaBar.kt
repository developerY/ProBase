package com.zoewave.probase.core.ui.intelligence

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A high-fidelity visualization of a product's CIELAB color profile.
 * Maps A* (Red/Green) and B* (Yellow/Blue) onto a 2D grid and L* (Luminosity) onto a vertical axis.
 */
@Composable
fun ChromaticDnaBar(
    l: Float,
    a: Float,
    b: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color(0xFFFBF8F5), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. CIELAB 2D Chromaticity Map (A* vs B*)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
        ) {
            ChromaticityGrid(a = a, b = b, modifier = Modifier.fillMaxSize())
            
            Text(
                text = "UNDERTONE MAP",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // 2. Luminosity Depth Axis (L*)
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LuminosityAxis(l = l, modifier = Modifier.weight(1f))
            
            Text(
                text = "DEPTH",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ChromaticityGrid(a: Float, b: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(12.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2
        val centerY = h / 2

        // Draw Axes
        drawLine(Color.Black.copy(alpha = 0.05f), Offset(0f, centerY), Offset(w, centerY), strokeWidth = 1.dp.toPx())
        drawLine(Color.Black.copy(alpha = 0.05f), Offset(centerX, 0f), Offset(centerX, h), strokeWidth = 1.dp.toPx())

        // Map A (-128 to 127) and B (-128 to 127) to 0..1
        // We normalize to a sensible range (e.g. -60 to 60) for cosmetics
        val range = 60f
        val posX = centerX + (a / range) * (w / 2)
        val posY = centerY - (b / range) * (h / 2) // Invert Y for Cartesian feel

        // Draw Crosshair
        drawCircle(
            color = Color(0xFF745E7A),
            radius = 6.dp.toPx(),
            center = Offset(posX.coerceIn(0f, w), posY.coerceIn(0f, h))
        )
        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = Offset(posX.coerceIn(0f, w), posY.coerceIn(0f, h)),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun LuminosityAxis(l: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(12.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color.White, Color.Black)))
    ) {
        // Marker at L* percentage (0 to 100)
        val markerPos = (100f - l) / 100f
        
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight(markerPos)
        )
        
        Box(
            modifier = Modifier
                .padding(top = (100.dp * markerPos).coerceAtLeast(0.dp)) // Approximation for visual marker
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFF745E7A), CircleShape)
        )
    }
}
