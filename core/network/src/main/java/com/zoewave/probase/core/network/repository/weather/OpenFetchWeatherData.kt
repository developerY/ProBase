package com.zoewave.probase.core.network.repository.weather

import android.util.Log
import com.zoewave.probase.core.model.weather.EnvironmentalContext
import com.zoewave.probase.core.model.weather.OpenWeatherResponse
import com.zoewave.probase.core.network.BuildConfig.OPEN_WEATHER_API_KEY
import com.zoewave.probase.core.network.api.interfaces.OpenMeteoService
import com.zoewave.probase.core.network.api.interfaces.OpenWeatherService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


suspend fun openFetchWeatherByCityData(cityName: String): OpenWeatherResponse? {
    var call: OpenWeatherResponse? = null

    try {
        call = OpenRetrofitClient.openWeatherService.getCurrentOpenWeatherByCity(
            cityName,
            OPEN_WEATHER_API_KEY
        )
    } catch (e: HttpException) {
        if (e.code() == 404) {
            Log.d("WeatherRepoImpl", "City not found")
        } else {
            Log.d("WeatherRepoImpl", "Error: ${e.message()}")
        }
        call = null
    } catch (e: Exception) {
        Log.d("WeatherRepoImpl", "An unexpected error occurred")
        call = null
    }
    return call
}


suspend fun openFetchWeatherByCoords(lat: Double, lon: Double): OpenWeatherResponse? {
    var call: OpenWeatherResponse? = null

    try {
        call = OpenRetrofitClient.openWeatherService.getCurrentOpenWeatherByCoords(
            lat = lat,
            lon = lon,
            OPEN_WEATHER_API_KEY
        )
    } catch (e: HttpException) {
        if (e.code() == 404) {
            Log.d("WeatherRepoImpl", "City not found")
        } else {
            Log.d("WeatherRepoImpl", "Error: ${e.message()}")
        }
        call = null
    } catch (e: Exception) {
        Log.d("WeatherRepoImpl", "An unexpected error occurred")
        call = null
    }
    return call
}

suspend fun openFetchEnvironmentalContext(lat: Double, lon: Double): EnvironmentalContext? {
    try {
        val response = OpenMeteoRetrofitClient.openMeteoService.getEnvironmentalContext(lat, lon)
        return EnvironmentalContext(
            temperature = response.current.temperature,
            humidity = response.current.humidity,
            uvIndex = response.current.uvIndex,
            isDay = response.current.isDay == 1,
            weatherCode = response.current.weatherCode
        )
    } catch (e: Exception) {
        Log.e("WeatherRepoImpl", "Error fetching environmental context", e)
        return null
    }
}

private object OpenMeteoRetrofitClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val openMeteoService: OpenMeteoService =
        retrofit.create(OpenMeteoService::class.java)
}

private object OpenRetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val openWeatherService: OpenWeatherService = retrofit.create(OpenWeatherService::class.java)
}