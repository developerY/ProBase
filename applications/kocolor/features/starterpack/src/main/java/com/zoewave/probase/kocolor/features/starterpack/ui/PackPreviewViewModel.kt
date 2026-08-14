package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.ClothingItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.KcpsPayload
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.ProductEditorialNotes
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
    val filteredItems: List<PackItemDto> = emptyList(),
    val groupedItems: Map<String, List<PackItemDto>> = emptyMap(),
    val selectedIds: Set<String> = emptySet(),
    val collapsedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isInstalled: Boolean = false,
    val targetItemId: String? = null,
    val searchQuery: String = "",
    val sortByValue: Boolean = false,
    val selectedItemNotes: ProductEditorialNotes? = null,
    val selectedItemThumbnail: String? = null,
    val selectedItemColor: String? = null,
    val isNotesLoading: Boolean = false
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
                    val filtered = applyFilterAndSort(items, state.searchQuery, state.sortByValue)
                    state.copy(
                        items = items, 
                        filteredItems = filtered,
                        groupedItems = filtered.groupBy { it.macroCategory },
                        isLoading = false,
                        selectedIds = if (state.targetItemId != null) setOf(state.targetItemId) else emptySet()
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.items, query, state.sortByValue)
            state.copy(
                searchQuery = query,
                filteredItems = filtered,
                groupedItems = filtered.groupBy { it.macroCategory }
            )
        }
    }

    fun onToggleValueSort() {
        _uiState.update { state ->
            val newSort = !state.sortByValue
            val filtered = applyFilterAndSort(state.items, state.searchQuery, newSort)
            state.copy(
                sortByValue = newSort,
                filteredItems = filtered,
                groupedItems = filtered.groupBy { it.macroCategory }
            )
        }
    }

    private fun applyFilterAndSort(
        items: List<PackItemDto>,
        query: String,
        sortByValue: Boolean
    ): List<PackItemDto> {
        val filtered = if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                item.brand?.contains(query, ignoreCase = true) == true ||
                item.calculatedSearchTokens.any { it.contains(query, ignoreCase = true) }
            }
        }

        return if (sortByValue) {
            filtered.sortedBy { it.calculatedUnitPrice ?: Double.MAX_VALUE }
        } else {
            filtered
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

    fun onItemInfoClick(itemId: String) {
        val item = _uiState.value.items.find { it.id == itemId }
        val thumbnail = item?.thumbnailUrl
        val color = item?.colorHex
        viewModelScope.launch {
            _uiState.update { it.copy(
                isNotesLoading = true, 
                selectedItemNotes = null, 
                selectedItemThumbnail = thumbnail,
                selectedItemColor = color
            ) }
            repository.getProductEditorialNotes(itemId)
                .onSuccess { notes ->
                    _uiState.update { it.copy(isNotesLoading = false, selectedItemNotes = notes) }
                }
                .onFailure {
                    _uiState.update { it.copy(isNotesLoading = false) }
                }
        }
    }

    fun onDismissNotes() {
        _uiState.update { it.copy(selectedItemNotes = null, selectedItemThumbnail = null, selectedItemColor = null) }
    }
}
