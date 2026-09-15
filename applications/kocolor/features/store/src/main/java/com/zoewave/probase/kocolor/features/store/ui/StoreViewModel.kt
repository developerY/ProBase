package com.zoewave.probase.kocolor.features.store.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
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
                
                val cosmetics = manifest.packs.filter { pack -> 
                    pack.packType == "STARTER_PACK" && 
                    (pack.id.contains("cosmetics") || pack.id.contains("lips") || pack.id.contains("eyes") || pack.id.contains("nails") || pack.id.contains("hair") || pack.id.contains("prep") || pack.id.contains("frag")) 
                }
                val fashion = manifest.packs.filter { pack -> 
                    pack.packType == "STARTER_PACK" && 
                    (pack.id.contains("fashion") || pack.id.contains("shirts") || pack.id.contains("pants") || pack.id.contains("shoes") || pack.id.contains("dresses") || pack.id.contains("outerwear") || pack.id.contains("active")) 
                }

                _uiState.value = _uiState.value.copy(
                    cosmeticsPacks = cosmetics,
                    fashionPacks = fashion
                )
            } catch (e: Exception) {
                // Handle failure implicitly for now
            }
        }
    }

    fun onEvent(event: StoreEvent) {
        when (event) {
            StoreEvent.ToggleExpansion -> {
                _uiState.value = _uiState.value.copy(isExpanded = !_uiState.value.isExpanded)
            }
            StoreEvent.EnterStore -> {}
        }
    }
}
