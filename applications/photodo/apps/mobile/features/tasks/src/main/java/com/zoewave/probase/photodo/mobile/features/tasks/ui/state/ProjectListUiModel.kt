package com.zoewave.probase.photodo.mobile.features.tasks.ui.state

// And define the new model:
data class ProjectListUiModel(
    val id: Long,
    val title: String,
    val categoryName: String,
    // ✅ Add the flags to the UI Model
    val isFavorite: Boolean = false,
    val isUrgent: Boolean = false
)