package com.zoewave.probase.core.data.repository.weather

import android.util.Log
import com.zoewave.probase.core.data.repository.travel.LocationRepository
import com.zoewave.probase.core.model.weather.AtmosphericState
import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AtmosphericRepository @Inject constructor(
    private val weatherRepo: WeatherRepo,
    private val locationRepository: LocationRepository
) {
    private val _atmosphericState = MutableStateFlow(AtmosphericState())
    val atmosphericState: StateFlow<AtmosphericState> = _atmosphericState.asStateFlow()

    private val refreshMutex = Mutex()

    suspend fun fetchWeatherIfNeeded(forceRefresh: Boolean = false) {
        val current = _atmosphericState.value
        val now = System.currentTimeMillis()
        
        // Cache for 15 minutes unless forced
        if (!forceRefresh && current.weather != null && (now - current.lastUpdated) < 15 * 60 * 1000) {
            Log.d("AtmosphericRepository", "Using cached weather data")
            return
        }

        refreshWeather()
    }

    suspend fun refreshWeather() {
        refreshMutex.withLock {
            val current = _atmosphericState.value
            val now = System.currentTimeMillis()
            
            // Check cache again inside the lock to handle concurrent hits
            if (current.weather != null && (now - current.lastUpdated) < 1 * 60 * 1000) {
                Log.d("AtmosphericRepository", "Recent refresh detected inside lock, skipping.")
                return
            }

            try {
                Log.d("AtmosphericRepository", "Refreshing atmospheric data...")
                val latLng = withTimeoutOrNull(5000) {
                    locationRepository.updateLocation()
                    locationRepository.currentLocation.first { it != null }
                }

                if (latLng != null) {
                    val response = weatherRepo.openCurrentWeatherByCoords(latLng.latitude, latLng.longitude)
                    val envContext = weatherRepo.getEnvironmentalContext(latLng.latitude, latLng.longitude)
                    
                    if (response != null && envContext != null) {
                        _atmosphericState.value = AtmosphericState(
                            weather = response,
                            environmentalContext = envContext,
                            lastUpdated = System.currentTimeMillis(),
                            isFallback = false
                        )
                    } else {
                        Log.w("AtmosphericRepository", "One of the weather calls returned null. Skipping state update.")
                    }
                } else {
                    Log.d("AtmosphericRepository", "GPS timeout/unavailable. Falling back to Santa Barbara.")
                    val fallbackCity = "Santa Barbara, US"
                    val response = weatherRepo.openCurrentWeatherByCity(fallbackCity)
                    val envContext = response?.coord?.let { 
                        weatherRepo.getEnvironmentalContext(it.lat, it.lon) 
                    }
                    
                    if (response != null && envContext != null) {
                        _atmosphericState.value = AtmosphericState(
                            weather = response,
                            environmentalContext = envContext,
                            lastUpdated = System.currentTimeMillis(),
                            isFallback = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AtmosphericRepository", "Error refreshing weather", e)
            }
        }
    }
}
