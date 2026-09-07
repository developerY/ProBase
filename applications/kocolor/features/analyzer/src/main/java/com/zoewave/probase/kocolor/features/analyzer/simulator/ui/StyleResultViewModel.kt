package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.repository.StyleResultRepository
import com.zoewave.probase.kocolor.data.usecase.GenerateStyleResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StyleResultViewModel @Inject constructor(
    private val generateStyleResultUseCase: GenerateStyleResultUseCase,
    private val styleResultRepository: StyleResultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StyleResultUiState())
    val uiState: StateFlow<StyleResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            styleResultRepository.latestResult.collect { result ->
                if (result != null) {
                    _uiState.value = StyleResultUiState(
                        blueprint = result.blueprint,
                        fashionistaScore = result.fashionistaScore,
                        intentFulfillment = result.intentFulfillment,
                        fashionistaCoverage = result.fashionistaScore.coverage.toFloat(),
                        calibrationVersion = result.fashionistaScore.standardVersion,
                        selectedClothing = result.selectedClothing,
                        selectedCosmetics = result.selectedCosmetics,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun generateStyleRecommendation(intent: String = "Daily Outfit") {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = generateStyleResultUseCase.execute(intent)
                _uiState.value = StyleResultUiState(
                    blueprint = result.blueprint,
                    fashionistaScore = result.fashionistaScore,
                    intentFulfillment = result.intentFulfillment,
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

    fun generateSurpriseStyle() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = generateStyleResultUseCase.executeSurpriseStyle()
                _uiState.value = StyleResultUiState(
                    blueprint = result.blueprint,
                    fashionistaScore = result.fashionistaScore,
                    intentFulfillment = result.intentFulfillment,
                    fashionistaCoverage = result.fashionistaScore.coverage.toFloat(),
                    calibrationVersion = result.fashionistaScore.standardVersion,
                    selectedClothing = result.selectedClothing,
                    selectedCosmetics = result.selectedCosmetics,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to generate surprise style."
                )
            }
        }
    }
}
