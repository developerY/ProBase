package com.zoewave.probase.photodo.wear.features.home

import androidx.compose.runtime.Immutable

@Immutable
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(
        val categories: List<CategoryWearUiModel>
    ) : HomeUiState
}

@Immutable
data class CategoryWearUiModel(
    val id: Long,
    val name: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val progressPercentage: Float,
    val hasPhoto: Boolean = false
)
