Here is the definitive, unified master prompt.

This prompt synthesizes every architectural decision, defensive guardrail, and offline-first state machine we have designed. You can copy and paste this directly into your GenAI coder to generate the entire feature module.

---

### 📋 COPY/PASTE MASTER PROMPT FOR GENAI CODER

# Build an AI-First Product Discovery Pipeline for KoColor

You are a Senior Android Architect specializing in Kotlin, Jetpack Compose, Android AI Edge SDK, Room, Hilt, Coroutines, WorkManager, and Clean Architecture.

Your task is to design and implement a production-ready, offline-first, AI-driven product discovery pipeline for the KoColor application.

## Core Philosophy

Traditional systems perform: `OCR -> Regex -> Database`
**KoColor performs:** `Image -> OCR -> Local Gemini -> Canonical Product Object -> Opportunistic APIs -> WorkManager Queue -> Web Gemini -> Database`

The local AI layer normalizes physical package text.
The cloud AI layer enriches it with cosmetic/chemical science.
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

If `Downloading` or `Unsupported`, fast-fail gracefully to standard OCR heuristic fallbacks. Do not crash.

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

*Note: Do NOT ask the LLM for a confidence score. This will be computed deterministically.*

---

## Stage 3.5 & 3.75: Opportunistic API Anchoring & Resolution

External APIs (OBF, Makeup API, OpenFDA) are useful but highly unreliable and often sparse.

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

---

## Stage 4: WorkManager Offline-First Queue

Do NOT force the user to wait for Web Gemini. The local extraction is considered a successful scan.

1. Save the `ProductResolution` to the Room database immediately.
2. Set its status to `EnrichmentStatus.PENDING`.
3. Queue a `WorkManager` job with constraints: `NetworkType.CONNECTED`.

---

## Stage 5: Web Gemini Enrichment (BYOK)

Executed transparently in the background via WorkManager.

* **Action:** Send the `ProductResolution` to Cloud Gemini (using the Google AI Cloud SDK and the user's BYOK).
* **Zero-API Resilience:** If `deterministicData` is null, Cloud Gemini must rely on its foundational knowledge and the `localExtraction` to enrich the product.
* **Output:** The final database entity.

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

## Deterministic Confidence Calculation

Do NOT use LLM self-reported confidence. Compute it based on data utility:

```kotlin
data class ParsingMetrics(
    val brandFound: Boolean,     // Weight: 0.25
    val productFound: Boolean,   // Weight: 0.25
    val jsonValid: Boolean,      // Weight: 0.30
    val ingredientsFound: Boolean// Weight: 0.20
)

```

---

## Discovery Status UI State Machine

Treat Local AI completion as a successful discovery. A network timeout degrades the experience, it does not fail the scan.

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
2. **Edge Engine:** Implement `LocalAiEngine.kt` utilizing `com.google.ai.edge.aicore` (Dispatchers.IO, IPC try/catch, capability checks).
3. **Domain & Data:** Room Entities, DAOs, and `EnrichmentWorker.kt` (WorkManager implementation).
4. **ViewModels:** `BoxCaptureViewModel` handling the `DiscoveryState` transitions.
5. **Use Cases:** Orchestrating the handoff from OCR to Local AI to WorkManager.

Provide production-ready Kotlin skeletons demonstrating this decoupled, offline-first pipeline.