package com.zoewave.probase.kocolor.features.starterpack.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SearchIndexEntry
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StarterPackUiState(
    val availablePacks: List<PackInfo> = emptyList(),
    val installedPacks: List<InstalledPackEntity> = emptyList(),
    val seedingState: SeedingState = SeedingState.Idle,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val filteredSearchIndex: List<SearchIndexEntry> = emptyList()
)

sealed class StarterPackEvent {
    data class OnIngestPack(val pack: PackInfo) : StarterPackEvent()
    data class OnWipePack(val packId: String) : StarterPackEvent()
    data object RefreshManifest : StarterPackEvent()
    data class SearchQueryChanged(val query: String) : StarterPackEvent()
}

@HiltViewModel
class StarterPackViewModel @Inject constructor(
    private val repository: StarterPackRepository,
    private val syncRepository: PackSyncRepository
) : ViewModel() {

    private val _seedingState = MutableStateFlow<SeedingState>(SeedingState.Idle)
    private val _availablePacks = MutableStateFlow<List<PackInfo>>(emptyList())
    private val _isRefreshing = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")
    private val _searchIndex = MutableStateFlow<List<SearchIndexEntry>>(emptyList())

    val filteredSearchIndex: StateFlow<List<SearchIndexEntry>> = _searchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .combine(_searchIndex) { query, index ->
            if (query.isBlank()) index
            else index.filter { entry ->
                entry.term.contains(query, ignoreCase = true) ||
                entry.brand.contains(query, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<StarterPackUiState> = combine(
        _availablePacks,
        syncRepository.getInstalledPacks(),
        _seedingState,
        _isRefreshing,
        _searchQuery,
        filteredSearchIndex
    ) { args ->
        StarterPackUiState(
            availablePacks = args[0] as List<PackInfo>,
            installedPacks = args[1] as List<InstalledPackEntity>,
            seedingState = args[2] as SeedingState,
            isRefreshing = args[3] as Boolean,
            searchQuery = args[4] as String,
            filteredSearchIndex = args[5] as List<SearchIndexEntry>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StarterPackUiState())

    init {
        refreshManifest()
        fetchSearchIndex()
    }

    private fun fetchSearchIndex() {
        viewModelScope.launch {
            try {
                _searchIndex.value = repository.getSearchIndex()
            } catch (e: Exception) {
                Log.e("StarterPackVM", "Failed to fetch search index", e)
            }
        }
    }

    fun onEvent(event: StarterPackEvent) {
        when (event) {
            is StarterPackEvent.OnIngestPack -> ingestPack(event.pack)
            is StarterPackEvent.OnWipePack -> wipePack(event.packId)
            StarterPackEvent.RefreshManifest -> refreshManifest()
            is StarterPackEvent.SearchQueryChanged -> _searchQuery.value = event.query
        }
    }

    private fun refreshManifest() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncRepository.fetchManifest()
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
            
            syncRepository.ingestPack(pack)
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
            syncRepository.wipePack(packId)
                .onSuccess {
                    _seedingState.value = SeedingState.Success
                }
                .onFailure {
                    _seedingState.value = SeedingState.Error("Failed to wipe pack.")
                }
        }
    }
}
