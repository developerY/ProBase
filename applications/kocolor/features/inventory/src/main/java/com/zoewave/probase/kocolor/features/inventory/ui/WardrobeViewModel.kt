package com.zoewave.probase.kocolor.features.inventory.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
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
    val itemsByCategory: Map<String, Int> = emptyMap(),
    val categoriesMetadata: Map<String, CategoryMetadata> = emptyMap()
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
    private val sessionRepository: FashionSessionRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _draftItem = MutableStateFlow(ClothingItem(name = "", category = ClothingCategory.TOPS, colorHex = "#FFFFFF"))

    private var lastProcessedUri: String? = null

    init {
        // 1. Initial load from Database if itemId is present
        val itemId: Long? = savedStateHandle["itemId"]
        itemId?.let { id ->
            if (id != 0L) {
                viewModelScope.launch {
                    wardrobeRepository.getClothingById(id).first()?.let { model ->
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
                    android.util.Log.d("WardrobeVM", "Processing NEW captured URI: $uri")
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
        wardrobeRepository.getAllClothing(),
        _draftItem
    ) { models, draft ->
        val totalInvestment = models.sumOf { it.price ?: 0.0 }
        val itemsByCategory = models.groupBy { it.category.name }.mapValues { it.value.size }
        
        val categoryMetadata = models.groupBy { it.category.name }.mapValues { (name, items) ->
            val cat = items.firstOrNull()?.category
            val representativeItem = items.filter { it.imageUrl != null }.maxByOrNull { it.timestamp } ?: items.maxByOrNull { it.timestamp }
            val brands = items.mapNotNull { it.brand }.groupBy { it }.mapValues { it.value.size }
            val leadingBrand = brands.maxByOrNull { it.value }?.key
            val usages = items.map { it.usageCount }
            val averageUsage = if (usages.isEmpty()) null else usages.average()

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

        WardrobeUiState(
            items = models,
            isLoading = false,
            draftItem = draft,
            totalInvestment = totalInvestment,
            totalItems = models.size,
            itemsByCategory = itemsByCategory,
            categoriesMetadata = categoryMetadata
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
                if (_draftItem.value.id != event.itemId) {
                    viewModelScope.launch {
                        wardrobeRepository.getClothingById(event.itemId).first()?.let { model ->
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
            id = 0L,
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
        android.util.Log.d("WardrobeVM", "Triggering save for item: ${item.name} (image: ${item.imageUrl})")
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
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
        viewModelScope.launch {
            val cloned = item.copy(
                id = 0L,
                sourceType = InventorySource.CLONED,
                sourcePackId = null,
                parentItemId = item.id.toString(),
                timestamp = System.currentTimeMillis()
            )
            wardrobeRepository.saveClothingItem(cloned)
            Log.d("WardrobeVM", "Clothing item cloned: ${cloned.name}")
        }
    }
}
