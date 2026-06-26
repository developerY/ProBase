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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
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

    // Premium gradient background matching image_266714.png
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFF3E7FF), Color(0xFFD6C8F7), Color(0xFFC4B5FD)),
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset.Infinite
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
                    onLeftClick = { navTo(KoColorRoute.Weather) },
                    onRightClick = { isExpanded = false }
                )
            } else {
                ShortHeader(
                    uiState = uiState,
                    onWeatherClick = { navTo(KoColorRoute.Weather) },
                    onExpandClick = { isExpanded = true }
                )
            }
        }
    }
}

@Composable
private fun ShortHeader(
    uiState: HomeHeaderUiState,
    onWeatherClick: () -> Unit,
    onExpandClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HALF: Weather Widget (Go to weather)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onWeatherClick)
                .padding(top = 28.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Cloud Icon with Premium Gradient and Shadow
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            alpha = 0.99f
                            shadowElevation = 8f
                            shape = CircleShape
                            clip = false
                        }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color.White.copy(alpha = 0.4f))
                                ),
                                blendMode = BlendMode.SrcIn
                            )
                        },
                    tint = Color.Unspecified
                )

                // Temperature Text (Popping Bold Numbers with Shadow)
                Text(
                    text = "${uiState.weather?.temperature?.toInt() ?: 24}°",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.15f),
                            offset = Offset(0f, 4f),
                            blurRadius = 10f
                        )
                    ),
                    color = Color(0xFF374151), // High contrast charcoal
                    modifier = Modifier.align(Alignment.Center).padding(top = 10.dp)
                )

                // UV Badge (Accented purple circle with shadow)
                Surface(
                    color = Color(0xFF9E84C1),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 2.dp)
                        .size(36.dp),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "UV",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 8.sp
                        )
                        Text(
                            text = "${uiState.weather?.uvIndex?.toInt() ?: 8}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        // BOTTOM HALF: Greeting words (Expand header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpandClick)
                .padding(bottom = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (uiState.isDaytime) "Radiant Morning." else "Deep Restoration.",
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                letterSpacing = (-0.5).sp
            )
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