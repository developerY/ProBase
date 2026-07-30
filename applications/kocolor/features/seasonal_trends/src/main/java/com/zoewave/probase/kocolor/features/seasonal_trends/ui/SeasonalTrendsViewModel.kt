package com.zoewave.probase.kocolor.features.seasonal_trends.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorIntelligenceRepository
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
    // Assuming GeminiService exists and is injectable
    // private val geminiService: GeminiService 
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
                
                // Mocking Gemini call for now
                // val response = geminiService.generateContent(prompt)
                val response = "As we transition into the Fall/Winter 2026 season, your $userProfile profile finds its most sophisticated expression in the rich, nocturnal depths of Deep Plum velvet. This material doesn't just hold color; it captures light, mirroring the clarity of your $userUndertone undertones. When paired with the grounded, architectural strength of Espresso brown, your silhouette achieves a high-contrast balance that feels both modern and timeless.\n\nTo complete the look, lean into the 'blurred berry' cosmetic trend. These soft-focus textures enhance your natural radiance without competing with the saturation of your ensemble. This season is about more than just matching palettes; it's about amplifying your biological canvas with materials that feel as luxurious as they look."
                
                _uiState.value = SeasonalTrendsUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = SeasonalTrendsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
