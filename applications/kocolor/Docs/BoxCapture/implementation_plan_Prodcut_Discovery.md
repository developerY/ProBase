# Implementation Plan: AI-First Product Discovery Pipeline

Implement a local-first, cloud-enriched discovery pipeline utilizing Gemini Nano (local), Room 3 (persistence), and Cloud Gemini (WorkManager enrichment).

## Proposed Changes

### 1. Local AI Refinement (`:features:ai:local`)
- Update `LocalAiEngine.kt` to implement the `NanoState` machine.
- Enhance `LocalStandardizedData` schema to include `claims` and `directions`.
- Implement safe JSON extraction via Regex to bypass LLM markdown artifacts.

### 2. Data Layer Evolution (Room 3 & WorkManager)
- Define `ProductEntity` and `ProductResolution` in the domain layer.
- Set up Room 3 (`androidx.room3`) database infrastructure.
- Implement `@ColumnTypeConverter` utilizing `kotlinx.serialization` for complex types.
- Create `EnrichmentWorker.kt` for background Web Gemini processing.

### 3. Orchestration & Use Cases
- Implement `ResolveProductUseCase.kt` to merge Local AI output with opportunistic API data.
- Implement deterministic confidence calculation (ParsingMetrics).

### 4. UI State Integration
- Update `BoxCaptureViewModel` to drive the `DiscoveryState` state machine.
- Refactor UI components to handle `EnrichmentDeferred` and `FullyEnriched` states.

## Verification Plan

### Automated Tests
- `LocalAiEngineTest`: Verify Regex-based JSON extraction.
- `ResolveProductUseCaseTest`: Verify confidence score weighting.
- `Room3ConverterTest`: Ensure serialization/deserialization of lists and enums.

### Manual Verification
- Deploy to device and verify "Local AI" spinner on Discovery Health screen.
- Verify WorkManager triggers Cloud Gemini enrichment when network becomes available.
- Inspect Room database using App Inspection to verify standardized data persistence.

---

Instructions:
Here is the definitive, fully integrated master prompt.

It synthesizes every architectural pivot we have made today—from the `com.google.ai.edge.aicore` capability check, to the deterministic API resolution, to the strict Room 3.0.0-rc01 bleeding-edge requirements.

Drop this into your GenAI coder to generate the complete, production-ready feature module and close out this Friday sprint.

---

### 📋 COPY/PASTE MASTER PROMPT FOR GENAI CODER

# Build an AI-First Product Discovery Pipeline for KoColor

You are a Senior Android Architect specializing in Kotlin, Jetpack Compose, Android AI Edge SDK, Room 3, Hilt, Coroutines, WorkManager, and Clean Architecture.

Your task is to design and implement a production-ready, offline-first, AI-driven product discovery pipeline for the KoColor application.

## Core Philosophy

Traditional systems perform: `OCR -> Regex -> Database`
**KoColor performs:** `Image -> OCR -> Local Gemini -> Canonical Product Object -> Opportunistic APIs -> Product Resolution -> Room 3 (Pending) -> WorkManager Queue -> Web Gemini -> Database`

The local AI layer normalizes physical package text (identity).
The cloud AI layer enriches it with cosmetic/chemical science (knowledge).
The system must be privacy-first, offline-capable, and progressively enhanced.

---

## Stage 1 & 2: Capture and OCR

* **Input:** `Bitmap` (from CameraX/Gallery).
* **Action:** Run local ML Kit Text Recognition.
* **Output:** `OcrResult(val rawText: String)`

---

## Stage 3: Local Gemini (The Normalization Engine)

The local model translates messy OCR into a structured JSON schema. It is NOT responsible for market knowledge, reviews, or skin science.

**CRITICAL IMPLEMENTATION RULES:**

1. **System Delegation:** You MUST use the Android system service via `com.google.ai.edge.aicore:aicore`. Do NOT use the cloud SDK (`client.generativeai`) for the local module. No API keys are allowed for the local model.
2. **Context Protection:** Truncate OCR text before prompting (e.g., `rawText.take(4000)`) to prevent NPU token overflow.
3. **JSON Sanitization:** Do not use `.substringAfter`. Use Regex (e.g., `Regex("""\{[\s\S]*\}""")`) to safely extract JSON, ignoring markdown fences.

**Hardware Capability State Machine:**
Implement a strict capability check *before* executing IPC:

```kotlin
sealed interface NanoState {
    data object Available : NanoState
    data object Downloading : NanoState
    data object Unsupported : NanoState
}

```

If `Downloading` or `Unsupported`, fast-fail gracefully to standard OCR heuristic fallbacks. Do not crash or block the UI.

**Output Schema:**

```kotlin
@Serializable
data class LocalStandardizedData(
    val brand: String? = null,
    val productName: String? = null,
    val category: String? = null,
    val size: String? = null,
    val ingredients: List<String> = emptyList(),
    val claims: List<String> = emptyList(),
    val directions: String? = null
)

```

*Note: Do NOT ask the LLM for a confidence score.*

---

## Stage 3.5 & 3.75: Opportunistic API Anchoring & Resolution

External APIs (Makeup API, OpenFDA) are useful but highly unreliable and often sparse.

* **Action:** Query deterministic APIs using the `LocalStandardizedData.brand` and `productName`. Apply strict network timeouts.
* **Product Resolution:** Merge the local extraction and nullable API metadata into a single source of truth.

```kotlin
data class ProductResolution(
    val canonicalBrand: String,
    val canonicalProductName: String,
    val localExtraction: LocalStandardizedData,
    val deterministicData: DeterministicApiMetadata?, // May be null if APIs fail/404
    val deterministicConfidence: Float // Computed via ParsingMetrics, NOT by LLM
)

```

**Deterministic Confidence Calculation:**
Compute `deterministicConfidence` based on data utility, not an LLM guess:

* Brand Found: +0.25
* Product Found: +0.25
* JSON Valid: +0.30
* Ingredients Found: +0.20

---

## Stage 4: Room 3 Database & WorkManager Queue

Do NOT force the user to wait for Web Gemini. The local extraction is considered a successful scan.

1. Save the `ProductResolution` to the database immediately with `EnrichmentStatus.PENDING`.
2. Queue a `WorkManager` job with `NetworkType.CONNECTED` constraints.

🚨 **CRITICAL TECH STACK DIRECTIVE: ROOM 3.0.0-rc01** 🚨
You must strictly adhere to the new Room 3 API surface.

* **DO NOT** use `androidx.room.*`. You must use `androidx.room3.*`.
* **DO NOT** use `@TypeConverter` or `@TypeConverters`. You must use `@ColumnTypeConverter` and `@ColumnTypeConverters`.
* Use `kotlinx.serialization.json.Json` to write `@ColumnTypeConverter` functions that flatten complex objects (`List<String>`, `Map<String, String>`, Enums) for SQLite. Wrap enum deserialization in `try/catch` blocks for safety.

---

## Stage 5: Web Gemini Enrichment (BYOK)

Executed transparently in the background via WorkManager.

* **Action:** Send the `ProductResolution` to Cloud Gemini (using the Google AI Cloud SDK and the user's BYOK).
* **Zero-API Resilience:** If `deterministicData` is null, Cloud Gemini must rely on its foundational knowledge and the `localExtraction` to enrich the product.

```kotlin
data class ProductEntity(
    val brand: String,
    val productName: String,
    val category: String?,
    val ingredients: List<String>,
    // Web Gemini Enrichments:
    val skinConcerns: List<String>,
    val benefits: List<String>,
    val ingredientDescriptions: Map<String, String>,
    val enrichmentStatus: EnrichmentStatus
)

```

---

## Discovery Status UI State Machine

Treat Local AI completion as a successful discovery. A network timeout degrades the experience; it does not fail the scan.

```kotlin
sealed interface DiscoveryState {
    data object Processing : DiscoveryState
    
    // User sees: "✓ Product Identified. Attempting enrichment..."
    data class LocalSuccess(val product: LocalStandardizedData) : DiscoveryState 
    
    // User sees: "✓ Product Identified. Additional intelligence unavailable offline."
    data class EnrichmentDeferred(val product: LocalStandardizedData) : DiscoveryState 
    
    // User sees: "✓ Product Identified ✓ Intelligence Retrieved"
    data class FullyEnriched(val product: ProductEntity) : DiscoveryState 
    
    data class Failed(val reason: String) : DiscoveryState
}

```

---

## Technical Deliverables Required

Generate the following assuming a modern MAD stack:

1. **Architecture Setup:** Define the Clean Architecture layers.
2. **Edge Engine:** Implement `LocalAiEngine.kt` utilizing `com.google.ai.edge.aicore` (Dispatchers.IO, IPC try/catch, capability state machine).
3. **Domain & Data (Room 3):** Entities, DAOs, and Converters utilizing `androidx.room3.*` and `kotlinx.serialization`.
4. **Resolution Use Case:** Orchestrate the handoff from OCR to Local AI to Opportunistic APIs, calculating the deterministic confidence score.
5. **WorkManager:** Implement `EnrichmentWorker.kt` to handle the background Web Gemini BYOK sync.
6. **ViewModels:** `BoxCaptureViewModel` handling the `DiscoveryState` UI transitions.

Provide clean, offline-first, production-ready Kotlin skeletons.
