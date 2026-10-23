package com.zoewave.probase.goswift.data.caffeine

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

interface CaffeineDecayEngine {
    fun calculateRemainingMg(
        records: List<CaffeineIntakeRecord>,
        evaluationTime: Instant,
        config: PharmacokineticConfig = PharmacokineticConfig(),
        includePlanned: Boolean = false
    ): Double

    fun evaluateSnapshot(
        records: List<CaffeineIntakeRecord>,
        evaluationTime: Instant,
        userZoneId: ZoneId,
        config: PharmacokineticConfig = PharmacokineticConfig(),
        timelineWindow: Duration = Duration.ofHours(24),
        timelineStep: Duration = Duration.ofMinutes(30)
    ): CaffeineTelemetrySnapshot
}

class DeterministicCaffeineDecayEngine @Inject constructor() : CaffeineDecayEngine {

    override fun calculateRemainingMg(
        records: List<CaffeineIntakeRecord>,
        evaluationTime: Instant,
        config: PharmacokineticConfig,
        includePlanned: Boolean
    ): Double {
        val halfLifeMillis = config.halfLife.toMillis().toDouble()
        val evalMillis = evaluationTime.toEpochMilli()

        val totalMg = records.asSequence()
            .filter { includePlanned || !it.isPlanned }
            .filter { !it.timestamp.isAfter(evaluationTime) }
            .sumOf { record ->
                val elapsedMillis = (evalMillis - record.timestamp.toEpochMilli()).toDouble()
                val halfLivesElapsed = elapsedMillis / halfLifeMillis
                if (halfLivesElapsed > 10.0) {
                    0.0
                } else {
                    record.amountMg * 0.5.pow(halfLivesElapsed)
                }
            }

        return if (totalMg < config.negligibleCutoffMg) 0.0 else totalMg
    }

    override fun evaluateSnapshot(
        records: List<CaffeineIntakeRecord>,
        evaluationTime: Instant,
        userZoneId: ZoneId,
        config: PharmacokineticConfig,
        timelineWindow: Duration,
        timelineStep: Duration
    ): CaffeineTelemetrySnapshot {
        val loggedRecords = records.filter { !it.isPlanned }
        val currentRemainingMg = calculateRemainingMg(
            records = loggedRecords,
            evaluationTime = evaluationTime,
            config = config,
            includePlanned = false
        )

        val currentLocalDate: LocalDate = evaluationTime.atZone(userZoneId).toLocalDate()
        val startOfDayInstant = currentLocalDate.atStartOfDay(userZoneId).toInstant()
        val endOfDayInstant = currentLocalDate.plusDays(1).atStartOfDay(userZoneId).toInstant()

        val dailyConsumedMg = loggedRecords
            .filter { !it.timestamp.isBefore(startOfDayInstant) && it.timestamp.isBefore(endOfDayInstant) }
            .sumOf { it.amountMg }

        val sleepReadyInstant = projectClearanceInstant(
            currentMg = currentRemainingMg,
            targetThresholdMg = config.sleepThresholdMg,
            evaluationTime = evaluationTime,
            halfLife = config.halfLife
        )

        val zeroClearanceInstant = projectClearanceInstant(
            currentMg = currentRemainingMg,
            targetThresholdMg = config.negligibleCutoffMg,
            evaluationTime = evaluationTime,
            halfLife = config.halfLife
        )

        val startWindow = evaluationTime.minus(timelineWindow.dividedBy(2))
        val stepsCount = (timelineWindow.toMillis() / timelineStep.toMillis()).toInt()
        val timelineSeries = (0..stepsCount).map { stepIndex ->
            val pointInstant = startWindow.plus(timelineStep.multipliedBy(stepIndex.toLong()))
            TelemetryPoint(
                timestamp = pointInstant,
                estimatedRemainingMg = calculateRemainingMg(
                    records = records,
                    evaluationTime = pointInstant,
                    config = config,
                    includePlanned = true
                ),
                includesPlanned = pointInstant.isAfter(evaluationTime)
            )
        }

        return CaffeineTelemetrySnapshot(
            evaluatedAt = evaluationTime,
            estimatedRemainingMg = currentRemainingMg,
            dailyConsumedMg = dailyConsumedMg,
            dailyLimitMg = config.dailyLimitMg,
            dailyLimitRatio = (dailyConsumedMg / config.dailyLimitMg).toFloat().coerceAtLeast(0f),
            estimatedSleepReadyInstant = sleepReadyInstant,
            estimatedZeroClearanceInstant = zeroClearanceInstant,
            timelineSeries = timelineSeries
        )
    }

    private fun projectClearanceInstant(
        currentMg: Double,
        targetThresholdMg: Double,
        evaluationTime: Instant,
        halfLife: Duration
    ): Instant? {
        if (currentMg <= targetThresholdMg) return evaluationTime
        val log2Ratio = ln(currentMg / targetThresholdMg) / LN_2
        val millisToClear = (halfLife.toMillis().toDouble() * log2Ratio).toLong()
        return evaluationTime.plusMillis(millisToClear)
    }

    private companion object {
        const val LN_2 = 0.6931471805599453
    }
}
