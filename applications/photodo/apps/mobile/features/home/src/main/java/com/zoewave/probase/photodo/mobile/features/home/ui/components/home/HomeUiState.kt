package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.runtime.Immutable
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@Immutable
data class ProjectSearchGroupUiModel(
    val projectId: Long,
    val projectName: String,
    val tasks: List<TaskSearchUiModel>
)

@Immutable
data class TaskSearchUiModel(
    val taskId: Long,
    val taskText: String,
    val isChecked: Boolean
)

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryOverviewUiModel> = emptyList(),
    val urgentProjects: List<ProjectListUiModel> = emptyList(),
    val searchResults: List<ProjectSearchGroupUiModel> = emptyList(),
    val isQuickProjectSheetOpen: Boolean = false,
    val quickProjectCategoryOverride: String? = null,
    val searchQuery: String = "",
    val isAiEnabled: Boolean = false
) {
    val isEmpty: Boolean = !isLoading && categories.isEmpty()
    val isSearching: Boolean = searchQuery.isNotBlank()
}
