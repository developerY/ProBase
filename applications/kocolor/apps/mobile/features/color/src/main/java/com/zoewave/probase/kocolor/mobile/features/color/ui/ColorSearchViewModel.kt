package com.zoewave.probase.kocolor.mobile.features.color.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.mobile.features.color.util.ColorScienceUtils
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.CosmeticItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchMode {
    EXACT, COMPLEMENTARY, ANALOGOUS, TRIADIC, MONOCHROMATIC
}

data class ColorSearchUiState(
    val selectedColorHex: String = "#C25C4A",
    val searchMode: SearchMode = SearchMode.EXACT,
    val matchedCosmetics: List<CosmeticItem> = emptyList(),
    val matchedWardrobe: List<ClothingItem> = emptyList(),
    val recentColors: List<String> = listOf(
        "#800020", "#C25C4A", "#8B8378", "#556B2F", "#BC8F8F",
        "#8B4513", "#D2691E", "#CD853F", "#6B8E23", "#D8BFD8",
        "#A98274", "#E9967A", "#C0C0C0", "#8FBC8F", "#FFB6C1"
    )
)

@HiltViewModel
class ColorSearchViewModel @Inject constructor(
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val sessionRepository: FashionSessionRepository
) : ViewModel() {

    private val _selectedColorHex = MutableStateFlow("#C25C4A")
    private val _searchMode = MutableStateFlow(SearchMode.EXACT)

    val uiState: StateFlow<ColorSearchUiState> = combine(
        _selectedColorHex,
        _searchMode,
        cosmeticRepository.getAllCosmetics(),
        wardrobeRepository.getAllClothing()
    ) { color, mode, cosmetics, wardrobe ->
        
        val filteredCosmetics = filterCosmetics(color, mode, cosmetics)
        val filteredWardrobe = filterWardrobe(color, mode, wardrobe)

        ColorSearchUiState(
            selectedColorHex = color,
            searchMode = mode,
            matchedCosmetics = filteredCosmetics,
            matchedWardrobe = filteredWardrobe
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ColorSearchUiState())

    init {
        // Listen for color scans from camera
        sessionRepository.capturedItemUri
            .filterNotNull()
            .onEach { uri ->
                // In a real app, we'd analyze the image to get the dominant color.
                // For now, we'll just mock a new color from scan.
                _selectedColorHex.value = "#CD5C5C" // Mock IndianRed
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ColorSearchEvent) {
        when (event) {
            is ColorSearchEvent.SelectColor -> _selectedColorHex.value = event.hex
            is ColorSearchEvent.SetMode -> _searchMode.value = event.mode
        }
    }

    private fun filterCosmetics(targetHex: String, mode: SearchMode, items: List<CosmeticItem>): List<CosmeticItem> {
        return items.filter { item ->
            val itemHex = item.colorHex ?: return@filter false
            isMatch(targetHex, itemHex, mode)
        }
    }

    private fun filterWardrobe(targetHex: String, mode: SearchMode, items: List<ClothingItem>): List<ClothingItem> {
        return items.filter { item ->
            val itemHex = item.dominantHex ?: item.colorHex ?: return@filter false
            isMatch(targetHex, itemHex, mode)
        }
    }

    private fun isMatch(target: String, item: String, mode: SearchMode): Boolean {
        return when (mode) {
            SearchMode.EXACT -> ColorScienceUtils.calculateDistance(target, item) < 50.0
            SearchMode.COMPLEMENTARY -> {
                val comp = ColorScienceUtils.getComplementary(target)
                ColorScienceUtils.calculateDistance(comp, item) < 80.0
            }
            SearchMode.ANALOGOUS -> {
                ColorScienceUtils.getAnalogous(target).any { 
                    ColorScienceUtils.calculateDistance(it, item) < 60.0 
                }
            }
            SearchMode.TRIADIC -> {
                ColorScienceUtils.getTriadic(target).any { 
                    ColorScienceUtils.calculateDistance(it, item) < 60.0 
                }
            }
            SearchMode.MONOCHROMATIC -> {
                ColorScienceUtils.getMonochromatic(target).any { 
                    ColorScienceUtils.calculateDistance(it, item) < 60.0 
                }
            }
        }
    }
}
