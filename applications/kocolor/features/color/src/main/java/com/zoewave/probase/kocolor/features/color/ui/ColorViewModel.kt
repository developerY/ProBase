package com.zoewave.probase.kocolor.features.color.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.model.FashionProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ColorUiState(
    val fashionProfile: FashionProfile? = null
)

sealed class ColorEvent {
    // Events can be added here
}

@HiltViewModel
class ColorViewModel @Inject constructor(
    private val fashionRepository: FashionRepository
) : ViewModel() {

    val uiState: StateFlow<ColorUiState> = fashionRepository.getProfile()
        .map { profile -> ColorUiState(fashionProfile = profile) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ColorUiState()
        )

    fun onEvent(event: ColorEvent) {
        // Handle events
    }
}
