package com.zoewave.probase.core.data.api.interfaces


import com.zoewave.probase.core.model.weather.OpenWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

// Old and remove
interface OpenWeatherService {
    // By city name (you already have this)
    @GET("data/2.5/weather")
    suspend fun getCurrentOpenWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherResponse

    // By geographic coordinates
    @GET("data/2.5/weather")
    suspend fun getCurrentOpenWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherResponse
}
