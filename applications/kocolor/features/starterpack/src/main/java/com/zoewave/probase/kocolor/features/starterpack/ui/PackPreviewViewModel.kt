package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.ClothingItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.KcpsPayload
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackPreviewUiState(
    val items: List<PackItemDto> = emptyList(),
    val groupedItems: Map<String, List<PackItemDto>> = emptyMap(),
    val selectedIds: Set<String> = emptySet(),
    val collapsedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isInstalled: Boolean = false,
    val targetItemId: String? = null
)

@HiltViewModel
class PackPreviewViewModel @Inject constructor(
    private val repository: StarterPackRepository,
    private val syncRepository: PackSyncRepository
) : ViewModel() {

    private var packId: String? = null

    private val _uiState = MutableStateFlow(PackPreviewUiState())
    val uiState: StateFlow<PackPreviewUiState> = _uiState.asStateFlow()

    fun initialize(packId: String, targetItemId: String?, sha256: String?, publisher: String?) {
        if (this.packId != null) return // Already initialized
        
        this.packId = packId
        
        _uiState.update { it.copy(targetItemId = targetItemId) }
        
        // Check if pack is installed to show Wipe button
        viewModelScope.launch {
            syncRepository.getInstalledPacks().collectLatest { installedPacks ->
                val isInstalled = installedPacks.any { it.packId == packId && it.status == PackStatus.INSTALLED }
                _uiState.update { it.copy(isInstalled = isInstalled) }
            }
        }
        
        loadPackItems()
    }

    private fun loadPackItems() {
        val currentPackId = packId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val items = repository.getPackItems(currentPackId)
                _uiState.update { state ->
                    state.copy(
                        items = items, 
                        groupedItems = items.groupBy { it.macroCategory ?: "OTHER" },
                        isLoading = false,
                        selectedIds = if (state.targetItemId != null) setOf(state.targetItemId) else emptySet()
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

    fun onToggleCategoryCollapse(category: String) {
        _uiState.update { state ->
            val updated = if (state.collapsedCategories.contains(category)) {
                state.collapsedCategories - category
            } else {
                state.collapsedCategories + category
            }
            state.copy(collapsedCategories = updated)
        }
    }

    fun onSelectCategoryAll(category: String) {
        _uiState.update { state ->
            val categoryItemIds = state.groupedItems[category]?.map { it.id }.orEmpty()
            state.copy(selectedIds = state.selectedIds + categoryItemIds)
        }
    }

    fun onClearCategory(category: String) {
        _uiState.update { state ->
            val categoryItemIds = state.groupedItems[category]?.map { it.id }.orEmpty().toSet()
            state.copy(selectedIds = state.selectedIds - categoryItemIds)
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
        val currentPackId = packId ?: return
        viewModelScope.launch {
            val selectedItems = _uiState.value.items.filter { it.id in _uiState.value.selectedIds }
            if (selectedItems.isNotEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
                
                // Construct a V1 payload for selected items
                val cosmetics: List<CosmeticItemDto> = selectedItems.mapNotNull { it as? CosmeticItemDto }
                val clothing: List<ClothingItemDto> = selectedItems.mapNotNull { it as? ClothingItemDto }
                val payload = KcpsPayload(
                    schemaVersion = 1, 
                    cosmetics = cosmetics, 
                    clothing = clothing
                )
                
                syncRepository.importSelectedItems(currentPackId, payload)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onWipeCollection() {
        val currentPackId = packId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            syncRepository.wipePack(currentPackId)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
