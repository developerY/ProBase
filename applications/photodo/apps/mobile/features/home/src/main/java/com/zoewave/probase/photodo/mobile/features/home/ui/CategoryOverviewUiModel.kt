package com.zoewave.probase.photodo.mobile.features.home.ui

data class CategoryOverviewUiModel(
    val id: Long,
    val name: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val progressPercentage: Float // 0.0f to 1.0f
) {
    val progressText: String
        get() = "${(progressPercentage * 100).toInt()}%"
}