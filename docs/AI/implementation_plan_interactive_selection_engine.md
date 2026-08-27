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

1. **Role-Complete Candidate Budget (`CandidateAdditions`):** The AI provider dictates the maximum candidate budget ($K$). The deterministic engine does **not** simply take the global highest scores across all categories; it allocates $K$ across `missingRoleRequirements` (e.g., 4 Tops, 4 Bottoms, 3 Shoes, 1 Accessory) and selects the highest-value slice *within those allocations* to ensure a structurally complete reasoning set.
2. **Top-K Candidate Separation:** User-locked anchors **never** consume the active AI provider's retrieval budget. The prompt inventory formula is strictly: `Locked Context + Candidate Additions (K) = Total Prompt Inventory`.
3. **Hard Constraints vs. Soft Scoring:** Hard constraints (weather, availability, rotation) strictly eliminate impossible/inappropriate items. Soft constraints (color mathematics, appearance compatibility) continuously score and rank the viable eligible items.
4. **Type-Safe Privacy Boundary:** The AI input model uses `sealed interface AiInput`. Cloud AI providers strictly accept `TextOnly`. Raw images/bitmaps are encapsulated in `Multimodal` and can only be routed to local on-device providers (`supportsLocalImageIngestion = true`). Cloud requests receive only derived `StyleTelemetry` and semantic text manifests.
5. **Retrieval Invariant:** The AI provider dictates the maximum candidate budget; the deterministic engine selects the highest-value, role-complete slice of the ranked pool within that budget.
6. **Chroma-Weighted Hues & Neutrals:** `CompositeColorProfile` MUST calculate dominant hues using circular statistics weighted by chroma ($x = \Sigma (\text{chroma} \times \cos(\theta))$, $y = \Sigma (\text{chroma} \times \sin(\theta))$, $\text{meanHue} = \text{atan2}(y, x)$). Neutrals (gray/black/white) have near-zero chroma and must not distort the dominant hue vector, but their lightness/value MUST strongly contribute to contrast and composite scoring.
7. **Unified Anchor Pipeline:**
   - **Interactive Styling:** User-selected items become immutable anchors. User-forced items must be preserved even if violating normal deterministic rules (record the violation in `CandidateProvenance`).
   - **Free Styling (No Locks):** If no locked items exist, the engine deterministically selects an anchor item (User-Locked $\to$ User-Selected $\to$ Context-Fit $\to$ Color-Fit $\to$ Freshness $\to$ Tie-breaker), builds the profile, and retrieves the candidate pool.
8. **Continuous Color Scoring:** Color compatibility is a weighted continuous score ($0.0\text{--}1.0$) combining Hue geometry, $\Delta E_{00}$, lightness, saturation, and contrast balance—not a rigid binary filter.
9. **Signature Item Bypass:** Cosmetics and accessories marked `isSignature = true` bypass rotation cooldowns while still tracking usage analytics.

---

## 2. Component Specifications & Implementation Modules

### Module 1: Domain Models & Selection State (`:features:ai:core` & `:applications:kocolor:domain`)

- **`ColorTelemetry`**: Mathematical continuous representation (`undertoneScore: Float`, `depthScore: Float`, `contrastScore: Float`).
- **`AppearanceProfile`**: Categorical semantic representation (`undertone: String`, `depth: String`, `contrast: String`).
- **`AiInput`** (Sealed Interface for Type-Safe Privacy):
  - `data class TextOnly(val prompt: String) : AiInput`
  - `data class Multimodal(val prompt: String, val localImage: Bitmap) : AiInput`
- **`RoleRequirement`**: `val role: String`, `val minCount: Int`, `val maxCount: Int? = null`.
- **`LockedConstraint`**: `val itemId: String`, `val category: String`, `val isUserForced: Boolean = true`.
- **`StyleSelectionState`**:
  - `val lockedAnchors: List<ClothingItem>`
  - `val missingRoleRequirements: List<RoleRequirement>`
  - `val compositeProfile: CompositeColorProfile`
  - `val fullRankedCandidatePool: List<CandidateProvenance>`
- **`CandidateProvenance`**: Holds `clothingItem: ClothingItem?`, `cosmeticItem: CosmeticItem?`, `contextScore: Float`, `colorScore: Float`, `appearanceScore: Float`, `freshnessScore: Float`, `compositeScore: Float`, and `retrievalReason: String`.

---

### Module 2: Mathematical Color Harmony Engine (`:applications:kocolor:data:color`)

- **`CompositeColorProfile`**:
  - `dominantHues: List<Float>`, `secondaryHues: List<Float>`
  - `temperatureDistribution: Map<String, Float>`, `contrastRange: Float`
- **`ColorHarmonyEngine`**:
  - `fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile`: Applies chroma-weighted circular vectors ($x = \Sigma (\text{chroma} \times \cos(\theta))$, $y = \Sigma (\text{chroma} \times \sin(\theta))$). Neutrals bypass hue distortion but factor heavily into contrast/lightness calculations.
  - `fun scoreCandidateAgainstComposite(candidateHsl: Triple<Float, Float, Float>, composite: CompositeColorProfile, telemetry: ColorTelemetry): Float`: Continuous compatibility scoring ($0.0\text{--}1.0$).
  - **Color Math Utilities**:
    - RGB $\leftrightarrow$ HSL $\leftrightarrow$ CIELAB color space conversions.
    - Continuous Hue Harmony geometry: Complementary ($\pm 180^\circ$), Analogous ($\pm 30^\circ$), Triadic ($\pm 120^\circ$).
    - Perceptual color distance using the $\Delta E_{00}$ (CIEDE2000) formula as a continuous feature rather than a binary clash detector.
    - Contrast ratio validation against user `ColorTelemetry.contrastScore`.

---

### Module 3: Deterministic Context Orchestration (`:applications:kocolor:data:usecase`)

- **`RoleGapAnalyzer`**:
  - `fun determineRoleRequirements(lockedItems: List<ClothingItem>, occasion: String): List<RoleRequirement>`
  - Evaluates standard outfit requirements (e.g., min 1 TOP, min 1 BOTTOM, min 1 FOOTWEAR; formal adds min 1 OUTERWEAR) with min/max quantities and subtracts present categories.
- **`RotationScoringUseCase`**:
  - `fun calculatePenalty(lastUsedDays: Int, isSignature: Boolean): Float`
  - Instantly returns `0.0f` if `isSignature == true`. Otherwise computes recency decay ($>0.70$ prunes item).
- **`DeterministicContextEngine`**:
  - Inject `RoleGapAnalyzer`, `ColorHarmonyEngine`, and `RotationScoringUseCase`.
  - `suspend fun generateSelectionState(inventory: List<ClothingItem>, lockedConstraints: List<LockedConstraint>, context: StyleRequestContext): StyleSelectionState`
  - **Pipeline**:
    1. Resolve Anchors. If `lockedConstraints` is empty, deterministically select an anchor. If `isUserForced == true`, preserve the item and tag `CandidateProvenance` with the constraint violation.
    2. Calculate `CompositeColorProfile` from anchors using chroma-weighted circular statistics.
    3. Identify `missingRoleRequirements` via `RoleGapAnalyzer`.
    4. **HARD CONSTRAINTS:** Eliminate weather/availability/rotation violations (unless `isUserForced == true`).
    5. **SOFT SCORING:** Score remaining eligible inventory via `ColorHarmonyEngine`. Return `fullRankedCandidatePool`.

---

### Module 4: Semantic Compression & Provider Abstraction (`:features:ai:core`)

- **`CompactManifestSerializer`**:
  - `enum class SerializationDetailLevel { MINIMAL, BALANCED, EXPANDED }`
  - `fun serialize(locked: List<ClothingItem>, additions: List<CandidateProvenance>, detailLevel: SerializationDetailLevel): String`
  - **Formats**:
    - `MINIMAL`: `[id|category|name|hex|LOCKED/CANDIDATE]`
    - `BALANCED`: `[id|category|name|hex|temp|depth|LOCKED/CANDIDATE]`
    - `EXPANDED`: `[id|category|name|hex|temp|depth|material|LOCKED/CANDIDATE]`
- **`AiProviderCapability`**: `id`, `displayName`, `maxInputTokens`, `maxOutputTokens`, `timeoutMillis`, `maxCandidateBudget: Int = 12`, `minCandidateBudget: Int = 4`, `isLocal: Boolean`, `supportsLocalImageIngestion: Boolean = false`.
- **`sealed interface AiExecutionFailure`**: `Unavailable`, `ContextTooLarge`, `QuotaExceeded`, `Timeout`, `NetworkError`, `ExecutionError(val t: Throwable)`.
- **`interface AiProvider`**:
  - `val capability: AiProviderCapability`
  - `suspend fun isAvailable(): Boolean`
  - `suspend fun countTokens(input: AiInput): Int`
  - `suspend fun execute(input: AiInput): Result<StyleBlueprint>`

---

### Module 5: Adaptive Preflight & Waterfall Engine (`:applications:kocolor:data:usecase`)

- **`PromptAssembler`**:
  - `fun buildRequest(state: StyleSelectionState, candidateBudget: Int, detailLevel: SerializationDetailLevel, supportsImage: Boolean, context: StyleRequestContext): AiInput`
  - **Role-Allocated Candidate Budgeting:** Does **not** take a global Top-$K$. Allocates `candidateBudget` across `state.missingRoleRequirements` (e.g., $K=12$ distributed as 4 Tops, 4 Bottoms, 3 Shoes, 1 Accessory) and takes the highest-scoring candidates *within* those role buckets.
  - Generates `TextOnly` or `Multimodal` strictly based on `supportsImage`.
  - Explicitly prompts the AI to complete the locked anchors by filling `state.missingRoleRequirements` using candidate additions without replacing locked items.
- **`StyleSimulatorEngine`**:
  - Inject `DeterministicContextEngine`, `CompactManifestSerializer`, `PromptAssembler`, `CapabilityRouter`, and `DeterministicFallbackEngine`.
  - `suspend fun generateBlueprint(inventory: List<ClothingItem>, context: StyleRequestContext): StyleBlueprint`
  - Iterates ranked providers (`Local Multimodal` $\to$ `BYOK` $\to$ `Firebase Cloud` $\to$ `Deterministic Fallback`).
  - **Adaptive Step-Down Loop**:
    - Starts with `K = provider.capability.maxCandidateBudget` and `EXPANDED` detail.
    - While token limit exceeded (must call `countTokens()` on the exact assembled `AiInput` every loop):
      1. `EXPANDED` $\to$ `BALANCED`
      2. `BALANCED` $\to$ `MINIMAL`
      3. `K -= 2` (reduces candidate additions only; locked anchors remain intact).
      4. If $K$ hits `minCandidateBudget` and still fails, return `AiExecutionFailure.ContextTooLarge` and route to next provider.

---

### Module 6: Audit & Transparency Logging (`:applications:kocolor:data:telemetry`)

- **`StyleAuditLogger`**:
  - Logs structured timeline for every request under Logcat tag `KoColor_Audit`:
    1. `[1] USER SELECTION / ANCHORS`: Locked IDs, categories, composite color profile.
    2. `[2] DETERMINISTIC GAP ANALYSIS`: Missing role requirements identified, pruned inventory counts.
    3. `[3] CANDIDATE PROVENANCE`: Role-allocated Top-K candidates, individual score components, signature bypass markers.
    4. `[4] ADAPTIVE PREFLIGHT`: Provider chosen, final candidate budget $K$, detail level, token audit.
    5. `[5] AI SYNTHESIS`: Tokens used, execution latency (ms), resulting blueprint rationale.

---

## 3. Verification Plan

1. **Selection Cascade Test**:
   - Start with 300 wardrobe items. Lock "Charcoal Trousers" (neutral). Verify hue vector remains stable while contrast constraints activate, and `RoleGapAnalyzer` identifies missing TOP / FOOTWEAR.
   - Lock "Burgundy Jacket". Verify both remain immutable, composite profile updates, global pool is re-ranked, and `PromptAssembler` correctly allocates the remaining candidate budget exclusively to Tops, Footwear, and Accessories.
2. **Chroma-Weighted Hue & Neutral Test**: Verify `CompositeColorProfile` correctly calculates chroma-weighted circular mean for hues across $350^\circ$ and $10^\circ$ boundary $\to$ $0^\circ$ (not $180^\circ$), and low-chroma gray/neutral items don't skew the dominant hue.
3. **Role-Allocated Top-K Test**: Confirm that candidate allocation distributes $K$ across missing roles rather than returning a global Top-K of a single category (e.g., 12 tops and 0 shoes).
4. **Candidate Additions Separation & Preflight**: Confirm that locked items do not decrement the candidate $K$ budget during preflight step-down iterations.
5. **Type-Safe Privacy & App Check Audit**: Verify `AiInput` guarantees `Multimodal` inputs are never passed to cloud `AiProvider` implementations.
