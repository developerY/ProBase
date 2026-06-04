package com.zoewave.probase.core.network.api.interfaces

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class OpenMeteoResponse(
    @SerializedName("current") val current: CurrentData
)

data class CurrentData(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Double,
    @SerializedName("is_day") val isDay: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("uv_index") val uvIndex: Double
)

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getEnvironmentalContext(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") currentFields: String = "temperature_2m,relative_humidity_2m,is_day,weather_code,uv_index",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}
