package com.zoewave.probase.goswift.mobile.healthsync.ui

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HealthSyncUiState(
    val isSdkAvailable: Boolean = false,
    val isGranted: Boolean = false,
    val latestHeartRateBpm: Long? = null,
    val latestHrvMs: Double? = null,
    val totalSleepMinutes: Long? = null,
    val deepSleepMinutes: Long? = null,
    val remSleepMinutes: Long? = null,
    val activeCalories: Long? = null,
    val stepCount: Long? = null,
    val backgroundPollingEnabled: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class HealthSyncViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HealthSyncUiState())
    val uiState: StateFlow<HealthSyncUiState> = _uiState.asStateFlow()

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    )

    fun toggleBackgroundPolling(enabled: Boolean) {
        _uiState.update { it.copy(backgroundPollingEnabled = enabled) }
    }

    fun loadRealHealthData(context: Context) {
        val status = HealthConnectClient.getSdkStatus(context)
        if (status != HealthConnectClient.SDK_AVAILABLE) {
            _uiState.update {
                it.copy(
                    isSdkAvailable = false,
                    isGranted = false,
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSdkAvailable = true) }
            try {
                val client = HealthConnectClient.getOrCreate(context)
                val grantedPermissions = client.permissionController.getGrantedPermissions()
                val isGranted = grantedPermissions.containsAll(requiredPermissions)

                if (isGranted) {
                    val now = Instant.now()
                    val startOf24h = now.minus(24, ChronoUnit.HOURS)

                    // Real Heart Rate
                    val hrRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startOf24h, now)
                        )
                    ).records
                    val latestHeartRateBpm = hrRecords.flatMap { it.samples }.maxByOrNull { it.time }?.beatsPerMinute

                    // Real HRV
                    val hrvRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateVariabilityRmssdRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startOf24h, now)
                        )
                    ).records
                    val latestHrvMs = hrvRecords.lastOrNull()?.heartRateVariabilityMillis

                    // Real Sleep
                    val sleepRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = SleepSessionRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startOf24h, now)
                        )
                    ).records
                    val totalSleepMinutes = if (sleepRecords.isNotEmpty()) {
                        sleepRecords.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
                    } else null

                    // Real Steps
                    val stepRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = StepsRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startOf24h, now)
                        )
                    ).records
                    val stepCount = if (stepRecords.isNotEmpty()) {
                        stepRecords.sumOf { it.count }
                    } else null

                    // Real Calories
                    val calorieRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = ActiveCaloriesBurnedRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startOf24h, now)
                        )
                    ).records
                    val activeCalories = if (calorieRecords.isNotEmpty()) {
                        calorieRecords.sumOf { it.energy.inKilocalories.toLong() }
                    } else null

                    _uiState.update {
                        it.copy(
                            isGranted = true,
                            latestHeartRateBpm = latestHeartRateBpm,
                            latestHrvMs = latestHrvMs,
                            totalSleepMinutes = totalSleepMinutes,
                            activeCalories = activeCalories,
                            stepCount = stepCount,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isGranted = false,
                            latestHeartRateBpm = null,
                            latestHrvMs = null,
                            totalSleepMinutes = null,
                            activeCalories = null,
                            stepCount = null,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isGranted = false,
                        latestHeartRateBpm = null,
                        latestHrvMs = null,
                        totalSleepMinutes = null,
                        activeCalories = null,
                        stepCount = null,
                        isLoading = false
                    )
                }
            }
        }
    }
}
