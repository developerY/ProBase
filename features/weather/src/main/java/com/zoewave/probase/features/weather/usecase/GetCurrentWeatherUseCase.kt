import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val weatherRepo: WeatherRepo
) {
    suspend operator fun invoke(city: String): WeatherInfo {
        val response = weatherRepo.openCurrentWeatherByCity(city)
        val envContext = response?.coord?.let { 
            weatherRepo.getEnvironmentalContext(it.lat, it.lon)
        }
        
        return WeatherInfo(
            temperature = response?.main?.temp,
            condition = response?.weather?.firstOrNull()?.main ?: "Clear",
            location = "${response?.name}, ${response?.sys?.country}",
            cityName = response?.name,
            windDegree = response?.wind?.deg,
            windSpeed = response?.wind?.speed?.toFloat(),
            uvIndex = envContext?.uvIndex,
            humidity = envContext?.humidity
        )
    }
}

data class WeatherInfo(
    val temperature: Double?,
    val condition: String?,
    val location: String?,
    val cityName: String?,
    val windDegree: Int?,
    val windSpeed: Float?,
    val uvIndex: Double?,
    val humidity: Double?
)

