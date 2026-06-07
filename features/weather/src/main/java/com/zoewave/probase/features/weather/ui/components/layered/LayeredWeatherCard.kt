package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LayeredWeatherCondition {
    SUNNY, CLOUDY, RAINY, THUNDER, WINDY
}

data class LayeredWeatherUiState(
    val temperature: Double = 22.0,
    val uvIndex: Double = 4.5,
    val conditions: List<LayeredWeatherCondition> = listOf(LayeredWeatherCondition.SUNNY),
    val locationName: String? = null
)

@Composable
fun LayeredWeatherInfoIcon(
    uiState: LayeredWeatherUiState,
    onEvent: (Unit) -> Unit = {},
    navTo: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        WeatherLayeredIcon(
            conditions = uiState.conditions,
            modifier = Modifier.fillMaxSize()
        )

        // High-density info badge - Made smaller and more elegant
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = CircleShape,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            modifier = Modifier
                .size(32.dp) // Reduced from 36.dp
                .align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "${uiState.temperature.toInt()}°",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Black,
                    lineHeight = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "UV ${uiState.uvIndex.toInt()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    lineHeight = 7.sp
                )
            }
        }
    }
}

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
            
            // Layering logic with offsets - Cleaned up to be more centered
            val offset = when (condition) {
                LayeredWeatherCondition.SUNNY -> IntOffset(0, 0)
                LayeredWeatherCondition.CLOUDY -> IntOffset(0, 0)
                LayeredWeatherCondition.RAINY -> IntOffset(0, 0)
                LayeredWeatherCondition.THUNDER -> IntOffset(0, 0)
                LayeredWeatherCondition.WINDY -> IntOffset(0, 0)
            }

            val size = when (condition) {
                LayeredWeatherCondition.SUNNY -> 72.dp // Increased from 56.dp
                LayeredWeatherCondition.CLOUDY -> 68.dp // Increased from 52.dp
                else -> 52.dp // Increased from 40.dp
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
private fun LayeredWeatherInfoIconPreview() {
    MaterialTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LayeredWeatherInfoIcon(
                uiState = LayeredWeatherUiState(
                    temperature = 31.0,
                    uvIndex = 9.0,
                    conditions = listOf(LayeredWeatherCondition.SUNNY)
                )
            )
            LayeredWeatherInfoIcon(
                uiState = LayeredWeatherUiState(
                    temperature = 18.0,
                    uvIndex = 1.0,
                    conditions = listOf(LayeredWeatherCondition.RAINY, LayeredWeatherCondition.CLOUDY)
                )
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

/**
 * A square version of the weather card with the icon overlay centered and the city name underneath.
 */
@Composable
fun LayeredWeatherSquareCard(
    uiState: LayeredWeatherUiState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState != null) {
                LayeredWeatherInfoIcon(
                    uiState = uiState,
                    modifier = Modifier.size(80.dp) // Maintain consistent size for the icon group
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = (uiState.locationName ?: "Unknown").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            } else {
                // Loading / Unavailable state
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cloud,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "LOCATING...",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Black,
                    color = Color.Gray.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LayeredWeatherSquareCardPreview() {
    MaterialTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LayeredWeatherSquareCard(
                uiState = LayeredWeatherUiState(
                    locationName = "Manhattan",
                    temperature = 24.0,
                    uvIndex = 0.0,
                    conditions = listOf(LayeredWeatherCondition.CLOUDY)
                ),
                onClick = {}
            )
            LayeredWeatherSquareCard(uiState = null, onClick = {})
        }
    }
}
