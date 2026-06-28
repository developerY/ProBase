package com.zoewave.probase.features.health.core.domain

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.features.health.core.WellnessCorrelationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetHealthSummaryUseCase @Inject constructor(
    private val healthSessionManager: HealthSessionManager,
    private val wellnessEngine: WellnessCorrelationEngine
) {
    operator fun invoke(): Flow<HealthSummary> {
        return healthSessionManager.availability.flatMapLatest { availability ->
            if (availability == HealthConnectClient.SDK_AVAILABLE) {
                val permissions = setOf(
                    HealthPermission.getReadPermission(SleepSessionRecord::class),
                    HealthPermission.getReadPermission(HydrationRecord::class),
                    HealthPermission.getWritePermission(HydrationRecord::class)
                )
                flow {
                    val hasPerms = healthSessionManager.hasAllPermissions(permissions)
                    if (hasPerms) {
                        val summary = fetchSummary()
                        emit(summary.copy(hasPermissions = true))
                    } else {
                        emit(HealthSummary(hasPermissions = false))
                    }
                }
            } else {
                flowOf(HealthSummary(hasPermissions = false))
            }
        }
    }

    private suspend fun fetchSummary(): HealthSummary {
        val now = Instant.now()
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()

        val sleepSessions = try { healthSessionManager.readSleepSessions() } catch (e: Exception) { emptyList() }
        val lastNight = sleepSessions.firstOrNull()
        val sleepHours = lastNight?.duration?.toMinutes()?.div(60f)
        val sleepLabel = lastNight?.let { "${it.duration?.toHours()}h ${it.duration?.toMinutes()?.rem(60)}m" }

        val hydrationLiters = try {
            healthSessionManager.readTotalHydration(startOfDay, now)?.inLiters ?: 0.0
        } catch (e: Exception) {
            0.0
        }

        val insights = wellnessEngine.analyzeTriggers(
            sleepHours = sleepHours ?: 8f,
            sugarIntake = "Medium",
            stressLevel = 5
        )

        return HealthSummary(
            hasPermissions = true,
            sleepHours = sleepHours,
            sleepDurationLabel = sleepLabel,
            hydrationLiters = hydrationLiters,
            insights = insights
        )
    }
}
