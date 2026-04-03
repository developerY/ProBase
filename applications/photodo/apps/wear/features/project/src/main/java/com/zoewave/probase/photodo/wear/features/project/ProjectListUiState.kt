package com.zoewave.probase.photodo.wear.features.project

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ProjectListUiState {
    data object Loading : ProjectListUiState
    data object Empty : ProjectListUiState
    data class Success(
        val categoryName: String,
        val projects: List<ProjectWearUiModel>
    ) : ProjectListUiState
}

@Immutable
data class ProjectWearUiModel(
    val id: Long,
    val name: String,
    val budget: Double,
    val currentSpend: Double,
    val dueDate: Long?,
    val isUrgent: Boolean,
    val progress: Float,
    val hasPhoto: Boolean = false
)
