package com.zoewave.ashbike.mobile.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.data.usecases.WeatherUseCase
import com.zoewave.ashbike.model.bike.BikeRideInfo
import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.ashbike.model.glass.GlassButtonState
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.ashbike.features.main.service.BikeForegroundService
import com.zoewave.probase.ashbike.features.main.service.BikeServiceManager
import com.zoewave.probase.core.model.weather.BikeWeatherInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Define the Side Effects (One-time events sent to UI)

@HiltViewModel
class HomeViewModel @Inject constructor(
    val bikeServiceManager: BikeServiceManager, // <--- Injected Manager
    private val weatherUseCase: WeatherUseCase, // Inject WeatherUseCase here
    private val appSettingsRepository: AppSettingsRepository,
    // 1. INJECT THE GLASS REPO (Even if it's an object, injecting it is cleaner for testing)
    private val bikeRepository: BikeRepository
) : ViewModel() {

    // (Channel is better than SharedFlow for navigation because it buffers
    // the event if the UI isn't ready to receive it immediately)
    private val _navigationChannel = Channel<AshBikeDestination>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    // 2. Create a Channel for Side Effects
    // Channels are perfect for one-off events (navigation, toasts)
    // --- Side Effects & Navigation ---
    private val _effects = Channel<BikeSideEffect>()
    val effects = _effects.receiveAsFlow()

    // --- Navigation Channel ---
    private val _navigateTo = MutableSharedFlow<String>()
    val navigateTo: SharedFlow<String> = _navigateTo.asSharedFlow()

    // --- State exposed to the UI ---
    // --- State ---
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.WaitingForGps)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // --- In-memory UI-only override for total distance ---
    private val _uiPathDistance = MutableStateFlow<Float?>(null)

    // 2) One‐shot weather at ride start (null until we fetch it)
    private val _weatherInfo = MutableStateFlow<BikeWeatherInfo?>(null)

    // 1. ADD THIS NEW STATE FLOW FOR THE DIALOG
    private val _showSetDistanceDialog = MutableStateFlow(false)

    // 1. ADD THIS NEW STATE FLOW FOR THE DIALOG
    private val _showGpsCountdownFlow = MutableStateFlow(true)

    // Helper Data Class to group Glass inputs
    data class GlassState(
        val gear: Int,
        val isSimulatedActive: Boolean,
        val buttonState: GlassButtonState // <--- The calculated state
    )

    // A temporary data holder class to make the logic cleaner
    // Update CombinedData to hold the new state
    data class CombinedData(
        val rideInfo: BikeRideInfo,
        val totalDistance: Float?,
        val weather: BikeWeatherInfo?,
        val showDialog: Boolean,
        val showGpsCountdown: Boolean,
        val gpsAccuracy: LocationEnergyLevel,
        val glassGear: Int,
        val isGlassActive: Boolean,
        val glassButtonState: GlassButtonState // <--- Added
    )

    init {
        observeBikeData()
        fetchWeatherForDashboard()
    }

    // Add this function
    fun updateGlassConnection(isConnected: Boolean) {
        viewModelScope.launch {
            bikeRepository.updateGlassConnectionState(isConnected)
        }
    }

    // NOTE: The BikeViewModel starts it but GlassViewModel also uses it. We might want it to start elsewhere?
    private fun observeBikeData() {
        Log.d("BikeViewModel", "observeServiceData called.")
        viewModelScope.launch {
            Log.d("BikeViewModel", "Starting to collect from service.rideInfo.")


            // 1. HELPER FLOW A: Group the Glass Data (Reduces 2 flows -> 1 flow)
            // 1. UPDATED GLASS FLOW: Calculates the 3-State Logic
            val glassStateFlow = combine(
                bikeRepository.currentGear,
                bikeRepository.isBikeConnected,       // Simulated Data Connection
                bikeRepository.isGlassConnected,  // Hardware Connection
                bikeRepository.isGlassSessionActive // App Running? (Flow<Boolean>)
            ) { gear, simActive, hwConnected, sessionActive ->

                // --- LOGGING THE INPUTS ---
                Log.d("DEBUG_GLASS", "2. VM Combine Input: HW=$hwConnected, Session=$sessionActive")

                // --- THE 3-STATE LOGIC ---
                val btnState = when {
                    !hwConnected -> GlassButtonState.NO_GLASSES
                    sessionActive -> GlassButtonState.PROJECTING
                    else -> GlassButtonState.READY_TO_START
                }

                // --- LOGGING THE OUTPUT ---
                Log.d("DEBUG_GLASS", "2. VM Calculated State: $btnState")

                GlassState(gear, simActive, btnState)
            }

            // 2. HELPER FLOW B: Group the UI Flags (Reduces 3 flows -> 1 flow)
            val uiFlagsFlow = combine(
                _showSetDistanceDialog,
                _showGpsCountdownFlow,
                _uiPathDistance
            ) { showDialog, showCountdown, distance ->
                Triple(showDialog, showCountdown, distance)
            }

            // 3. MAIN COMBINE: Now we only have 5 inputs! (Safe & Clean)
            combine(
                bikeRepository.rideInfo, // <--- Uses Repo Data (25.0 from Simulator) // bikeServiceManager.rideInfo.sample(1000L), // <--- Use Manager Flow
                _weatherInfo,
                appSettingsRepository.gpsAccuracyFlow,
                glassStateFlow, // The grouped Glass data
                uiFlagsFlow     // The grouped UI flags
            ) { rideInfo, weather, gpsAccuracy, glassState, uiFlags ->


                // Unpack the helper objects
                // val (glassGear, isGlassActive) = glassState
                val (showDialog, showCountdown, totalDistance) = uiFlags
                // Log inside the combine lambda
                Log.d(
                    "BikeViewModel_DEBUG",
                    "Combine lambda. rideInfo.location: ${rideInfo.location}, gpsAccuracy (from settings): $gpsAccuracy, showGpsCountdown: $showCountdown"
                )
                CombinedData(
                    rideInfo = rideInfo, // <--- USE THE HACKED DATA HERE,
                    totalDistance,
                    weather,
                    showDialog,
                    showCountdown,
                    gpsAccuracy,
                    // Pass Glass Data
                    // --- MAPPING GLASS STATE ---
                    glassGear = glassState.gear,
                    isGlassActive = glassState.isSimulatedActive,
                    glassButtonState = glassState.buttonState // <--- Pass to Holder
                )
            }
                // 2. MAP: This block's job is to transform the raw data into the final UI State.
                //    Crucially, its return type is declared as the supertype, 'HomeUiState'.
                .map<CombinedData, HomeUiState> { data ->
                    // Log inside the map lambda
                    Log.d("BikeViewModel_DEBUG", "Map lambda. Input CombinedData: $data.")
                    val stateToEmit = HomeUiState.Success(
                        bikeData = data.rideInfo.copy(
                            totalTripDistance = data.totalDistance,
                            bikeWeatherInfo = data.weather
                        ),
                        showSetDistanceDialog = data.showDialog,
                        showGpsCountdown = data.showGpsCountdown,
                        locationEnergyLevel = data.gpsAccuracy, // <<< USE data.gpsAccuracy HERE
                        // 4. POPULATE UI STATE
                        // (Ensure you added these fields to HomeUiState.Success data class)
                        glassGear = data.glassGear,
                        isGlassActive = data.isGlassActive,
                        glassButtonState = data.glassButtonState // Ensure this field exists in HomeUiState.Success
                    )
                    // Log the state being emitted and the key gpsAccuracy value from CombinedData
                    Log.d(
                        "BikeViewModel_DEBUG",
                        "Map lambda. Emitting HomeUiState.Success: $stateToEmit. data.gpsAccuracy (energy level from settings) was: ${data.gpsAccuracy}"
                    )
                    stateToEmit
                }
                // 3. CATCH: This now works perfectly, because the flow is of type Flow<HomeUiState>.
                .catch { e ->
                    Log.e(
                        "BikeViewModel_DEBUG",
                        "Error in UI state flow: ${e.message}",
                        e
                    ) // Added _DEBUG to tag
                    emit(HomeUiState.Error(e.localizedMessage ?: "Service error"))
                }
                .collect { state ->
                    Log.d(
                        "BikeViewModel_DEBUG",
                        "Collected final UI state: $state"
                    ) // Added _DEBUG to tag
                    _uiState.value = state
                }
        }

    }


    private fun fetchWeatherForDashboard() {
        // Prevent re-fetching
        if (_weatherInfo.value != null) return

        viewModelScope.launch {
            // Use Manager to get location safely
            try {
                // We wait for the first valid location from the service manager
                val rideInfo = bikeServiceManager.rideInfo.first { it.location != null }
                val location = rideInfo.location ?: return@launch

                _weatherInfo.value = weatherUseCase(location.latitude, location.longitude)
                Log.d("BikeViewModel", "Weather fetched successfully.")
            } catch (e: Exception) {
                Log.e("BikeViewModel", "Error fetching weather", e)
            }
        }
    }

    // The context parameter is now gone!
    fun onEvent(event: HomeEvent) {
        // This function's only job is to update the raw "source of truth" StateFlows.
        // The `combine` block in `observeServiceData` will automatically react to these
        // changes and produce the new, correct UI state.
        // Ensure this when statement is exhaustive by covering all event types
        // defined in the HomeEvent sealed class.
        when (event) {
            is HomeEvent.SetTotalDistance -> {
                _uiPathDistance.value = event.distanceKm
                // Hide the dialog when the user confirms a new distance
                _showSetDistanceDialog.value = false
            }

            HomeEvent.StartRide -> bikeServiceManager.sendCommand(BikeForegroundService.ACTION_START_RIDE)
            HomeEvent.StopRide -> {
                bikeServiceManager.sendCommand(BikeForegroundService.ACTION_STOP_RIDE)
                _uiPathDistance.value = null
            }

            HomeEvent.OnBikeClick -> {
                // The user clicked the bike icon, so we need to show the dialog.
                _showSetDistanceDialog.value = true
            }

            HomeEvent.DismissSetDistanceDialog -> {
                // The user dismissed the dialog, so we need to hide it.
                _showSetDistanceDialog.value = false
            }

            // The UI intercepts this event directly (in the eventHandler), so the VM ignores it.
            // ✅ 2. Handle the event here
            is HomeEvent.NavigateToSettingsRequested -> {
                Log.d("BikeViewModel", "Handling Nav Request for: ${event.cardKey}")

                // You can do logic here! (e.g. Analytics.log("Settings Clicked"))

                viewModelScope.launch {
                    // Send the Type-Safe Object
                    _navigationChannel.send(
                        AshBikeDestination.Settings(sectionToExpand = event.cardKey)
                    )
                }
            }


            is HomeEvent.ToggleGlassProjection -> {
                // Perform any business logic checks here (e.g., isConnected?)
                viewModelScope.launch {
                    // Tell the UI to launch the activity
                    _effects.send(BikeSideEffect.LaunchGlassProjection)
                }
            }

            is HomeEvent.ToggleDemo -> {
                Log.d("BikeViewModel", "ToggleDemo event received.")
                bikeServiceManager.toggleDemoMode()
            }


            // 5. HANDLE GLASS EVENTS
            // If the Phone UI has controls to change gears (e.g. testing buttons), handle them here.
            // If the 'Launch' logic is purely UI-side (Activity start), you might not need an event here,
            // but if you want to track it or reset state:
            /*
            HomeEvent.GearUp -> glassRepository.gearUp()
            HomeEvent.GearDown -> glassRepository.gearDown()
            */
        }
    }

}
