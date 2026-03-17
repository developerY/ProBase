package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import com.zoewave.probase.applications.photodo.db.entity.TaskListWithPhotos

import androidx.compose.runtime.Immutable


/**
 * Represents the different states for the database-loading portion of the detail screen.
 */
sealed interface DetailLoadState {
    data object Loading : DetailLoadState

    data class Success(
        val taskListWithPhotos: TaskListWithPhotos
    ) : DetailLoadState

    data class Error(val message: String) : DetailLoadState
}

/**
 * The complete, immutable UI state for the Task Detail screen.
 * * @param loadState The current state of data loading from Room.
 * @param showCamera True if the camera UI should be shown as an overlay.
 * (Note: If using Nav3 for the camera, you might not need this flag!)
 */
@Immutable
data class TaskDetailUiState(
    val loadState: DetailLoadState = DetailLoadState.Loading,
    val showCamera: Boolean = false
)