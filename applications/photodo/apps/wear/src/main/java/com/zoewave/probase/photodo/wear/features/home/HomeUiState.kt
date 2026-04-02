package com.zoewave.probase.photodo.wear.features.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(
        val categories: List<CategoryWearUiModel>
    ) : HomeUiState
}

data class CategoryWearUiModel(
    val id: Long,
    val name: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val progressPercentage: Float
)
