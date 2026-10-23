package com.zoewave.probase.goswift.data.health

import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant

enum class HealthConnectAvailability {
    AVAILABLE,
    UPDATE_REQUIRED,
    NOT_SUPPORTED
}

data class HealthPermissionState(
    val availability: HealthConnectAvailability,
    val grantedPermissions: Set<String>,
    val requiredCorePermissions: Set<String>,
    val isCoreSyncAuthorized: Boolean,
    val isHistoryReadSupported: Boolean,
    val isHistoryReadGranted: Boolean,
    val isBackgroundReadSupported: Boolean,
    val isBackgroundReadGranted: Boolean
) {
    val maxHistoricalLookback: Duration
        get() = if (isHistoryReadGranted) Duration.ofDays(365) else Duration.ofDays(30)
}

data class SleepSummary(
    val sessionId: String,
    val startTime: Instant,
    val endTime: Instant,
    val totalDuration: Duration,
    val stagesSummary: Map<SleepStageType, Duration> = emptyMap(),
    val sourceAppPackage: String
)

enum class SleepStageType { AWAKE, LIGHT, DEEP, REM, UNKNOWN }

data class HrvSample(
    val timestamp: Instant,
    val rmssdMillis: Double,
    val sourceAppPackage: String
)

data class HeartRateSample(
    val timestamp: Instant,
    val beatsPerMinute: Long,
    val sourceAppPackage: String
)

data class DailyActivitySummary(
    val startTime: Instant,
    val endTime: Instant,
    val totalSteps: Long,
    val activeCaloriesKcal: Double
)

sealed interface HealthDataResult<out T> {
    data class Success<T>(val data: T, val isClampedTo30Days: Boolean = false) : HealthDataResult<T>
    data object PermissionDenied : HealthDataResult<Nothing>
    data object SdkUnavailable : HealthDataResult<Nothing>
    data class Error(val throwable: Throwable) : HealthDataResult<Nothing>
}

interface HealthRepository {
    val permissionState: Flow<HealthPermissionState>
    suspend fun refreshPermissionState(): HealthPermissionState
    suspend fun buildSafePermissionRequestSet(
        includeHistoryRead: Boolean = false,
        includeBackgroundRead: Boolean = false
    ): Set<String>
    suspend fun revokeAllHealthPermissions()
    suspend fun getSleepSessions(startTime: Instant, endTime: Instant): HealthDataResult<List<SleepSummary>>
    suspend fun getHrvSamples(startTime: Instant, endTime: Instant): HealthDataResult<List<HrvSample>>
    suspend fun getHeartRateSamples(startTime: Instant, endTime: Instant): HealthDataResult<List<HeartRateSample>>
    suspend fun getDailyActivitySummary(startTime: Instant, endTime: Instant): HealthDataResult<DailyActivitySummary>
}
