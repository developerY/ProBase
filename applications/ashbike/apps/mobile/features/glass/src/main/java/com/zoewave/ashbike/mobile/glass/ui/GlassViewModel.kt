package com.zoewave.ashbike.mobile.glass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.data.repository.bike.BikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlassViewModel @Inject constructor(
    private val repository: BikeRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(ScreenState.BIKE)

    val uiState: StateFlow<GlassUiState> = combine(
        repository.currentGear,
        repository.suspensionState,
        repository.rideInfo,
        _currentScreen
    ) { gear, susp, info, screen ->
        GlassUiState(
            isBikeConnected = info.isBikeConnected,
            currentGear = gear,
            suspension = susp,
            rawBattery = info.batteryLevel,
            rawSpeed = info.currentSpeed,
            rawHeading = info.heading,
            rawMotorPower = info.motorPower?.toDouble(),
            rawHeartRate = info.heartbeat,
            tripDistance = String.format("%.1f", info.currentTripDistance),
            calories = info.caloriesBurned.toString(),
            rideDuration = info.rideDuration,
            averageSpeed = String.format("%.1f", info.averageSpeed),
            currentScreen = screen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlassUiState(currentScreen = ScreenState.BIKE)
    )

    fun onEvent(event: GlassUiEvent) {
        viewModelScope.launch {
            when (event) {
                GlassUiEvent.GearUp -> repository.gearUp()
                GlassUiEvent.GearDown -> repository.gearDown()
                GlassUiEvent.ToggleSuspension -> repository.toggleSuspension()
                is GlassUiEvent.SelectGear -> repository.setGear(event.gear)
                is GlassUiEvent.ChangeScreen -> _currentScreen.value = event.screen
                else -> { /* Navigation handled in UI */ }
            }
        }
    }
}
