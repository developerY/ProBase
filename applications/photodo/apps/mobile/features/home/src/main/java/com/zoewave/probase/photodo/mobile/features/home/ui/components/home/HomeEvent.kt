package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import com.zoewave.probase.applications.photodo.db.model.ProjectTemplate
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel

sealed interface HomeEvent {
    data object OnRefresh : HomeEvent
    data class OnTaskClicked(val taskId: String) : HomeEvent
    data class OnTaskToggled(val taskId: String, val isCompleted: Boolean) : HomeEvent

    data class OnCategoryClicked(val categoryId: Long, val categoryName: String) :HomeEvent
    // ✅ ADD THE NEW EVENT
    data class OnAddCategory(val name: String, val iconUri: String? = null, val colorHex: String? = null, val description: String? = null) : HomeEvent
    data class OnCreateFromTemplate(val template: ProjectTemplate) : HomeEvent
    data class OnDeleteCategory(val categoryId: Long) : HomeEvent

    data class OnAddQuickProjectClicked(val overrideCategoryName: String? = null) : HomeEvent
    data class OnCategorySearchQueryChanged(val query: String) : HomeEvent
    data class OnTaskSearchQueryChanged(val query: String) : HomeEvent
    data object OnToggleCategoriesSummary : HomeEvent
    data object OnDismissBottomSheet : HomeEvent

    // UI State Toggles
    data class OnShowAddCategoryDialog(val show: Boolean) : HomeEvent
    data class OnCategoryToDeleteChanged(val category: CategoryOverviewUiModel?) : HomeEvent
    data class OnFabMenuToggle(val expanded: Boolean) : HomeEvent
    data class OnSearchModeToggle(val enabled: Boolean) : HomeEvent
    data class OnSearchScopeChanged(val scope: SearchScope) : HomeEvent
}
