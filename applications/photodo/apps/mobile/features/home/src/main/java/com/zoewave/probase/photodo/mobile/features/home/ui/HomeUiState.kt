package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.compose.runtime.Immutable
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@Immutable
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(
        val categories: List<CategoryOverviewUiModel>,
        val urgentProjects: List<ProjectListUiModel> // ✅ ADDED THIS
    ) : HomeUiState
}

/* data class HomeUiState(
    val isLoading: Boolean = true,
    val recentPhotoTasks: List<PhotoTask> = emptyList(),
    val errorMessage: String? = null
) */

