package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
data class RoutineStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isRecommended: Boolean = false,
    val productIds: List<Long> = emptyList(),
    val layeringOrder: Int = 0,
    val minWaitMinutes: Int = 0,
    
    // Knowledge & Content
    val subtitle: String? = null,
    val actionLabel: String? = null,
    val photoUris: List<String> = emptyList(),
    val journalEntries: List<JournalEntry> = emptyList(),
    val linkedMealId: String? = null,
    
    // Professional Metadata
    val microCategory: MicroCategory? = null,
    val chemistryConflictWarning: String? = null
)

@Serializable
data class JournalEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val text: String
)

@Serializable
enum class RoutineTime {
    MORNING, MEALS, EVENING, OTHER;

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }

    val formattedName: String
        get() = name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }

    val biologicalObjective: String
        get() = when (this) {
            MORNING -> "Protection & Preparation"
            MEALS -> "Metabolic Synchronization"
            EVENING -> "Restoration & Repair"
            OTHER -> "Maintenance"
        }
}

@Serializable
data class BeautyRoutine(
    val id: Long = 0,
    val title: String,
    val time: RoutineTime,
    val steps: List<RoutineStep>,
    val date: Long,
    val lastUpdated: Long = System.currentTimeMillis(),
    
    // Professional Attributes
    val biologicalObjective: String? = null,
    val contextFactors: List<String> = emptyList()
)
