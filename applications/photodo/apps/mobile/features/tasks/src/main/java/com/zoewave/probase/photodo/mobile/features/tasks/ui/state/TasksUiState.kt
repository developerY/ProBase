package com.zoewave.probase.photodo.mobile.features.tasks.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TaskItemUiModel

@Immutable
data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskItemUiModel> = emptyList(),
    val errorMessage: String? = null,

    // ✅ Bottom Sheet Visibility Flags
    val isAddCategorySheetOpen: Boolean = false,
    val isAddListSheetOpen: Boolean = false,
    val isAddTaskItemSheetOpen: Boolean = false,
    val isAddPhotoSheetOpen: Boolean = false
)