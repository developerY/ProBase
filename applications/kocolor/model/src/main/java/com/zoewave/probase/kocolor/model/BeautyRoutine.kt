package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoutineTime {
    MORNING, EVENING, OTHER;

    val biologicalObjective: String
        get() = when (this) {
            MORNING -> "Defense & Protection"
            EVENING -> "Repair & Regeneration"
            OTHER -> "Specialized Care"
        }

    val objectiveDescription: String
        get() = when (this) {
            MORNING -> "Focus on UV defense, pollution protection, and oxidative stress reduction."
            EVENING -> "Focus on repair, regeneration, and collagen synthesis."
            OTHER -> "Targeted treatment for specific skin needs."
        }
}

@Serializable
data class RoutineStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isRecommended: Boolean = false,
    /** Recommended layering order (e.g., 1 for Cleansing, 2 for Toning). */
    val layeringOrder: Int = 0,
    /** Minimum wait time in minutes before proceeding to the next step or sleep. */
    val minWaitMinutes: Int = 0,
    /** Product category linked to this step. */
    val category: CosmeticCategory? = null,
    /** IDs of actual inventory products linked to this step. */
    val productIds: List<Long> = emptyList()
)

@Serializable
data class BeautyRoutine(
    val id: Long = 0,
    val title: String,
    val time: RoutineTime,
    val steps: List<RoutineStep>,
    val date: Long, // Timestamp
    /** Biological objective of this routine (e.g., "Circadian Alignment"). */
    val biologicalObjective: String? = null,
    /** External factors influencing this routine (e.g., "High UV Index", "Low Sleep"). */
    val contextFactors: List<String> = emptyList()
)
