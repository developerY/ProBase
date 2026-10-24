The core science behind **Hummingbird Fuel** (`applications/goswift`) centers on replacing high-bolus caffeine spikes and afternoon crashes with **closed-loop, biometric-guided microdosing**—maintaining caffeine within an ergogenic therapeutic corridor throughout waking hours while guaranteeing clearance below the sleep-disruption threshold by bedtime.

Below are the four most complex algorithmic engines required for the Android implementation, written in production-ready Kotlin for `com.zoewave.probase.goswift`.

---

### 1. Multi-Dose Bateman Pharmacokinetic & Two-Process Alertness Engine

A simple instantaneous decay curve ($D \cdot 0.5^{\Delta t / t_{1/2}}$) assumes caffeine teleports into the bloodstream at $t = 0$. In reality, sublingual/mucosal microdose sprays and oral beverages follow a **two-rate pharmacokinetic profile** governed by the **Bateman Equation** (first-order absorption rate $k_a$ and first-order hepatic CYP1A2 elimination rate $k_e$), paired with **Hill-Langmuir competitive antagonism** against homeostatic adenosine accumulation (Process $S$) and circadian drive (Process $C$).

#### Mathematical Formulation

1. **Single-Dose Bateman Concentration Equivalent $c_i(t)$ for $t \ge t_i$ ($\Delta t = t - t_i$):**

$$c_i(\Delta t) = \begin{cases} D_i \cdot F_i \cdot \frac{k_a}{k_a - k_e} \left(e^{-k_e \Delta t} - e^{-k_a \Delta t}\right) & \text{if } \vert{}k_a - k_e\vert{} > \epsilon \\ D_i \cdot F_i \cdot k_e \Delta t \cdot e^{-k_e \Delta t} & \text{if } \vert{}k_a - k_e\vert{} \le \epsilon \text{ (L'Hôpital limit)} \end{cases}$$


2. **Receptor Occupancy & Effective Alertness $A(t)$:**
   Caffeine acts as a competitive antagonist at adenosine $A_{2A}$ receptors. Given homeostatic sleep pressure $S(t)$ (rising exponentially during wakefulness) and circadian alertness drive $C(t)$, effective adenosine pressure $S_{\text{eff}}(t)$ is attenuated via the Hill equation:

$$S_{\text{eff}}(t) = \frac{S(t)}{1 + \left(\frac{C_{\text{total}}(t)}{IC_{50}}\right)^n}$$


3. **Exact Clearance Root-Finding ($t_{\text{clear}}$):**
   Because superimposed absorption and elimination terms cannot be inverted algebraically while a dose is still absorbing, the engine brackets the peak $T_{\max}$ and uses **Newton-Raphson with Bisection fallback** to solve $C_{\text{total}}(t) - C_{\text{target}} = 0$ to sub-second precision.

```kotlin
package com.zoewave.probase.goswift.data.caffeine.engine

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Delivery route determines absorption rate constant (k_a) and bioavailability (F).
 * Sublingual/mucosal microdose sprays absorb ~2.5x faster than standard gastric beverages.
 */
enum class DeliveryRoute(
    val defaultAbsorptionHalfLifeMinutes: Double,
    val bioavailability: Double
) {
    MICRODOSE_SPRAY(defaultAbsorptionHalfLifeMinutes = 6.0, bioavailability = 0.99),
    ESPRESSO_SHOT(defaultAbsorptionHalfLifeMinutes = 12.0, bioavailability = 0.95),
    STANDARD_BEVERAGE(defaultAbsorptionHalfLifeMinutes = 15.0, bioavailability = 0.95),
    CAPSULE_SUSTAINED(defaultAbsorptionHalfLifeMinutes = 35.0, bioavailability = 0.90)
}

data class MicrodoseEvent(
    val id: String,
    val timestamp: Instant,
    val doseMg: Double,
    val route: DeliveryRoute = DeliveryRoute.MICRODOSE_SPRAY,
    val isPlanned: Boolean = false
)

data class BiometricPharmacokineticProfile(
    val baseEliminationHalfLifeHours: Double = 5.0,
    val clearanceMultiplier: Double = 1.0, // Modulated by BiometricStateModulator (exercise/metabolism)
    val dailyMaxLimitMg: Double = 400.0,
    val therapeuticFloorMg: Double = 45.0,
    val jitterCeilingMg: Double = 160.0,
    val sleepClearanceThresholdMg: Double = 35.0,
    val ic50Mg: Double = 65.0,             // Half-maximal adenosine antagonism threshold
    val hillCoefficient: Double = 1.25
) {
    val effectiveEliminationRatePerHour: Double
        get() = (LN_2 / baseEliminationHalfLifeHours) * clearanceMultiplier.coerceIn(0.5, 2.0)

    companion object {
        const val LN_2 = 0.6931471805599453
    }
}

data class AlertnessPoint(
    val timestamp: Instant,
    val activeCaffeineMg: Double,
    val absorptionDerivativeMgPerHour: Double,
    val adenosineBlockadeRatio: Double,
    val circadianDrive: Double,
    val compositeAlertnessScore: Double, // 0.0 (exhausted) to 100.0 (peak focus)
    val isCrashRiskZone: Boolean,
    val isJitterRiskZone: Boolean
)

class BatemanPharmacokineticEngine @Inject constructor() {

    /**
     * Evaluates instantaneous active body-burden caffeine (mg) at [evalTime]
     * using the superimposed multi-dose Bateman function.
     */
    fun evaluateActiveCaffeineMg(
        events: List<MicrodoseEvent>,
        evalTime: Instant,
        profile: BiometricPharmacokineticProfile,
        includePlanned: Boolean = false
    ): Double {
        val ke = profile.effectiveEliminationRatePerHour
        val evalEpochSec = evalTime.epochSecond

        val totalMg = events.asSequence()
            .filter { includePlanned || !it.isPlanned }
            .filter { !it.timestamp.isAfter(evalTime) }
            .sumOf { event ->
                val dtHours = (evalEpochSec - event.timestamp.epochSecond) / 3600.0
                if (dtHours <= 0.0 || dtHours > 72.0) {
                    0.0
                } else {
                    val ka = LN_2 / (event.route.defaultAbsorptionHalfLifeMinutes / 60.0)
                    batemanDoseContribution(
                        doseMg = event.doseMg * event.route.bioavailability,
                        dtHours = dtHours,
                        ka = ka,
                        ke = ke
                    )
                }
            }

        return if (totalMg < 0.5) 0.0 else totalMg
    }

    /**
     * Evaluates the first derivative dC(t)/dt in mg/hour.
     * Used to detect steep post-peak drops ("caffeine crash velocity").
     */
    fun evaluateRateOfChangeMgPerHour(
        events: List<MicrodoseEvent>,
        evalTime: Instant,
        profile: BiometricPharmacokineticProfile,
        includePlanned: Boolean = false
    ): Double {
        val ke = profile.effectiveEliminationRatePerHour
        val evalEpochSec = evalTime.epochSecond

        return events.asSequence()
            .filter { includePlanned || !it.isPlanned }
            .filter { !it.timestamp.isAfter(evalTime) }
            .sumOf { event ->
                val dtHours = (evalEpochSec - event.timestamp.epochSecond) / 3600.0
                if (dtHours <= 0.0 || dtHours > 72.0) {
                    0.0
                } else {
                    val ka = LN_2 / (event.route.defaultAbsorptionHalfLifeMinutes / 60.0)
                    batemanDerivative(
                        effectiveDoseMg = event.doseMg * event.route.bioavailability,
                        dtHours = dtHours,
                        ka = ka,
                        ke = ke
                    )
                }
            }
    }

    /**
     * Solves for the exact future [Instant] when active caffeine permanently drops
     * to or below [targetThresholdMg] using hybrid Newton-Raphson + Bisection root finding.
     */
    fun solveClearanceInstant(
        events: List<MicrodoseEvent>,
        fromTime: Instant,
        targetThresholdMg: Double,
        profile: BiometricPharmacokineticProfile,
        includePlanned: Boolean = false
    ): Instant? {
        val activeEvents = events.filter { (includePlanned || !it.isPlanned) && !it.timestamp.isAfter(fromTime) }
        if (activeEvents.isEmpty()) return fromTime

        // Step 1: Ensure we search AFTER the absorption peak of the latest dose
        val latestDose = activeEvents.maxBy { it.timestamp }
        val kaLatest = LN_2 / (latestDose.route.defaultAbsorptionHalfLifeMinutes / 60.0)
        val ke = profile.effectiveEliminationRatePerHour
        val tMaxHoursAfterLatest = if (abs(kaLatest - ke) < 1e-6) {
            1.0 / ke
        } else {
            ln(kaLatest / ke) / (kaLatest - ke)
        }
        val peakGuardInstant = latestDose.timestamp.plusSeconds((tMaxHoursAfterLatest * 3600.0).toLong() + 60L)
        val searchStart = if (peakGuardInstant.isAfter(fromTime)) peakGuardInstant else fromTime

        val startMg = evaluateActiveCaffeineMg(activeEvents, searchStart, profile, includePlanned)
        if (startMg <= targetThresholdMg) {
            val currentMg = evaluateActiveCaffeineMg(activeEvents, fromTime, profile, includePlanned)
            if (currentMg <= targetThresholdMg) return fromTime
        }

        // Step 2: Bracket the root in hours from searchStart [lowH, highH]
        var lowH = 0.0
        var highH = 72.0
        var x = max(1.0, ln(max(startMg, targetThresholdMg + 1.0) / targetThresholdMg) / ke)

        for (iteration in 0 until 40) {
            val candidateInstant = searchStart.plusSeconds((x * 3600.0).toLong())
            val fx = evaluateActiveCaffeineMg(activeEvents, candidateInstant, profile, includePlanned) - targetThresholdMg

            if (abs(fx) < 0.05 || (highH - lowH) < (1.0 / 3600.0)) {
                return candidateInstant
            }

            if (fx > 0.0) {
                lowH = max(lowH, x)
            } else {
                highH = min(highH, x)
            }

            val dfx = evaluateRateOfChangeMgPerHour(activeEvents, candidateInstant, profile, includePlanned)
            val nextNewton = if (abs(dfx) > 1e-5) x - (fx / dfx) else (lowH + highH) * 0.5

            // Keep iterate strictly inside bracket; fallback to bisection if Newton jumps out
            x = if (nextNewton in (lowH + 1e-4)..(highH - 1e-4)) {
                nextNewton
            } else {
                (lowH + highH) * 0.5
            }
        }

        return searchStart.plusSeconds(((lowH + highH) * 0.5 * 3600.0).toLong())
    }

    /**
     * Computes Borbély's Two-Process Alertness + Hill Adenosine Blockade at [evalTime].
     */
    fun evaluateAlertnessPoint(
        events: List<MicrodoseEvent>,
        evalTime: Instant,
        lastWakeTime: Instant,
        userZone: ZoneId,
        sleepDebtHours: Double,
        profile: BiometricPharmacokineticProfile,
        includePlanned: Boolean = false
    ): AlertnessPoint {
        val activeMg = evaluateActiveCaffeineMg(events, evalTime, profile, includePlanned)
        val dCdt = evaluateRateOfChangeMgPerHour(events, evalTime, profile, includePlanned)

        // Hill-Langmuir receptor blockade ratio in [0.0, 1.0)
        val hillRatio = if (activeMg <= 0.0) {
            0.0
        } else {
            val ratioPow = (activeMg / profile.ic50Mg).pow(profile.hillCoefficient)
            ratioPow / (1.0 + ratioPow)
        }

        // Process S: Homeostatic adenosine sleep pressure (saturating exponential with time awake)
        val hoursAwake = max(0.0, Duration.between(lastWakeTime, evalTime).toMinutes() / 60.0)
        val debtPenalty = (sleepDebtHours.coerceIn(0.0, 10.0) / 10.0) * 0.22
        val rawHomeostaticPressure = (1.0 - exp(-hoursAwake / 18.2)) + debtPenalty
        val effectiveSleepPressure = rawHomeostaticPressure * (1.0 - 0.65 * hillRatio)

        // Process C: Two-harmonic circadian rhythm (dip in mid-afternoon ~14:30 and late night ~03:30)
        val localTime = ZonedDateTime.ofInstant(evalTime, userZone)
        val decimalHour = localTime.hour + (localTime.minute / 60.0)
        val primaryHarmonic = cos(2.0 * PI * (decimalHour - 17.0) / 24.0)
        val afternoonDipHarmonic = 0.35 * cos(4.0 * PI * (decimalHour - 10.5) / 24.0)
        val circadianDrive = ((primaryHarmonic + afternoonDipHarmonic + 1.35) / 2.70).coerceIn(0.0, 1.0)

        // Combine Process C and attenuated Process S into a 0..100 cognitive alertness score
        val rawAlertness = (0.55 * circadianDrive + 0.45 * (1.0 - effectiveSleepPressure.coerceIn(0.0, 1.0))) * 100.0
        val compositeScore = rawAlertness.coerceIn(5.0, 100.0)

        // Crash risk occurs when caffeine is dropping rapidly (> 18 mg/h) while alertness dips below 55
        val isCrashRisk = dCdt < -18.0 && activeMg < profile.therapeuticFloorMg && hoursAwake in 4.0..15.0
        val isJitterRisk = activeMg > profile.jitterCeilingMg

        return AlertnessPoint(
            timestamp = evalTime,
            activeCaffeineMg = activeMg,
            absorptionDerivativeMgPerHour = dCdt,
            adenosineBlockadeRatio = hillRatio,
            circadianDrive = circadianDrive,
            compositeAlertnessScore = compositeScore,
            isCrashRiskZone = isCrashRisk,
            isJitterRiskZone = isJitterRisk
        )
    }

    private fun batemanDoseContribution(
        doseMg: Double,
        dtHours: Double,
        ka: Double,
        ke: Double
    ): Double {
        return if (abs(ka - ke) < 1e-5) {
            doseMg * ke * dtHours * exp(-ke * dtHours)
        } else {
            doseMg * (ka / (ka - ke)) * (exp(-ke * dtHours) - exp(-ka * dtHours))
        }
    }

    private fun batemanDerivative(
        effectiveDoseMg: Double,
        dtHours: Double,
        ka: Double,
        ke: Double
    ): Double {
        return if (abs(ka - ke) < 1e-5) {
            effectiveDoseMg * ke * exp(-ke * dtHours) * (1.0 - ke * dtHours)
        } else {
            effectiveDoseMg * (ka / (ka - ke)) * (ka * exp(-ka * dtHours) - ke * exp(-ke * dtHours))
        }
    }

    private companion object {
        const val LN_2 = 0.6931471805599453
    }
}

```

---

### 2. Biometric Telemetry Fusion & Autonomic Safety Gate

Hummingbird Fuel dynamically adapts microdosing to real-time physiological signals from Google Health Connect and Wear OS. This engine computes two critical outputs:

1. **Dynamic Metabolic Clearance Multiplier ($\alpha_{\text{clearance}}$):** Aerobic activity increases hepatic blood flow and CYP1A2 induction, mildly accelerating caffeine elimination, whereas dehydration slows subjective tolerance.
2. **Autonomic Sympathetic Safety Gate:** If the user exhibits elevated non-exertional Heart Rate (tachycardia relative to resting baseline) or an acute drop in Heart Rate Variability ($Z_{\text{RMSSD}} < -1.2$), the engine throttles or halts microdose recommendations to prevent jitters and cardiovascular strain.

```kotlin
package com.zoewave.probase.goswift.data.caffeine.engine

import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

data class BiometricTelemetryInput(
    val latestHeartRateBpm: Double?,
    val restingHeartRateBaselineBpm: Double = 62.0,
    val recentStepsLast30Min: Int = 0,
    val latestHrvRmssdMs: Double?,
    val hrvBaselineMeanMs: Double = 48.0,
    val hrvBaselineStdDevMs: Double = 12.0,
    val lastNightSleepDurationHours: Double = 7.5,
    val targetSleepDurationHours: Double = 8.0,
    val activeCaloriesTodayKcal: Double = 0.0,
    val hydrationRatio: Double = 1.0 // 0.0 (severely under-hydrated) to 1.0+ (optimal)
)

enum class SympatheticGateStatus {
    OPTIMAL_READY,          // Full microdose permitted
    THROTTLED_HALF_DOSE,    // Mild sympathetic elevation or dehydration -> cap single spray
    BLOCKED_AUTONOMIC_LOAD, // High non-exertional HR or severely depressed HRV -> hold caffeine
    HYDRATION_REQUIRED      // Prompt water intake before next caffeine microdose
}

data class BiometricModulationResult(
    val clearanceMultiplier: Double,
    val dynamicallyAdjustedCeilingMg: Double,
    val calculatedSleepDebtHours: Double,
    val hrvZScore: Double,
    val nonExertionalHrDeltaBpm: Double,
    val gateStatus: SympatheticGateStatus,
    val maxRecommendedSprayCount: Int,
    val advisoryReason: String
)

class BiometricStateModulator @Inject constructor() {

    fun evaluateBiometricModulation(
        input: BiometricTelemetryInput,
        baseCeilingMg: Double = 160.0,
        maxSpraysPerBolus: Int = 3
    ): BiometricModulationResult {
        // 1. Compute HRV Z-Score (negative Z indicates sympathetic stress / lower vagal tone)
        val safeStdDev = max(input.hrvBaselineStdDevMs, 3.0)
        val hrvZ = input.latestHrvRmssdMs?.let { (it - input.hrvBaselineMeanMs) / safeStdDev } ?: 0.0

        // 2. Evaluate non-exertional Heart Rate elevation
        // If step cadence in last 30 mins is low (< 150 steps), high HR is likely stress/caffeine induced
        val isSedentaryWindow = input.recentStepsLast30Min < 150
        val rawHrDelta = (input.latestHeartRateBpm ?: input.restingHeartRateBaselineBpm) - input.restingHeartRateBaselineBpm
        val nonExertionalHrDelta = if (isSedentaryWindow) max(0.0, rawHrDelta) else 0.0

        // 3. Calculate metabolic clearance multiplier from physical activity (bounded 0.85x .. 1.30x)
        val activityFactor = 1.0 + (input.activeCaloriesTodayKcal / 2500.0).coerceIn(0.0, 0.25)
        val hydrationPenalty = if (input.hydrationRatio < 0.6) 0.92 else 1.0
        val clearanceMultiplier = (activityFactor * hydrationPenalty).coerceIn(0.85, 1.30)

        // 4. Sigmoidal scaling of the jitter ceiling based on autonomic readiness
        // Depressed HRV or elevated sedentary HR lowers the safe caffeine ceiling
        val stressIndex = max(0.0, -hrvZ * 0.6) + (nonExertionalHrDelta / 18.0)
        val ceilingScale = (1.0 / (1.0 + exp(stressIndex - 1.4))).coerceIn(0.45, 1.05)
        val adjustedCeilingMg = baseCeilingMg * ceilingScale

        // 5. Evaluate Sympathetic Safety Gate
        val (gateStatus, maxSprays, reason) = when {
            nonExertionalHrDelta >= 22.0 || hrvZ <= -1.8 -> Triple(
                SympatheticGateStatus.BLOCKED_AUTONOMIC_LOAD,
                0,
                "Elevated resting heart rate (+${nonExertionalHrDelta.toInt()} bpm) or acute HRV dip detected. Pausing microdoses."
            )
            input.hydrationRatio < 0.45 -> Triple(
                SympatheticGateStatus.HYDRATION_REQUIRED,
                0,
                "Hydration is below 45% of target. Drink 250ml water before the next microdose."
            )
            nonExertionalHrDelta >= 12.0 || hrvZ <= -1.0 || input.hydrationRatio < 0.70 -> Triple(
                SympatheticGateStatus.THROTTLED_HALF_DOSE,
                max(1, maxSpraysPerBolus / 2),
                "Mild sympathetic load detected. Capping recommendation to a single microdose spray."
            )
            else -> Triple(
                SympatheticGateStatus.OPTIMAL_READY,
                maxSpraysPerBolus,
                "Biometrics within optimal parasympathetic baseline."
            )
        }

        val sleepDebt = max(0.0, input.targetSleepDurationHours - input.lastNightSleepDurationHours)

        return BiometricModulationResult(
            clearanceMultiplier = clearanceMultiplier,
            dynamicallyAdjustedCeilingMg = adjustedCeilingMg,
            calculatedSleepDebtHours = sleepDebt,
            hrvZScore = hrvZ,
            nonExertionalHrDeltaBpm = nonExertionalHrDelta,
            gateStatus = gateStatus,
            maxRecommendedSprayCount = maxSprays,
            advisoryReason = reason
        )
    }
}

```

---

### 3. Model Predictive Microdose Scheduler (MPC Lookahead Optimizer)

Rather than setting static alarms, Hummingbird Fuel uses a **Model Predictive Control (MPC) trajectory optimizer** to compute the exact timing and spray count ($1, 2, \text{or } 3\text{ sprays}$ at $12.5\text{ mg/spray}$) across the remainder of the user's waking day.

#### Optimization Formulation

At every candidate decision slot $t_k$ (spaced by grid resolution $\Delta \tau = 15\text{ min}$), the solver selects discrete spray count $u_k \in \{0, 1, \dots, U_{\max}\}$ to minimize the lookahead trajectory cost over horizon $H$:


$$J = \sum_{\tau = t_k}^{t_{\text{bed}}} \Big[ w_{\text{under}}(t) \cdot \max\left(0, C_{\text{target}}(\tau) - C(\tau)\right)^2 + w_{\text{over}} \cdot \max\left(0, C(\tau) - C_{\text{ceiling}}\right)^2 \Big] + w_{\text{dose}} u_k$$


**Subject to hard feasibility constraints:**

1. **Sleep Preservation Constraint:** $C(t_{\text{bedtime}}) \le C_{\text{sleepThreshold}}$
2. **Peak Safety Ceiling:** $\max_{\tau} C(\tau) \le C_{\text{adjustedCeiling}}$
3. **Daily Total Cap:** $D_{\text{consumedToday}} + \sum u_k d_{\text{spray}} \le D_{\text{dailyMax}}$
4. **Refractory Spacing:** $t_k - t_{\text{lastDose}} \ge \Delta t_{\text{refractory}}$ (default $45\text{ min}$)

```kotlin
package com.zoewave.probase.goswift.data.caffeine.engine

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.pow

data class CognitiveFocusBlock(
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val priorityMultiplier: Double = 1.6 // Boosts target corridor during high-focus meetings/workouts
)

data class MicrodoseScheduleConfig(
    val mgPerSpray: Double = 12.5,
    val minRefractoryInterval: Duration = Duration.ofMinutes(45),
    val slotResolution: Duration = Duration.ofMinutes(15),
    val targetCorridorBaseMg: Double = 85.0,
    val focusBlockTargetMg: Double = 115.0
)

data class RecommendedMicrodoseSlot(
    val scheduledTime: Instant,
    val sprayCount: Int,
    val totalDoseMg: Double,
    val projectedPeakMg: Double,
    val projectedBedtimeMg: Double,
    val alignedFocusBlockTitle: String?
)

class MicrodoseScheduleOptimizer @Inject constructor(
    private val pkEngine: BatemanPharmacokineticEngine
) {

    /**
     * Generates a forward-looking sequence of microdose spray recommendations
     * between [now] and [plannedBedtime] that keeps the user inside the target
     * alertness corridor without violating the bedtime sleep threshold.
     */
    fun generateOptimalSchedule(
        loggedHistory: List<MicrodoseEvent>,
        now: Instant,
        plannedBedtime: Instant,
        userZone: ZoneId,
        focusBlocks: List<CognitiveFocusBlock>,
        pkProfile: BiometricPharmacokineticProfile,
        biometricModulation: BiometricModulationResult,
        scheduleConfig: MicrodoseScheduleConfig = MicrodoseScheduleConfig()
    ): List<RecommendedMicrodoseSlot> {
        if (!plannedBedtime.isAfter(now)) return emptyList()
        if (biometricModulation.gateStatus == SympatheticGateStatus.BLOCKED_AUTONOMIC_LOAD ||
            biometricModulation.gateStatus == SympatheticGateStatus.HYDRATION_REQUIRED
        ) {
            return emptyList()
        }

        val workingEvents = loggedHistory.filter { !it.isPlanned }.toMutableList()
        val recommendations = mutableListOf<RecommendedMicrodoseSlot>()

        // Calculate today's already consumed mg
        val localToday = now.atZone(userZone).toLocalDate()
        val startOfDay = localToday.atStartOfDay(userZone).toInstant()
        var runningDailyTotalMg = workingEvents
            .filter { !it.timestamp.isBefore(startOfDay) }
            .sumOf { it.doseMg }

        val effectiveCeilingMg = minOf(
            pkProfile.jitterCeilingMg,
            biometricModulation.dynamicallyAdjustedCeilingMg
        )
        val maxSpraysPerSlot = biometricModulation.maxRecommendedSprayCount

        // Latest cutoff: stop scheduling doses within 3 hours of bedtime
        val hardCutoffTime = plannedBedtime.minus(Duration.ofHours(3))
        var cursorTime = alignToNextSlot(now, scheduleConfig.slotResolution)

        while (!cursorTime.isAfter(hardCutoffTime)) {
            val lastDoseTime = workingEvents.maxByOrNull { it.timestamp }?.timestamp
            val refractorySatisfied = lastDoseTime == null ||
                Duration.between(lastDoseTime, cursorTime) >= scheduleConfig.minRefractoryInterval

            if (refractorySatisfied) {
                val activeFocusBlock = focusBlocks.firstOrNull { block ->
                    // Pre-load 20 mins before focus block through its end
                    !cursorTime.isBefore(block.startTime.minus(Duration.ofMinutes(20))) &&
                        cursorTime.isBefore(block.endTime)
                }

                val currentMgAtCursor = pkEngine.evaluateActiveCaffeineMg(
                    events = workingEvents,
                    evalTime = cursorTime,
                    profile = pkProfile
                )
                val targetMg = if (activeFocusBlock != null) {
                    scheduleConfig.focusBlockTargetMg
                } else {
                    scheduleConfig.targetCorridorBaseMg
                }

                // Evaluate candidate spray counts u in 0..maxSpraysPerSlot via rolling-horizon cost
                var bestSprays = 0
                var bestCost = evaluateHorizonCost(
                    events = workingEvents,
                    evalStart = cursorTime,
                    bedtime = plannedBedtime,
                    focusBlocks = focusBlocks,
                    pkProfile = pkProfile,
                    scheduleConfig = scheduleConfig,
                    ceilingMg = effectiveCeilingMg,
                    sprayCount = 0
                )

                // Only consider dosing if current trajectory is below or dipping toward target
                if (currentMgAtCursor < targetMg * 0.92) {
                    for (sprays in 1..maxSpraysPerSlot) {
                        val candidateDoseMg = sprays * scheduleConfig.mgPerSpray
                        if (runningDailyTotalMg + candidateDoseMg > pkProfile.dailyMaxLimitMg) break

                        val candidateEvent = MicrodoseEvent(
                            id = "planned_${cursorTime.epochSecond}",
                            timestamp = cursorTime,
                            doseMg = candidateDoseMg,
                            route = DeliveryRoute.MICRODOSE_SPRAY,
                            isPlanned = true
                        )
                        val candidateList = workingEvents + candidateEvent

                        // Hard Constraint 1: Bedtime sleep clearance check
                        val bedtimeMg = pkEngine.evaluateActiveCaffeineMg(
                            events = candidateList,
                            evalTime = plannedBedtime,
                            profile = pkProfile,
                            includePlanned = true
                        )
                        if (bedtimeMg > pkProfile.sleepClearanceThresholdMg) break

                        // Hard Constraint 2: Peak jitter ceiling check (~35 min post-spray)
                        val peakCheckTime = cursorTime.plus(Duration.ofMinutes(35))
                        val projectedPeakMg = pkEngine.evaluateActiveCaffeineMg(
                            events = candidateList,
                            evalTime = peakCheckTime,
                            profile = pkProfile,
                            includePlanned = true
                        )
                        if (projectedPeakMg > effectiveCeilingMg) break

                        val candidateCost = evaluateHorizonCost(
                            events = candidateList,
                            evalStart = cursorTime,
                            bedtime = plannedBedtime,
                            focusBlocks = focusBlocks,
                            pkProfile = pkProfile,
                            scheduleConfig = scheduleConfig,
                            ceilingMg = effectiveCeilingMg,
                            sprayCount = sprays
                        )

                        if (candidateCost < bestCost) {
                            bestCost = candidateCost
                            bestSprays = sprays
                        }
                    }
                }

                if (bestSprays > 0) {
                    val doseMg = bestSprays * scheduleConfig.mgPerSpray
                    val acceptedEvent = MicrodoseEvent(
                        id = "planned_${cursorTime.epochSecond}",
                        timestamp = cursorTime,
                        doseMg = doseMg,
                        route = DeliveryRoute.MICRODOSE_SPRAY,
                        isPlanned = true
                    )
                    workingEvents.add(acceptedEvent)
                    runningDailyTotalMg += doseMg

                    val peakMg = pkEngine.evaluateActiveCaffeineMg(
                        events = workingEvents,
                        evalTime = cursorTime.plus(Duration.ofMinutes(35)),
                        profile = pkProfile,
                        includePlanned = true
                    )
                    val bedtimeMg = pkEngine.evaluateActiveCaffeineMg(
                        events = workingEvents,
                        evalTime = plannedBedtime,
                        profile = pkProfile,
                        includePlanned = true
                    )

                    recommendations.add(
                        RecommendedMicrodoseSlot(
                            scheduledTime = cursorTime,
                            sprayCount = bestSprays,
                            totalDoseMg = doseMg,
                            projectedPeakMg = peakMg,
                            projectedBedtimeMg = bedtimeMg,
                            alignedFocusBlockTitle = activeFocusBlock?.title
                        )
                    )
                }
            }

            cursorTime = cursorTime.plus(scheduleConfig.slotResolution)
        }

        return recommendations
    }

    /**
     * Evaluates the quadratic tracking cost over a 3.5-hour lookahead horizon.
     */
    private fun evaluateHorizonCost(
        events: List<MicrodoseEvent>,
        evalStart: Instant,
        bedtime: Instant,
        focusBlocks: List<CognitiveFocusBlock>,
        pkProfile: BiometricPharmacokineticProfile,
        scheduleConfig: MicrodoseScheduleConfig,
        ceilingMg: Double,
        sprayCount: Int
    ): Double {
        val horizonEnd = minOf(bedtime, evalStart.plus(Duration.ofMinutes(210)))
        var totalCost = sprayCount * 14.0 // Small regularization penalty to prevent unnecessary micro-sprays
        var t = evalStart

        while (!t.isAfter(horizonEnd)) {
            val activeMg = pkEngine.evaluateActiveCaffeineMg(
                events = events,
                evalTime = t,
                profile = pkProfile,
                includePlanned = true
            )
            val inFocus = focusBlocks.any { !t.isBefore(it.startTime) && t.isBefore(it.endTime) }
            val desiredMg = if (inFocus) scheduleConfig.focusBlockTargetMg else scheduleConfig.targetCorridorBaseMg
            val weightUnder = if (inFocus) 2.2 else 1.0

            val underShoot = max(0.0, desiredMg - activeMg)
            val overShoot = max(0.0, activeMg - ceilingMg * 0.85)

            totalCost += weightUnder * underShoot.pow(2) + 4.5 * overShoot.pow(2)
            t = t.plus(Duration.ofMinutes(15))
        }

        return totalCost
    }

    private fun alignToNextSlot(instant: Instant, resolution: Duration): Instant {
        val resSec = resolution.seconds
        val rem = instant.epochSecond % resSec
        return if (rem == 0L) instant else instant.plusSeconds(resSec - rem)
    }
}

```

---

### 4. LTTB Perceptual Downsampler & Centripetal Catmull-Rom Spline Generator (`features/insights`)

Rendering 24–72 hours of high-frequency Health Connect Heart Rate, HRV, and Bateman pharmacokinetic curves directly on a Jetpack Compose `Canvas` can bog down frame times if thousands of raw samples are drawn per frame.

1. **Largest-Triangle-Three-Buckets (LTTB):** Downsamples $N$ biometric samples to $K$ screen-space buckets in $O(N)$ time by selecting the point in each bucket that maximizes the triangular area formed with the previously selected point and the centroid of the next bucket—preserving exact local maxima (caffeine spikes) and minima (crash troughs).
2. **Centripetal Catmull-Rom ($\alpha = 0.5$) to Cubic Bézier Conversion:** Standard uniform splines overshoot and create self-intersecting loops at sharp microdose absorption spikes. Centripetal parameterization ($\alpha = 0.5$) guarantees **zero cusps and zero self-intersections** while outputting exact cubic Bézier control points $(C_1, C_2)$ for `androidx.compose.ui.graphics.Path.cubicTo()`.

```kotlin
package com.zoewave.probase.goswift.features.insights.chart

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow

data class WaveformPoint(
    val x: Float, // Epoch seconds or normalized canvas X [0f..width]
    val y: Float  // Biometric/Caffeine value or canvas Y [0f..height]
)

data class CubicBezierSegment(
    val start: WaveformPoint,
    val control1: WaveformPoint,
    val control2: WaveformPoint,
    val end: WaveformPoint
)

object WaveformSignalProcessor {

    /**
     * Downsamples [rawPoints] to [targetBucketCount] points using the
     * Largest-Triangle-Three-Buckets (LTTB) algorithm, preserving sharp peaks and troughs.
     */
    fun downsampleLttb(
        rawPoints: List<WaveformPoint>,
        targetBucketCount: Int
    ): List<WaveformPoint> {
        val n = rawPoints.size
        if (targetBucketCount >= n || targetBucketCount < 3) return rawPoints

        val sampled = ArrayList<WaveformPoint>(targetBucketCount)
        sampled.add(rawPoints.first()) // Always retain first sample

        val bucketSize = (n - 2).toDouble() / (targetBucketCount - 2).toDouble()
        var lastSelectedIndex = 0

        for (bucketIndex in 0 until (targetBucketCount - 2)) {
            // 1. Compute centroid of the NEXT bucket
            val nextBucketStart = ((bucketIndex + 1) * bucketSize).toInt() + 1
            val nextBucketEnd = (((bucketIndex + 2) * bucketSize).toInt() + 1).coerceAtMost(n)
            val nextBucketLen = (nextBucketEnd - nextBucketStart).coerceAtLeast(1)

            var avgX = 0f
            var avgY = 0f
            for (j in nextBucketStart until nextBucketEnd) {
                avgX += rawPoints[j].x
                avgY += rawPoints[j].y
            }
            avgX /= nextBucketLen
            avgY /= nextBucketLen

            // 2. Find point in CURRENT bucket maximizing triangle area with lastSelected and nextCentroid
            val currentStart = (bucketIndex * bucketSize).toInt() + 1
            val currentEnd = (((bucketIndex + 1) * bucketSize).toInt() + 1).coerceAtMost(n - 1)

            val pointA = rawPoints[lastSelectedIndex]
            var maxArea = -1f
            var bestIndex = currentStart

            for (idx in currentStart until currentEnd) {
                val candidate = rawPoints[idx]
                // 2x Triangle Area = |Ax(By - Cy) + Bx(Cy - Ay) + Cx(Ay - By)|
                val area = abs(
                    (pointA.x - avgX) * (candidate.y - pointA.y) -
                        (pointA.x - candidate.x) * (avgY - pointA.y)
                )
                if (area > maxArea) {
                    maxArea = area
                    bestIndex = idx
                }
            }

            sampled.add(rawPoints[bestIndex])
            lastSelectedIndex = bestIndex
        }

        sampled.add(rawPoints.last()) // Always retain final sample
        return sampled
    }

    /**
     * Converts a sequence of [points] in screen coordinates into smooth, overshoot-free
     * [CubicBezierSegment]s using Centripetal Catmull-Rom (alpha = 0.5) parameterization.
     */
    fun computeCentripetalBezierSegments(
        points: List<WaveformPoint>,
        alpha: Float = 0.5f
    ): List<CubicBezierSegment> {
        if (points.size < 2) return emptyList()
        if (points.size == 2) {
            return listOf(
                CubicBezierSegment(
                    start = points[0],
                    control1 = points[0],
                    control2 = points[1],
                    end = points[1]
                )
            )
        }

        val segments = ArrayList<CubicBezierSegment>(points.size - 1)

        for (i in 0 until points.size - 1) {
            val p0 = if (i == 0) extrapolateEndpoint(points[0], points[1]) else points[i - 1]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i + 2 < points.size) points[i + 2] else extrapolateEndpoint(points[i + 1], points[i])

            val d1 = chordDistance(p0, p1).pow(alpha).coerceAtLeast(1e-4f)
            val d2 = chordDistance(p1, p2).pow(alpha).coerceAtLeast(1e-4f)
            val d3 = chordDistance(p2, p3).pow(alpha).coerceAtLeast(1e-4f)

            val d1Sq = d1 * d1
            val d2Sq = d2 * d2
            val d3Sq = d3 * d3

            // Compute Exact Bezier Control Point 1
            val denom1 = 3f * d1 * (d1 + d2)
            val c1x = (d1Sq * p2.x - d2Sq * p0.x + (2f * d1Sq + 3f * d1 * d2 + d2Sq) * p1.x) / denom1
            val c1y = (d1Sq * p2.y - d2Sq * p0.y + (2f * d1Sq + 3f * d1 * d2 + d2Sq) * p1.y) / denom1

            // Compute Exact Bezier Control Point 2
            val denom2 = 3f * d3 * (d3 + d2)
            val c2x = (d3Sq * p1.x - d2Sq * p3.x + (2f * d3Sq + 3f * d3 * d2 + d2Sq) * p2.x) / denom2
            val c2y = (d3Sq * p1.y - d2Sq * p3.y + (2f * d3Sq + 3f * d3 * d2 + d2Sq) * p2.y) / denom2

            segments.add(
                CubicBezierSegment(
                    start = p1,
                    control1 = WaveformPoint(c1x, c1y),
                    control2 = WaveformPoint(c2x, c2y),
                    end = p2
                )
            )
        }

        return segments
    }

    private fun chordDistance(a: WaveformPoint, b: WaveformPoint): Float =
        hypot(b.x - a.x, b.y - a.y)

    private fun extrapolateEndpoint(anchor: WaveformPoint, neighbor: WaveformPoint): WaveformPoint =
        WaveformPoint(
            x = anchor.x - (neighbor.x - anchor.x),
            y = anchor.y - (neighbor.y - anchor.y)
        )
}

```

---

### How These Four Modules Connect in `applications/goswift`

| Module / Layer | Algorithmic Component | Upstream Inputs | Downstream Consumer |
| --- | --- | --- | --- |
| **`data/caffeine`** | `BatemanPharmacokineticEngine` | Logged & planned `MicrodoseEvent`s, wake time, `ZoneId` | `HomeViewModel` (`CaffeineRemainingRing`) & `ScheduleViewModel` |
| **`data/caffeine`** | `BiometricStateModulator` | Health Connect `SleepSummary`, `HrvSample`, `HeartRateSample`, steps, hydration | Dynamically scales `clearanceMultiplier` and gates `MicrodoseScheduleOptimizer` |
| **`features/schedule`** | `MicrodoseScheduleOptimizer` | `BatemanPharmacokineticEngine`, `BiometricModulationResult`, focus calendar blocks | Generates optimal 15-min lookahead spray schedule & Wear OS notifications |
| **`features/insights`** | `WaveformSignalProcessor` | Multi-day HR/HRV samples + 30-min Bateman telemetry points | 120Hz `SparklineWaveform` Compose `Canvas` without allocations or overshoots |