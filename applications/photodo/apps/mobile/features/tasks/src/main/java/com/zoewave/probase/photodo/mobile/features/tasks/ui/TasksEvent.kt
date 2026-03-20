package com.zoewave.probase.photodo.mobile.features.tasks.ui

sealed interface TasksEvent {

    data object OnGenerateFullMockDataClicked : TasksEvent

    data object OnClearDatabaseClicked : TasksEvent
    data object OnAddRandomTaskClicked : TasksEvent
    data class OnTaskToggled(val taskId: Long, val isCompleted: Boolean) : TasksEvent

    data object OnAddList : TasksEvent
    data object OnDeleteListClicked : TasksEvent



    // Draft Updates
    data class OnDraftCategorySelected(val categoryId: Long) : TasksEvent
    data class OnDraftTitleChanged(val title: String) : TasksEvent
    data class OnDraftChecklistItemAdded(val itemText: String) : TasksEvent
    data class OnDraftPhotoAttached(val uri: String) : TasksEvent

    data object OnSaveDraftClicked : TasksEvent


    // FAB Menu Actions
    data object OnAddCategoryClicked : TasksEvent
    data object OnAddListClicked : TasksEvent
    data object OnAddTaskItemClicked : TasksEvent
    data object OnAddPhotoClicked : TasksEvent


    // ✅ Sheet Dismissal
    data object OnDismissBottomSheet : TasksEvent
}