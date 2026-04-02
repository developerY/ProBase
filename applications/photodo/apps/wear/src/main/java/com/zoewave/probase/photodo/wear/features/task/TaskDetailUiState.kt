package com.zoewave.probase.photodo.wear.features.task

import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

sealed interface TaskDetailUiState {
    data object Loading : TaskDetailUiState
    data object Empty : TaskDetailUiState
    data class Success(
        val projectTitle: String,
        val tasks: List<TaskEntity>,
        val photos: List<PhotoEntity>,
        val photoCount: Int = 0 // Added photo count for View-Only mode
    ) : TaskDetailUiState
}
