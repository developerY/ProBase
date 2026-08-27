# Implementation Plan: Interactive Parametric Selection Mode & Deterministic Engine

This document details the refined technical specifications for KoColor's **Interactive Parametric Selection System** and **Deterministic-First Computational Styling Engine**.

---

## 1. System Architecture & Contract

The engine is built on the principle of **Information Elimination**: all computable constraints, color science, anchor profiling, and gap retrieval are executed locally in Kotlin, reserving Generative AI exclusively for high-order aesthetic coordination, drape/texture reasoning, and visual synthesis.

### Non-Negotiable System Invariants

1. **Top-K Candidate Separation:** User-locked anchors **never** consume the active AI provider's Top-K limit. The prompt inventory formula is strictly: `Locked Context + Candidate Budget (K) = Total Prompt Inventory`.
2. **Type-Safe Privacy Boundary:** The AI input model uses `sealed interface AiInput`. Cloud AI providers strictly accept `TextOnly`. Raw images/bitmaps are encapsulated in `Multimodal` and can only be routed to local on-device providers (`supportsLocalImageIngestion = true`).
3. **Retrieval Invariant:** The AI never performs wardrobe retrieval. The local engine ranks the *entire* eligible candidate pool. The AI provider merely dictates how much of that ranked pool it can afford to see (the $K$-slice) based on its token budget.
4. **Circular Hue Mathematics:** `CompositeColorProfile` must use circular statistics (vector addition) to calculate dominant hues. Arithmetic averaging of hues ($359^\circ$ and $1^\circ \to 180^\circ$) is mathematically invalid and strictly forbidden.
5. **Progressive Selection State:** Every user selection updates a first-class `StyleSelectionState`, dynamically recalculating the composite profile, missing roles, and the ranked candidate pool before the AI is invoked.
6. **Continuous Color Scoring:** Color compatibility is a weighted continuous score ($0.0\text{--}1.0$) combining Hue geometry, $\Delta E_{00}$, lightness, saturation, and contrast balance—not a rigid binary filter.
7. **Signature Item Bypass:** Cosmetics and accessories marked `isSignature = true` bypass rotation cooldowns while still tracking usage analytics.

---

## 2. Component Specifications & Implementation Modules

### Module 1: Domain Models & Selection State (`:features:ai:core` & `:applications:kocolor:domain`)

- **`ColorTelemetry`**: Mathematical continuous representation (`undertoneScore: Float`, `depthScore: Float`, `contrastScore: Float`).
- **`AppearanceProfile`**: Categorical semantic representation (`undertone: String`, `depth: String`, `contrast: String`).
- **`AiInput`** (Sealed Interface for Type-Safe Privacy):
  - `data class TextOnly(val prompt: String) : AiInput`
  - `data class Multimodal(val prompt: String, val localImage: Bitmap) : AiInput`
- **`StyleSelectionState`**:
  - `val lockedAnchors: List<ClothingItem>`
  - `val missingRoles: List<String>`
  - `val compositeProfile: CompositeColorProfile?`
  - `val fullRankedCandidatePool: List<CandidateProvenance>`
- **`CandidateProvenance`**: Holds `clothingItem: ClothingItem?`, `cosmeticItem: CosmeticItem?`, `contextScore: Float`, `colorScore: Float`, `appearanceScore: Float`, `freshnessScore: Float`, `compositeScore: Float`, and `retrievalReason: String`.

---

### Module 2: Mathematical Color Harmony Engine (`:applications:kocolor:data:color`)

- **`CompositeColorProfile`**:
  - `dominantHues: List<Float>`, `secondaryHues: List<Float>`
  - `temperatureDistribution: Map<String, Float>`, `contrastRange: Float`
- **`ColorHarmonyEngine`**:
  - `fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile`: Uses circular mean statistics for hue calculations to prevent wrap-around errors.
  - `fun scoreCandidateAgainstComposite(candidateHsl: Triple<Float, Float, Float>, composite: CompositeColorProfile, telemetry: ColorTelemetry): Float`: Continuous compatibility scoring ($0.0\text{--}1.0$).
  - **Color Math Utilities**:
    - RGB $\leftrightarrow$ HSL $\leftrightarrow$ CIELAB color space conversions.
    - Continuous Hue Harmony geometry: Complementary ($\pm 180^\circ$), Analogous ($\pm 30^\circ$), Triadic ($\pm 120^\circ$).
    - Perceptual color distance using the $\Delta E_{00}$ (CIEDE2000) formula.
    - Contrast ratio validation against user `ColorTelemetry.contrastScore`.

---

### Module 3: Deterministic Context & State Orchestration (`:applications:kocolor:data:usecase`)

- **`RoleGapAnalyzer`**:
  - `fun findMissingRoles(lockedItems: List<ClothingItem>, occasion: String): List<String>`
  - Evaluates standard outfit requirements (e.g., 1 TOP, 1 BOTTOM, 1 FOOTWEAR; formal adds OUTERWEAR) and subtracts present categories.
- **`RotationScoringUseCase`**:
  - `fun calculatePenalty(lastUsedDays: Int, isSignature: Boolean): Float`
  - Instantly returns `0.0f` if `isSignature == true`. Otherwise computes recency decay ($>0.70$ prunes item).
- **`DeterministicContextEngine`**:
  - Inject `RoleGapAnalyzer`, `ColorHarmonyEngine`, and `RotationScoringUseCase`.
  - `suspend fun generateSelectionState(inventory: List<ClothingItem>, lockedItems: List<ClothingItem>, context: StyleRequestContext): StyleSelectionState`
  - **Pipeline**:
    1. Calculate `CompositeColorProfile` from `lockedItems` using circular statistics.
    2. Identify `missingRoles` via `RoleGapAnalyzer`.
    3. Hard Filter remaining inventory (weather temperature gating, availability, rotation penalty).
    4. Soft Score all eligible items against `CompositeColorProfile` and `ColorTelemetry`.
    5. Return the full `StyleSelectionState` containing the comprehensively ranked candidate pool.

---

### Module 4: Semantic Compression & Provider Abstraction (`:features:ai:core`)

- **`CompactManifestSerializer`**:
  - `enum class SerializationDetailLevel { MINIMAL, BALANCED, EXPANDED }`
  - `fun serialize(locked: List<ClothingItem>, additions: List<CandidateProvenance>, detailLevel: SerializationDetailLevel): String`
  - **Formats**:
    - `MINIMAL`: `[id|category|name|hex|LOCKED/CANDIDATE]`
    - `BALANCED`: `[id|category|name|hex|temp|depth|LOCKED/CANDIDATE]`
    - `EXPANDED`: `[id|category|name|hex|temp|depth|material|LOCKED/CANDIDATE]`
- **`AiProviderCapability`**: `id`, `displayName`, `maxInputTokens`, `maxOutputTokens`, `timeoutMillis`, `maxTopK: Int = 16`, `minTopK: Int = 6`, `isLocal: Boolean`, `supportsLocalImageIngestion: Boolean = false`.
- **`sealed interface AiExecutionFailure`**: `Unavailable`, `ContextTooLarge`, `QuotaExceeded`, `Timeout`, `NetworkError`, `ExecutionError(val t: Throwable)`.
- **`interface AiProvider`**:
  - `val capability: AiProviderCapability`
  - `suspend fun isAvailable(): Boolean`
  - `suspend fun countTokens(input: AiInput): Int`
  - `suspend fun execute(input: AiInput): Result<StyleBlueprint>`

---

### Module 5: Adaptive Preflight & Waterfall Engine (`:applications:kocolor:data:usecase`)

- **`PromptAssembler`**:
  - `fun buildRequest(state: StyleSelectionState, additionsK: Int, detailLevel: SerializationDetailLevel, supportsImage: Boolean, context: StyleRequestContext): AiInput`
  - Takes the Top-K slice from `state.fullRankedCandidatePool`.
  - Generates `TextOnly` or `Multimodal` strictly based on `supportsImage`.
  - Explicitly prompts the AI to complete the locked anchors by filling `state.missingRoles` using candidate additions without replacing locked items.
- **`StyleSimulatorEngine`**:
  - Inject `DeterministicContextEngine`, `CompactManifestSerializer`, `PromptAssembler`, `CapabilityRouter`, and `DeterministicFallbackEngine`.
  - `suspend fun generateBlueprint(inventory: List<ClothingItem>, context: StyleRequestContext): StyleBlueprint`
  - Iterates ranked providers (`Local Multimodal` $\to$ `BYOK` $\to$ `Firebase Cloud` $\to$ `Deterministic Fallback`).
  - **Adaptive Step-Down Loop**:
    - Starts with `K = provider.capability.maxTopK` and `EXPANDED` detail.
    - While token limit exceeded (must call `countTokens()` on the exact assembled `AiInput` every loop):
      1. `EXPANDED` $\to$ `BALANCED`
      2. `BALANCED` $\to$ `MINIMAL`
      3. `K -= 2` (reduces additions only; locked anchors remain intact).
      4. If $K$ hits `minTopK` and still fails, return `AiExecutionFailure.ContextTooLarge` and route to next provider.

---

### Module 6: Audit & Transparency Logging (`:applications:kocolor:data:telemetry`)

- **`StyleAuditLogger`**:
  - Logs structured timeline for every request under Logcat tag `KoColor_Audit`:
    1. `[1] USER SELECTION / ANCHORS`: Locked IDs, categories, composite color profile.
    2. `[2] DETERMINISTIC GAP ANALYSIS`: Missing roles identified, pruned inventory counts.
    3. `[3] CANDIDATE PROVENANCE`: Top-K candidates, individual score components, signature bypass markers.
    4. `[4] ADAPTIVE PREFLIGHT`: Provider chosen, final K, detail level, token audit.
    5. `[5] AI SYNTHESIS`: Tokens used, execution latency (ms), resulting blueprint rationale.

---

## 3. Verification Plan

1. **Interactive Selection Audit**: Verify that user-locked items are never replaced or omitted by the engine, and that candidate retrieval only pulls from missing roles.
2. **Circular Hue Test**: Verify `CompositeColorProfile` correctly calculates circular mean for hues across $350^\circ$ and $10^\circ$ boundary $\to$ $0^\circ$ (not $180^\circ$).
3. **Top-K Separation & Preflight**: Confirm that locked items do not decrement the candidate $K$ budget during preflight step-down iterations.
4. **Type-Safe Privacy & App Check Audit**: Verify `AiInput` guarantees `Multimodal` inputs are never passed to cloud `AiProvider` implementations.
