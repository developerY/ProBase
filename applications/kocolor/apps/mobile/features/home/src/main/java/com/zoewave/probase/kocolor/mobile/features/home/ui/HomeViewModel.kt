package com.zoewave.probase.kocolor.mobile.features.home.ui

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

data class HomeUiState(
    val fashionProfile: FashionProfile? = null
)

sealed class HomeEvent {
    // Events can be added here as needed
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fashionRepository: FashionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = fashionRepository.getProfile()
        .map { profile -> HomeUiState(fashionProfile = profile) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun onEvent(event: HomeEvent) {
        // Handle events
    }
}
