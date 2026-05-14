package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.data.CosmeticDefaults
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.kocolor.model.CosmeticItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CosmeticsUiState(
    val items: List<CosmeticItem> = emptyList(),
    val isLoading: Boolean = true
)

sealed class CosmeticsEvent {
    data class AddItem(val item: CosmeticItem) : CosmeticsEvent()
    data class DeleteItem(val id: Long) : CosmeticsEvent()
}

@HiltViewModel
class CosmeticsViewModel @Inject constructor(
    private val cosmeticDao: CosmeticDao
) : ViewModel() {

    init {
        viewModelScope.launch {
            cosmeticDao.getAllCosmetics().first().let {
                if (it.isEmpty()) {
                    initializeDefaultCosmetics()
                }
            }
        }
    }

    private suspend fun initializeDefaultCosmetics() {
        for (item in CosmeticDefaults.getDefaultCosmetics()) {
            cosmeticDao.insertCosmetic(item)
        }
    }

    val uiState: StateFlow<CosmeticsUiState> = cosmeticDao.getAllCosmetics()
        .map { entities ->
            CosmeticsUiState(
                items = entities.map { it.toModel() },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CosmeticsUiState())

    fun onEvent(event: CosmeticsEvent) {
        when (event) {
            is CosmeticsEvent.AddItem -> addItem(event.item)
            is CosmeticsEvent.DeleteItem -> deleteItem(event.id)
        }
    }

    private fun addItem(item: CosmeticItem) {
        viewModelScope.launch {
            cosmeticDao.insertCosmetic(item.toEntity())
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            cosmeticDao.deleteCosmetic(id)
        }
    }

    private fun CosmeticItemEntity.toModel() = CosmeticItem(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        shadeName = shadeName,
        notes = notes,
        timestamp = timestamp
    )

    private fun CosmeticItem.toEntity() = CosmeticItemEntity(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        shadeName = shadeName,
        notes = notes,
        timestamp = timestamp
    )
}
