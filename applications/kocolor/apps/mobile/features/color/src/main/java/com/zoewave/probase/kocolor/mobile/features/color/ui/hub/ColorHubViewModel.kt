package com.zoewave.probase.kocolor.mobile.features.color.ui.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorSignature
import com.zoewave.probase.kocolor.features.colors.domain.model.StylistEdit
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorIntelligenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ColorHubUiState(
    val inventoryColors: List<ColorSignature> = emptyList(),
    val paletteGaps: List<String> = emptyList(),
    val stylistEdit: StylistEdit? = null,
    val userSeason: SeasonalType = SeasonalType.WINTER // Mocked for now
)

@HiltViewModel
class ColorHubViewModel @Inject constructor(
    private val colorIntelligenceRepository: ColorIntelligenceRepository
) : ViewModel() {

    val uiState: StateFlow<ColorHubUiState> = combine(
        colorIntelligenceRepository.getAllInventoryColors(),
        colorIntelligenceRepository.getPaletteGaps(SeasonalType.WINTER),
        colorIntelligenceRepository.getStylistEdit(SeasonalType.WINTER),
        flowOf(SeasonalType.WINTER)
    ) { colors, gaps, edit, season ->
        ColorHubUiState(
            inventoryColors = colors,
            paletteGaps = gaps,
            stylistEdit = edit,
            userSeason = season
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ColorHubUiState())
}
