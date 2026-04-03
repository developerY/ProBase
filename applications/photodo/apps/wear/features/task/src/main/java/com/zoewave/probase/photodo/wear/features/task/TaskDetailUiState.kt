package com.zoewave.probase.photodo.wear.features.task

import androidx.compose.runtime.Immutable
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

@Immutable
sealed interface TaskDetailUiState {
    data object Loading : TaskDetailUiState
    data object Empty : TaskDetailUiState
    data class Success(
        val projectId: Long,
        val projectTitle: String,
        val tasks: List<TaskEntity>,
        val photos: List<PhotoEntity>,
        val photoCount: Int = 0,
        val hasPhoto: Boolean = false
    ) : TaskDetailUiState
}
