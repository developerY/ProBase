package com.zoewave.probase.photodo.features.calendar.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class CalendarSyncUiModel(
    val taskId: Long,
    val taskText: String,
    val isSynced: Boolean,
    val lastSyncedTimestamp: Long? = null
)
