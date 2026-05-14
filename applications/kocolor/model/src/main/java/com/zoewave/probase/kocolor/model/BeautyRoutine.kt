package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoutineTime {
    MORNING, EVENING, OTHER
}

@Serializable
data class RoutineStep(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isRecommended: Boolean = false
)

@Serializable
data class BeautyRoutine(
    val id: Long = 0,
    val title: String,
    val time: RoutineTime,
    val steps: List<RoutineStep>,
    val date: Long // Timestamp
)
