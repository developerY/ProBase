package com.zoewave.probase.photodo.mobile.features.tasks.ui

sealed interface TasksEvent {

    data object OnGenerateFullMockDataClicked : TasksEvent

    data object OnClearDatabaseClicked : TasksEvent
    data object OnAddRandomTaskClicked : TasksEvent
    data class OnTaskToggled(val taskId: Long, val isCompleted: Boolean) : TasksEvent



    // Draft Updates
    data class OnDraftCategorySelected(val categoryId: Long) : TasksEvent
    data class OnDraftTitleChanged(val title: String) : TasksEvent
    data class OnDraftChecklistItemAdded(val itemText: String) : TasksEvent
    data class OnDraftPhotoAttached(val uri: String) : TasksEvent

    data object OnSaveDraftClicked : TasksEvent
}