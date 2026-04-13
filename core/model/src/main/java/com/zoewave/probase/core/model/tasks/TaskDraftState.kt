package com.zoewave.probase.core.model.tasks

import kotlinx.serialization.Serializable

/**
 * The structured result of an intelligent capture operation.
 * Designed to be passed back to the main app to pre-fill a task entry.
 */
@Serializable
data class SmartTaskDraft(
    val category: String? = null,
    val projectName: String? = null,
    val taskName: String? = null,
    val duration: String? = null,
    val dueDate: String? = null,
    val budget: Double? = null,
    val subTasks: List<String> = emptyList(),
    val photoUri: String? = null
)
