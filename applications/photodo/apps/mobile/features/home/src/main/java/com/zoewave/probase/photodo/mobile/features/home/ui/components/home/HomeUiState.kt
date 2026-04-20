package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import androidx.compose.runtime.Immutable
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel

@Immutable
enum class SearchScope {
    CATEGORIES, PROJECTS
}

@Immutable
data class TaskSearchResult(
    val projectId: Long,
    val projectTitle: String,
    val tasks: List<TaskEntity>
)

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryOverviewUiModel> = emptyList(),
    val urgentProjects: List<ProjectListUiModel> = emptyList(),
    val isQuickProjectSheetOpen: Boolean = false,
    val quickProjectCategoryOverride: String? = null,
    val categorySearchQuery: String = "",
    val taskSearchQuery: String = "",
    val taskSearchResults: List<TaskSearchResult> = emptyList(),
    val isAiEnabled: Boolean = false,
    val isCategoriesSummaryExpanded: Boolean = true,
    val showAddCategoryDialog: Boolean = false,
    val categoryToDelete: CategoryOverviewUiModel? = null,
    val fabMenuExpanded: Boolean = false,
    val isSearchMode: Boolean = false,
    val searchScope: SearchScope = SearchScope.CATEGORIES,
    val animationsEnabled: Boolean = true
) {
    val isEmpty: Boolean = !isLoading && categories.isEmpty()
}
