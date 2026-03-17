package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.compose.runtime.Immutable

@Immutable
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(val categories: List<CategoryOverviewUiModel>) : HomeUiState
}

/* data class HomeUiState(
    val isLoading: Boolean = true,
    val recentPhotoTasks: List<PhotoTask> = emptyList(),
    val errorMessage: String? = null
) */

