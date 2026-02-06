package com.zoewave.probase.ashbike.mobile.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import com.zoewave.probase.ashbike.mobile.usecase.GetUnsyncedRidesCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,

    // Inject Repositories here, not other ViewModels!
    // private val settingsRepository: SettingsRepository,
    getUnsyncedRidesCountUseCase: GetUnsyncedRidesCountUseCase
) : ViewModel() {

    // Use the injected use case
    val unsyncedRidesCount: StateFlow<Int> = getUnsyncedRidesCountUseCase() // Invoke the use case
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    // 1. Navigation State (Persisted via SavedStateHandle)
    // We use a custom setter to update both SavedState and Flow
    private val _destination = MutableStateFlow(
        savedStateHandle.get<AshBikeDestination>("dest") ?: AshBikeDestination.Home
    )

    private val _hasPermission = MutableStateFlow(false)

    // Simulate data from repositories
    private val _showSettingsBadge = MutableStateFlow(true) // Replace with repo flow
    private val _unsyncedCount = MutableStateFlow(0)        // Replace with repo flow

    // 2. Combine all sources into one UiState
    val uiState: StateFlow<MainUiState> = combine(
        _destination,
        _hasPermission,
        _showSettingsBadge,
        _unsyncedCount
    ) { dest, perm, badge, count ->
        MainUiState(
            currentDestination = dest,
            hasLocationPermission = perm,
            showSettingsBadge = badge,
            unsyncedRidesCount = count
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.OnTabSelected -> {
                updateDestination(event.destination)
            }
            is MainUiEvent.OnPermissionResult -> {
                _hasPermission.update { event.isGranted }
            }
            is MainUiEvent.OnBackPressed -> {
                // Logic: If not on Home, go Home.
                if (_destination.value != AshBikeDestination.Home) {
                    updateDestination(AshBikeDestination.Home)
                }
            }
        }
    }

    private fun updateDestination(dest: AshBikeDestination) {
        _destination.update { dest }
        savedStateHandle["dest"] = dest // Save for process death
    }
}