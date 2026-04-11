package com.zoewave.probase.photodo.mobile.features.home.ui.components.categories

data class CategoryOverviewUiModel(
    val id: Long,
    val name: String,
    val totalProjects: Int,
    val totalTasks: Int,
    val completedTasks: Int,
    val progressPercentage: Float // 0.0f to 1.0f
) {
    val progressText: String
        get() = "${(progressPercentage * 100).toInt()}%"
}