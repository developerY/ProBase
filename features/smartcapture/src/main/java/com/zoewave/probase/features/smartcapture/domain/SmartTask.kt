package com.zoewave.probase.features.smartcapture.domain

import kotlinx.serialization.Serializable

/**
 * Domain model for an intelligently parsed task.
 */
@Serializable
data class SmartTask(
    val title: String = "",
    val description: String? = null,
    val dueDate: String? = null, // Extracted date string
    val estimatedBudget: Double? = null,
    val suggestedCategory: String? = null,
    val rawText: String = ""
)
