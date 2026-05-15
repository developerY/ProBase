package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.data.ClothingDefaults
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WardrobeUiState(
    val items: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true
)

sealed class WardrobeEvent {
    data class AddItem(val item: ClothingItem) : WardrobeEvent()
    data class DeleteItem(val id: Long) : WardrobeEvent()
}

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val clothingDao: ClothingDao
) : ViewModel() {

    init {
        viewModelScope.launch {
            clothingDao.getAllClothing().first().let {
                if (it.isEmpty()) {
                    initializeDefaultClothing()
                }
            }
        }
    }

    private suspend fun initializeDefaultClothing() {
        for (item in ClothingDefaults.getDefaultClothing()) {
            clothingDao.insertClothing(item)
        }
    }

    val uiState: StateFlow<WardrobeUiState> = clothingDao.getAllClothing()
        .map { entities ->
            WardrobeUiState(
                items = entities.map { it.toModel() },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WardrobeUiState())

    fun onEvent(event: WardrobeEvent) {
        when (event) {
            is WardrobeEvent.AddItem -> addItem(event.item)
            is WardrobeEvent.DeleteItem -> deleteItem(event.id)
        }
    }

    private fun addItem(item: ClothingItem) {
        viewModelScope.launch {
            clothingDao.insertClothing(item.toEntity())
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
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp
    )
}
