package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import com.zoewave.probase.kocolor.data.usecase.StyleSimulatorEngine
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StyleResultViewModel @Inject constructor(
    private val simulatorEngine: StyleSimulatorEngine,
    private val fashionistaEvaluator: FashionistaEvaluator,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val fashionRepository: FashionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StyleResultUiState())
    val uiState: StateFlow<StyleResultUiState> = _uiState.asStateFlow()

    fun generateStyleRecommendation(intent: String = "Daily Outfit") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val wardrobe = wardrobeRepository.getAllClothing().first()
                val cosmetics = cosmeticRepository.getAllCosmetics().first()
                val context = StyleRequestContext(intent = intent)

                val blueprint = simulatorEngine.generateBlueprint(wardrobe, cosmetics, context)
                val fashionistaScore = fashionistaEvaluator.evaluate(blueprint, context)

                val selectedClothing = wardrobe.filter { item ->
                    "w_${item.internalId}" in blueprint.selectedClothingIds || item.remoteId in blueprint.selectedClothingIds
                }
                val selectedCosmetics = cosmetics.filter { item ->
                    "c_${item.internalId}" in blueprint.selectedCosmeticIds || item.remoteId in blueprint.selectedCosmeticIds
                }

                _uiState.value = StyleResultUiState(
                    blueprint = blueprint,
                    fashionistaScore = fashionistaScore,
                    selectedClothing = selectedClothing,
                    selectedCosmetics = selectedCosmetics,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to generate style recommendation."
                )
            }
        }
    }
}
