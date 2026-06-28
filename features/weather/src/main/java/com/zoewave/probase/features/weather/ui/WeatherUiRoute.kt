package com.zoewave.probase.features.weather.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLng
import com.zoewave.probase.core.model.weather.Clouds
import com.zoewave.probase.core.model.weather.Coord
import com.zoewave.probase.core.model.weather.EnvironmentalContext
import com.zoewave.probase.core.model.weather.Main
import com.zoewave.probase.core.model.weather.OpenWeatherResponse
import com.zoewave.probase.core.model.weather.Sys
import com.zoewave.probase.core.model.weather.Weather
import com.zoewave.probase.core.model.weather.WeatherOne
import com.zoewave.probase.core.model.weather.Wind
import com.zoewave.probase.features.weather.ui.components.WeatherScreen

@Preview(showBackground = true)
@Composable
fun WeatherUiRouteLoadingPreview() {
    MaterialTheme {
        WeatherUiRoute(
            uiState = WeatherUiState.Loading,
            onEvent = {},
            onBack = {},
            onNavigateToSunIntelligence = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherUiRouteErrorPreview() {
    MaterialTheme {
        WeatherUiRoute(
            uiState = WeatherUiState.Error("Failed to fetch weather data"),
            onEvent = {},
            onBack = {},
            onNavigateToSunIntelligence = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherUiRouteSuccessPreview() {
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
        WeatherUiRoute(
            uiState = WeatherUiState.Success(
                weatherOpen = sampleWeather,
                environmentalContext = sampleEnv,
                locationString = "Santa Barbara, US",
                weather = Weather(22.0, "Clear sky", "Santa Barbara", null),
                settings = emptyMap(),
                location = LatLng(34.42, -119.7),
                isLocationFallback = false,
                tempUnit = "CELSIUS"
            ),
            onEvent = {},
            onBack = {},
            onNavigateToSunIntelligence = {}
        )
    }
}

@Composable
fun WeatherUiRoute(
    onNavigateToSunIntelligence: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    tempUnit: String = "CELSIUS",
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    androidx.compose.runtime.LaunchedEffect(tempUnit) {
        viewModel.setTempUnit(tempUnit)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onNavigateToSunIntelligence = onNavigateToSunIntelligence,
        modifier = modifier
    )
}

@Composable
internal fun WeatherUiRoute(
    uiState: WeatherUiState,
    onEvent: (WeatherEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToSunIntelligence: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is WeatherUiState.Loading -> {
            LoadingScreen(modifier = modifier)
        }

        is WeatherUiState.Error -> {
            ErrorScreen(
                errorMessage = uiState.message,
                onRetry = { onEvent(WeatherEvent.Refresh) },
                modifier = modifier
            )
        }

        is WeatherUiState.Success -> {
            WeatherScreen(
                uiState = uiState,
                onEvent = onEvent,
                onBack = onBack,
                onNavigateToSunIntelligence = onNavigateToSunIntelligence,
                modifier = modifier
            )
        }
    }
}

// These will be move to a common directory.
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Text(text = "Loading...", modifier = modifier.fillMaxSize())
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "Error: $errorMessage",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Retry",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .clickable { onRetry() }
                .padding(vertical = 8.dp)
        )
    }
}

