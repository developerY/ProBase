package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.compose.runtime.Immutable

@Immutable
data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskItemUiModel> = emptyList(),
    val errorMessage: String? = null
)