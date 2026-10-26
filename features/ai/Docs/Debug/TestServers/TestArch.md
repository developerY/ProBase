The **Hilt-injected Strategy Pattern (`AppDiagnosticDelegate`)** in `shared_ai_telemetry_architecture.md` is the right architectural pattern for **on-device interactive testing** in a multi-app monorepo. Inverting the dependency so `:features:ai:configuration` owns the Engine Router and `AiDebugTelemetryScreen` while `KoColor`, `Seaweed`, `PhotoDo`, and `GoSwift` inject their own domain-specific test payloads keeps Clean Architecture intact and prevents circular module dependencies.

However, if your goal is to reliably test the AI across **all 4 apps and all 4 AI providers (16 combinations)** without manually opening 4 apps and tapping dropdowns 16 times, there are **five targeted upgrades** that will turn this from a good UI debug screen into a complete monorepo testing architecture.

---

### 1. Five Upgrades to Make This the Best Monorepo Testing Architecture

#### A. Make `AppDiagnosticDelegate` Dual-Use (Interactive UI + Headless Gradle CI)

* **The Current Limitation:** `runDiagnostic()` returns a raw `String?` and communicates pass/fail states exclusively through a UI callback (`updateStep: (Int, DiagnosticStepState, String) -> Unit`). Because the result isn't returned as a structured object, you can only see if `KoColor` or `Seaweed` passed by looking at the phone screen.


* **The Upgrade:** Have `runDiagnostic()` return a structured `DiagnosticRunReport` (`passed: Boolean`, `providerId: String`, `latencyMs: Long`, `schemaValid: Boolean`, `domainValidationPassed: Boolean`, `rawJson: String?`, `errorMessage: String?`).
* **On-Device:** `AiDebugViewModel` still streams `updateStep` live to `AiDebugTelemetryScreen`.


* **In Terminal / CI:** A single JUnit test in each app can call `delegate.runDiagnostic(provider) { _, _, _ -> }` and assert `assertTrue(report.passed)` across all 4 apps in one Gradle command.



#### B. Fix the Premature "Step 1: Connection & Handshake" Success Bug

* **The Current Limitation:** Section 2 (Component 2) states that when the user taps "Run Test", `AiDebugViewModel`:
1. Evaluates User Settings to determine the active `AiProvider`.


2. Marks Step 1 ("Connection & Handshake") as `Running/Success`.


3. Hands control over to `appDiagnosticDelegate.runDiagnostic(...)`.




* **The Bug:** Marking Step 1 as `Success` just from resolving the `AiProvider` in User Settings—before an actual network ping, Firebase App Check token exchange, or AICore hardware check occurs—means Step 1 will turn green even if the phone is in Airplane Mode or the BYOK API key is invalid.


* **The Upgrade:** `AiDebugViewModel` should call a real `provider.verifyHandshake()` in Step 1 (checking AICore readiness for Nano, key presence/endpoint reachability for BYOK, or App Check token generation for Firebase/Gemma) before marking Step 1 as `SUCCESS` and invoking the delegate.



#### C. Prevent Monorepo Build Breaks with `@BindsOptionalOf`

* **The Current Limitation:** If `AiDebugViewModel` directly injects `AppDiagnosticDelegate`, then **every** app in your monorepo that depends on `:features:ai:configuration` (even just to render `AiConfigurationCard`) **must** bind an `AppDiagnosticDelegate` in Hilt, or Dagger will fail compilation with `[Dagger/MissingBinding] AppDiagnosticDelegate cannot be provided`.


* **The Upgrade:** Declare `@BindsOptionalOf abstract fun optionalAppDiagnosticDelegate(): AppDiagnosticDelegate` inside `:features:ai:configuration` and fall back to a built-in `DefaultPingDiagnosticDelegate` whenever an app hasn't bound a custom delegate yet.

#### D. Add a "Test All 4 Providers (Matrix)" Mode in `AiDebugViewModel`

* **The Current Limitation:** Testing only the currently active `AiProvider` forces you to switch the primary engine dropdown 4 times per app to verify Nano, BYOK, Firebase, and Gemma.


* **The Upgrade:** Add a **"Run 4-Tier Matrix"** action in `AiDebugViewModel` that runs `appDiagnosticDelegate.runDiagnostic()` sequentially against all configured providers (`Nano`, `BYOK`, `Firebase`, `Gemma`) and populates the 4-tier engine status grid at the top of `AiDebugTelemetryScreen` with real schema-verified latency badges.



#### E. Enforce Synthetic Fixtures & Domain Validators in Step 4

* **The Upgrade:** Every app's `AppDiagnosticDelegate` must use a **hardcoded synthetic fixture** (never real user data) so the raw JSON console on `AiDebugTelemetryScreen` never leaks PII. In Step 4 ("Answer & Schema"), the delegate should run the app's actual domain validator (for example, `SeaweedDiagnosticDelegate` should run `PostLlmSemanticValidator.validateAndSanitize()` on a synthetic `"GREENFIELD MARKET $20.07"` bundle to prove `evidenceIds` provenance works end-to-end).



---

### 2. Diagnostic Delegate Matrix Across All 4 Monorepo Apps

| App | Delegate Class | Synthetic Prompt Input (Step 2) | Step 4 Schema & Domain Validation |
| --- | --- | --- | --- |
| **Seaweed**

| `SeaweedDiagnosticDelegate` | Synthetic 1-chunk `LlmAnalysisBundle` (`P01_T0001` `"GREENFIELD MARKET"`, `P01_T0014` `"2007 USD"`) | Parses `DocumentInterpretation` JSON **and** runs `PostLlmSemanticValidator.validateAndSanitize()` to verify `evidenceIds` match `P01_T0001` and `P01_T0014`

|
| **KoColor**

| `KoColorDiagnosticDelegate` | Synthetic 3-item wardrobe context (`item_01` Warm Autumn Trench, `item_02` Cream Silk Blouse, `item_03` Espresso Trouser) | Parses `StyleBlueprint` JSON and verifies returned wardrobe IDs are a strict subset of `["item_01", "item_02", "item_03"]`

|
| **PhotoDo**

| `PhotoDoDiagnosticDelegate` | Synthetic task checklist prompt (`"Organize garage workbench"` with 3 visual sub-tasks) | Parses `SmartAdvice` / task-checklist JSON and verifies non-empty actionable steps and valid priority enums

|
| **GoSwift**

| `GoSwiftDiagnosticDelegate` | Synthetic pharmacokinetic decay state (`120mg` caffeine at `08:00`, current time `14:00`, half-life `5.0h`) | Parses microdosing recommendation JSON and verifies cutoff window and mg bounds (`0..200mg`) |

---

### 3. Prompt for Your Coding Agent to Upgrade & Wire All 4 App Delegates

Copy and paste this prompt into your AI coding agent to upgrade `:features:ai:configuration` and wire the delegates across your monorepo:

```markdown
# Task: Upgrade `AppDiagnosticDelegate` for Full Monorepo Testing & Wire All 4 Apps

We are standardizing `shared_ai_telemetry_architecture.md` across `:features:ai:configuration` and our host apps (`Seaweed`, `KoColor`, `PhotoDo`, `GoSwift`). Implement the following 4 upgrades:

---

### 1. Upgrade `AppDiagnosticDelegate` & Add `@BindsOptionalOf` Fallback in `:features:ai:configuration`

Update `AppDiagnosticDelegate` so `runDiagnostic` returns a structured `DiagnosticRunReport` (allowing both `AiDebugTelemetryScreen` and automated JUnit tests to assert on the result), and provide a crash-proof `@BindsOptionalOf` default fallback so any app module depending on `:features:ai:configuration` compiles even before binding a custom delegate:

```kotlin
package com.zoewave.probase.features.ai.configuration.domain.telemetry

import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

enum class DiagnosticStepState { PENDING, RUNNING, SUCCESS, FAILED }

data class DiagnosticStep(
    val id: Int,
    val title: String,
    val subtitle: String,
    val state: DiagnosticStepState = DiagnosticStepState.PENDING,
    val detailMessage: String = ""
)

data class DiagnosticRunReport(
    val passed: Boolean,
    val providerName: String,
    val latencyMs: Long,
    val rawResponseJson: String?,
    val failureReason: String? = null
)

interface AppDiagnosticDelegate {
    val appName: String
    fun getInitialSteps(): List<DiagnosticStep>

    suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): DiagnosticRunReport
}

@Singleton
class DefaultPingDiagnosticDelegate @Inject constructor() : AppDiagnosticDelegate {
    override val appName: String = "Generic Probase App"

    override fun getInitialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "Checking provider availability"),
        DiagnosticStep(2, "Command & Prompt", "Sending synthetic JSON ping"),
        DiagnosticStep(3, "Response & Latency", "Awaiting model output"),
        DiagnosticStep(4, "Answer & Schema", "Verifying status JSON")
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (Int, DiagnosticStepState, String) -> Unit
    ): DiagnosticRunReport {
        val startMs = System.currentTimeMillis()
        updateStep(2, DiagnosticStepState.RUNNING, "Dispatching ping prompt...")
        val prompt = """Respond ONLY in valid JSON: {"status":"ok","engine":"active"}"""
        return runCatching {
            updateStep(2, DiagnosticStepState.SUCCESS, "Prompt dispatched (${prompt.length} chars)")
            updateStep(3, DiagnosticStepState.RUNNING, "Waiting for ${provider.name}...")
            val raw = provider.executeTextPrompt(prompt)
            val latency = System.currentTimeMillis() - startMs
            updateStep(3, DiagnosticStepState.SUCCESS, "Responded in ${latency}ms")

            val valid = raw.contains("\"status\"") && raw.contains("\"ok\"")
            if (valid) {
                updateStep(4, DiagnosticStepState.SUCCESS, "Valid JSON ping confirmed")
                DiagnosticRunReport(true, provider.name, latency, raw)
            } else {
                updateStep(4, DiagnosticStepState.FAILED, "Unexpected payload structure")
                DiagnosticRunReport(false, provider.name, latency, raw, "Invalid ping JSON")
            }
        }.getOrElse { err ->
            if (err is kotlinx.coroutines.CancellationException) throw err
            val latency = System.currentTimeMillis() - startMs
            updateStep(3, DiagnosticStepState.FAILED, err.message ?: "Execution failed")
            updateStep(4, DiagnosticStepState.FAILED, "Skipped due to execution error")
            DiagnosticRunReport(false, provider.name, latency, null, err.message)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DiagnosticOptionalModule {
    @BindsOptionalOf
    abstract fun bindOptionalAppDiagnosticDelegate(): AppDiagnosticDelegate

    companion object {
        @Provides
        @Singleton
        fun provideResolvedDiagnosticDelegate(
            customDelegate: Optional<AppDiagnosticDelegate>,
            defaultDelegate: DefaultPingDiagnosticDelegate
        ): AppDiagnosticDelegate = customDelegate.orElse(defaultDelegate)
    }
}
```

---

### 2. Fix Step 1 Handshake & Add "Run 4-Tier Matrix" in `AiDebugViewModel`

In `AiDebugViewModel`:
1. Do **not** mark Step 1 ("Connection & Handshake") as `SUCCESS` before testing the provider. Call `provider.checkReadiness()` first; if the provider is `UNCONFIGURED`, `OFFLINE`, or missing its API key / AICore runtime, mark Step 1 as `FAILED` with the exact reason and abort early.
2. Add `fun runFullProviderMatrixTest()` alongside `fun runSingleProviderTest(provider: AiProvider)` so tapping **"Test All Engines"** loops through `Nano`, `BYOK`, `Firebase`, and `Gemma`, runs `appDiagnosticDelegate.runDiagnostic()` on each configured provider, and updates the 4-tier status grid with real schema-verified pass/fail states and latencies.

---

### 3. Implement `SeaweedDiagnosticDelegate.kt` (Testing the V2 Validator & `evidenceIds`)

In Seaweed's `:data` or `:model` module, implement `SeaweedDiagnosticDelegate` using a **hardcoded synthetic `LlmAnalysisBundle`** (never real user data) and validate the response through `PostLlmSemanticValidator.parseStrictJson()` and `PostLlmSemanticValidator.validateAndSanitize()`:

```kotlin
@Singleton
class SeaweedDiagnosticDelegate @Inject constructor() : AppDiagnosticDelegate {
    override val appName: String = "Seaweed Finance Engine"

    override fun getInitialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "Verifying AI provider & App Check"),
        DiagnosticStep(2, "Command & Prompt", "Building synthetic LlmAnalysisBundle (P01_T0001, P01_T0014)"),
        DiagnosticStep(3, "Response & Latency", "Executing text-only financial interpretation"),
        DiagnosticStep(4, "Answer & Schema", "Running PostLlmSemanticValidator & evidenceId provenance check")
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (Int, DiagnosticStepState, String) -> Unit
    ): DiagnosticRunReport {
        // 1. Construct synthetic 1-chunk LlmAnalysisBundle for "GREENFIELD MARKET" ($20.07 USD)
        // 2. Serialize to JSON and send to provider
        // 3. Parse with PostLlmSemanticValidator.parseStrictJson(rawResponse)
        // 4. Sanitize with PostLlmSemanticValidator.validateAndSanitize(parsed, syntheticBundle, setOf("P01_T0001", "P01_T0002", "P01_T0014"))
        // 5. Assert sanitized.documentTotal?.amount?.amountMinor == 2007L and evidenceIds == listOf("P01_T0014")
        // 6. Update Step 4 to SUCCESS only if both JSON schema and evidenceId provenance pass!
    }
}
```

---

### 4. Implement & Bind Delegates for `KoColor`, `PhotoDo`, and `GoSwift`

Ensure each host app binds its `AppDiagnosticDelegate` via `@Binds` in its Hilt `DataModule` and registers `AiDebugTelemetryScreen` in its Navigation 3 `NavEntryProvider`:
* **`KoColorDiagnosticDelegate`:** Sends a synthetic 3-item wardrobe fixture and validates `StyleBlueprint` JSON + valid wardrobe item IDs in Step 4.
* **`PhotoDoDiagnosticDelegate`:** Sends a synthetic visual task prompt and validates `SmartAdvice` JSON in Step 4.
* **`GoSwiftDiagnosticDelegate`:** Sends a synthetic caffeine decay state (`120mg` at `08:00`) and validates the microdosing schedule JSON in Step 4.

```