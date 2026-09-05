package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.usecase.GenerateStyleResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StyleResultViewModel @Inject constructor(
    private val generateStyleResultUseCase: GenerateStyleResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StyleResultUiState())
    val uiState: StateFlow<StyleResultUiState> = _uiState.asStateFlow()

    fun generateStyleRecommendation(intent: String = "Daily Outfit") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = generateStyleResultUseCase.execute(intent)
                _uiState.value = StyleResultUiState(
                    blueprint = result.blueprint,
                    fashionistaScore = result.fashionistaScore,
                    fashionistaCoverage = result.fashionistaScore.coverage.toFloat(),
                    calibrationVersion = result.fashionistaScore.standardVersion,
                    selectedClothing = result.selectedClothing,
                    selectedCosmetics = result.selectedCosmetics,
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
