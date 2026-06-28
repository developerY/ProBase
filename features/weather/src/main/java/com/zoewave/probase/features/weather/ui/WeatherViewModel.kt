package com.zoewave.probase.features.weather.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.weather.AtmosphericRepository
import com.zoewave.probase.core.database.BaseProRepo
import com.zoewave.probase.core.model.weather.AtmosphericState
import com.zoewave.probase.core.model.weather.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val atmosphericRepository: AtmosphericRepository,
    private val repository: BaseProRepo
) : ViewModel() {

    private val _tempUnit = MutableStateFlow("CELSIUS")

    val uiState: StateFlow<WeatherUiState> = combine(
        atmosphericRepository.atmosphericState,
        _tempUnit
    ) { state, unit ->
        val weather = state.weather
        if (weather == null) {
            WeatherUiState.Loading
        } else {
            WeatherUiState.Success(
                weatherOpen = weather,
                environmentalContext = state.environmentalContext,
                locationString = weather.name,
                weather = Weather(
                    temperature = weather.main.temp,
                    description = weather.weather.firstOrNull()?.description ?: "Clear",
                    location = weather.name,
                    iconUrl = ""
                ),
                settings = emptyMap(),
                location = null, // LatLng if needed
                isLocationFallback = state.isFallback,
                tempUnit = unit
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeatherUiState.Loading
    )

    fun setTempUnit(unit: String) {
        _tempUnit.value = unit
    }

    init {
        viewModelScope.launch {
            atmosphericRepository.fetchWeatherIfNeeded()
        }
    }

    fun onEvent(event: WeatherEvent) {
        when (event) {
            WeatherEvent.Refresh, WeatherEvent.FetchWeather -> {
                viewModelScope.launch {
                    atmosphericRepository.refreshWeather()
                }
            }
            is WeatherEvent.UpdateSetting -> {
                // Handle settings if needed
            }
            WeatherEvent.DeleteAllEntries -> {
                viewModelScope.launch {
                    repository.deleteAll()
                }
            }
        }
    }
}
