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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.zoewave.probase.features.weather.R
import com.zoewave.probase.core.model.weather.Clouds
import com.zoewave.probase.core.model.weather.Coord
import com.zoewave.probase.core.model.weather.EnvironmentalContext
import com.zoewave.probase.core.model.weather.Main
import com.zoewave.probase.core.model.weather.OpenWeatherResponse
import com.zoewave.probase.core.model.weather.Sys
import com.zoewave.probase.core.model.weather.WeatherOne
import com.zoewave.probase.core.model.weather.Wind
import com.zoewave.probase.features.weather.ui.WeatherEvent
import com.zoewave.probase.features.weather.ui.components.atelier.*
import com.zoewave.probase.features.weather.ui.components.combine.WeatherConditionUnif
import java.time.LocalDate

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    val sampleWeather = OpenWeatherResponse(
        coord = Coord(lon = -119.7, lat = 34.42),
        weather = listOf(WeatherOne(id = 800, main = "Clear", description = "clear sky", icon = "01d")),
        base = "stations",
        main = Main(temp = 22.0, feels_like = 21.5, temp_min = 20.0, temp_max = 24.0, pressure = 1012, humidity = 45),
        visibility = 10000,
        wind = Wind(speed = 3.5, deg = 240, gust = 5.0),
        clouds = Clouds(all = 0),
        rain = null,
        snow = null,
        dt = 1678886400,
        sys = Sys(type = 1, id = 8000, country = "US", sunrise = 1678876800, sunset = 1678920000),
        timezone = -25200,
        id = 5392323,
        name = "Santa Barbara",
        cod = 200
    )

    val sampleEnv = EnvironmentalContext(
        temperature = 22.0,
        humidity = 45.0,
        uvIndex = 6.2,
        isDay = true,
        weatherCode = 1,
        hourlyUV = listOf(0.0, 0.0, 0.0, 0.5, 2.0, 4.5, 6.0, 7.5, 8.0, 7.0, 5.5, 3.5, 1.5, 0.5, 0.0)
    )

    MaterialTheme {
        WeatherScreen(
            weather = sampleWeather,
            environmentalContext = sampleEnv,
            isLocationFallback = false,
            settings = emptyMap(),
            location = LatLng(34.42, -119.7),
            onEvent = {},
            onBack = {},
            onNavigateToSunIntelligence = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    weather: OpenWeatherResponse?,
    environmentalContext: com.zoewave.probase.core.model.weather.EnvironmentalContext?,
    isLocationFallback: Boolean,
    settings: Map<String, List<String>>,
    location: LatLng?,
    onEvent: (WeatherEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToSunIntelligence: () -> Unit,
    modifier: Modifier = Modifier,
    tempUnit: String = "CELSIUS",
) {
    val tempCelsius = weather?.main?.temp ?: 21.0
    val temp = if (tempUnit == "FAHRENHEIT") (tempCelsius * 9 / 5) + 32 else tempCelsius
    val unitSuffix = if (tempUnit == "FAHRENHEIT") "°F" else "°C"

    val conditionText = weather?.weather?.firstOrNull()?.main ?: "Clear"
    val locationName = if (isLocationFallback) "Location could not be found" else (weather?.name ?: "Santa Barbara, US")
    val uvIndex = environmentalContext?.uvIndex ?: 0.0
    
    val isRainActive = weather?.rain != null
    val isSnowActive = weather?.snow != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Current Environment", 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(WeatherEvent.FetchWeather) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Spectacular Scenic Background
            AsyncImage(
                model = R.drawable.weather_bg,
                contentDescription = null,
                modifier = Modifier.matchParentSize().blur(20.dp),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(24.dp)
                    .alpha(if (isLocationFallback) 0.6f else 1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 1. Editorial Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${temp.toInt()}$unitSuffix",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.width(24.dp))
                    Column {
                        Text(
                            text = conditionText,
                            style = MaterialTheme.typography.displaySmall,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black.copy(alpha = 0.5f)
                        )
                    }
                }

                // 2. Atmospheric Metrics Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Atmospheric Metrics",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AtelierThermometerCard(
                            temp = temp,
                            unit = unitSuffix,
                            modifier = Modifier.weight(1f)
                        )
                        AtelierWindCompassCard(
                            degree = weather?.wind?.deg ?: 269,
                            speed = weather?.wind?.speed ?: 5.0,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. UV Intensity Gauge
                AtelierUVGaugeCard(
                    uvIndex = uvIndex,
                    onClick = onNavigateToSunIntelligence
                )

                // 4. Hydrometeors Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Hydrometeors",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AtelierHydrometeorCard(
                            label = "Rain Volume",
                            value = weather?.rain?.`1h` ?: 0.0,
                            isRain = true,
                            isActive = isRainActive,
                            modifier = Modifier.weight(1f)
                        )
                        AtelierHydrometeorCard(
                            label = "Snow Volume",
                            value = weather?.snow?.`1h` ?: 0.0,
                            isRain = false,
                            isActive = isSnowActive,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
