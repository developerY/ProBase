# Implementation Plan: Interactive Parametric Selection Mode & Deterministic Engine

This document details the refined technical specifications for KoColor's **Interactive Parametric Selection System** and **Deterministic-First Computational Styling Engine**.

---

## 1. System Architecture & Contract

The engine is built on the principle of **Information Elimination**: all computable constraints, color science, anchor profiling, and gap retrieval are executed locally in Kotlin, reserving Generative AI exclusively for high-order aesthetic coordination, drape/texture reasoning, and visual synthesis.

> [!IMPORTANT]
> ### Information Elimination Principle
> Token optimization begins **before tokenization**. KoColor does not attempt to compress an unnecessarily large wardrobe prompt; it first eliminates information that has no reasonable bearing on the current styling decision.
>
> The deterministic engine progressively transforms:
> **Entire Inventory $\to$ Eligible Inventory $\to$ Compatible Inventory $\to$ Ranked Inventory $\to$ Role-Complete Candidate Set $\to$ Compact AI Context.**
>
> AI therefore receives not a compressed representation of the wardrobe, but a **compressed representation of the relevant wardrobe**.

### Non-Negotiable System Invariants

1. **Selection State Hierarchy:**
   - **`SELECTED`**: User is exploring this item.
   - **`LOCKED`**: User explicitly wants this item included as an immutable anchor.
   - **`FORCED`**: System must include it even if it violates a normal constraint (e.g., heavy coat in summer). The violation is recorded in `CandidateProvenance`, but the item is **never** silently removed.
2. **Context Eliminates, Color Ranks:** Hard constraints (weather, availability, rotation) eliminate impossible/inappropriate items. Soft constraints (color mathematics, appearance compatibility) continuously score and rank the viable eligible items.
3. **Role-Complete Candidate Budget (`CandidateAdditions`):** The AI provider dictates the maximum candidate budget ($K$). The deterministic engine does **not** simply take the global highest scores across all categories; it allocates $K$ across `missingRoleRequirements` (e.g., 4 Tops, 4 Bottoms, 3 Shoes, 1 Accessory) and selects the highest-value slice *within those allocations* to ensure a structurally complete reasoning set.
4. **Top-K Candidate Separation:** User-locked anchors **never** consume the active AI provider's retrieval budget. The prompt inventory formula is strictly: `Locked Context + Candidate Additions (K) = Total Prompt Inventory`.
5. **True Color Science ($L^*C^*h^\circ$):** `CompositeColorProfile` MUST convert RGB $\to$ CIELAB $\to$ $L^*C^*h^\circ$. Dominant hues use circular statistics weighted by chroma ($x = \Sigma (C^* \times \cos(h^\circ))$, $y = \Sigma (C^* \times \sin(h^\circ))$). Neutrals (low $C^*$) do not distort the hue vector, but their lightness $L^*$ and contrast contribute heavily to contrast and composite scoring.
6. **Defense-in-Depth Privacy Boundary:** The AI input model uses `sealed interface AiInput`. Cloud AI providers strictly accept `TextOnly`. Raw images/bitmaps are encapsulated in `Multimodal` and can only be routed to local on-device providers (`supportsLocalImageIngestion = true`). The provider implementation **must** enforce this type restriction at runtime.
7. **Retrieval Invariant:** The AI provider dictates the maximum candidate budget; the deterministic engine ranks the entire eligible inventory, then constructs the highest-value role-complete candidate set permitted by that budget.
8. **Deterministic State Caching:** Cache fingerprints (SHA-256) are generated from the *post-retrieval* deterministic state (Selected IDs, `missingRoleRequirements`, Telemetry, Weather, Occasion, Provider ID), not the 300-item inventory.
9. **Signature Item Bypass:** Cosmetics and accessories marked `isSignature = true` bypass rotation cooldowns while still tracking usage analytics.

---

## 2. Component Specifications & Implementation Modules

### Module 1: Domain Models & Selection State (`:features:ai:core` & `:applications:kocolor:domain`)

- **`ColorTelemetry`**: Mathematical continuous representation (`undertoneScore: Float`, `depthScore: Float`, `contrastScore: Float`).
- **`AppearanceProfile`**: Categorical semantic representation (`undertone: String`, `depth: String`, `contrast: String`).
- **`AiInput`** (Sealed Interface for Type-Safe Privacy):
  - `data class TextOnly(val prompt: String) : AiInput`
  - `data class Multimodal(val prompt: String, val image: Bitmap) : AiInput`
- **`SelectionTier`**: Enum `SELECTED`, `LOCKED`, `FORCED`.
- **`UserConstraint`**: `val itemId: String`, `val category: String`, `val tier: SelectionTier`.
- **`RoleRequirement`**: `val role: String`, `val minCount: Int`, `val maxCount: Int? = null`.
- **`StyleRequestContext`**:
  - `val intent: String`
  - `val occasion: String` (e.g., "Formal", "Beach", "Business Casual")
  - `val weatherTempC: Float` (Environment temperature for hard gating)
  - `val uvIndex: Float`
  - `val appearanceTelemetry: ColorTelemetry`
  - `val lockedConstraints: List<UserConstraint>`
  - `val localImageBitmap: Bitmap? = null`
- **`StyleSelectionState`**:
  - `val activeAnchors: List<ClothingItem>`
  - `val missingRoleRequirements: List<RoleRequirement>`
  - `val compositeProfile: CompositeColorProfile`
  - `val fullRankedCandidatePool: List<CandidateProvenance>`
- **`CandidateProvenance`**: Holds `clothingItem: ClothingItem?`, `cosmeticItem: CosmeticItem?`, `contextScore: Float`, `colorScore: Float`, `appearanceScore: Float`, `freshnessScore: Float`, `compositeScore: Float`, and `retrievalReason: String`.

---

### Module 2: Mathematical Color Harmony Engine (`:applications:kocolor:data:color`)

- **`CompositeColorProfile`**:
  - `dominantHues: List<Float>`, `temperatureDistribution: Map<String, Float>`, `contrastRange: Float`
- **`ColorHarmonyEngine`**:
  - Implement RGB $\to$ CIELAB $\to$ $L^*C^*h^\circ$ conversions.
  - `fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile`: Applies chroma-weighted circular vectors ($x = \Sigma (C^* \times \cos(h^\circ))$, $y = \Sigma (C^* \times \sin(h^\circ))$). Neutrals (low $C^*$) bypass hue distortion but factor heavily into contrast/lightness calculations.
  - `fun scoreCandidate(candidateLCh: Triple<Float, Float, Float>, composite: CompositeColorProfile, telemetry: ColorTelemetry): Float`: Continuous compatibility scoring ($0.0\text{--}1.0$).
  - **Color Math Utilities**:
    - RGB $\leftrightarrow$ HSL $\leftrightarrow$ CIELAB $\leftrightarrow$ $L^*C^*h^\circ$ space conversions.
    - Continuous Hue Harmony geometry: Complementary ($\pm 180^\circ$), Analogous ($\pm 30^\circ$), Triadic ($\pm 120^\circ$).
    - Perceptual color distance using the $\Delta E_{00}$ (CIEDE2000) formula as a continuous feature rather than a binary clash detector.
    - Contrast ratio validation against user `ColorTelemetry.contrastScore`.

---

### Module 3: Deterministic Context Orchestration (`:applications:kocolor:data:usecase`)

- **`RoleGapAnalyzer`**:
  - `fun determineRoleRequirements(anchors: List<ClothingItem>, occasion: String, weatherTempC: Float): List<RoleRequirement>`
  - Evaluates standard outfit requirements (e.g., min 1 TOP, min 1 BOTTOM, min 1 FOOTWEAR; formal or cold weather adds min 1 OUTERWEAR) with min/max quantities and subtracts present categories.
- **`RotationScoringUseCase`**:
  - `fun calculatePenalty(lastUsedDays: Int, isSignature: Boolean): Float`
  - Instantly returns `0.0f` if `isSignature == true`. Otherwise computes recency decay ($>0.70$ prunes item).
- **`DeterministicContextEngine`**:
  - Inject `RoleGapAnalyzer`, `ColorHarmonyEngine`, and `RotationScoringUseCase`.
  - `suspend fun generateSelectionState(inventory: List<ClothingItem>, constraints: List<UserConstraint>, context: StyleRequestContext): StyleSelectionState`
  - **Pipeline**:
    1. **Resolve Anchors**: User locked, or deterministically chosen if empty. If `tier == FORCED`, preserve the item even if it violates normal constraints and tag `CandidateProvenance` with the constraint violation.
    2. **Calculate $L^*C^*h^\circ$ Composite Profile** from anchors using chroma-weighted circular statistics.
    3. **Identify Missing Role Requirements** via `RoleGapAnalyzer` using `context.occasion` and `context.weatherTempC`.
    4. **HARD CONSTRAINTS (Eliminate)**: Eliminate weather/availability/rotation violations (unless `tier == FORCED`).
    5. **SOFT SCORING (Rank)**: Score remaining eligible inventory via `ColorHarmonyEngine`. Return `fullRankedCandidatePool`.

---

### Module 4: Provider Abstraction & Caching (`:features:ai:core`)

- **`CompactManifestSerializer`**:
  - `enum class SerializationDetailLevel { MINIMAL, BALANCED, EXPANDED }`
  - `fun serialize(locked: List<ClothingItem>, additions: List<CandidateProvenance>, detailLevel: SerializationDetailLevel): String`
  - **Formats**:
    - `MINIMAL`: `[id|category|name|hex|LOCKED/CANDIDATE]`
    - `BALANCED`: `[id|category|name|hex|temp|depth|LOCKED/CANDIDATE]`
    - `EXPANDED`: `[id|category|name|hex|temp|depth|material|LOCKED/CANDIDATE]`
- **`AiProviderCapability`**: `id`, `displayName`, `maxInputTokens`, `maxOutputTokens`, `timeoutMillis`, `maxCandidateAdditions: Int = 12`, `minCandidateAdditions: Int = 4`, `isLocal: Boolean`, `supportsLocalImageIngestion: Boolean = false`.
- **`sealed interface AiExecutionFailure`**: `Unavailable`, `ContextTooLarge`, `QuotaExceeded`, `Timeout`, `NetworkError`, `ExecutionError(val t: Throwable)`.
- **`interface AiProvider`**:
  - `val capability: AiProviderCapability`
  - `suspend fun isAvailable(): Boolean`
  - `suspend fun countTokens(input: AiInput): Int`
  - `suspend fun execute(input: AiInput): Result<StyleBlueprint>`
- **`StyleCacheRepository`**:
  - `suspend fun getOrFetch(state: StyleSelectionState, context: StyleRequestContext, providerId: String, fetcher: suspend () -> StyleBlueprint): StyleBlueprint`
  - Generates SHA-256 fingerprint from post-retrieval deterministic state: `Selected IDs + missingRoleRequirements + occasion + weatherTempC + telemetry + providerId`.

---

### Module 5: Adaptive Preflight & Waterfall Engine (`:applications:kocolor:data:usecase`)

- **`PromptAssembler`**:
  - `fun buildRequest(state: StyleSelectionState, candidateBudget: Int, detailLevel: SerializationDetailLevel, supportsImage: Boolean, context: StyleRequestContext): AiInput`
  - **Role-Allocated Candidate Budgeting:** Allocates `candidateBudget` across `state.missingRoleRequirements` dynamically and takes the highest-scoring candidates *within* those allocations to ensure a structurally complete reasoning set.
  - Generates `TextOnly` or `Multimodal` strictly based on `supportsImage`.
  - Explicitly prompts the AI to complete the locked anchors by filling `state.missingRoleRequirements` using candidate additions without replacing locked items.
- **`StyleSimulatorEngine`**:
  - Inject `DeterministicContextEngine`, `CompactManifestSerializer`, `PromptAssembler`, `CapabilityRouter`, `StyleCacheRepository`, and `DeterministicFallbackEngine`.
  - `suspend fun generateBlueprint(inventory: List<ClothingItem>, context: StyleRequestContext): StyleBlueprint`
  - Iterates ranked providers (`Local Multimodal` $\to$ `BYOK` $\to$ `Firebase Cloud` $\to$ `Deterministic Fallback`).
  - **Adaptive Step-Down Loop**:
    - Starts with `K = provider.capability.maxCandidateAdditions` and `EXPANDED` detail.
    - While token limit exceeded (must call `countTokens()` on the exact assembled `AiInput` every loop):
      1. `EXPANDED` $\to$ `BALANCED`
      2. `BALANCED` $\to$ `MINIMAL`
      3. `K -= 2` (reduces candidate additions only; locked anchors remain intact).
      4. If $K$ hits `minCandidateAdditions` and still fails, return `AiExecutionFailure.ContextTooLarge` and route to next provider.

---

### Module 6: Audit & Transparency Logging (`:applications:kocolor:data:telemetry`)

- **`StyleAuditLogger`**:
  - Logs structured timeline for every request under Logcat tag `KoColor_Audit`:
    1. `[1] USER SELECTION / ANCHORS`: Locked IDs, categories, selection tier (SELECTED/LOCKED/FORCED), composite color profile.
    2. `[2] DETERMINISTIC GAP ANALYSIS`: Missing role requirements identified, occasion, weatherTempC, pruned inventory counts.
    3. `[3] CANDIDATE PROVENANCE`: Role-allocated Top-K candidates, individual score components, signature bypass markers.
    4. `[4] ADAPTIVE PREFLIGHT`: Provider chosen, final candidate budget $K$, detail level, token audit.
    5. `[5] AI SYNTHESIS`: Tokens used, execution latency (ms), resulting blueprint rationale.

---

### Module 7: UI Rehydration & Mapping (`:applications:kocolor:features:analyzer`)

- **`GreedyRehydrator`**:
  - `fun mapToVisualBlueprintData(cosmetics: List<CosmeticItem>, clothing: List<ClothingItem>, palette: List<String>, isComplete: Boolean, activeClothingAnchors: List<ClothingItem> = emptyList(), activeCosmeticAnchors: List<CosmeticItem> = emptyList()): VisualBlueprintData`
  - **The Anchor Fail-Safe**: If the Generative AI hallucinates and omits a `LOCKED` or `FORCED` anchor from its JSON response (`selectedClothingIds` or `selectedCosmeticIds`), the `GreedyRehydrator` intercepts the response and manually re-injects the anchor item (clothing or cosmetic) back into `VisualBlueprintData`. This guarantees the "user is always correct" invariant holds true at the UI layer regardless of LLM behavior.
  - **Keyword Fallback**: If an item's primary category is generic (e.g., `ACTIVEWEAR`), scans product name for keywords ("tank" $\to$ Top, "pants" $\to$ Bottom, "jacket" $\to$ Outerwear).
  - **No Item Left Behind**: Greedily assigns leftover recommended items to empty slots so the Compose UI state machine never hangs on "Pending...".
  - **Completion State**: When `isComplete == true`, sets unassigned slots to "None" / "Not required" instead of "Pending...".

---

## 3. Verification Plan

1. **Selection Cascade & FORCED Item Test**:
   - Start with 300 wardrobe items. Apply a `FORCED` heavy coat in Summer. Verify it bypasses weather elimination but logs a constraint violation. Verify the dynamic role allocator requests appropriate complementary items without exceeding `maxCandidateAdditions`.
2. **Chroma-Weighted Hue & Neutral Test**: Verify `CompositeColorProfile` correctly calculates $L^*C^*h^\circ$ chroma-weighted circular mean for hues across $350^\circ$ and $10^\circ$ boundary $\to$ $0^\circ$ (not $180^\circ$), and low-chroma gray/neutral items don't skew the dominant hue.
3. **Cache Key Integrity Test**: Verify locking the same Black Shirt for a casual morning ($22^\circ\text{C}$, "Casual") vs formal evening ($5^\circ\text{C}$, "Formal") produces distinct SHA-256 cache fingerprints due to differing `missingRoleRequirements` and environmental context.
4. **UI Rehydration Test**: Verify an AI result containing `ACTIVEWEAR` items is successfully mapped into Top/Bottom slots without leaving the Compose UI in a "Pending..." state.
5. **Type-Safe Privacy & App Check Audit**: Verify `AiInput` guarantees `Multimodal` inputs are never passed to cloud `AiProvider` implementations.
