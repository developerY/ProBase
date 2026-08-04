package com.zoewave.probase.kocolor.features.starterpack.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.features.starterpack.data.repository.StarterPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StarterPackUiState(
    val seedingState: SeedingState = SeedingState.Idle
)

sealed class StarterPackEvent {
    data object OnIngestStarterPack : StarterPackEvent()
    data object OnWipeStarterPack : StarterPackEvent()
}

@HiltViewModel
class StarterPackViewModel @Inject constructor(
    private val repository: StarterPackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StarterPackUiState())
    val uiState: StateFlow<StarterPackUiState> = _uiState.asStateFlow()

    fun onEvent(event: StarterPackEvent) {
        when (event) {
            StarterPackEvent.OnIngestStarterPack -> ingestStarterPack()
            StarterPackEvent.OnWipeStarterPack -> wipeStarterPack()
        }
    }

    private fun ingestStarterPack() {
        viewModelScope.launch {
            Log.d("StarterPackVM", "ingestStarterPack: Starting ingestion")
            _uiState.value = _uiState.value.copy(seedingState = SeedingState.Loading)
            
            repository.ingestStarterPack()
                .onSuccess {
                    Log.d("StarterPackVM", "ingestStarterPack: SUCCESS")
                    _uiState.value = _uiState.value.copy(seedingState = SeedingState.Success)
                }
                .onFailure { error ->
                    val errorMessage = error.localizedMessage ?: "Ingestion Failed"
                    Log.e("StarterPackVM", "ingestStarterPack: ERROR: $errorMessage")
                    _uiState.value = _uiState.value.copy(seedingState = SeedingState.Error(errorMessage))
                }
        }
    }

    private fun wipeStarterPack() {
        viewModelScope.launch {
            Log.d("StarterPackVM", "wipeStarterPack: Wiping items")
            _uiState.value = _uiState.value.copy(seedingState = SeedingState.Loading)
            
            repository.wipeStarterPack()
                .onSuccess {
                    Log.d("StarterPackVM", "wipeStarterPack: SUCCESS")
                    _uiState.value = _uiState.value.copy(seedingState = SeedingState.Success)
                }
                .onFailure { error ->
                    Log.e("StarterPackVM", "wipeStarterPack: ERROR", error)
                    _uiState.value = _uiState.value.copy(seedingState = SeedingState.Error("Failed to wipe starter pack items."))
                }
        }
    }
}
