package com.zoewave.probase.kocolor.features.inventory.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.usecase.RotationScoringUseCase
import com.zoewave.probase.core.model.ritual.ArchiveStatus
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.InventorySource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CategoryMetadata(
    val itemCount: Int = 0,
    val totalValue: Double = 0.0,
    val representativeImageUrl: String? = null,
    val representativeColorHex: String? = null,
    val leadingBrand: String? = null,
    val averageUsage: Double? = null,
    val description: String? = null
)

data class WardrobeUiState(
    val items: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true,
    val draftItem: ClothingItem = ClothingItem(name = "", category = ClothingCategory.TOPS, colorHex = "#FFFFFF"),
    val totalInvestment: Double = 0.0,
    val totalItems: Int = 0,
    val totalOutfitsCommitted: Long = 0,
    val itemsByCategory: Map<String, Int> = emptyMap(),
    val categoriesMetadata: Map<String, CategoryMetadata> = emptyMap(),
    val archiveStatuses: Map<Long, ArchiveStatus> = emptyMap(),
    val glowScore: Double? = null, // Unique Items Worn / Total Items. Null if cold start.
    val diversityIndex: String = "Calculating..."
)

sealed class WardrobeEvent {
    data class AddItem(val item: ClothingItem) : WardrobeEvent()
    data class UpdateItem(val item: ClothingItem) : WardrobeEvent()
    data class DeleteItem(val id: Long) : WardrobeEvent()
    data class WearItem(val id: Long) : WardrobeEvent()
    data class UpdateDraft(val item: ClothingItem) : WardrobeEvent()
    data class InitializeEdit(val itemId: Long) : WardrobeEvent()
    data class CloneToPersonal(val item: ClothingItem) : WardrobeEvent()
}

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
    private val rotationScoringUseCase: RotationScoringUseCase,
    private val sessionRepository: FashionSessionRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _draftItem = MutableStateFlow(ClothingItem(name = "", category = ClothingCategory.TOPS, colorHex = "#FFFFFF"))
    private val _archiveStatuses = MutableStateFlow<Map<Long, ArchiveStatus>>(emptyMap())

    private var lastProcessedUri: String? = null

    init {
        // 1. Initial load from Database if itemId is present
        val itemId: Long? = savedStateHandle["itemId"]
        itemId?.let { id ->
            if (id != 0L) {
                viewModelScope.launch {
                    wardrobeRepository.getClothingById(id).firstOrNull()?.let { model ->
                        _draftItem.update { current ->
                            // If we already have a NEW image in current (captured), keep it
                            if (current.imageUrl != null && current.imageUrl != model.imageUrl) {
                                model.copy(imageUrl = current.imageUrl)
                            } else {
                                model
                            }
                        }
                    }
                }
            }
        }

        // 2. Listen for captures (can happen after return from camera)
        sessionRepository.capturedItemUri
            .filterNotNull()
            .onEach { uri ->
                if (uri != lastProcessedUri) {
                    Log.d("WardrobeVM", "Processing NEW captured URI: $uri")
                    lastProcessedUri = uri
                    val properUri = if (uri.startsWith("content://") || uri.startsWith("file://")) {
                        uri
                    } else {
                        "file://$uri"
                    }
                    _draftItem.update { it.copy(imageUrl = properUri, dominantHex = null) }
                }
            }
            .launchIn(viewModelScope)
    }

    val uiState: StateFlow<WardrobeUiState> = combine(
        rotationScoringUseCase.observeAllClothingWithUsage(),
        rotationScoringUseCase.observeGlobalMetrics(),
        _draftItem,
        _archiveStatuses
    ) { itemsWithUsage, globalMetrics, draft, archiveStatuses ->
        val enrichedModels = itemsWithUsage.map { wrapper ->
            wrapper.garment.copy(
                usageCount = wrapper.usage?.useCount?.toInt() ?: 0,
                lastUsedTimestamp = wrapper.usage?.lastUsedTimestamp
            )
        }

        val totalInvestment = enrichedModels.sumOf { it.price ?: 0.0 }
        val itemsByCategory = enrichedModels.groupBy { it.category.name }.mapValues { it.value.size }
        
        val categoryMetadata = enrichedModels.groupBy { it.category.name }.mapValues { (name, items) ->
            val cat = items.firstOrNull()?.category
            val representativeItem = items.filter { it.imageUrl != null }.maxByOrNull { it.timestamp } ?: items.maxByOrNull { it.timestamp }
            val brands = items.mapNotNull { it.brand }.groupBy { it }.mapValues { it.value.size }
            val leadingBrand = brands.maxByOrNull { it.value }?.key
            val usagesCount = items.map { it.usageCount }
            val averageUsage = if (usagesCount.isEmpty()) null else usagesCount.average()

            CategoryMetadata(
                itemCount = items.size,
                totalValue = items.sumOf { it.price ?: 0.0 },
                representativeImageUrl = representativeItem?.imageUrl,
                representativeColorHex = representativeItem?.colorHex,
                leadingBrand = leadingBrand,
                averageUsage = averageUsage,
                description = cat?.description
            )
        }

        val itemsWithAnyUsage = enrichedModels.count { it.usageCount > 0 }
        val totalOutfits = globalMetrics?.totalOutfitsCommitted ?: 0L
        
        val glowScore = if (totalOutfits < 5) {
            null // Cold Start
        } else if (enrichedModels.isNotEmpty()) {
            itemsWithAnyUsage.toDouble() / enrichedModels.size
        } else {
            0.0
        }
        
        // Diversity Index logic based on category distribution
        val diversityIndex = if (totalOutfits < 5) {
            "Initializing"
        } else if (itemsByCategory.size >= 4) {
            "Eclectic"
        } else if (itemsByCategory.size >= 2) {
            "Strategic"
        } else {
            "Focused"
        }

        WardrobeUiState(
            items = enrichedModels,
            isLoading = false,
            draftItem = draft,
            totalInvestment = totalInvestment,
            totalItems = enrichedModels.size,
            totalOutfitsCommitted = totalOutfits,
            itemsByCategory = itemsByCategory,
            categoriesMetadata = categoryMetadata,
            archiveStatuses = archiveStatuses,
            glowScore = glowScore,
            diversityIndex = diversityIndex
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WardrobeUiState())

    fun onEvent(event: WardrobeEvent) {
        when (event) {
            is WardrobeEvent.AddItem -> addItem(event.item)
            is WardrobeEvent.UpdateItem -> updateItem(event.item)
            is WardrobeEvent.DeleteItem -> deleteItem(event.id)
            is WardrobeEvent.WearItem -> wearItem(event.id)
            is WardrobeEvent.UpdateDraft -> _draftItem.value = event.item
            is WardrobeEvent.InitializeEdit -> {
                if (_draftItem.value.internalId != event.itemId) {
                    viewModelScope.launch {
                        wardrobeRepository.getClothingById(event.itemId).firstOrNull()?.let { model ->
                            _draftItem.update { current ->
                                // Prioritize newly captured image in current draft
                                if (current.imageUrl != null && current.imageUrl != model.imageUrl) {
                                    model.copy(imageUrl = current.imageUrl, dominantHex = null)
                                } else {
                                    model
                                }
                            }
                        }
                    }
                }
            }
            is WardrobeEvent.CloneToPersonal -> cloneToPersonal(event.item)
        }
    }

    private fun addItem(item: ClothingItem) {
        val userItem = item.copy(
            internalId = 0L,
            sourceType = InventorySource.USER_SCAN,
            sourceName = "My Wardrobe",
            sourcePackId = null
        )
        Log.d("WardrobeVM", "Triggering add for item: ${userItem.name} (image: ${userItem.imageUrl})")
        viewModelScope.launch {
            withContext(Dispatchers.IO + NonCancellable) {
                wardrobeRepository.saveClothingItem(userItem)
            }
        }
    }

    private fun updateItem(item: ClothingItem) {
        Log.d("WardrobeVM", "Triggering save for item: ${item.name} (image: ${item.imageUrl})")
        viewModelScope.launch {
            withContext(Dispatchers.IO + NonCancellable) {
                wardrobeRepository.saveClothingItem(item)
            }
        }
    }

    private fun wearItem(id: Long) {
        viewModelScope.launch {
            wardrobeRepository.wearClothingItem(id)
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            wardrobeRepository.deleteClothing(id)
        }
    }

    private fun cloneToPersonal(item: ClothingItem) {
        if (_archiveStatuses.value[item.internalId] == ArchiveStatus.SUCCESS || 
            _archiveStatuses.value[item.internalId] == ArchiveStatus.ARCHIVING) return

        viewModelScope.launch {
            _archiveStatuses.update { it + (item.internalId to ArchiveStatus.ARCHIVING) }
            
            try {
                wardrobeRepository.cloneToPersonalArchive(item.internalId)
                _archiveStatuses.update { it + (item.internalId to ArchiveStatus.SUCCESS) }
            } catch (e: Exception) {
                Log.e("WardrobeVM", "Cloning failed", e)
                _archiveStatuses.update { it + (item.internalId to ArchiveStatus.ERROR) }
            }
        }
    }
}
