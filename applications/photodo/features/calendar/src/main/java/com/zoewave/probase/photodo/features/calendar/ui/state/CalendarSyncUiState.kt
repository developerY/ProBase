package com.zoewave.probase.photodo.features.calendar.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class CalendarSyncUiState(
    val isLoading: Boolean = false,
    val tasksToSync: List<CalendarSyncUiModel> = emptyList(),
    val errorMessage: String? = null
)
