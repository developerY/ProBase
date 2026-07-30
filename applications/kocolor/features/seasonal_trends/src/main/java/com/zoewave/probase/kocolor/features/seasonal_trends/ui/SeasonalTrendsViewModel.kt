package com.zoewave.probase.kocolor.features.seasonal_trends.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorIntelligenceRepository
import com.zoewave.probase.kocolor.features.seasonal_trends.domain.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SeasonalTrendsUiState {
    object Idle : SeasonalTrendsUiState()
    object Loading : SeasonalTrendsUiState()
    data class Success(val editorial: String) : SeasonalTrendsUiState()
    data class Error(val message: String) : SeasonalTrendsUiState()
}

@HiltViewModel
class SeasonalTrendsViewModel @Inject constructor(
    private val colorRepository: ColorIntelligenceRepository,
    private val geminiService: GeminiService 
) : ViewModel() {

    private val _uiState = MutableStateFlow<SeasonalTrendsUiState>(SeasonalTrendsUiState.Idle)
    val uiState: StateFlow<SeasonalTrendsUiState> = _uiState.asStateFlow()

    fun fetchSeasonalTrends(userProfile: String, userUndertone: String) {
        viewModelScope.launch {
            _uiState.value = SeasonalTrendsUiState.Loading
            try {
                val prompt = """
                    Act as a high-end fashion editor. 
                    The user has a '$userProfile' color profile with '$userUndertone' skin undertones. 
                    Provide a short, 2-paragraph editorial on why the Fall/Winter 2026 trends of 
                    Deep Plum velvet, Espresso brown, and blurred berry cosmetics are the perfect match 
                    for their specific profile.
                """.trimIndent()
                
                val response = geminiService.generateContent(prompt)
                _uiState.value = SeasonalTrendsUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = SeasonalTrendsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
