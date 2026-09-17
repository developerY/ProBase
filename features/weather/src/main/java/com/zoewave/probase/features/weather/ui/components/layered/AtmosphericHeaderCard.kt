package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.features.weather.R

data class AtmosphericHeaderUiState(
    val fashionProfile: FashionProfile? = null,
    val isDaytime: Boolean = true,
    val tip: String = "",
    val weather: LayeredWeatherUiState? = null,
    val locationName: String? = null,
    val isLocationFallback: Boolean = false,
    val backgroundUrl: String? = null,
    val tempUnit: String = "CELSIUS"
)

@Composable
fun AtmosphericHeaderCard(
    uiState: AtmosphericHeaderUiState,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Dynamic background gradient based on weather mood
    val weatherConditions = uiState.weather?.conditions ?: listOf(LayeredWeatherCondition.SUNNY)
    val mainCondition = weatherConditions.firstOrNull() ?: LayeredWeatherCondition.SUNNY
    val gradientColors = WeatherMoodGradient.getColors(mainCondition, uiState.isDaytime)

    val gradientBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset.Zero,
        end = Offset.Infinite
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(gradientBrush)
            .animateContentSize(animationSpec = tween(350))
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(tween(250)) togetherWith fadeOut(tween(250))
            },
            label = "HeaderStateTransition"
        ) { expanded ->
            if (expanded) {
                LongHeader(
                    uiState = uiState,
                    onWeatherClick = onWeatherClick,
                    onCollapseClick = { isExpanded = false }
                )
            } else {
                ShortHeader(
                    uiState = uiState,
                    onWeatherClick = onWeatherClick,
                    onExpandClick = { isExpanded = true }
                )
            }
        }
    }
}

@Composable
private fun ShortHeader(
    uiState: AtmosphericHeaderUiState,
    onWeatherClick: () -> Unit,
    onExpandClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HALF: Weather Widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onWeatherClick)
                .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // The translucent pill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp),
                shape = RoundedCornerShape(44.dp),
                color = Color.White.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    DynamicWeatherIcon(
                        conditions = uiState.weather?.conditions ?: listOf(LayeredWeatherCondition.SUNNY),
                        modifier = Modifier.size(width = 80.dp, height = 80.dp)
                    )
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Text(
                        text = "${uiState.weather?.temperature?.toInt() ?: 24}${if (uiState.tempUnit == "FAHRENHEIT") "°" else "°C"}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp
                        ),
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(Modifier.width(28.dp)) // Padding to offset the UV badge
                }
            }

            // UV Badge intersecting top right of the pill
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = (-12).dp)
                    .size(52.dp),
                shape = CircleShape,
                color = Color(0xFFEFE8E1).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color.White),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFC6B492).copy(alpha = 0.4f), CircleShape)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "UV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A4A4A)
                        )
                        Text(
                            text = "${uiState.weather?.uvIndex?.toInt() ?: 8}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }

        // BOTTOM HALF: Greeting words (Expand header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpandClick)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.isDaytime) stringResource(R.string.features_weather_atmospheric_greeting_day) else stringResource(R.string.features_weather_atmospheric_greeting_night),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand Header",
                tint = Color(0xFF8C7F72),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LongHeader(
    uiState: AtmosphericHeaderUiState,
    onWeatherClick: () -> Unit,
    onCollapseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCollapseClick)
                    .padding(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.features_weather_atmospheric_current_location),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFF6A6577).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.locationName?.replace(", ", ",\n") ?: "San Francisco,\nCA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1B1F)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Collapse Header",
                tint = Color(0xFF6A6577).copy(alpha = 0.7f),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onCollapseClick)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onWeatherClick)
                .padding(4.dp)
        ) {
            Text(
                text = "${uiState.weather?.temperature?.toInt() ?: 20}${if (uiState.tempUnit == "FAHRENHEIT") "°" else "°C"}",
                fontSize = 56.sp,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF1C1B1F)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color(0xFF1C1B1F).copy(alpha = 0.15f))
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "UV ${uiState.weather?.uvIndex?.toInt() ?: 8}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1C1B1F),
                    fontFamily = FontFamily.Serif
                )
                Surface(
                    color = Color(0xFF4A4458),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "HIGH",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.45f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.04f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✧", fontSize = 14.sp, color = Color(0xFF1C1B1F))
                    }
                }
                Text(
                    text = uiState.tip.ifBlank { stringResource(R.string.features_weather_atmospheric_high_uv_tip) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF49454F),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// --- Preview Engine ---

@Preview(showBackground = true, backgroundColor = 0xFFF8F7FA)
@Composable
private fun AtmosphericHeaderCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AtmosphericHeaderCard(
                uiState = AtmosphericHeaderUiState(
                    weather = LayeredWeatherUiState(temperature = 24.0, uvIndex = 8.0),
                    tempUnit = "FAHRENHEIT"
                ),
                onWeatherClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7FA)
@Composable
private fun AtmosphericHeaderCardRainyPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AtmosphericHeaderCard(
                uiState = AtmosphericHeaderUiState(
                    weather = LayeredWeatherUiState(
                        temperature = 24.0, 
                        uvIndex = 8.0,
                        conditions = listOf(LayeredWeatherCondition.RAINY, LayeredWeatherCondition.THUNDER)
                    ),
                    tempUnit = "FAHRENHEIT",
                ),
                onWeatherClick = {}
            )
        }
    }
}
