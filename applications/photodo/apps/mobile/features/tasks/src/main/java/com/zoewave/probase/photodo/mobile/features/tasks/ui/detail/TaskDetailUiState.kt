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
    val isAiEnabled: Boolean = false,
    val fabMenuExpanded: Boolean = false,
    val showAddTaskDialog: Boolean = false,
    val newTaskText: String = "",
    val showAddExpenseDialog: Boolean = false,
    val newExpenseAmount: String = "",
    val newExpenseDesc: String = "",
    val showDeleteProjectConfirmation: Boolean = false
)
