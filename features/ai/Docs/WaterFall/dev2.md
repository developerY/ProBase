# Implementation Plan: Dynamic Capability Router & Adaptive Token Optimization Engine

This document details the refined architecture for KoColor’s **Capability-Aware AI Router** and **Adaptive Token Optimization Subsystem**, incorporating context-aware serialization, adaptive Top-$K$ pruning, unified failure handling, and exact request preflight verification.

---

## 1. Architectural Principles

* **The Local-First Cognitive Split (Design Target):** Deterministic filtering, environmental constraints, laundry/availability states, rotation penalties, and candidate scoring are executed entirely on-device. Generative AI is reserved strictly for high-order aesthetic reasoning and silhouette coordination.
* **Capability-Driven, Not Tier-Hardcoded:** Instead of a fixed sequence, the system detects all available providers on the device, ranks them by capability, and routes dynamically.
* **Adaptive Context Fitting:** If a candidate request exceeds a provider’s token budget, the engine adapts by trimming optional metadata fields and reducing Top-$K$ candidates before failing over to another provider.
* **Type-Safe Privacy Invariant:** Raw camera imagery may be processed by on-device providers capable of local multimodal input, but raw images/bitmaps never cross the network boundary to cloud providers.

---

## 2. System Architecture

```
                    ┌─────────────────────────┐
                    │      Style Request      │
                    └────────────┬────────────┘
                                 ↓
                    ┌─────────────────────────┐
                    │   Capability Router     │
                    │ (Detect & Rank Providers)│
                    └────────────┬────────────┘
                                 ↓
                    ┌─────────────────────────┐
                    │   Local Candidate RAG   │
                    │  (Hard Prune & Score)   │
                    └────────────┬────────────┘
                                 ↓
            ┌─────────────────────────────────────────┐
            │       Adaptive Preflight Loop           │
            │                                         │
            │  1. Build Context-Aware Manifest        │
            │  2. Assemble EXACT Final Prompt         │
            │  3. countTokens(exactRequest)           │
            │                                         │
            │  Fits budget? ───► NO ──► Reduce Top-K  │
            │       │                   / Strip fields│
            │      YES                        │       │
            └───────┼─────────────────────────┴───────┘
                    ↓
            ┌─────────────────────────┐
            │    Provider Execution   │
            └────────────┬────────────┘
                         │
             Failure / Timeout?
              /             \
            YES              NO
             ↓                ↓
     Select Next Provider  Return Result & Cache
             ↓
     Deterministic Fallback

```

---

## 3. Detailed Component Specifications

### Phase 1: Provider Abstraction & Unified Failure Model

#### `AiProvider.kt` & `AiProviderCapability.kt`

* **Location:** `:features:ai:core`
* **Purpose:** Decouple provider implementations from routing logic.

```kotlin
data class AiProviderCapability(
    val id: String,
    val displayName: String,
    val maxInputTokens: Int,
    val maxOutputTokens: Int,
    val timeoutMillis: Long,
    val initialTopK: Int = 16,
    val minTopK: Int = 6,
    val isLocal: Boolean,
    val supportsLocalImageIngestion: Boolean = false
)

sealed interface AiExecutionFailure {
    data class ContextLimitExceeded(val details: String) : AiExecutionFailure
    data class QuotaExceeded(val retryAfterMillis: Long? = null) : AiExecutionFailure
    data class Timeout(val elapsedMillis: Long) : AiExecutionFailure
    data class NetworkUnavailable(val reason: String) : AiExecutionFailure
    data class ProviderUnavailable(val reason: String) : AiExecutionFailure
    data class Unknown(val throwable: Throwable) : AiExecutionFailure
}

interface AiProvider {
    val capability: AiProviderCapability
    suspend fun isAvailable(): Boolean
    suspend fun countTokens(request: StylePromptRequest): Int
    suspend fun execute(request: StylePromptRequest): Result<StyleBlueprint>
}

```

---

### Phase 2: Local Candidate Retrieval (RAG)

#### `WardrobeCandidateFilter.kt`

* **Location:** `:applications:kocolor:data`
* **Purpose:** Reduces full wardrobe inventory ($N=300+$) to an initial ranked candidate pool.
* **Stage 1: Hard Pruning (Deterministic):**
* **Availability:** Exclude garments marked as in laundry, altered, or archived.
* **Weather Gating:** Filter garment thermal weight against ambient temperature.
* **Rotation Penalty:** Apply exclusion or severe score reduction if worn within the last 3 days.


* **Stage 2: Soft Scoring & Ranking:**
* Calculate alignment score against `AppearanceTelemetry` (undertone match, contrast delta).
* Score contextual relevance against `userIntent` keywords and occasion tags.


* **Stage 3: Pool Truncation:**
* Expose a parameterized method `getCandidates(limit: Int): List<ClothingItem>` to support dynamic resizing during token preflight.



---

### Phase 3: Context-Aware Semantic Compression

#### `CompactManifestSerializer.kt`

* **Location:** `:applications:kocolor:data`
* **Purpose:** Serialize wardrobe candidates into dense, minimal tuples where every field earns its place.

```kotlin
enum class SerializationDetailLevel {
    MINIMAL,   // [id|category|name|hex]
    BALANCED,  // [id|category|name|hex|temperature|depth]
    EXPANDED   // [id|category|name|hex|temperature|depth|material]
}

class CompactManifestSerializer {
    fun serialize(
        items: List<ClothingItem>,
        detailLevel: SerializationDetailLevel = SerializationDetailLevel.BALANCED
    ): String {
        return items.joinToString(separator = "\n") { item ->
            when (detailLevel) {
                SerializationDetailLevel.MINIMAL ->
                    "[${item.id}|${item.category}|${item.name}|${item.hex}]"
                SerializationDetailLevel.BALANCED ->
                    "[${item.id}|${item.category}|${item.name}|${item.hex}|${item.temperature}|${item.depth}]"
                SerializationDetailLevel.EXPANDED ->
                    "[${item.id}|${item.category}|${item.name}|${item.hex}|${item.temperature}|${item.depth}|${item.material}]"
            }
        }
    }
}

```

---

### Phase 4: Exact Request Preflight & Adaptive Fit Engine

#### `PromptAssembler.kt` & `StyleSimulatorEngine.kt`

* **Location:** `:applications:kocolor:data`
* **Preflight Rule:** Never measure just the manifest. Token budgeting must evaluate the **exact final request**, including system prompt, instructions, telemetry, intent, weather, and schema requirements.

```kotlin
class StyleSimulatorEngine(
    private val candidateFilter: WardrobeCandidateFilter,
    private val serializer: CompactManifestSerializer,
    private val promptAssembler: PromptAssembler,
    private val capabilityRouter: CapabilityRouter,
    private val fallbackEngine: DeterministicStyleEngine
) {
    suspend fun generateBlueprint(requestContext: StyleRequestContext): StyleBlueprint {
        val providers = capabilityRouter.getRankedAvailableProviders()
        
        for (provider in providers) {
            val fitResult = adaptContextToProvider(provider, requestContext) ?: continue
            
            val executionResult = provider.execute(fitResult)
            if (executionResult.isSuccess) {
                return executionResult.getOrThrow()
            }
            // If failed, loop continues to next provider in ranked list
        }
        
        // Indestructible baseline
        return fallbackEngine.generate(requestContext)
    }

    private suspend fun adaptContextToProvider(
        provider: AiProvider,
        context: StyleRequestContext
    ): StylePromptRequest? {
        val cap = provider.capability
        var currentK = cap.initialTopK
        var detailLevel = SerializationDetailLevel.EXPANDED

        while (currentK >= cap.minTopK) {
            val candidates = candidateFilter.getCandidates(limit = currentK)
            val manifest = serializer.serialize(candidates, detailLevel)
            
            val candidatePrompt = promptAssembler.buildExactRequest(
                context = context,
                compactManifest = manifest
            )
            
            val tokenCount = provider.countTokens(candidatePrompt)
            if (tokenCount <= cap.maxInputTokens) {
                return candidatePrompt
            }

            // Step-down strategy: First drop optional fields, then reduce Top-K
            if (detailLevel == SerializationDetailLevel.EXPANDED) {
                detailLevel = SerializationDetailLevel.BALANCED
            } else if (detailLevel == SerializationDetailLevel.BALANCED) {
                detailLevel = SerializationDetailLevel.MINIMAL
            } else {
                currentK -= 2 // Step down candidate count
                detailLevel = SerializationDetailLevel.BALANCED // Reset detail for smaller K
            }
        }
        return null // Cannot fit within provider budget, proceed to next provider
    }
}

```

---

## 4. Verification & Testing Matrix

### 1. Adaptive Token Fitting Tests

* **Over-Budget Step-Down:** Simulate a candidate list that exceeds budget at $K=16$. Verify that the engine automatically steps down to $K=12$, adjusts serialization detail, and successfully executes without switching providers prematurely.
* **Exact Prompt Token Count:** Assert that `countTokens` is executed on the final assembled `StylePromptRequest` rather than raw intermediate strings.

### 2. Privacy Boundary Verification

* **Network Payload Audit:** Mock HTTP/gRPC outbound requests for cloud providers and assert:
* `StyleTelemetry` values are present (`temperature`, `depth`, `contrast`).
* No image references, Base64 strings, `byte[]`, or URI schemes exist in the payload.



### 3. Fault Resilience & Waterfall Cascade

* **Simulated Failures:** Inject `AiExecutionFailure.Timeout` and `AiExecutionFailure.ContextLimitExceeded` sequentially.
* **Assertion:** Verify graceful step-down from primary on-device model $\to$ Cloud Firebase $\to$ BYOK $\to$ Deterministic Heuristics without UI freezes or unhandled exceptions.