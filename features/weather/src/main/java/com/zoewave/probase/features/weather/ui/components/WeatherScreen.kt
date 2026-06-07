package com.zoewave.probase.features.weather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.zoewave.probase.features.weather.R
import com.zoewave.probase.core.model.weather.OpenWeatherResponse
import com.zoewave.probase.features.weather.ui.WeatherEvent
import com.zoewave.probase.features.weather.ui.UnifiedDynamicWeatherCard
import com.zoewave.probase.features.weather.ui.components.backgrounds.WeatherBackgroundAnimation
import com.zoewave.probase.features.weather.ui.components.combine.WeatherConditionUnif
import com.zoewave.probase.features.weather.ui.components.rain.RainVolumeCard
import com.zoewave.probase.features.weather.ui.components.snow.BetterSnowVolumeCardAI
import com.zoewave.probase.features.weather.ui.components.sun.TemperatureCardAI
import com.zoewave.probase.features.weather.ui.components.wind.WindCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    weather: OpenWeatherResponse?,
    settings: Map<String, List<String>>,
    location: LatLng?,
    onEvent: (WeatherEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val conditionText = weather?.weather?.firstOrNull()?.main ?: "Clear"
    val weatherCondition = when {
        weather?.rain != null -> WeatherConditionUnif.RAINY
        weather?.snow != null -> WeatherConditionUnif.SNOWY
        conditionText.equals("Clouds", ignoreCase = true) -> WeatherConditionUnif.CLOUDY
        conditionText.equals("Clear", ignoreCase = true) -> WeatherConditionUnif.SUNNY
        else -> WeatherConditionUnif.CLEAR
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.features_weather_title), 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold,
                        color = if (weatherCondition == WeatherConditionUnif.SUNNY) Color.Black else Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = if (weatherCondition == WeatherConditionUnif.SUNNY) Color.Black else Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(WeatherEvent.FetchWeather) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh, 
                            contentDescription = "Refresh",
                            tint = if (weatherCondition == WeatherConditionUnif.SUNNY) Color.Black else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gorgeous Background Layer
            if (weather != null) {
                WeatherBackgroundAnimation(
                    weatherCondition = weatherCondition,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC))))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Hero Animated Card
                if (weather != null) {
                    UnifiedDynamicWeatherCard(
                        response = weather,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Spectacular Placeholder
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // 2. High-Precision Detail Cards
                Text(
                    text = "ATMOSPHERIC METRICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = (if (weatherCondition == WeatherConditionUnif.SUNNY) Color.Black else Color.White).copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Start)
                )

                TemperatureCardAI(
                    temp = weather?.main?.temp ?: 0.0,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        RainVolumeCard(
                            volume = weather?.rain?.`1h` ?: 0.0
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BetterSnowVolumeCardAI(
                            volume = weather?.snow?.`1h` ?: 0.0
                        )
                    }
                }

                WindCard(
                    windDegree = weather?.wind?.deg ?: 0,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
