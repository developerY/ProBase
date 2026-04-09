package com.zoewave.probase.photodo.mobile.features.tasks.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TaskItemUiModel

@Immutable
data class TasksUiState(
    // --- YOUR EXISTING WORKING CODE ---
    val isLoading: Boolean = false,
    val tasks: List<TaskItemUiModel> = emptyList(),
    val errorMessage: String? = null,

    // Bottom Sheet Visibility Flags
    val isAddCategorySheetOpen: Boolean = false,
    val isAddListSheetOpen: Boolean = false,
    val isAddTaskItemSheetOpen: Boolean = false,
    val isAddPhotoSheetOpen: Boolean = false,
    val isQuickProjectSheetOpen: Boolean = false,
    val quickProjectCategoryOverride: String? = null,

    val projectLists: List<ProjectListUiModel> = emptyList(),
    val draftState: TaskDraftState = TaskDraftState(),

    // --- NEW: SMART DEFAULT FIELDS ---
    val categoryId: Long? = null,
    val categoryName: String = "Loading...",
    val isNoCategoriesYet: Boolean = false
)