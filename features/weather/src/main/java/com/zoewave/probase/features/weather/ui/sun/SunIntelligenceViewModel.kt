package com.zoewave.probase.features.weather.ui.sun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.travel.LocationRepository
import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SunIntelligenceViewModel @Inject constructor(
    private val weatherRepo: WeatherRepo,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SunIntelligenceUiState>(SunIntelligenceUiState.Loading)
    val uiState: StateFlow<SunIntelligenceUiState> = _uiState

    private var timerJob: Job? = null
    private val twoHoursInMillis = 2 * 60 * 60 * 1000L

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = SunIntelligenceUiState.Loading
            try {
                // 1. Get current location coords with 5s timeout
                val latLng = kotlinx.coroutines.withTimeoutOrNull(5000) {
                    locationRepository.updateLocation()
                    locationRepository.currentLocation.first { it != null }
                }
                
                val isFallback = latLng == null
                val lat = latLng?.latitude ?: 34.4208 // Santa Barbara
                val lon = latLng?.longitude ?: -119.6982
                
                val context = weatherRepo.getEnvironmentalContext(lat, lon)
                _uiState.value = SunIntelligenceUiState.Success(
                    context = context,
                    reapplicationTimeRemaining = twoHoursInMillis,
                    isTimerActive = false,
                    isLocationFallback = isFallback
                )
            } catch (e: Exception) {
                _uiState.value = SunIntelligenceUiState.Error(e.message ?: "Failed to load UV data")
            }
        }
    }

    fun onEvent(event: SunIntelligenceEvent) {
        when (event) {
            SunIntelligenceEvent.Refresh -> loadData()
            SunIntelligenceEvent.ResetTimer -> resetTimer()
            is SunIntelligenceEvent.ToggleTimer -> toggleTimer(event.active)
        }
    }

    private fun toggleTimer(active: Boolean) {
        val currentState = _uiState.value as? SunIntelligenceUiState.Success ?: return
        _uiState.value = currentState.copy(isTimerActive = active)
        
        if (active) {
            startTimer()
        } else {
            timerJob?.cancel()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _uiState.value as? SunIntelligenceUiState.Success ?: break
                if (currentState.reapplicationTimeRemaining > 0) {
                    _uiState.value = currentState.copy(
                        reapplicationTimeRemaining = currentState.reapplicationTimeRemaining - 1000
                    )
                } else {
                    _uiState.value = currentState.copy(isTimerActive = false)
                    break
                }
            }
        }
    }

    private fun resetTimer() {
        val currentState = _uiState.value as? SunIntelligenceUiState.Success ?: return
        _uiState.value = currentState.copy(
            reapplicationTimeRemaining = twoHoursInMillis,
            isTimerActive = true
        )
        startTimer()
    }
}
