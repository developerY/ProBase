package com.zoewave.ashbike.data.usecases

// import com.zoewave.probase.core.database.BaseProRepo  // Import your repository
import com.zoewave.probase.core.model.weather.BikeWeatherInfo
import com.zoewave.probase.core.model.weather.OpenWeatherResponse
import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import javax.inject.Inject
import javax.inject.Singleton


// 1) A new WeatherUseCase to fetch & hold current weather once
@Singleton
class WeatherUseCase @Inject constructor(
    private val weatherRepo: WeatherRepo
) {
    // keep one cached result per app run
    private var lastWeather: BikeWeatherInfo? = null

    suspend operator fun invoke(lat: Double, lng: Double): BikeWeatherInfo? {
        return getWeather(lat, lng)
    }

    /** One‐shot fetch of BikeWeatherInfo or null on error */
    private suspend fun getWeather(lat: Double, lng: Double): BikeWeatherInfo? {
        // if we’ve already fetched once, just return it
        lastWeather?.let { return it }

        // otherwise fetch and cache
        val fetched = runCatching {
            weatherRepo
                .openCurrentWeatherByCoords(lat, lng)
                ?.toBikeWeatherInfo()
        }.getOrNull()

        lastWeather = fetched
        return fetched
    }
}

// Reuse your existing mapper:
private fun OpenWeatherResponse.toBikeWeatherInfo(): BikeWeatherInfo =
    BikeWeatherInfo(
        windDegree = wind.deg,
        windSpeed = (wind.speed * 3.6f),
        conditionText = weather.firstOrNull()?.main.orEmpty(),
        conditionDescription = weather.firstOrNull()?.description.orEmpty(),
        conditionIcon = weather.firstOrNull()?.icon.orEmpty(),
        temperature = main.temp,
        feelsLike = main.feels_like,
        humidity = main.humidity
    )
