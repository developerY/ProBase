package com.zoewave.probase.goswift.mobile.shots.ui

import com.zoewave.probase.goswift.model.CaffeineShot

sealed interface ShotsUiState {
    object Loading : ShotsUiState
    data class Success(
        val shots: List<CaffeineShot> = emptyList()
    ) : ShotsUiState
}

sealed interface ShotsUiEvent {
    data class DeleteShot(val id: String) : ShotsUiEvent
}
