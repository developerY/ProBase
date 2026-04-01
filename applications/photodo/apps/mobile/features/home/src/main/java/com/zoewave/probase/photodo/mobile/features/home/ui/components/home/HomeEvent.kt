package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import com.zoewave.probase.applications.photodo.db.model.ProjectTemplate

sealed interface HomeEvent {
    data object OnRefresh : HomeEvent
    data class OnTaskClicked(val taskId: String) : HomeEvent
    data class OnTaskToggled(val taskId: String, val isCompleted: Boolean) : HomeEvent

    data class OnCategoryClicked(val categoryId: Long, val categoryName: String) :HomeEvent
    // ✅ ADD THE NEW EVENT
    data class OnAddCategory(val name: String, val description: String? = null) : HomeEvent
    data class OnCreateFromTemplate(val template: ProjectTemplate) : HomeEvent
    data class OnDeleteCategory(val categoryId: Long) : HomeEvent
}