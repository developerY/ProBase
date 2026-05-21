package com.zoewave.probase.feature.weather.ui.components.layered

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

enum class LayeredWeatherCondition {
    SUNNY, CLOUDY, RAINY, THUNDER, WINDY
}

data class LayeredWeatherUiState(
    val temperature: Double = 22.0,
    val uvIndex: Double = 4.5,
    val conditions: List<LayeredWeatherCondition> = listOf(LayeredWeatherCondition.SUNNY)
)

@Composable
fun LayeredWeatherCard(
    uiState: LayeredWeatherUiState,
    onEvent: (Unit) -> Unit = {},
    navTo: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "${uiState.temperature.toInt()}°",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFD600)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UV Index: ${uiState.uvIndex}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            WeatherLayeredIcon(
                conditions = uiState.conditions,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

@Composable
fun WeatherLayeredIcon(
    conditions: List<LayeredWeatherCondition>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        conditions.forEach { condition ->
            val icon = when (condition) {
                LayeredWeatherCondition.SUNNY -> Icons.Rounded.WbSunny
                LayeredWeatherCondition.CLOUDY -> Icons.Rounded.Cloud
                LayeredWeatherCondition.RAINY -> Icons.Rounded.WaterDrop
                LayeredWeatherCondition.THUNDER -> Icons.Rounded.FlashOn
                LayeredWeatherCondition.WINDY -> Icons.Rounded.Air
            }
            val tint = when (condition) {
                LayeredWeatherCondition.SUNNY -> Color(0xFFFFD600)
                LayeredWeatherCondition.CLOUDY -> Color(0xFF90A4AE)
                LayeredWeatherCondition.RAINY -> Color(0xFF2196F3)
                LayeredWeatherCondition.THUNDER -> Color(0xFFFFC107)
                LayeredWeatherCondition.WINDY -> Color(0xFF81D4FA)
            }
            
            // Layering logic with offsets
            val offset = when (condition) {
                LayeredWeatherCondition.SUNNY -> IntOffset(-10, -10)
                LayeredWeatherCondition.CLOUDY -> IntOffset(10, 5)
                LayeredWeatherCondition.RAINY -> IntOffset(5, 25)
                LayeredWeatherCondition.THUNDER -> IntOffset(15, 20)
                LayeredWeatherCondition.WINDY -> IntOffset(-20, 15)
            }

            val size = when (condition) {
                LayeredWeatherCondition.SUNNY -> 56.dp
                LayeredWeatherCondition.CLOUDY -> 52.dp
                else -> 40.dp
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(size)
                    .offset(offset.x.dp, offset.y.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LayeredWeatherCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LayeredWeatherCard(
                uiState = LayeredWeatherUiState(
                    temperature = 28.0,
                    uvIndex = 8.2,
                    conditions = listOf(LayeredWeatherCondition.SUNNY, LayeredWeatherCondition.WINDY)
                )
            )
            LayeredWeatherCard(
                uiState = LayeredWeatherUiState(
                    temperature = 19.0,
                    uvIndex = 2.1,
                    conditions = listOf(LayeredWeatherCondition.CLOUDY, LayeredWeatherCondition.RAINY, LayeredWeatherCondition.THUNDER)
                )
            )
        }
    }
}
