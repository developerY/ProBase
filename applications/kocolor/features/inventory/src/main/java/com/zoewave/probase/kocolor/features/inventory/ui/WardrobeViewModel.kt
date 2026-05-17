package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.db.data.ClothingDefaults
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WardrobeUiState(
    val items: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true,
    val draftItem: ClothingItem = ClothingItem(name = "", category = ClothingCategory.TOPS)
)

sealed class WardrobeEvent {
    data class AddItem(val item: ClothingItem) : WardrobeEvent()
    data class UpdateItem(val item: ClothingItem) : WardrobeEvent()
    data class DeleteItem(val id: Long) : WardrobeEvent()
    data class UpdateDraft(val item: ClothingItem) : WardrobeEvent()
    data class InitializeEdit(val itemId: Long) : WardrobeEvent()
}

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val clothingDao: ClothingDao,
    private val sessionRepository: FashionSessionRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _draftItem = MutableStateFlow(ClothingItem(name = "", category = ClothingCategory.TOPS))

    init {
        viewModelScope.launch {
            clothingDao.getAllClothing().first().let {
                if (it.isEmpty()) {
                    initializeDefaultClothing()
                }
            }
        }

        // Listen for new captures
        sessionRepository.clothesUri
            .filterNotNull()
            .onEach { uri ->
                _draftItem.value = _draftItem.value.copy(imageUrl = uri)
                sessionRepository.setClothesUri(null) // Consume
            }
            .launchIn(viewModelScope)

        // Initialize draft from itemId if editing
        val itemId: Long? = savedStateHandle["itemId"]
        itemId?.let { id ->
            if (id != 0L) {
                viewModelScope.launch {
                    clothingDao.getClothingById(id).first()?.let { entity ->
                        _draftItem.value = entity.toModel()
                    }
                }
            }
        }
    }

    private suspend fun initializeDefaultClothing() {
        for (item in ClothingDefaults.getDefaultClothing()) {
            clothingDao.insertClothing(item)
        }
    }

    val uiState: StateFlow<WardrobeUiState> = combine(
        clothingDao.getAllClothing(),
        _draftItem
    ) { entities, draft ->
        WardrobeUiState(
            items = entities.map { it.toModel() },
            isLoading = false,
            draftItem = draft
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WardrobeUiState())

    fun onEvent(event: WardrobeEvent) {
        when (event) {
            is WardrobeEvent.AddItem -> addItem(event.item)
            is WardrobeEvent.UpdateItem -> updateItem(event.item)
            is WardrobeEvent.DeleteItem -> deleteItem(event.id)
            is WardrobeEvent.UpdateDraft -> _draftItem.value = event.item
            is WardrobeEvent.InitializeEdit -> {
                viewModelScope.launch {
                    clothingDao.getClothingById(event.itemId).first()?.let { entity ->
                        _draftItem.value = entity.toModel()
                    }
                }
            }
        }
    }

    private fun addItem(item: ClothingItem) {
        viewModelScope.launch {
            clothingDao.insertClothing(item.toEntity())
        }
    }

    private fun updateItem(item: ClothingItem) {
        viewModelScope.launch {
            clothingDao.updateClothing(item.toEntity())
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            clothingDao.deleteClothing(id)
        }
    }

    private fun ClothingItemEntity.toModel() = ClothingItem(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        size = size,
        material = material,
        price = price,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp
    )

    private fun ClothingItem.toEntity() = ClothingItemEntity(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        size = size,
        material = material,
        price = price,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp
    )
}
