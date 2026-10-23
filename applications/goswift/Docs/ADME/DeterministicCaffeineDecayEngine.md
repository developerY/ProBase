### 1. Deterministic Caffeine Decay Engine (`core/domain` or `data/caffeine`)

By anchoring all intake timestamps to UTC `java.time.Instant` and restricting `ZoneId` strictly to calendar-day aggregation ("Today's Total Intake"), the engine remains immune to Daylight Saving Time transitions and timezone travel.

#### Mathematical Model

For any evaluation timestamp $t$ and a set of logged intake events $(t_i, D_i)$ where $t_i \le t$, the estimated remaining caffeine $C(t)$ follows first-order exponential elimination:

$$C(t) = \sum_{i : t_i \le t} D_i \cdot \left(\frac{1}{2}\right)^{\frac{t - t_i}{t_{1/2}}}$$

Because every active dose decays with the same half-life $t_{1/2}$, the superimposed total $C(t)$ also decays at rate $t_{1/2}$ for all times after the latest dose. This yields an exact closed-form equation for the **Estimated Clearance Time** $t_{\text{clear}}$ (the exact `Instant` when remaining caffeine drops to the user's configured sleep threshold $C_{\text{sleep}}$):

$$\Delta t_{\text{clear}} = t_{1/2} \cdot \log_2\left(\frac{C(t)}{C_{\text{sleep}}}\right), \quad t_{\text{clear}} = t + \Delta t_{\text{clear}}$$

#### Kotlin Contract & Reference Implementation

```kotlin
package com.zoewave.probase.goswift.domain.caffeine

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

/**
 * Immutable domain record representing a single caffeine event.
 * Stored in Room using UTC epoch milliseconds for [timestamp].
 */
data class CaffeineIntakeRecord(
    val id: String,
    val timestamp: Instant,
    val amountMg: Double,
    val sourceName: String,
    val isPlanned: Boolean = false
) {
    init {
        require(amountMg >= 0.0) { "Caffeine amount cannot be negative." }
    }
}

/**
 * User-configurable parameters governing the deterministic estimation model.
 */
data class PharmacokineticConfig(
    val halfLife: Duration = Duration.ofHours(5),
    val dailyLimitMg: Double = 400.0,
    val sleepThresholdMg: Double = 50.0,
    val negligibleCutoffMg: Double = 1.0
) {
    init {
        require(!halfLife.isZero && !halfLife.isNegative) { "Half-life must be positive." }
        require(dailyLimitMg > 0.0) { "Daily limit must be positive." }
        require(sleepThresholdMg > 0.0) { "Sleep threshold must be positive." }
    }
}

data class TelemetryPoint(
    val timestamp: Instant,
    val estimatedRemainingMg: Double,
    val includesPlanned: Boolean = false
)

data class CaffeineTelemetrySnapshot(
    val evaluatedAt: Instant,
    val estimatedRemainingMg: Double,
    val dailyConsumedMg: Double,
    val dailyLimitMg: Double,
    val dailyLimitRatio: Float,
    val estimatedSleepReadyInstant: Instant?,
    val estimatedZeroClearanceInstant: Instant?,
    val timelineSeries: List<TelemetryPoint>
)

interface CaffeineDecayEngine {
    /**
     * Computes instantaneous remaining caffeine in mg at [evaluationTime].
     */
    fun calculateRemainingMg(
        records: List<CaffeineIntakeRecord>,
        evaluationTime: Instant,
        config: PharmacokineticConfig = PharmacokineticConfig(),
        includePlanned: Boolean = false
    ): Double

    /**
     * Generates a complete telemetry snapshot for the Home ring and Schedule timeline.
     */
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
                // Negligible contribution cutoff after ~10 half-lives (< 0.1% remaining)
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

        // Aggregate daily intake strictly within the user's current local calendar day
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

        // Generate 24h window (-12h to +12h around evaluationTime) for Sparkline / Schedule cards
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

```

---

### 2. Shared Health Data Repository Contract (`data/health`)

This contract isolates `androidx.health.connect.client` inside `data/health`. Feature modules (`features/healthsync` and `features/insights`) consume pure domain types and capability states, preventing UI-layer SDK leakage and enforcing the Cloud AI privacy boundary.

#### Kotlin Contract & Permission Capability Architecture

```kotlin
package com.zoewave.probase.goswift.data.health

import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant

/**
 * Represents the availability of the Health Connect SDK on the host device.
 */
enum class HealthConnectAvailability {
    AVAILABLE,
    UPDATE_REQUIRED,
    NOT_SUPPORTED
}

/**
 * Tracks granular permission and feature capability state.
 * Historical read (>30 days) and background read require explicit feature status checks
 * via HealthConnectFeatures before requesting permissions.
 */
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
    /**
     * Without [isHistoryReadGranted], queries must be clamped to no earlier than
     * Instant.now().minus(Duration.ofDays(30)) to prevent SecurityException.
     */
    val maxHistoricalLookback: Duration
        get() = if (isHistoryReadGranted) Duration.ofDays(365) else Duration.ofDays(30)
}

/**
 * Pure domain models for Insights & Health Sync.
 * PRIVACY BOUNDARY: These models must NEVER be serialized into Cloud AI prompts.
 */
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
    /**
     * Reactive stream of current Health Connect availability and permission states.
     */
    val permissionState: Flow<HealthPermissionState>

    /**
     * Refreshes permission and feature capability status from HealthConnectClient.
     */
    suspend fun refreshPermissionState(): HealthPermissionState

    /**
     * Returns the exact Set of permission strings safe to pass to
     * PermissionController.createRequestPermissionResultContract(), dynamically
     * filtering out history/background permissions if the device's HealthConnectFeatures
     * reports FEATURE_STATUS_UNAVAILABLE.
     */
    suspend fun buildSafePermissionRequestSet(
        includeHistoryRead: Boolean = false,
        includeBackgroundRead: Boolean = false
    ): Set<String>

    suspend fun revokeAllHealthPermissions()

    /**
     * Queries sleep sessions within [startTime]..[endTime].
     * Automatically clamps [startTime] to 30 days ago if [HealthPermissionState.isHistoryReadGranted] is false.
     */
    suspend fun getSleepSessions(
        startTime: Instant,
        endTime: Instant
    ): HealthDataResult<List<SleepSummary>>

    /**
     * Queries HRV (RMSSD) samples within [startTime]..[endTime].
     */
    suspend fun getHrvSamples(
        startTime: Instant,
        endTime: Instant
    ): HealthDataResult<List<HrvSample>>

    /**
     * Queries Heart Rate samples within [startTime]..[endTime] for waveform sparklines.
     */
    suspend fun getHeartRateSamples(
        startTime: Instant,
        endTime: Instant
    ): HealthDataResult<List<HeartRateSample>>

    /**
     * Queries aggregated steps and active calories within [startTime]..[endTime].
     */
    suspend fun getDailyActivitySummary(
        startTime: Instant,
        endTime: Instant
    ): HealthDataResult<DailyActivitySummary>
}

```

#### Implementation Guardrails for `HealthConnectRepositoryImpl`

1. **Feature Gating Before Requesting Optional Permissions:**
```kotlin
val historyStatus = healthConnectClient.features.getFeatureStatus(
    HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY
)
val isHistorySupported = historyStatus == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE

```


Never append `HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY` or `PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND` to the permission request contract unless `getFeatureStatus()` returns `FEATURE_STATUS_AVAILABLE`.
2. **Automatic 30-Day Window Clamping:**
   Inside `getSleepSessions`, `getHrvSamples`, `getHeartRateSamples`, and `getDailyActivitySummary`, compute a safe floor:
```kotlin
val thirtyDaysAgo = clock.instant().minus(Duration.ofDays(29).plusHours(23))
val effectiveStart = if (!state.isHistoryReadGranted && startTime.isBefore(thirtyDaysAgo)) {
    thirtyDaysAgo
} else {
    startTime
}

```


This guarantees that even if a user denies the optional 30-day history permission, 90-day chart views in `features/insights` gracefully render the most recent 30 days (`isClampedTo30Days = true`) rather than crashing with a `SecurityException`.

---

### 3. Prompt for the AI Coding Agent

Copy and paste the block below directly into your AI coding agent:


# Role & Objective
You are a Senior Android Architect and MAD (Modern Android Development) Engineer working in the `ProBase` monorepo. Your task is to completely revamp `applications/goswift` (a caffeine telemetry and tracker app) to match the Stitch "Warm Biometric Telemetry" Dark Theme export while upgrading its architecture to mirror `applications/kocolor`.

## Core Architectural & Regulatory Mandates
1. **Deterministic Local Math:** All caffeine decay and clearance projections must be 100% deterministic, offline-first, and unit-tested using first-order elimination math with UTC `Instant` timestamps.
2. **Domain Safety:** Never display clinical/medical claims such as "serum caffeine in mg/dL". Use explicit labels: `"Estimated caffeine remaining"` and `"Your configured daily limit"`. Rename the hero ring component to `"Caffeine Remaining Ring"`.
3. **Bounded AI & Privacy Boundary:** Gemini AI is strictly advisory (summarizing trends and explaining observations), placed behind an interface and a feature flag. Cloud AI requests require explicit user consent, send minimal data, and MUST EXCLUDE all Health Connect records (`SleepSummary`, `HrvSample`, `HeartRateSample`, `DailyActivitySummary`) by default. The app must be 100% functional with AI disabled or offline.
4. **Strict Health Connect Layering:** Keep `androidx.health.connect.client` confined to `data/health`. Check `HealthConnectFeatures.getFeatureStatus()` before requesting `PERMISSION_READ_HEALTH_DATA_HISTORY` (>30 days) or `PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND`. Automatically clamp query start times to 30 days ago if historical permission is not granted.

---

## Execution Workflow (Incremental & Verified)

### Phase 0: Discovery & Migration Checklist
Before moving, renaming, or modifying any files:
1. Inspect `settings.gradle.kts`, `gradle/libs.versions.toml`, `applications/kocolor`, and `applications/goswift`.
2. Output a concise **Migration Checklist** mapping existing `goswift` modules, packages, and dependency versions against `kocolor`.
3. Verify the baseline project compiles before making structural changes. Run relevant Gradle test/compile tasks after each phase below.

---

### Phase 1: Design System & Theming (`apps/mobile/ui/theme`)
Modify `GoSwiftTheme.kt` and associated theme files (`Type.kt`, `Color.kt`, `Shape.kt`):
- **ColorScheme:** Implement a complete dark-centric Material 3 `ColorScheme`:
  - `background` / `surface`: `#101418`
  - `primary`: `#FFB4AA` (with `#FF6F61` for `primaryContainer` / high-emphasis accents)
  - `secondary`: `#FDBA74`
  - `tertiary`: `#FEF08A`
  - Explicitly define accessible `onPrimary`, `onSecondary`, `onTertiary`, `onSurface`, `onSurfaceVariant`, `surfaceContainerLow/Default/High`, `outline`, `outlineVariant`, and `error` tokens meeting WCAG contrast targets.
- **Typography:** Configure `Space Grotesk` (Headlines/Display), `Manrope` (Body/Labels), and `JetBrains Mono` (Telemetry & Data Readouts). Enable tabular numerals (`fontFeatureSettings = "tnum"`) on `JetBrains Mono` and numeric headline styles so live countdowns do not jitter horizontally. Use scalable `sp` units.
- **Components & Surfaces:** Build reusable base components (`GoSwiftPrimaryButton`, `GoSwiftSecondaryButton`, `BiometricChip`, `TelemetryCard`). Replace expensive CSS `backdrop-blur` concepts from the Stitch mockups with Material 3 tonal surface elevation, subtle translucent fills, and `1.dp` hairline borders. Replace the broken mockup header image with a clean vector icon + `Space Grotesk` wordmark.

---

### Phase 2: Deterministic Decay Engine & Persistence (`data/caffeine` or `core/domain`)
1. Implement `CaffeineIntakeRecord` (storing timestamps as UTC `Instant` epoch millis in Room), `PharmacokineticConfig`, `TelemetryPoint`, and `CaffeineTelemetrySnapshot`.
2. Implement `CaffeineDecayEngine` and `DeterministicCaffeineDecayEngine` using first-order exponential decay:
   - `C(t) = sum(D_i * 0.5^((t - t_i) / t_half))` for all `t_i <= t`.
   - Closed-form clearance time projection to `sleepThresholdMg` (default `50.0` mg) and `negligibleCutoffMg` (default `1.0` mg).
   - Calculate "Today's Total Intake" using the user's `ZoneId` only for local midnight-to-midnight boundary filtering.
3. Bind `DeterministicCaffeineDecayEngine` via Hilt (`@Binds`).

---

### Phase 3: Shared Health Data Repository (`data/health`)
Create or update the `data/health` module:
1. Implement `HealthRepository` and `HealthConnectRepositoryImpl` exposing `permissionState: Flow<HealthPermissionState>`.
2. Query `SleepSessionRecord`, `HeartRateVariabilityRmssdRecord`, `HeartRateRecord`, and `StepsRecord`/`ActiveCaloriesBurnedRecord`, mapping them to pure domain models (`SleepSummary`, `HrvSample`, `HeartRateSample`, `DailyActivitySummary`).
3. In `buildSafePermissionRequestSet()`, inspect `healthConnectClient.features.getFeatureStatus()` for `FEATURE_READ_HEALTH_DATA_HISTORY` and `FEATURE_READ_HEALTH_DATA_IN_BACKGROUND` before including those permission strings.
4. Clamp query start times to 30 days prior to `Clock.instant()` whenever `isHistoryReadGranted` is `false`, returning `HealthDataResult.Success(data, isClampedTo30Days = true)` instead of throwing a `SecurityException`.

---

### Phase 4: Adaptive Navigation & Main Scaffold (`apps/mobile`)
Update `GoSwiftMainScreen.kt`:
1. Adopt `NavigationSuiteScaffold` following `applications/kocolor` patterns for seamless phone (Bottom Navigation Bar) vs. tablet/foldable (Navigation Rail / Drawer) adaptation.
2. Use `currentWindowAdaptiveInfoV2()` (or the matching version in `libs.versions.toml`) for window-size class and posture detection.
3. Configure Jetpack Navigation 3 (`NavDisplay` / `rememberNavBackStack`) with entry decorators ordered strictly as:
   1. `rememberSaveableStateHolderNavEntryDecorator()`
   2. `rememberViewModelStoreNavEntryDecorator()`
4. Ensure full Edge-to-Edge compliance with organic system-bar inset consumption.

---

### Phase 5: Feature Modules (`features/home`, `features/schedule`, `features/healthsync`, `features/insights`, `features/settings`)
Restructure/update the feature modules cleanly, verifying Gradle dependency graphs at each step:
1. **`features/home` (Live Fuel Dashboard):**
   - Build the `"Caffeine Remaining Ring"` (multi-segmented Compose `Canvas` progress arc).
   - Display `"Estimated caffeine remaining"` (mg), `"Your configured daily limit"` (mg), and projected sleep-ready clearance time from `CaffeineDecayEngine`.
   - Provide quick-add intake logging persisted to Room via Hilt ViewModel.
2. **`features/schedule` (Telemetry Timeline & Bounded AI):**
   - Build horizontal swipe cards for telemetry timeline breakdowns (past logged vs. future planned intake).
   - Define `CaffeineAdvisoryAiService` behind a feature flag and explicit user-consent gate. Provide a `NoOpAdvisoryAiService` / local deterministic fallback when offline, unconsented, unsupported, or out of quota. Ensure zero Health Connect data is passed into the prompt builder.
3. **`features/healthsync` (Health Connect Permissions & Sources):**
   - Consume `HealthRepository` to display connection status, connected source apps, and toggles for core permissions, >30-day historical read, and background read using standard Health Connect permission contracts.
4. **`features/insights` (Biometric Correlation & Sparkline Waveforms):**
   - Consume `HealthRepository` and `CaffeineDecayEngine` to display permissioned Sleep, HRV, and Heart Rate data alongside historical caffeine intake.
   - Implement custom Compose `Canvas` Sparkline Waveform charts with scrubbing support.
5. **`features/settings`:**
   - Manage appearance, privacy/consent toggles, AI feature flags, and pharmacokinetic parameters (`dailyLimitMg`, `halfLife` hours, `sleepThresholdMg`).

---

### Phase 6: Automated Verification Suite
Write and execute unit tests covering:
1. **Pharmacokinetic Math & Timezones:** Exact half-life decay at $0\text{h}, 2.5\text{h}, 5\text{h}, 10\text{h}$; superimposed multi-dose decay; closed-form clearance time accuracy; DST spring-forward/fall-back transitions; and cross-timezone travel (`ZoneId` shifts).
2. **Health Connect Repository:** Denied permissions, mid-session permission revocation, missing metrics, SDK unavailability, and 30-day clamping when `PERMISSION_READ_HEALTH_DATA_HISTORY` is denied or unsupported.
3. **Bounded AI & Privacy Guard:** Verify provider fallback when offline/quota-exceeded, and assert that prompt payloads contain only consented caffeine summary numbers and zero Health Connect records.
4. **Full Build Check:** Run `./gradlew :applications:goswift:apps:mobile:assembleDebug` and all unit test tasks for `goswift` modules, fixing any compiler warnings or deprecations before completing the task.

