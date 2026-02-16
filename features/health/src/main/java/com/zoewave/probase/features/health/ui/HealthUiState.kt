package com.zoewave.probase.features.health.ui

import androidx.health.connect.client.records.ExerciseSessionRecord
import java.util.UUID

sealed interface HealthUiState {
    object Uninitialized : HealthUiState
    object Loading : HealthUiState
    object Disabled : HealthUiState
    data class PermissionsRequired(val message: String) : HealthUiState
    data class Error(val message: String, val uuid: UUID = UUID.randomUUID()) : HealthUiState

    data class Success(
        val sessions: List<ExerciseSessionRecord>,
        val weeklySteps: Map<String, Long> = emptyMap(),
        // Add these two new fields:
        val weeklyDistance: Map<String, Double> = emptyMap(), // Meters
        val weeklyCalories: Map<String, Double> = emptyMap()  // Kcal
    ) : HealthUiState
}