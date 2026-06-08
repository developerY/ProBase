package com.zoewave.probase.features.weather.ui

import com.google.android.gms.maps.model.LatLng
import com.zoewave.probase.core.model.weather.OpenWeatherResponse
import com.zoewave.probase.core.model.weather.Weather


// Define UI states for weather.
sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val weatherOpen: OpenWeatherResponse?,
        val environmentalContext: com.zoewave.probase.core.model.weather.EnvironmentalContext? = null,
        var locationString: String = "Santa Barbara, US",
        val weather: Weather,
        val settings: Map<String, List<String>>,
        val location: LatLng? = null,
        val isLocationFallback: Boolean = false
    ) : WeatherUiState()

    data class Error(val message: String) : WeatherUiState()
}