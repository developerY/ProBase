# Redo GoSwift Application - Stitch UI Migration & MAD Upgrade

This specification is **ready to execute**. Every architectural, regulatory, and privacy edge case is tightly bounded for an implementation agent based on `AGENT_BUILD_SPEC.md` and `DeterministicCaffeineDecayEngine.md`.

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