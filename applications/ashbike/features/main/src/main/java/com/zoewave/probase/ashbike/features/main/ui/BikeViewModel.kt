package com.zoewave.probase.ashbike.features.main.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BikeViewModel @Inject constructor(
    // No dependencies needed for the "Hi" screen!
) : ViewModel() {

    // A simple placeholder state so the app doesn't crash if something observes it
    private val _uiState = MutableStateFlow<BikeUiState>(BikeUiState.Idle)
    val uiState: StateFlow<BikeUiState> = _uiState.asStateFlow()

    // A placeholder event handler
    fun onEvent(event: BikeEvent) {
        // Handle events here later
    }
}