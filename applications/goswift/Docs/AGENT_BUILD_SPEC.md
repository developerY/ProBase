Drop this self-contained master instruction file (e.g., `AGENT_BUILD_SPEC.md` or system prompt) directly into your coding agent. It combines the operating guardrails, module topology, exact Kotlin contracts, UI specifications, and verification loop into a single autonomous execution brief.

# MASTER AGENT INSTRUCTION: Redo GoSwift Application (Stitch UI Migration & MAD Upgrade)

## 1. Mission & Architectural Mandate
You are an autonomous Principal Android Architect operating inside the `ProBase` monorepo. Your objective is to completely revamp `applications/goswift` (a caffeine telemetry and tracker application) to implement the Stitch **"Warm Biometric Telemetry"** Dark Theme design system while upgrading the architecture to mirror `applications/kocolor`.

You must execute this migration end-to-end, incrementally, and deterministically. Do not leave `TODO` placeholders, stubbed UI screens, or unimplemented interfaces.

---

## 2. Non-Negotiable Operating Guardrails

1. **Inspect Before Mutating:** Before creating, renaming, or deleting any Gradle module or package, inspect `settings.gradle.kts`, `gradle/libs.versions.toml`, `build-logic` convention plugins, `applications/kocolor`, and `applications/goswift`. Reuse existing convention plugins and version catalog aliases from `kocolor`.
2. **Regulatory & Domain Safety:**
   - **NEVER** display clinical or diagnostic claims such as `"serum caffeine in mg/dL"` or `"blood concentration"`.
   - Use explicit, transparent estimation labels: `"Estimated caffeine remaining"` and `"Your configured daily limit"`.
   - Name the primary dashboard arc component `CaffeineRemainingRing` (replacing "Metabolic Fuel Ring").
3. **100% Deterministic Core Math:**
   - All caffeine decay, remaining mg, daily totals, and sleep-clearance projections must be computed locally and deterministically via first-order exponential elimination.
   - Store and evaluate all timestamps as UTC `java.time.Instant` (epoch milliseconds in Room). Use `java.time.ZoneId` strictly for local calendar-day boundaries ("Today's Consumed Caffeine").
4. **Bounded AI & Strict Privacy Wall:**
   - AI (On-Device / Cloud Gemini) is strictly optional and advisory (summarizing logged caffeine trends and explaining schedule patterns), placed behind `CaffeineAdvisoryAiService` and a user-configurable feature flag.
   - Cloud AI requests require explicit user consent (`isCloudAiConsentGranted`), send minimal aggregated caffeine telemetry, and **MUST NEVER** include or serialize any Health Connect data (`SleepSummary`, `HrvSample`, `HeartRateSample`, `DailyActivitySummary`).
   - Every screen and calculation must work 100% offline or with AI disabled.
5. **Health Connect Isolation & Feature Gating:**
   - Confine all `androidx.health.connect.client` imports strictly to `data/health`. UI and feature modules must never import Health Connect SDK classes directly (except standard permission request contract launchers in `features/healthsync`).
   - Always check `healthConnectClient.features.getFeatureStatus(...)` before requesting optional permissions (`PERMISSION_READ_HEALTH_DATA_HISTORY` for data older than 30 days and `PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND`).
   - Automatically clamp query `startTime` values to 30 days prior to `Clock.instant()` whenever `isHistoryReadGranted == false` so 90-day or custom views degrade gracefully without throwing a `SecurityException`.
6. **Incremental Compilation Gate:** Run Gradle compilation and unit tests after completing each phase before moving to the next.

---

## 3. Target Module Topology (`applications/goswift`)

Align the module hierarchy under `applications/goswift` with `applications/kocolor` while preserving working code during refactoring:

```text
applications/goswift/
├── apps/
│   └── mobile/                  # App entry point, GoSwiftMainScreen, Adaptive Scaffold, Nav3 Host, GoSwiftTheme
├── data/
│   ├── caffeine/                # Room database, DAOs, CaffeineRepository, DeterministicCaffeineDecayEngine
│   └── health/                  # HealthRepository, HealthConnectRepositoryImpl, pure health domain models
└── features/
    ├── home/                    # Live Fuel Dashboard, CaffeineRemainingRing, quick intake logging
    ├── schedule/                # Telemetry timeline horizontal swipe cards, planned intake, Bounded AI advisor
    ├── healthsync/              # Health Connect permission flows, source attribution, sync status UI
    ├── insights/                # Biometric correlation (Sleep, HRV, HR vs. Caffeine), SparklineWaveform charts
    └── settings/                # Theme, privacy/AI consent toggles, pharmacokinetic parameters (half-life, limit)
```

*Note on Navigation 3 Route Keys:* Define `@Serializable` route keys either inside each feature module's public navigation package or a shared navigation contract (matching the pattern used in `applications/kocolor`) so `GoSwiftMainScreen.kt` resolves the backstack cleanly without circular dependencies.

---

## 4. Core Domain & Data Contracts

### 4.1 Deterministic Caffeine Decay Engine (`data/caffeine`)

Implement the following exact mathematical model and Kotlin contract:
- **Superimposed First-Order Decay:**
  $$C(t) = \sum_{i : t_i \le t} D_i \cdot \left(\frac{1}{2}\right)^{\frac{t - t_i}{t_{1/2}}}$$
- **Closed-Form Clearance Time Projection ($t_{\text{clear}}$):**
  $$\Delta t_{\text{clear}} = t_{1/2} \cdot \log_2\left(\frac{C(t)}{C_{\text{target}}}\right), \quad t_{\text{clear}} = t + \Delta t_{\text{clear}}$$

```kotlin
package com.zoewave.probase.goswift.data.caffeine

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

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
```

### 4.2 Shared Health Data Repository Contract (`data/health`)

```kotlin
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
```

### 4.3 Bounded AI Advisory Contract (`features/schedule` or `data/caffeine`)

```kotlin
package com.zoewave.probase.goswift.data.caffeine.ai

/**
 * Strictly sanitized payload for AI summarization.
 * PRIVACY WALL: Must NEVER contain Health Connect records or PII.
 */
data class SanitizedCaffeineTrendPayload(
    val dailyConsumedMg: Double,
    val configuredDailyLimitMg: Double,
    val currentEstimatedRemainingMg: Double,
    val hoursUntilSleepThreshold: Double,
    val intakeCountLast7Days: Int,
    val averageDailyMgLast7Days: Double
)

sealed interface AdvisorySummaryResult {
    data class Available(val summaryText: String, val isOnDevice: Boolean) : AdvisorySummaryResult
    data class FallbackDeterministic(val summaryText: String, val reason: String) : AdvisorySummaryResult
    data object DisabledByUser : AdvisorySummaryResult
}

interface CaffeineAdvisoryAiService {
    suspend fun generateTrendAdvisory(
        payload: SanitizedCaffeineTrendPayload,
        isAiEnabled: Boolean,
        isCloudAiConsentGranted: Boolean
    ): AdvisorySummaryResult
}
```

---

## 5. Design System & UI Specifications ("Warm Biometric Telemetry")

Implement in `apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/ui/theme/`:

1. **Color Palette (`Color.kt` & `GoSwiftTheme.kt`):**
    - `background` / `surface`: `#101418`
    - `surfaceContainerLow`: `#161B20`
    - `surfaceContainer`: `#1C2228`
    - `surfaceContainerHigh`: `#242C34`
    - `primary`: `#FFB4AA` | `onPrimary`: `#561E18` | `primaryContainer`: `#FF6F61` | `onPrimaryContainer`: `#101418`
    - `secondary`: `#FDBA74` | `onSecondary`: `#4A2800` | `secondaryContainer`: `#693C00` | `onSecondaryContainer`: `#FFDCBE`
    - `tertiary`: `#FEF08A` | `onTertiary`: `#3A3000` | `tertiaryContainer`: `#544600` | `onTertiaryContainer`: `#FEF08A`
    - `onSurface`: `#E1E2E8` | `onSurfaceVariant`: `#C1C7CE`
    - `outline`: `#8B9198` | `outlineVariant`: `#41474D`
    - `error`: `#FFB4AB` | `onError`: `#690005`
2. **Typography (`Type.kt`):**
    - **Headlines & Display:** `Space Grotesk` (Bold/SemiBold). Enable `fontFeatureSettings = "tnum"` on numeric display styles.
    - **Body & UI Labels:** `Manrope` (Regular/Medium/SemiBold).
    - **Telemetry & Data Readouts:** `JetBrains Mono` with `fontFeatureSettings = "tnum"` mandatory on all styles so animated/live counters never shift horizontally.
    - Respect scalable `sp` units and ensure layouts survive 200% font scaling without clipping.
3. **Surface & Component Rendering:**
    - Do **not** use expensive blur shaders (`RenderEffect.createBlurEffect`) on scrolling lists or dynamic telemetry cards. Achieve depth using `surfaceContainer*` tonal layering, subtle translucent fills (`Color.White.copy(alpha = 0.04f)`), and `1.dp` `outlineVariant` borders.
    - Replace the broken Stitch mockup logo image in the top bar with a crisp vector bolt/wave icon paired with a `Space Grotesk` `"GOSWIFT"` wordmark.

---

## 6. Autonomous Execution Phases

Execute the following phases sequentially. Do not skip ahead until the current phase compiles cleanly.

### Phase 0: Repository Audit & Migration Plan
1. Inspect `settings.gradle.kts`, `gradle/libs.versions.toml`, `applications/kocolor`, and `applications/goswift`.
2. Print a concise **Migration Checklist** detailing:
    - Existing `goswift` modules to retain, rename, or create.
    - Version catalog aliases for Compose Adaptive (`currentWindowAdaptiveInfoV2`), Navigation 3, Health Connect, Hilt, and Room.
3. Run `./gradlew :applications:goswift:apps:mobile:tasks` (or equivalent compile check) to confirm baseline repository health.

### Phase 1: Design System & Base Telemetry Components (`apps/mobile/ui/theme`)
1. Implement `Color.kt`, `Type.kt`, `Shape.kt`, and `GoSwiftTheme.kt`.
2. Build reusable design system primitives:
    - `GoSwiftTopBar` (with fixed vector logo + wordmark and telemetry status pill)
    - `GoSwiftPrimaryButton` & `GoSwiftSecondaryButton` (minimum `48.dp` touch target)
    - `BiometricChip` (status indicator with `JetBrains Mono` readout)
    - `TelemetrySurfaceCard` (tonal container with `1.dp` subtle border)

### Phase 2: Deterministic Caffeine Engine & Room Persistence (`data/caffeine`)
1. Create Room `CaffeineIntakeEntity` (storing `timestampEpochMillis: Long`), `CaffeineIntakeDao`, and `GoSwiftDatabase`.
2. Implement `DeterministicCaffeineDecayEngine` and `CaffeineRepository` (exposing reactive `Flow<List<CaffeineIntakeRecord>>` and `Flow<PharmacokineticConfig>` via DataStore/preferences).
3. Implement `CaffeineAdvisoryAiService` with a deterministic local fallback generator (`DeterministicAdvisoryFallback`) so schedule summaries always work offline or without AI consent.
4. Wire all bindings via Hilt (`@Module` / `@InstallIn(SingletonComponent::class)`).

### Phase 3: Shared Health Connect Repository (`data/health`)
1. Implement `HealthConnectRepositoryImpl` backed by `HealthConnectClient`.
2. Implement `buildSafePermissionRequestSet()` with `HealthConnectFeatures.getFeatureStatus()` checks for `FEATURE_READ_HEALTH_DATA_HISTORY` and `FEATURE_READ_HEALTH_DATA_IN_BACKGROUND`.
3. Implement automatic 30-day lookback clamping when `isHistoryReadGranted == false`.
4. Provide a graceful mock/preview or empty-state fallback when `HealthConnectAvailability != AVAILABLE`.

### Phase 4: Feature Modules Implementation
1. **`features/home`:**
    - Implement `CaffeineRemainingRing` using Compose `Canvas` (multi-segmented arc showing current remaining caffeine relative to daily limit, plus sleep-threshold marker).
    - Display `"Estimated caffeine remaining"` in `JetBrains Mono`, `"Your configured daily limit"`, and projected sleep-ready clearance time.
    - Add quick-log buttons (Espresso 63mg, Cold Brew 160mg, Green Tea 35mg, Custom intake dialog).
2. **`features/schedule`:**
    - Build horizontal swipe telemetry timeline cards (`HorizontalPager` or `LazyRow` with snap behavior) showing hourly clearance decay and planned intakes.
    - Integrate `CaffeineAdvisoryAiService` card with explicit consent badge and deterministic fallback status.
3. **`features/healthsync`:**
    - Build the Health Connect permission management dashboard using `PermissionController.createRequestPermissionResultContract()`.
    - Display granular permission toggles (Sleep, HRV, Heart Rate, Activity, >30-Day History, Background Read) and connected data sources.
4. **`features/insights`:**
    - Build `SparklineWaveform` using Compose `Canvas` (smooth cubic bezier or polyline path with gradient fill and touch-scrubbing readout).
    - Correlate logged caffeine intake against permissioned Sleep duration, HRV (`rmssdMillis`), and Heart Rate trends.
5. **`features/settings`:**
    - Build controls for `dailyLimitMg` (100–800 mg), `halfLife` (3–9 hours), `sleepThresholdMg` (20–100 mg), On-Device/Cloud AI toggle, explicit Cloud AI Consent switch, and Health Connect quick link.

### Phase 5: Adaptive Navigation & App Wiring (`apps/mobile`)
1. Update `GoSwiftMainScreen.kt` to use `NavigationSuiteScaffold` modeled after `applications/kocolor`.
2. Query `currentWindowAdaptiveInfoV2()` to switch seamlessly between Bottom Navigation Bar on compact width and Navigation Rail on medium/expanded widths or tabletop postures.
3. Wire Jetpack Navigation 3 (`NavDisplay` / `rememberNavBackStack`) with entry decorators ordered strictly as:
   ```kotlin
   entryDecorators = listOf(
       rememberSaveableStateHolderNavEntryDecorator(),
       rememberViewModelStoreNavEntryDecorator()
   )
   ```
4. Configure Edge-to-Edge in `MainActivity.kt` (`enableEdgeToEdge()`) and verify system bar insets are consumed cleanly across all 5 destinations.

### Phase 6: Automated Verification & Build Sign-Off
1. **Unit Tests (`data/caffeine`):**
    - Verify exact decay math at `0h`, `2.5h`, `5h`, `10h`, and `>10 half-lives` cutoff.
    - Verify multi-dose superposition and closed-form `estimatedSleepReadyInstant`.
    - Verify DST transitions (`America/Los_Angeles` spring-forward and fall-back) and timezone shifts (`UTC` to `Asia/Tokyo`) do not corrupt `calculateRemainingMg()` or `dailyConsumedMg`.
2. **Unit Tests (`data/health`):**
    - Verify `SdkUnavailable` and `PermissionDenied` handling.
    - Verify 30-day clamping sets `isClampedTo30Days = true` without throwing when historical permission is absent.
    - Verify `buildSafePermissionRequestSet()` omits history/background permissions when `HealthConnectFeatures` reports unavailable.
3. **Unit Tests (`features/schedule` / Bounded AI):**
    - Verify `SanitizedCaffeineTrendPayload` contains zero references to `data/health` models.
    - Verify fallback to `AdvisorySummaryResult.FallbackDeterministic` when offline, unconsented, or when the AI provider throws an exception.
4. **Final Gradle Gate:**
    - Execute `./gradlew :applications:goswift:apps:mobile:assembleDebug` and run all unit test tasks under `:applications:goswift:...`.
    - Resolve all compilation errors, unresolved imports, and deprecation warnings before reporting completion.

