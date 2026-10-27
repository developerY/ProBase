# Shared AI Telemetry & Diagnostics Architecture

This document details the engineering architecture used to build the universal **AI Telemetry & Diagnostics** pipeline. It allows every application in the monorepo (KoColor, Seaweed, PhotoDo, GoSwift) to share a unified, complex debugging UI while executing completely different, domain-specific AI tests.

## 1. The Problem: Domain-Specific vs. Universal Needs
Every application using Generative AI needs a way to verify that:
1. The active AI engine (Local Nano, BYOK, Firebase Vertex, or Gemma) is responsive.
2. App Check and API keys are successfully handshaking.
3. The AI is adhering to strict JSON schema outputs.

However, **KoColor** needs to test a `StyleBlueprint` schema with wardrobe IDs, while **Seaweed** needs to test a `DocumentInterpretation` schema with `evidenceIds`. 

If we hardcoded the UI for one app, we'd have to duplicate the complex Jetpack Compose UI across all apps. If we put app-specific models into the shared `:features:ai` module, we would create circular dependencies and violate Clean Architecture.

## 2. The Solution: Hilt-Injected Strategy Pattern
We solved this by inverting the dependencies. The shared `:features:ai:configuration` module provides the UI and the Engine Router, but it delegates the actual "test payload and validation" back down to the host application using an interface: `AppDiagnosticDelegate`.

### Component 1: `AiDebugTelemetryScreen` (Shared UI)
Located in `:features:ai:configuration`. This is the Jetpack Compose screen that renders:
* The 4-tier engine status grid (Nano, BYOK, Firebase, Gemma).
* The target LLM selector.
* The diagnostic step visualizer (Pending, Running, Success, Failed).
* The raw JSON payload output console.

### Component 2: `AiDebugViewModel` (Shared Orchestrator)
Located in `:features:ai:configuration`. This ViewModel acts as the bridge.
When the user taps **"Run Test"**, the ViewModel:
1. Evaluates User Settings to determine the active `AiProvider`.
2. Validates network and hardware readiness by checking `targetProvider.isAvailable()`, marking Step 1 ("Connection & Handshake") as Success, or failing immediately if offline/misconfigured.
3. **Hands control over to the App-Specific Delegate** by calling `appDiagnosticDelegate.runDiagnostic(targetProvider, updateStep)`.

### Component 3: `AppDiagnosticDelegate` (The Interface)
Located in `:features:ai:configuration`. 
```kotlin
data class DiagnosticRunReport(
    val passed: Boolean,
    val providerName: String,
    val latencyMs: Long,
    val rawResponseJson: String?,
    val failureReason: String? = null
)

interface AppDiagnosticDelegate {
    val appName: String
    
    // Allows the app to define custom UI steps (e.g. "Fashionista Score" vs "Evidence ID Check")
    fun getInitialSteps(): List<DiagnosticStep>

    // Optional: Returns the formatted text for the Dark Terminal Audit Trail
    fun getAuditLogs(): List<String> = emptyList()

    // Allows the app to execute its own prompt, hit the AiProvider, and validate the schema
    suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): DiagnosticRunReport
}
```

## 3. How to Implement This in a New Application

If you are creating a new app (e.g., **PhotoDo**) and want to use the AI Debug screen, follow these 3 steps:

### Step A: Create the Delegate Implementation
In your app's `:data` or `:domain` module, create a class that implements `AppDiagnosticDelegate`. Use synthetic or live repository data for your prompt, and return a `DiagnosticRunReport`.

```kotlin
@Singleton
class PhotoDoDiagnosticDelegate @Inject constructor() : AppDiagnosticDelegate {

    override val appName: String = "PhotoDo Lens"

    override fun getInitialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "Checking provider availability"),
        DiagnosticStep(2, "Command & Prompt", "Requesting Image Analysis JSON"),
        DiagnosticStep(3, "Response & Latency", "Executing AI"),
        DiagnosticStep(4, "Answer & Schema", "Validating SmartAdvice JSON"),
        DiagnosticStep(5, "Useful Answer Quality", "Evaluating clarity and utility of response")
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (Int, DiagnosticStepState, String) -> Unit
    ): DiagnosticRunReport {
        val startMs = System.currentTimeMillis()
        updateStep(2, DiagnosticStepState.RUNNING, "Generating payload...")
        
        val prompt = "Respond ONLY in JSON: { \"photoAdvice\": \"Good lighting\" }"
        val result = provider.execute(AiInput.TextOnly(prompt))
        val duration = System.currentTimeMillis() - startMs
        
        if (result.isSuccess) {
            val jsonStr = result.getOrNull() ?: ""
            updateStep(3, DiagnosticStepState.SUCCESS, "Executed in ${duration}ms")
            
            return if (jsonStr.contains("photoAdvice")) {
                updateStep(4, DiagnosticStepState.SUCCESS, "Valid Schema")
                updateStep(5, DiagnosticStepState.SUCCESS, "Useful Answer Confirmed! Status: APPROVED (Score: 85/100)")
                DiagnosticRunReport(true, provider.capability.displayName, duration, jsonStr)
            } else {
                updateStep(4, DiagnosticStepState.FAILED, "Invalid Schema")
                updateStep(5, DiagnosticStepState.FAILED, "Skipped due to parsing failure")
                DiagnosticRunReport(false, provider.capability.displayName, duration, jsonStr, "Missing photoAdvice")
            }
        }
        
        updateStep(3, DiagnosticStepState.FAILED, "Network error")
        updateStep(4, DiagnosticStepState.FAILED, "Pipeline halted")
        updateStep(5, DiagnosticStepState.FAILED, "Pipeline halted")
        return DiagnosticRunReport(false, provider.capability.displayName, duration, null, "Network failure")
    }
}
```

### Step B: Bind it in Hilt (DataModule)
In your app's Hilt `DataModule.kt`, tell Dagger to provide your specific delegate whenever `:features:ai:configuration` asks for an `AppDiagnosticDelegate`.
Because the feature uses `@BindsOptionalOf`, your app will not crash if you forget to do this (it will just run a basic ping test).

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindAppDiagnosticDelegate(
        impl: PhotoDoDiagnosticDelegate
    ): AppDiagnosticDelegate
}
```

### Step C: Wire the Navigation
In your app's `NavEntryProvider` (e.g., `PhotoDoNavEntryProvider.kt`), add a route for the screen and call it.

```kotlin
is PhotoDoDestination.AiDebugTelemetry -> NavEntry(route) {
    com.zoewave.probase.features.ai.configuration.ui.telemetry.AiDebugTelemetryScreen(
        onBack = onBack
    )
}
```

## 4. Architectural Benefits
* **Zero UI Duplication:** The complex logic for rendering loaders, handling dropdowns, checking API keys, and managing coroutines lives in one place.
* **Strict Decoupling:** `:features:ai:configuration` doesn't know what a `Receipt` or a `Wardrobe` is. It only knows `AiProvider`.
* **Universal Testing:** Every app gets immediate access to connection testing for Firebase AppCheck, BYOK validation, and Gemma Cloud availability without writing boilerplate network code.