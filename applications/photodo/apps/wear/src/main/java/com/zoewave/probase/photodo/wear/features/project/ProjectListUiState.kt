package com.zoewave.probase.photodo.wear.features.project

sealed interface ProjectListUiState {
    data object Loading : ProjectListUiState
    data object Empty : ProjectListUiState
    data class Success(
        val categoryName: String,
        val projects: List<ProjectWearUiModel>
    ) : ProjectListUiState
}

data class ProjectWearUiModel(
    val id: Long,
    val name: String,
    val budget: Double,
    val currentSpend: Double,
    val dueDate: Long?,
    val isUrgent: Boolean,
    val progress: Float,
    val hasPhoto: Boolean = false // Added flag for photo sync
)
