package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherSquareCard
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

    // Using a softer, premium gradient matching the screenshots
    val gradientColors = listOf(
        Color(0xFFE8E5F0), // Soft lilac top
        Color(0xFFEFEFF3)  // Clean grayish-white bottom
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.linearGradient(colors = gradientColors))
            .animateContentSize(animationSpec = tween(400)) // Animates the container height automatically
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "HeaderState"
        ) { expanded ->
            if (expanded) {
                LongHeader(
                    uiState = uiState,
                    onLeftClick = { isExpanded = false } , // Click left (icon) to collapse,
                    onRightClick = { navTo(KoColorRoute.Weather) },
                )
            } else {
                ShortHeader(
                    uiState = uiState,
                    onLeftClick = { isExpanded = true }, // Click left (pill) to expand
                    onRightClick = { navTo(KoColorRoute.Weather) },
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
            .padding(vertical = 32.dp, horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT ZONE (Nav)
        Text(
            text = if (uiState.isDaytime) "Radiant\nMorning." else "Deep\nRestoration.",
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A),
            lineHeight = 44.sp,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLeftClick
                )
        )

        // RIGHT ZONE (Expand)
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRightClick
            )
        ) {
            LayeredWeatherSquareCard(
                uiState = uiState.weather,
                onClick = onRightClick
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
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // TOP ROW: Location & Close Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // LEFT ZONE
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLeftClick
                    )
            ) {
                Text(
                    text = "CURRENT LOCATION",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFF8C8A90),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Format location into two lines if comma separated
                Text(
                    text = uiState.locationName?.replace(", ", ",\n") ?: "San Francisco,\nCA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A4444)
                )
            }

            // RIGHT ZONE
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Collapse",
                tint = Color(0xFF8C8A90),
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRightClick
                    )
            )
        }

        // MIDDLE ROW: Temp & UV (Also Navigates to Weather)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onLeftClick
            )
        ) {
            Text(
                text = "${uiState.weather?.temperature?.toInt() ?: 17}°",
                fontSize = 64.sp,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF4A4444)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(Color(0xFF8C8A90).copy(alpha = 0.3f))
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "UV ${uiState.weather?.uvIndex?.toInt() ?: 6}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4A4444),
                    fontFamily = FontFamily.Serif
                )
                Surface(
                    color = Color(0xFF6B6262),
                    shape = RoundedCornerShape(8.dp)
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

        // BOTTOM ROW: Advisory Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.05f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✧", fontSize = 16.sp, color = Color(0xFF6B6262))
                    }
                }
                Text(
                    text = uiState.beautyTip.ifBlank { "High UV. Reapply your mineral SPF every 2 hours and stay in the shade during peak sun." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeHeaderShortPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HomeHeader(uiState = HomeHeaderUiState())
        }
    }
}