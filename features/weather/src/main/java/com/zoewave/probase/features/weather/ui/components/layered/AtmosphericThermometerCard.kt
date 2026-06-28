package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.weather.R
import kotlin.math.roundToInt

@Preview(showBackground = true, backgroundColor = 0xFFF9F7F2)
@Composable
fun AtmosphericThermometerCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AtmosphericThermometerCard(temp = 22.0)
        }
    }
}

@Composable
fun AtmosphericThermometerCard(
    temp: Double,
    modifier: Modifier = Modifier,
    unit: String = "°C"
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
                text = stringResource(R.string.features_weather_metric_temperature),
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
                        val isFahrenheit = unit.contains("F")
                        val minTemp = if (isFahrenheit) -4.0 else -20.0
                        val maxTemp = if (isFahrenheit) 122.0 else 50.0
                        val range = maxTemp - minTemp
                        
                        val fillPercentage = (temp.coerceIn(minTemp, maxTemp) - minTemp) / range
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
                text = "${temp.roundToInt()}$unit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }
    }
}
