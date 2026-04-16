package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import androidx.compose.runtime.Immutable
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails

sealed interface DetailLoadState {
    data object Loading : DetailLoadState

    data class Success(
        val projectDetails: ProjectDetails
    ) : DetailLoadState

    data class Error(val message: String) : DetailLoadState
}

@Immutable
data class TaskDetailUiState(
    val loadState: DetailLoadState = DetailLoadState.Loading,
    val showCamera: Boolean = false,
    val isAiEnabled: Boolean = false
)
