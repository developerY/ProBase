package com.zoewave.probase.features.calendar.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.features.calendar.domain.CalendarEventModel

@Immutable
data class CalendarUiState(
    val isLoading: Boolean = false,
    val events: List<CalendarEventModel> = emptyList(),
    val errorMessage: String? = null
)
