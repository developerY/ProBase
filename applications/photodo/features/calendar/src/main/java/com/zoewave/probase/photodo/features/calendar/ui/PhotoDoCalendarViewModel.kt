package com.zoewave.probase.photodo.features.calendar.ui

import androidx.lifecycle.ViewModel
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.features.calendar.domain.CalendarRepository
import com.zoewave.probase.photodo.features.calendar.ui.state.CalendarSyncUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
internal class PhotoDoCalendarViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarSyncUiState(isLoading = true))
    val uiState: StateFlow<CalendarSyncUiState> = _uiState.asStateFlow()

    // Logic to bridge PhotoDo tasks with CalendarRepository will go here.
}
