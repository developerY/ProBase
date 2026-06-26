package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherCondition
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherSquareCard
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.model.KoColorRoute

data class HomeHeaderUiState(
    val fashionProfile: FashionProfile?,
    val isDaytime: Boolean,
    val beautyTip: String,
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
    val gradientColors = if (uiState.isDaytime) {
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
    } else {
        listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.surfaceVariant)
    }

    // Refined, slightly less aggressive asymmetrical shape for a modern, compact look
    val expressiveShape = RoundedCornerShape(
        topStart = 32.dp,
        topEnd = 12.dp,
        bottomEnd = 32.dp,
        bottomStart = 12.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(expressiveShape)
            .background(Brush.linearGradient(colors = gradientColors))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), expressiveShape)
    ) {
        val bgRes = when {
            uiState.weather?.conditions?.contains(LayeredWeatherCondition.THUNDER) == true -> R.drawable.home_storm_bg
            uiState.weather?.conditions?.contains(LayeredWeatherCondition.RAINY) == true -> R.drawable.home_rainy_bg
            uiState.weather?.conditions?.contains(LayeredWeatherCondition.CLOUDY) == true -> R.drawable.home_cloudy_bg
            else -> R.drawable.home_sunny_bg
        }

        AsyncImage(
            model = bgRes,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.4f)
                .blur(16.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.padding(20.dp), // Reduced from 32.dp to compact the layout
            verticalArrangement = Arrangement.spacedBy(16.dp) // Tighter spacing
        ) {
            // Top Row: Title & Weather
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isDaytime)
                        stringResource(R.string.applications_kocolor_apps_mobile_features_home_radiant_morning)
                    else
                        stringResource(R.string.applications_kocolor_apps_mobile_features_home_deep_restoration),
                    style = MaterialTheme.typography.headlineSmall, // Scaled down for elegance
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                LayeredWeatherSquareCard(
                    uiState = uiState.weather?.copy(
                        locationName = if (uiState.isLocationFallback)
                            stringResource(R.string.applications_kocolor_apps_mobile_features_home_location_not_found)
                        else
                            uiState.locationName
                    ),
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .alpha(if (uiState.isLocationFallback) 0.6f else 1f),
                    onClick = { navTo(KoColorRoute.Weather) }
                )
            }

            // Middle Row: Beauty Tip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.9f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top // Aligns icon with the first line of text
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp), // Micro-adjustment to baseline
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = uiState.beautyTip,
                    style = MaterialTheme.typography.bodyLarge, // Swapped from titleMedium for better multi-line readability
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 24.sp, // Tighter line height
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Bottom Row: Fashion Profile Badges
            if (uiState.fashionProfile != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = uiState.fashionProfile.seasonalType.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = stringResource(
                            R.string.applications_kocolor_apps_mobile_features_home_undertone_format,
                            uiState.fashionProfile.undertone.name.lowercase().replaceFirstChar { it.uppercase() }
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- Preview ---

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeHeaderPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HomeHeader(
                uiState = HomeHeaderUiState(
                    fashionProfile = null, // Mock this if you have a dummy FashionProfile available
                    isDaytime = true,
                    beautyTip = "High UV detected. Prioritize SPF in your ritual today to maintain that radiant glow.",
                    weather = LayeredWeatherUiState(
                        temperature = 17.0,
                        uvIndex = 6.0,
                        conditions = listOf(LayeredWeatherCondition.SUNNY)
                    ),
                    locationName = "LOCATION COULD NOT BE FOUND", // Mimicking the screenshot text
                    isLocationFallback = true
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeHeaderNightPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HomeHeader(
                uiState = HomeHeaderUiState(
                    fashionProfile = null,
                    isDaytime = false,
                    beautyTip = "Deep hydration is essential tonight. Consider applying a ceramide-rich night cream.",
                    weather = LayeredWeatherUiState(
                        temperature = 12.0,
                        uvIndex = 0.0,
                        conditions = listOf(LayeredWeatherCondition.SUNNY)
                    ),
                    locationName = "San Francisco",
                    isLocationFallback = false
                )
            )
        }
    }
}

// ~~~ Orig

data class HomeHeaderUiStateOrig(
    val fashionProfile: FashionProfile?,
    val isDaytime: Boolean,
    val beautyTip: String,
    val weather: LayeredWeatherUiState? = null,
    val locationName: String? = null,
    val isLocationFallback: Boolean = false,
    val backgroundUrl: String? = null
)

@Composable
fun HomeHeaderOrig(
    uiState: HomeHeaderUiStateOrig,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val gradientColors = if (uiState.isDaytime) {
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
    } else {
        listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.surfaceVariant)
    }

    val expressiveShape = RoundedCornerShape(topStart = 48.dp, topEnd = 12.dp, bottomEnd = 48.dp, bottomStart = 12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(expressiveShape)
            .background(Brush.linearGradient(colors = gradientColors))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), expressiveShape)
    ) {
        val bgRes = when {
            uiState.weather?.conditions?.contains(com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherCondition.THUNDER) == true -> R.drawable.home_storm_bg
            uiState.weather?.conditions?.contains(com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherCondition.RAINY) == true -> R.drawable.home_rainy_bg
            uiState.weather?.conditions?.contains(com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherCondition.CLOUDY) == true -> R.drawable.home_cloudy_bg
            else -> R.drawable.home_sunny_bg
        }

        AsyncImage(
            model = bgRes,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.4f)
                .blur(16.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isDaytime) stringResource(R.string.applications_kocolor_apps_mobile_features_home_radiant_morning) else stringResource(R.string.applications_kocolor_apps_mobile_features_home_deep_restoration),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                LayeredWeatherSquareCard(
                    uiState = uiState.weather?.copy(
                        locationName = if (uiState.isLocationFallback) stringResource(R.string.applications_kocolor_apps_mobile_features_home_location_not_found) else uiState.locationName
                    ),
                    modifier = Modifier.padding(start = 16.dp).alpha(if (uiState.isLocationFallback) 0.6f else 1f),
                    onClick = { navTo(KoColorRoute.Weather) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().alpha(0.9f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(top = 4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = uiState.beautyTip,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.fashionProfile != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                        Text(text = uiState.fashionProfile.seasonalType.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_undertone_format, uiState.fashionProfile.undertone.name.lowercase().replaceFirstChar { it.uppercase() }), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}