package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

data class HomeHeaderUiState(
    val fashionProfile: FashionProfile? = null,
    val isDaytime: Boolean = true,
    val beautyTip: String = "",
    val weather: LayeredWeatherUiState? = null,
    val locationName: String? = null,
    val isLocationFallback: Boolean = false,
    val backgroundUrl: String? = null
)

@Composable
fun HomeHeader(
    uiState: HomeHeaderUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Lavender background matching image_2a7699.png
    val containerColor = Color(0xFFE4E0F4)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(containerColor)
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
                    onLeftClick = { navTo(KoColorRoute.Weather) },
                    onRightClick = { isExpanded = false }
                )
            } else {
                ShortHeader(
                    uiState = uiState,
                    onLeftClick = { navTo(KoColorRoute.Weather) },
                    onRightClick = { isExpanded = true }
                )
            }
        }
    }
}

@Composable
private fun ShortHeader(
    uiState: HomeHeaderUiState,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT ZONE: Highly Interactive "Radiant Morning" bounding box
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onLeftClick) // Standard ripple bounded inside this quadrant
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (uiState.isDaytime) "Radiant\nMorning." else "Deep\nRestoration.",
                style = MaterialTheme.typography.displaySmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1B1F),
                lineHeight = 40.sp
            )
        }

        // MIDDLE ZONE: Vertical divider line from image_2a7699.png
        Box(
            modifier = Modifier
                .height(80.dp)
                .width(1.dp)
                .background(Color(0xFF1C1B1F).copy(alpha = 0.12f))
        )

        // RIGHT ZONE: Circular weather dial block
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRightClick),
                color = Color(0xFFFAF7EC), // Warm cream dial color
                tonalElevation = 1.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Weather Condition + Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFF1C1B1F),
                            modifier = Modifier.size(16.dp)
                        )
                        Surface(
                            color = Color(0xFF1C1B1F).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "HIGH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Temperature Text
                    Text(
                        text = "${uiState.weather?.temperature?.toInt() ?: 20}°",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1C1B1F)
                    )

                    // UV Metric Footer
                    Text(
                        text = "UV ${uiState.weather?.uvIndex?.toInt() ?: 8}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1C1B1F).copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LongHeader(
    uiState: HomeHeaderUiState,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
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
                    .clickable(onClick = onLeftClick)
                    .padding(4.dp)
            ) {
                Text(
                    text = "CURRENT LOCATION",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFF6A6577),
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
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Collapse Header",
                tint = Color(0xFF6A6577),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRightClick)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onLeftClick)
                .padding(4.dp)
        ) {
            Text(
                text = "${uiState.weather?.temperature?.toInt() ?: 20}°",
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
                    text = uiState.beautyTip.ifBlank { "High UV. Reapply your mineral SPF every 2 hours and stay in the shade during peak sun." },
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
private fun HomeHeaderShortResponsivePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HomeHeader(
                uiState = HomeHeaderUiState(
                    weather = LayeredWeatherUiState(temperature = 20.0, uvIndex = 8.0)
                )
            )
        }
    }
}