package com.zoewave.probase.photodo.mobile.features.home.ui

sealed interface HomeEvent {
    data object OnRefresh : HomeEvent
    data class OnTaskClicked(val taskId: String) : HomeEvent
    data class OnTaskToggled(val taskId: String, val isCompleted: Boolean) : HomeEvent
}