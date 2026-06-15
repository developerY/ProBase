package com.zoewave.probase.kocolor.mobile.features.color.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ColorUiState(
    val savedSuggestions: List<SavedAnalysis> = emptyList()
)

sealed class ColorEvent {
    data class DeleteCollection(val id: Long) : ColorEvent()
}

@HiltViewModel
class ColorViewModel @Inject constructor(
    private val fashionRepository: FashionRepository
) : ViewModel() {

    val uiState: StateFlow<ColorUiState> = fashionRepository.getSavedSuggestions()
        .map { list -> ColorUiState(savedSuggestions = list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ColorUiState()
        )

    fun onEvent(event: ColorEvent) {
        when (event) {
            is ColorEvent.DeleteCollection -> {
                viewModelScope.launch {
                    fashionRepository.deleteSuggestion(event.id)
                }
            }
        }
    }
}
