package com.zoewave.probase.features.weather.ui.components.layered

import com.zoewave.probase.core.model.weather.EnvironmentalContext
import com.zoewave.probase.core.model.weather.OpenWeatherResponse

object LayeredWeatherMapper {
    fun mapToConditions(response: OpenWeatherResponse): List<LayeredWeatherCondition> {
        val conditions = mutableListOf<LayeredWeatherCondition>()
        val main = response.weather.firstOrNull()?.main ?: ""
        when {
            main.contains("Cloud", true) -> conditions.add(LayeredWeatherCondition.CLOUDY)
            main.contains("Rain", true) -> conditions.add(LayeredWeatherCondition.RAINY)
            main.contains("Thunder", true) -> conditions.add(LayeredWeatherCondition.THUNDER)
            else -> conditions.add(LayeredWeatherCondition.SUNNY)
        }
        if (response.wind.speed > 5.0) conditions.add(LayeredWeatherCondition.WINDY)
        return conditions
    }

    fun mapToUiState(
        response: OpenWeatherResponse,
        envContext: EnvironmentalContext,
        isFallback: Boolean
    ): LayeredWeatherUiState {
        return LayeredWeatherUiState(
            temperature = response.main.temp,
            uvIndex = envContext.uvIndex,
            conditions = mapToConditions(response),
            locationName = if (isFallback) "Location could not be found" else response.name
        )
    }
}
