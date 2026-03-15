package com.zoewave.probase.photodo.mobile.features.tasks.ui

sealed interface TasksEvent {
    data object OnAddRandomTaskClicked : TasksEvent
    data class OnTaskToggled(val taskId: Long, val isCompleted: Boolean) : TasksEvent
}