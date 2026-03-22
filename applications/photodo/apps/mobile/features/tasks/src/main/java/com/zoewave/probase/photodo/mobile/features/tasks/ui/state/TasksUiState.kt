package com.zoewave.probase.photodo.mobile.features.tasks.ui.state

import com.zoewave.probase.photodo.mobile.features.tasks.ui.TaskItemUiModel
// Make sure you have the @Immutable annotation if you are using it!
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

    val projectLists: List<ProjectListUiModel> = emptyList(),
    val draftState: TaskDraftState = TaskDraftState(),

    // --- NEW: SMART DEFAULT FIELDS ---
    val categoryId: Long? = null,
    val categoryName: String = "Loading...",
    val isNoCategoriesYet: Boolean = false
)