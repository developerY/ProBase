package com.zoewave.probase.kocolor.features.suggestions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.features.suggestions.data.SuggestionEngine
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.FashionProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SuggestionsLoadingState {
    data object Idle : SuggestionsLoadingState()
    data object Loading : SuggestionsLoadingState()
    data class Success(val advice: FashionAdvice) : SuggestionsLoadingState()
    data class Error(val message: String) : SuggestionsLoadingState()
}

data class SuggestionsScreenUiState(
    val fashionProfile: FashionProfile? = null,
    val loadingState: SuggestionsLoadingState = SuggestionsLoadingState.Idle
)

sealed class SuggestionsEvent {
    data object OnRefreshClicked : SuggestionsEvent()
}

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val suggestionEngine: SuggestionEngine,
    private val fashionRepository: FashionRepository,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _loadingState = MutableStateFlow<SuggestionsLoadingState>(SuggestionsLoadingState.Idle)

    val uiState: StateFlow<SuggestionsScreenUiState> = combine(
        fashionRepository.getProfile(),
        _loadingState
    ) { profile, loadingState ->
        SuggestionsScreenUiState(
            fashionProfile = profile,
            loadingState = loadingState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SuggestionsScreenUiState()
    )

    fun onEvent(event: SuggestionsEvent) {
        when (event) {
            is SuggestionsEvent.OnRefreshClicked -> {
                getSuggestions()
            }
        }
    }

    fun getSuggestions(query: String? = null) {
        viewModelScope.launch {
            val profile = uiState.value.fashionProfile ?: return@launch
            _loadingState.value = SuggestionsLoadingState.Loading

            val apiKey = aiSettings.getGeminiApiKey() ?: ""
            if (apiKey.isBlank()) {
                _loadingState.value = SuggestionsLoadingState.Error("API Key not set.")
                return@launch
            }

            val modelName = aiSettings.aiModelFlow.first()

            try {
                val result = suggestionEngine.getPersonalizedAdvice(profile, query, apiKey, modelName)
                _loadingState.value = SuggestionsLoadingState.Success(result)
            } catch (e: Exception) {
                _loadingState.value = SuggestionsLoadingState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
