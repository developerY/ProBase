package com.zoewave.probase.kocolor.features.starterpack.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StarterPackUiState(
    val availablePacks: List<PackInfo> = emptyList(),
    val installedPacks: List<InstalledPackEntity> = emptyList(),
    val seedingState: SeedingState = SeedingState.Idle,
    val isRefreshing: Boolean = false
)

sealed class StarterPackEvent {
    data class OnIngestPack(val pack: PackInfo) : StarterPackEvent()
    data class OnWipePack(val packId: String) : StarterPackEvent()
    data object RefreshManifest : StarterPackEvent()
}

@HiltViewModel
class StarterPackViewModel @Inject constructor(
    private val repository: PackSyncRepository
) : ViewModel() {

    private val _seedingState = MutableStateFlow<SeedingState>(SeedingState.Idle)
    private val _availablePacks = MutableStateFlow<List<PackInfo>>(emptyList())
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<StarterPackUiState> = combine(
        _availablePacks,
        repository.getInstalledPacks(),
        _seedingState,
        _isRefreshing
    ) { available, installed, seeding, refreshing ->
        StarterPackUiState(
            availablePacks = available,
            installedPacks = installed,
            seedingState = seeding,
            isRefreshing = refreshing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StarterPackUiState())

    init {
        refreshManifest()
    }

    fun onEvent(event: StarterPackEvent) {
        when (event) {
            is StarterPackEvent.OnIngestPack -> ingestPack(event.pack)
            is StarterPackEvent.OnWipePack -> wipePack(event.packId)
            StarterPackEvent.RefreshManifest -> refreshManifest()
        }
    }

    private fun refreshManifest() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchManifest()
                .onSuccess { packs ->
                    _availablePacks.value = packs
                }
                .onFailure { error ->
                    Log.e("StarterPackVM", "Failed to fetch manifest", error)
                }
            _isRefreshing.value = false
        }
    }

    private fun ingestPack(pack: PackInfo) {
        viewModelScope.launch {
            Log.d("StarterPackVM", "ingestPack: Starting ingestion for ${pack.id}")
            _seedingState.value = SeedingState.Loading
            
            repository.ingestPack(pack)
                .onSuccess {
                    _seedingState.value = SeedingState.Success
                }
                .onFailure { error ->
                    val errorMessage = error.localizedMessage ?: "Ingestion Failed"
                    Log.e("StarterPackVM", "ingestPack: ERROR: $errorMessage")
                    _seedingState.value = SeedingState.Error(errorMessage)
                }
        }
    }

    private fun wipePack(packId: String) {
        viewModelScope.launch {
            _seedingState.value = SeedingState.Loading
            repository.wipePack(packId)
                .onSuccess {
                    _seedingState.value = SeedingState.Success
                }
                .onFailure { error ->
                    _seedingState.value = SeedingState.Error("Failed to wipe pack.")
                }
        }
    }
}
