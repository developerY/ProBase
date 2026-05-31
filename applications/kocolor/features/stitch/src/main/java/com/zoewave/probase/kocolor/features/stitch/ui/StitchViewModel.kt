package com.zoewave.probase.kocolor.features.stitch.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StitchUiState(
    val collectionId: Long = 0,
    val draftAdvice: FashionAdvice? = null,
    val allCosmetics: List<CosmeticItem> = emptyList(),
    val allWardrobe: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val pickingTarget: PickingTarget? = null
)

sealed class PickingTarget {
    data class Makeup(val index: Int) : PickingTarget()
    data class Outfit(val outfitIndex: Int, val itemIndex: Int) : PickingTarget()
}

sealed class StitchEvent {
    data class Initialize(val id: Long, val isCopy: Boolean = false) : StitchEvent()
    data class UpdateTitle(val title: String) : StitchEvent()
    data class UpdateSummary(val summary: String) : StitchEvent()
    data class DeleteCollection(val id: Long) : StitchEvent()
    data object SaveCollection : StitchEvent()
    
    // Item Management
    data class RequestPickItem(val target: PickingTarget) : StitchEvent()
    data class OnItemSelected(val item: Any) : StitchEvent() // Can be CosmeticItem or ClothingItem
    data object ClearPickingTarget : StitchEvent()
    
    data class RemoveMakeupSuggestion(val index: Int) : StitchEvent()
    data class RemoveOutfitItem(val outfitIndex: Int, val itemIndex: Int) : StitchEvent()
    data object AddMakeupSlot : StitchEvent()
    data object AddOutfitSlot : StitchEvent()
}

@HiltViewModel
class StitchViewModel @Inject constructor(
    private val fashionRepository: FashionRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(StitchUiState())
    val uiState: StateFlow<StitchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                cosmeticRepository.getAllCosmetics(),
                wardrobeRepository.getAllClothing()
            ) { cosmetics, wardrobe ->
                _uiState.update { it.copy(allCosmetics = cosmetics, allWardrobe = wardrobe, isLoading = false) }
            }.collect()
        }
    }

    fun onEvent(event: StitchEvent) {
        when (event) {
            is StitchEvent.Initialize -> loadCollection(event.id, event.isCopy)
            is StitchEvent.UpdateTitle -> updateDraft { it.copy(title = event.title) }
            is StitchEvent.UpdateSummary -> updateDraft { it.copy(summary = event.summary) }
            is StitchEvent.DeleteCollection -> deleteCollection(event.id)
            StitchEvent.SaveCollection -> saveCollection()
            is StitchEvent.RequestPickItem -> _uiState.update { it.copy(pickingTarget = event.target) }
            StitchEvent.ClearPickingTarget -> _uiState.update { it.copy(pickingTarget = null) }
            is StitchEvent.OnItemSelected -> handleItemSelected(event.item)
            is StitchEvent.RemoveMakeupSuggestion -> removeMakeup(event.index)
            is StitchEvent.RemoveOutfitItem -> removeOutfitItem(event.outfitIndex, event.itemIndex)
            StitchEvent.AddMakeupSlot -> addMakeupSlot()
            StitchEvent.AddOutfitSlot -> addOutfitSlot()
        }
    }

    private fun loadCollection(id: Long, isCopy: Boolean) {
        if (id == 0L) {
            _uiState.update { it.copy(draftAdvice = emptyAdvice(), collectionId = 0) }
            return
        }
        viewModelScope.launch {
            fashionRepository.getSuggestionById(id)?.let { analysis ->
                _uiState.update { 
                    it.copy(
                        collectionId = if (isCopy) 0 else id,
                        draftAdvice = if (isCopy) analysis.advice.copy(title = "Copy of ${analysis.advice.title}") else analysis.advice
                    )
                }
            }
        }
    }

    private fun handleItemSelected(item: Any) {
        val target = _uiState.value.pickingTarget ?: return
        when (target) {
            is PickingTarget.Makeup -> {
                if (item is CosmeticItem) {
                    updateMakeup(target.index, item)
                }
            }
            is PickingTarget.Outfit -> {
                if (item is ClothingItem) {
                    updateOutfitItem(target.outfitIndex, target.itemIndex, item)
                }
            }
        }
        _uiState.update { it.copy(pickingTarget = null) }
    }

    private fun updateMakeup(index: Int, item: CosmeticItem) {
        updateDraft { advice ->
            val newList = advice.makeupSuggestions.toMutableList()
            if (index < newList.size) {
                newList[index] = newList[index].copy(
                    productId = item.id,
                    suggestedProductName = item.name,
                    suggestedProductImageUrl = item.imageUrl,
                    category = item.microCategory.displayName
                )
            } else {
                newList.add(MakeupSuggestion(
                    category = item.microCategory.displayName,
                    advice = "Custom selection.",
                    recommendedColors = item.colorHex?.let { listOf(it) } ?: emptyList(),
                    productId = item.id,
                    suggestedProductName = item.name,
                    suggestedProductImageUrl = item.imageUrl
                ))
            }
            advice.copy(makeupSuggestions = newList)
        }
    }

    private fun updateOutfitItem(outfitIndex: Int, itemIndex: Int, item: ClothingItem) {
        updateDraft { advice ->
            val newList = advice.outfitSuggestions.toMutableList()
            if (outfitIndex < newList.size) {
                val outfit = newList[outfitIndex]
                val itemIds = outfit.wardrobeItemIds.toMutableList()
                val suggestedItems = outfit.suggestedItems.toMutableList()
                
                // For simplicity, we match itemIndex to the lists
                if (itemIndex < itemIds.size) {
                    itemIds[itemIndex] = item.id
                    suggestedItems[itemIndex] = SuggestedPiece(
                        name = item.name,
                        category = item.category.name,
                        imageUrl = item.imageUrl,
                        description = item.notes,
                        isOwned = true
                    )
                } else {
                    itemIds.add(item.id)
                    suggestedItems.add(SuggestedPiece(
                        name = item.name,
                        category = item.category.name,
                        imageUrl = item.imageUrl,
                        description = item.notes,
                        isOwned = true
                    ))
                }
                newList[outfitIndex] = outfit.copy(
                    wardrobeItemIds = itemIds,
                    suggestedItems = suggestedItems
                )
            }
            advice.copy(outfitSuggestions = newList)
        }
    }

    private fun removeMakeup(index: Int) {
        updateDraft { advice ->
            val newList = advice.makeupSuggestions.toMutableList()
            if (index in newList.indices) newList.removeAt(index)
            advice.copy(makeupSuggestions = newList)
        }
    }

    private fun removeOutfitItem(outfitIndex: Int, itemIndex: Int) {
        updateDraft { advice ->
            val outfits = advice.outfitSuggestions.toMutableList()
            if (outfitIndex in outfits.indices) {
                val outfit = outfits[outfitIndex]
                val suggestedItems = outfit.suggestedItems.toMutableList()
                val itemIds = outfit.wardrobeItemIds.toMutableList()
                
                if (itemIndex in suggestedItems.indices) {
                    suggestedItems.removeAt(itemIndex)
                    // Also try to remove from IDs if they match (simplified)
                    if (itemIndex in itemIds.indices) itemIds.removeAt(itemIndex)
                }
                
                outfits[outfitIndex] = outfit.copy(
                    suggestedItems = suggestedItems,
                    wardrobeItemIds = itemIds
                )
            }
            advice.copy(outfitSuggestions = outfits)
        }
    }

    private fun addMakeupSlot() {
        updateDraft { advice ->
            val makeups = advice.makeupSuggestions.toMutableList()
            makeups.add(MakeupSuggestion(category = "LIPS", advice = "Suggested.", recommendedColors = emptyList()))
            advice.copy(makeupSuggestions = makeups)
        }
    }

    private fun addOutfitSlot() {
        updateDraft { advice ->
            val outfits = advice.outfitSuggestions.toMutableList()
            if (outfits.isEmpty()) {
                outfits.add(OutfitSuggestion(occasion = "New Look", advice = "", keyPieces = emptyList(), colorCombinations = emptyList()))
            }
            val firstOutfit = outfits[0]
            val suggestedItems = firstOutfit.suggestedItems.toMutableList()
            suggestedItems.add(SuggestedPiece(name = "New Item", category = "TOPS", isOwned = false))
            outfits[0] = firstOutfit.copy(suggestedItems = suggestedItems)
            advice.copy(outfitSuggestions = outfits)
        }
    }

    private fun saveCollection() {
        val advice = _uiState.value.draftAdvice ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            fashionRepository.saveSuggestion(advice)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun deleteCollection(id: Long) {
        viewModelScope.launch {
            fashionRepository.deleteSuggestion(id)
        }
    }

    private fun updateDraft(block: (FashionAdvice) -> FashionAdvice) {
        _uiState.update { it.copy(draftAdvice = it.draftAdvice?.let(block)) }
    }

    private fun emptyAdvice() = FashionAdvice(
        title = "New Collection",
        summary = "Custom curated collection.",
        seasonalType = SeasonalType.UNKNOWN,
        undertone = Undertone.UNKNOWN,
        makeupSuggestions = emptyList(),
        outfitSuggestions = emptyList(),
        recommendedPalette = emptyList()
    )
}
