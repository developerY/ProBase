package com.zoewave.probase.features.health.ui

import androidx.health.connect.client.records.ExerciseSessionRecord
import java.util.UUID

sealed interface HealthUiState {
    object Uninitialized : HealthUiState
    object Loading : HealthUiState
    object Disabled : HealthUiState

    data class PermissionsRequired(val message: String) : HealthUiState

    data class Success(
        // Renamed 'healthData' to 'sessions' to match the ViewModel and be more specific
        val sessions: List<ExerciseSessionRecord>,
        // Added this field to hold the data for your weekly graph
        val weeklySteps: Map<String, Long> = emptyMap()
    ) : HealthUiState

    data class Error(
        val message: String,
        val uuid: UUID = UUID.randomUUID()
    ) : HealthUiState
}