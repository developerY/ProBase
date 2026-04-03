package com.zoewave.probase.photodo.wear.features.project

/**
 * User intents for the Project List screen.
 */
sealed interface ProjectListEvent {
    data class OnProjectClick(val id: Long, val name: String) : ProjectListEvent
}
