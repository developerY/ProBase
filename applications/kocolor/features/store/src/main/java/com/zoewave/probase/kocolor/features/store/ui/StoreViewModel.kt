package com.zoewave.probase.kocolor.features.store.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.ClothingItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val starterPackRepository: StarterPackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        fetchStoreInventory()
    }

    private fun fetchStoreInventory() {
        viewModelScope.launch {
            try {
                val envelope = starterPackRepository.getManifest()
                val manifest = envelope.data
                
                val cosmeticsPacks = manifest.packs.filter { pack -> 
                    pack.packType == "STARTER_PACK" && 
                    (pack.id.contains("cosmetics") || pack.id.contains("lips") || pack.id.contains("eyes") || pack.id.contains("nails") || pack.id.contains("hair") || pack.id.contains("prep") || pack.id.contains("frag")) 
                }.shuffled()

                val fashionPacks = manifest.packs.filter { pack -> 
                    pack.packType == "STARTER_PACK" && 
                    (pack.id.contains("fashion") || pack.id.contains("shirts") || pack.id.contains("pants") || pack.id.contains("shoes") || pack.id.contains("dresses") || pack.id.contains("outerwear") || pack.id.contains("active")) 
                }.shuffled()

                val completeCosmeticPack = manifest.packs.find { it.id == "com.kocolor.pack.cosmetics.complete" }
                val completeFashionPack = manifest.packs.find { it.id == "com.kocolor.pack.fashion.complete" }

                val realCosmetics = try {
                    if (completeCosmeticPack != null) {
                        val items = starterPackRepository.getPackItems(completeCosmeticPack.id)
                        items.filterIsInstance<CosmeticItemDto>().shuffled().take(6).map { dto ->
                            StoreProductItem(
                                id = dto.id,
                                title = dto.name,
                                subtitle = dto.notes ?: "${dto.brand} Formula",
                                category = dto.macroCategory.uppercase(),
                                price = "$${dto.price?.toInt() ?: 48}",
                                detailTag = dto.volume ?: "30ml",
                                badge = "98% Harmony",
                                shadeName = dto.shadeName?.uppercase() ?: "SIGNATURE SHADE",
                                shadeColor = try { Color(android.graphics.Color.parseColor(dto.colorHex)) } catch (e: Exception) { Color(0xFFD4AF37) },
                                imageModel = dto.imageUrl.ifBlank { dto.thumbnailUrl }
                            )
                        }
                    } else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                val realFashion = try {
                    if (completeFashionPack != null) {
                        val items = starterPackRepository.getPackItems(completeFashionPack.id)
                        items.filterIsInstance<ClothingItemDto>().shuffled().take(6).map { dto ->
                            StoreProductItem(
                                id = dto.id,
                                title = dto.name,
                                subtitle = dto.notes ?: "${dto.brand} Atelier",
                                category = dto.macroCategory.uppercase(),
                                price = "$${dto.price?.toInt() ?: 215}",
                                detailTag = dto.material ?: "Archival Cut",
                                badge = "Warm Tone",
                                shadeName = dto.shadeName?.uppercase(),
                                shadeColor = try { Color(android.graphics.Color.parseColor(dto.colorHex)) } catch (e: Exception) { null },
                                imageModel = dto.imageUrl.ifBlank { dto.thumbnailUrl }
                            )
                        }
                    } else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    cosmeticsPacks = cosmeticsPacks,
                    fashionPacks = fashionPacks,
                    realCosmeticItems = realCosmetics,
                    realFashionItems = realFashion
                )
            } catch (e: Exception) {
                // Handle failure
            }
        }
    }

    fun onEvent(event: StoreEvent) {
        when (event) {
            StoreEvent.ToggleExpansion -> {
                _uiState.value = _uiState.value.copy(isExpanded = !_uiState.value.isExpanded)
            }
            is StoreEvent.AddProductToInventory -> {
                val newAdded = _uiState.value.addedItemIds + event.itemId
                val updatedCosmetics = _uiState.value.realCosmeticItems.map {
                    if (it.id == event.itemId) it.copy(isAdded = true) else it
                }
                val updatedFashion = _uiState.value.realFashionItems.map {
                    if (it.id == event.itemId) it.copy(isAdded = true) else it
                }
                _uiState.value = _uiState.value.copy(
                    addedItemIds = newAdded,
                    cartCount = newAdded.size,
                    realCosmeticItems = updatedCosmetics,
                    realFashionItems = updatedFashion
                )
            }
            is StoreEvent.ToggleFavorite -> {
                val currentFavs = _uiState.value.favoriteItemIds
                val newFavs = if (currentFavs.contains(event.itemId)) currentFavs - event.itemId else currentFavs + event.itemId
                val updatedCosmetics = _uiState.value.realCosmeticItems.map {
                    if (it.id == event.itemId) it.copy(isFavorite = newFavs.contains(it.id)) else it
                }
                val updatedFashion = _uiState.value.realFashionItems.map {
                    if (it.id == event.itemId) it.copy(isFavorite = newFavs.contains(it.id)) else it
                }
                _uiState.value = _uiState.value.copy(
                    favoriteItemIds = newFavs,
                    realCosmeticItems = updatedCosmetics,
                    realFashionItems = updatedFashion
                )
            }
            StoreEvent.EnterStore -> {}
        }
    }
}
