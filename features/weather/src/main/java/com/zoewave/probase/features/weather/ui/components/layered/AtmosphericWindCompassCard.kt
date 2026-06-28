package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true, backgroundColor = 0xFFF9F7F2)
@Composable
fun AtmosphericWindCompassCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AtmosphericWindCompassCard(degree = 240, speed = 3.5)
        }
    }
}

@Composable
fun AtmosphericWindCompassCard(
    degree: Int,
    speed: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(280.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Wind",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black.copy(alpha = 0.7f),
                fontFamily = FontFamily.Serif
            )

            Box(modifier = Modifier.size(140.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f
                    
                    // Draw outer circle
                    drawCircle(
                        color = Color(0xFFE0C097).copy(alpha = 0.3f),
                        radius = radius,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Draw Inner circles
                    drawCircle(
                        color = Color(0xFFE0C097).copy(alpha = 0.2f),
                        radius = radius * 0.9f,
                        style = Stroke(width = 0.5.dp.toPx())
                    )

                    rotate(degrees = degree.toFloat(), pivot = centerOffset) {
                        val needlePath = Path().apply {
                            moveTo(centerOffset.x, centerOffset.y - radius * 0.8f)
                            lineTo(centerOffset.x - 8.dp.toPx(), centerOffset.y)
                            lineTo(centerOffset.x, centerOffset.y + 4.dp.toPx())
                            lineTo(centerOffset.x + 8.dp.toPx(), centerOffset.y)
                            close()
                        }
                        drawPath(needlePath, color = Color(0xFFE0C097))
                    }
                }
                
                // Overlay text markers manually for simplicity
                Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(4.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("S", modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("W", modifier = Modifier.align(Alignment.CenterStart).padding(4.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("E", modifier = Modifier.align(Alignment.CenterEnd).padding(4.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Text(
                text = "${degree}° - ${"%.1f".format(speed)} m/s",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }
    }
}
