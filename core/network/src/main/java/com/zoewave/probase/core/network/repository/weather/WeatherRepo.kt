package com.zoewave.probase.core.network.repository.weather

import com.zoewave.probase.core.model.weather.EnvironmentalContext
import com.zoewave.probase.core.model.weather.OpenWeatherResponse


interface WeatherRepo {
    suspend fun openCurrentWeatherByCity(location: String): OpenWeatherResponse?
    suspend fun openCurrentWeatherByCoords(lat: Double, lon: Double): OpenWeatherResponse?
    suspend fun getEnvironmentalContext(lat: Double, lon: Double): EnvironmentalContext?

}
