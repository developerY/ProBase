package com.zoewave.probase.photodo.mobile.features.tasks.ui

sealed interface TasksEvent {

    data object OnGenerateFullMockDataClicked : TasksEvent

    data object OnClearDatabaseClicked : TasksEvent
    data object OnAddRandomTaskClicked : TasksEvent
    data class OnTaskToggled(val taskId: Long, val isCompleted: Boolean) : TasksEvent

    data object OnAddList : TasksEvent
    data object OnDeleteListClicked : TasksEvent

    data class OnToggleProjectFavorite(val projectId: Long, val isFavorite: Boolean) : TasksEvent
    data class OnToggleProjectUrgent(val projectId: Long, val isUrgent: Boolean) : TasksEvent

    // NEW: Handle the row tap as an event!
    data class OnProjectClicked(val projectId: Long, val projectTitle: String) : TasksEvent

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