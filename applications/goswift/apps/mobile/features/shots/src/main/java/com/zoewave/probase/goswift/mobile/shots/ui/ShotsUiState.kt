package com.zoewave.probase.goswift.mobile.shots.ui

import com.zoewave.probase.goswift.model.CaffeineShot

data class TimelineItemData(
    val id: String,
    val time: String,
    val title: String,
    val subtitle: String,
    val amountMg: Int,
    val status: String,
    val isCompleted: Boolean,
    val isNextWindow: Boolean = false,
    val isCurfew: Boolean = false
)

sealed interface ShotsUiState {
    object Loading : ShotsUiState
    data class Success(
        val shots: List<CaffeineShot> = emptyList(),
        val timelineItems: List<TimelineItemData> = emptyList(),
        val intendedActiveDosingMg: Int = 0,
        val loggedMg: Int = 0,
        val safeCeilingMg: Int = 120
    ) : ShotsUiState
}

sealed interface ShotsUiEvent {
    data class DeleteShot(val id: String) : ShotsUiEvent
}
