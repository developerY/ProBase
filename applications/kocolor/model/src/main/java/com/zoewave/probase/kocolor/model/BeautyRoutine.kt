package com.zoewave.probase.kocolor.model

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
    
    // Professional Metadata
    val microCategory: MicroCategory? = null,
    val chemistryConflictWarning: String? = null
)

@Serializable
enum class RoutineTime {
    MORNING, EVENING, OTHER;

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }

    val formattedName: String
        get() = name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }

    val biologicalObjective: String
        get() = when (this) {
            MORNING -> "Protection & Preparation"
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
