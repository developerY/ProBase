package com.zoewave.probase.core.data.repository.weather

import com.zoewave.probase.core.model.weather.OpenWeatherResponse


interface WeatherRepo {
    suspend fun openCurrentWeatherByCity(location: String): OpenWeatherResponse?
    suspend fun openCurrentWeatherByCoords(lat: Double, lon: Double): OpenWeatherResponse?

}