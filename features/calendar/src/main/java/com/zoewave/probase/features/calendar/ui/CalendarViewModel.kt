package com.zoewave.probase.features.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.calendar.domain.CalendarEventModel
import com.zoewave.probase.features.calendar.domain.CalendarRepository
import com.zoewave.probase.features.calendar.ui.state.CalendarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    // Default query range: last 30 days to next 30 days
    private val currentTime = System.currentTimeMillis()
    private val monthInMillis = 30L * 24 * 60 * 60 * 1000

    val uiState: StateFlow<CalendarUiState> = calendarRepository
        .queryEvents(currentTime - monthInMillis, currentTime + monthInMillis)
        .map { events ->
            CalendarUiState(events = events)
        }
        .catch { e ->
            emit(CalendarUiState(errorMessage = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarUiState(isLoading = true)
        )

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            calendarRepository.deleteEvent(eventId)
        }
    }
}
