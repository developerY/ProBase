# ProBase AI Engine Architecture (`:features:ai`)

This document provides a comprehensive, up-to-date overview of the **ProBase Shared AI Engine** (`:features:ai`). This modular ecosystem provides an agnostic, secure, and extensible architecture for routing generative intelligence and vision requests across the entire ProBase monorepo (KoColor, SeaWeed, PhotoDo, and GoSwift).

---

## 1. Core Philosophy: The Local-First Split

Across all ProBase applications, we have decoupled "intelligence" into two distinct cognitive phases to protect privacy, eliminate LLM hallucinations, and drastically reduce token costs:

1. **Deterministic Reasoning (85% of load):** Executed locally via pure mathematical functions and embedded databases. This handles tasks like ML Kit bounding box projection (SeaWeed), wardrobe rotation penalization (KoColor), and pharmacokinetic decay formulas (GoSwift). LLMs are *never* used for math or structured database queries.
2. **Generative Synthesis (15% of load):** Executed via an `AiProvider` interface. The generative engine solves complex synthesis problems (aesthetic harmonization, receipt item categorization, or natural language extraction) using strictly trimmed, PII-scrubbed contexts derived from Phase 1.

---

## 2. Module Structure

The `:features:ai` directory acts as an umbrella for distinct, encapsulated libraries:

### A. `:features:ai:core`
Defines the universal contract for all generative AI backends in the monorepo.
* **`AiProvider` Interface**: Exposes a unified `suspend fun execute(input: AiInput): Result<String>`.
* **`AiProviderCapability`**: A metadata model describing the provider's limits (`maxInputTokens`, `isLocal`, timeout capabilities, and `displayName`).
* **`ByokAiProvider` (Bring Your Own Key)**: A fallback cloud provider that injects the user's personal Gemini API key directly to `generativelanguage.googleapis.com` if managed cloud limits are exhausted.

### B. `:features:ai:firebase`
Provides managed cloud execution via Firebase, ensuring 100% keyless operation.
* **`AppCheckInitializer`**: Dynamically installs Firebase App Check. In debug builds/emulators, it installs `DebugAppCheckProviderFactory`. In release builds, it installs `PlayIntegrityAppCheckProviderFactory` to cryptographically verify app authenticity.
* **`FirebaseAiProvider`**: Uses the `Firebase Vertex AI` SDK (`gemini-3.8-flash`) mapped directly to the `AiProvider` interface with `useLimitedUseAppCheckTokens = false`.
* **`GemmaRestFallbackProvider`**: Uses a secure Cloudflare/Firebase proxy to access open-weights models (e.g., Gemma 4) when Vertex quotas are constrained. Prompt execution logic has been genericized so it never injects app-specific schemas.

### C. `:features:ai:local`
Executes foundation models directly on device silicon.
* **`LocalNanoAiProvider`**: Interfaces with Android AICore (`Gemini Nano`) via Android 14+ system bindings. Ensures that sensitive operations can complete in Airplane Mode with zero network egress.

### D. `:features:ai:configuration`
Provides a universal settings interface and developer tooling that drops seamlessly into any ProBase app.
* **`AiConfigurationCard.kt`**: A Jetpack Compose UI component that allows users to toggle between **Gemma 4 Cloud**, **Firebase AI Logic**, **BYOK Cloud**, and **Local Nano**. It uses `MaterialTheme` semantic tokens (`surfaceContainer`, `onSurface`) so it visually matches KoColor's vibrant design or SeaWeed's minimalist dark mode automatically.
* **`AiConfigurationSettings`**: An interface hosted in `:core:data:repository`. Individual apps (like `UserSettingsRepositoryImpl` in SeaWeed) inject their specific persistence layer (Room/DataStore) into this interface so preferences persist globally.
* **`AiDebugTelemetryScreen.kt` & `AppDiagnosticDelegate`**: The crown jewel of the testing architecture. See Section 3 below.

### E. `:features:ai:vision` & `:features:ai:capture`
Handles the transition from pixels to text.
* **`SmartCaptureOrchestrator`**: Loads image URIs/bitmaps.
* **`LocalOcrEngine` (ML Kit)**: Extracts raw bounding boxes, element locations, and rotation angles from device images entirely offline.

---

## 3. The Universal Telemetry & Diagnostic Architecture

To test multiple AI networks (Local, Firebase, BYOK, Gemma) across multiple host apps (KoColor, SeaWeed, PhotoDo) without duplicating UI code, we built an injected strategy pattern.

1. **The Shared UI (`AiDebugTelemetryScreen.kt`)**: Displays a real-time status matrix of AI engines, live latency progress tracking, parsed JSON payload outputs, and a dark-mode "Audit Trail" terminal.
2. **The Controller (`AiDebugViewModel.kt`)**: Handles the network handshake (`targetProvider.isAvailable()`) and exposes a **"Run Matrix Test"** function that loops through all four providers sequentially to evaluate network/hardware readiness.
3. **The `AppDiagnosticDelegate`**: The host application injects its own **live repositories and DAOs** to test the system using actual user data states (e.g., active database items).
    *   **KoColor (`KoColorDiagnosticDelegate`)**: Injects the active `WardrobeRepository` and `CosmeticInventoryRepository`, retrieves the actual catalog (e.g., all 54 items), runs the real `PromptAssembler` and mathematical color-scoring engine, and verifies the returned JSON matches a `StyleBlueprint`.
    *   **Seaweed (`SeaweedDiagnosticDelegate`)**: Retrieves the latest parsed `LlmAnalysisBundle` metadata containing real PII-redacted `OcrElements` (like "GREENFIELD MARKET"), validates the schema via `PostLlmSemanticValidator`, and ensures that `evidenceId` constraints are perfectly bound back to the source image.
4. **Resilience (`@BindsOptionalOf`)**: In `:features:ai:configuration`, a default `DefaultPingDiagnosticDelegate` is provided using Dagger's `@BindsOptionalOf`. If an app (e.g., GoSwift) uses the AI configuration card but hasn't built a custom diagnostic delegate yet, the debug screen gracefully falls back to a simple `{"status":"ok"}` test ping without breaking the compilation graph.

---

## 4. Seaweed Pipeline Execution Integration

To ensure the AI engine executes flawlessly within Seaweed's financial constraints, the core modules integrate into a true 8-gear pipeline:

*   **Gear 1: Crypto Capture**: `TinkStreamingFileEncryptor` encrypts bitmaps as AES-256 GCM to disk before analysis begins.
*   **Gear 2 & 3: Homography & Layout**: ML Kit bounds are projected into $3 \times 3$ normalized matrices to reconstruct line groupings across curled thermal receipts.
*   **Gear 4 & 5: Heuristics & Deterministic Gate**: Candidate dates/amounts are checked via exact ISO 4217 scales (`StrictMoneyParser`). If the amounts mathematically reconcile to the total (e.g., Subtotal + Tax = Total), the document is **Locked** and skips the AI entirely.
*   **Gear 6: Bundle Trimmer**: If AI *is* required, `BundleTrimmer` strips all PII (Phones, Addresses, 8+ digit accounts) and slices the table layout into `< 12,288` byte chunks to protect token limits.
*   **Gear 7: Text-Only AI Validation**: `AiInterpretationRouter` routes the text-only JSON chunks to Firebase/Nano. Responses are rigorously verified by `PostLlmSemanticValidator` against original evidence IDs to guarantee zero hallucinations.
*   **Gear 8: Database & Envelope Intervention**: Validated events undergo Envelope limit checking and commit with HMAC-SHA256 digests.

**All of these pipeline transitions are logged live using the `PipelineGearLogger` (tag `SeaWeedEngine`), and execution runs entirely off the main thread (`Dispatchers.Default`) ensuring zero UI freezes.**
