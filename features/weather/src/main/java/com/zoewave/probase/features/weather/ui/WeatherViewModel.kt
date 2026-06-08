package com.zoewave.probase.features.weather.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.travel.LocationRepository
import com.zoewave.probase.core.database.BaseProRepo
import com.zoewave.probase.core.model.weather.Weather
import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepo: WeatherRepo,
    private val repository: BaseProRepo,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        // Initialize the UI state by loading settings
        loadSettings()
    }

    /**
     * Called when the user presses a button to fetch the weather from OpenWeather API.
     */
    fun onFetchWeatherClicked() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is WeatherUiState.Success) {
                val city = currentState.locationString  // e.g., "Santa Barbara, US"
                try {
                    val response = weatherRepo.openCurrentWeatherByCity(city)
                    Log.d("Weather", "API call success: $response")
                    _uiState.value = currentState.copy(weatherOpen = response)
                } catch (e: Exception) {
                    Log.e("Weather", "API call failed", e)
                    // Optionally update UI state with an error.
                }
            } else {
                Log.w("Weather", "UI state not ready for API call.")
            }
        }
    }


    /**
     * Handle various events (Load, Update, Delete).
     */
    fun onEvent(event: WeatherEvent) {
        when (event) {
            is WeatherEvent.LoadBike -> loadSettings()
            is WeatherEvent.UpdateSetting -> updateSetting(event.settingKey, event.settingValue)
            is WeatherEvent.DeleteAllEntries -> deleteAllEntries()
            is WeatherEvent.FetchWeather -> onFetchWeatherClicked()  // Handle the fetch weather event
        }
    }


    /**
     * Loads or simulates loading initial settings/data.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                // 1. Get real location coords with 5s timeout
                val latLng = kotlinx.coroutines.withTimeoutOrNull(5000) {
                    locationRepository.updateLocation()
                    locationRepository.currentLocation.first { it != null }
                }

                val isFallback = latLng == null
                val lat = latLng?.latitude ?: 34.4208 // Santa Barbara
                val lon = latLng?.longitude ?: -119.6982
                
                // 2. Fetch real data
                val weatherResp = weatherRepo.openCurrentWeatherByCoords(lat, lon)
                val envContext = weatherRepo.getEnvironmentalContext(lat, lon)

                _uiState.value = WeatherUiState.Success(
                    weather = Weather(
                        temperature = weatherResp?.main?.temp ?: 22.5,
                        description = weatherResp?.weather?.firstOrNull()?.description ?: "Sunny",
                        iconUrl = "",
                        location = if (isFallback) "Location could not be found" else (weatherResp?.name ?: "San Francisco, CA")
                    ),
                    settings = mapOf(
                        "Theme" to listOf("Light", "Dark", "System Default"),
                        "Language" to listOf("English", "Spanish", "French"),
                        "Notifications" to listOf("Enabled", "Disabled")
                    ),
                    location = latLng,
                    weatherOpen = weatherResp,
                    environmentalContext = envContext,
                    locationString = weatherResp?.name ?: "Santa Barbara, US",
                    isLocationFallback = isFallback
                )
            } catch (e: Exception) {
                Log.e("Weather", "Failed to initialize real weather data", e)
                _uiState.value = WeatherUiState.Error("Environment data unavailable.")
            }
        }
    }

    /**
     * Updates a specific setting in the UI state.
     */
    private fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is WeatherUiState.Success) {
                val updatedSettings = currentState.settings.toMutableMap().apply {
                    // Modify your settings as needed
                    this[key] = listOf(value)
                }
                _uiState.value = currentState.copy(settings = updatedSettings)
            }
        }
    }

    /**
     * Deletes all entries in your repository (example usage).
     */
    private fun deleteAllEntries() {
        viewModelScope.launch {
            repository.deleteAll()
            Log.d("Weather", "All entries deleted from repository.")
        }
    }
}
