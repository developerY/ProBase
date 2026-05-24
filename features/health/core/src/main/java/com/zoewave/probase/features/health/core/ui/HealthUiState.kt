package com.zoewave.probase.features.health.core.ui

import androidx.health.connect.client.records.ExerciseSessionRecord
import com.zoewave.probase.core.model.health.SleepSessionData
import com.zoewave.probase.core.model.ble.GattConnectionState
import java.util.UUID

sealed interface HealthUiState {
    object Uninitialized : HealthUiState
    object Loading : HealthUiState
    object Disabled : HealthUiState
    data class PermissionsRequired(val message: String) : HealthUiState
    data class Error(val message: String, val uuid: UUID = UUID.randomUUID()) : HealthUiState

    data class Success(
        val sessions: List<ExerciseSessionRecord>,
        val sleepSessions: List<SleepSessionData> = emptyList(),
        val weeklySteps: Map<String, Long> = emptyMap(),
        val weeklyDistance: Map<String, Double> = emptyMap(), // Meters
        val weeklyCalories: Map<String, Double> = emptyMap(), // Kcal
        val weeklyHydration: Map<String, Double> = emptyMap(), // Liters
        val bleConnectionState: GattConnectionState = GattConnectionState.Disconnected,
        val trackerMetrics: Map<String, String> = emptyMap()
    ) : HealthUiState
}
