package com.zoewave.probase.photodo.mobile.features.tasks.ui

// UI-specific model. Your ViewModel will map the Room TaskItemEntity to this.
data class TaskItemUiModel(
    val id: Long,
    val title: String,
    val isCompleted: Boolean
)