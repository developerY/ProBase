package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackPreviewUiState(
    val items: List<PackItem> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val targetItemId: String? = null
)

@HiltViewModel
class PackPreviewViewModel @Inject constructor(
    private val repository: StarterPackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packId: String = checkNotNull(savedStateHandle["packId"])
    private val targetItemId: String? = savedStateHandle["targetItemId"]
    private val sha256: String? = savedStateHandle["sha256"]
    private val publisher: String? = savedStateHandle["publisher"]

    private val _uiState = MutableStateFlow(PackPreviewUiState(targetItemId = targetItemId))
    val uiState: StateFlow<PackPreviewUiState> = _uiState.asStateFlow()

    init {
        loadPackItems()
    }

    private fun loadPackItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val items = repository.getPackItems(packId, sha256, publisher)
                _uiState.update { 
                    it.copy(
                        items = items, 
                        isLoading = false,
                        selectedIds = if (targetItemId != null) setOf(targetItemId) else emptySet()
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onToggleSelection(itemId: String) {
        _uiState.update { state ->
            val newSelected = if (state.selectedIds.contains(itemId)) {
                state.selectedIds - itemId
            } else {
                state.selectedIds + itemId
            }
            state.copy(selectedIds = newSelected)
        }
    }

    fun onSelectAll() {
        _uiState.update { state ->
            state.copy(selectedIds = state.items.map { it.id }.toSet())
        }
    }

    fun onDeselectAll() {
        _uiState.update { it.copy(selectedIds = emptySet()) }
    }

    fun onImportSelected() {
        viewModelScope.launch {
            val selectedItems = _uiState.value.items.filter { it.id in _uiState.value.selectedIds }
            if (selectedItems.isNotEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
                repository.importItems(selectedItems)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
