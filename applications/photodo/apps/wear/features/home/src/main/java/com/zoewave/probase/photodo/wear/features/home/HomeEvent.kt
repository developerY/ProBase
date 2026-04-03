package com.zoewave.probase.photodo.wear.features.home

/**
 * User intents for the Home screen.
 */
sealed interface HomeEvent {
    data class OnCategoryClick(val id: Long, val name: String) : HomeEvent
    data object OnRequestSync : HomeEvent
}
