package com.zoewave.probase.features.weather.ui.components.atelier

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun AtelierThermometerCard(
    temp: Double,
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
                text = "Temperature",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black.copy(alpha = 0.7f),
                fontFamily = FontFamily.Serif
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFE0C097),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.width(40.dp).fillMaxHeight()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val bulbRadius = 8.dp.toPx()
                        val stemWidth = 6.dp.toPx()
                        val stemHeight = size.height - bulbRadius * 2
                        
                        // Draw Outline
                        drawRoundRect(
                            color = Color(0xFFE0C097).copy(alpha = 0.4f),
                            topLeft = Offset(centerX - stemWidth / 2f, 0f),
                            size = Size(stemWidth, stemHeight + bulbRadius),
                            cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        
                        drawCircle(
                            color = Color(0xFFE0C097).copy(alpha = 0.4f),
                            radius = bulbRadius,
                            center = Offset(centerX, size.height - bulbRadius),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Draw Fill
                        val fillPercentage = (temp.coerceIn(-20.0, 50.0) + 20) / 70.0
                        val fillHeight = stemHeight * fillPercentage.toFloat()
                        
                        drawRoundRect(
                            color = Color(0xFFE0C097),
                            topLeft = Offset(centerX - stemWidth / 2f + 1.dp.toPx(), stemHeight - fillHeight),
                            size = Size(stemWidth - 2.dp.toPx(), fillHeight + bulbRadius),
                            cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f)
                        )
                        
                        drawCircle(
                            color = Color(0xFFE0C097),
                            radius = bulbRadius - 1.dp.toPx(),
                            center = Offset(centerX, size.height - bulbRadius)
                        )
                    }
                }
            }

            Text(
                text = "${temp.roundToInt()}°C",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }
    }
}
