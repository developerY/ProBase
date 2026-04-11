package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.runtime.Immutable
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryOverviewUiModel> = emptyList(),
    val urgentProjects: List<ProjectListUiModel> = emptyList(),
    val isQuickProjectSheetOpen: Boolean = false,
    val quickProjectCategoryOverride: String? = null,
    val searchQuery: String = ""
) {
    val isEmpty: Boolean = !isLoading && categories.isEmpty()
}
