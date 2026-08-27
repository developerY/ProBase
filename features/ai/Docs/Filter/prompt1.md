Execute this comprehensive, production-grade master prompt in your AI coding environment (Cursor, Copilot, or Gemini) to generate the complete end-to-end interactive styling engine.

---

### 📋 Master Implementation Prompt

```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the complete **Deterministic-First Computational Styling Engine** for KoColor. 

The architecture is built on the principle of **Information Elimination**: all computable constraints, color science, anchor profiling, and gap retrieval are executed locally in Kotlin, reserving Generative AI exclusively for high-order aesthetic coordination, drape/texture reasoning, and visual synthesis.

---

### Non-Negotiable System Invariants
1. **Interactive User Selection Mode:** When a user explicitly selects/locks garments or cosmetics, they become immutable anchors. The local engine derives a `CompositeColorProfile`, performs a `RoleGapAnalyzer` to find missing categories, and retrieves only additions that fill those gaps.
2. **Top-K Candidate Separation:** User-locked items DO NOT consume the active AI provider's Top-K retrieval budget (e.g., 2 locked items + 12 retrieved additions = 14 items sent).
3. **Adaptive Token Preflight:** Budgeting counts the EXACT assembled prompt. If it exceeds `maxInputTokens`, step down metadata detail (`EXPANDED` → `BALANCED` → `MINIMAL`), then decrement candidate additions ($K -= 2$) while preserving locked anchors.
4. **Type-Safe Privacy Boundary:** Cloud AI providers (Firebase, BYOK) strictly receive mathematical `StyleTelemetry` and compact text manifests. Raw images/bitmaps are only accessible to local on-device providers (`supportsLocalImageIngestion = true`).
5. **Continuous Color Scoring:** Color compatibility is a weighted continuous score ($0.0\text{--}1.0$) combining Hue geometry, $\Delta E_{00}$, lightness, saturation, and contrast balance—not a rigid binary filter.
6. **Signature Item Bypass:** Cosmetics and accessories marked `isSignature = true` bypass rotation cooldowns while still tracking usage analytics.

---

### Target Tech Stack
* Language: Kotlin Multiplatform / Native Android
* Frameworks: Kotlinx Coroutines, Flow, Kotlinx Serialization, Hilt / Dependency Injection
* Architecture: Clean Multi-Module Architecture (`:features:ai:core`, `:applications:kocolor:data`)

---

### Implementation Instructions: Generate the Following 6 Modules Sequentially

#### 1. Domain Models & Provenance (`:features:ai:core` & `:applications:kocolor:domain`)
* `ClothingItem`: `id`, `category` (TOP, BOTTOM, FOOTWEAR, OUTERWEAR, ACCESSORY), `subcategory`, `name`, `hexColor`, `hsl: Triple<Float, Float, Float>`, `temperature: String`, `depth: String`, `material: String`, `isAvailable: Boolean`, `isSignature: Boolean = false`, `lastWornDaysAgo: Int`, `thermalWeight: Int`.
* `CosmeticItem`: `id`, `category` (EYES, CHEEKS, LIPS, NAILS), `name`, `hexColor`, `hsl: Triple<Float, Float, Float>`, `finish: String`, `isSignature: Boolean = false`, `lastUsedTimestamp: Long`, `usageCount: Int`.
* `StyleTelemetry`: `temperature: Float`, `depth: Float`, `contrast: Float`.
* `LockedConstraint`: `val itemId: String`, `val category: String`, `val isUserForced: Boolean = true`.
* `CandidateProvenance`: Holds `item: ClothingItem`, `contextScore: Float`, `colorScore: Float`, `appearanceScore: Float`, `freshnessScore: Float`, `compositeScore: Float`, and `retrievalReason: String`.
* `StyleRequestContext`: `intent: String`, `occasion: String`, `weatherTempC: Float`, `uvIndex: Float`, `telemetry: StyleTelemetry`, `lockedConstraints: List<LockedConstraint>`, `localImageBitmap: Any?`.

#### 2. Mathematical Color Harmony Engine (`:applications:kocolor:data:color`)
* `CompositeColorProfile`: `dominantHue: Float`, `secondaryHue: Float?`, `temperature: String`, `contrastRatio: Float`.
* `ColorHarmonyEngine`:
  - `fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile`
  - `fun scoreCandidateAgainstComposite(candidateHsl: Triple<Float, Float, Float>, composite: CompositeColorProfile, telemetry: StyleTelemetry): Float`
  - Implement mathematical utilities:
    - RGB to HSL and HSL to CIELAB space conversions.
    - Continuous Hue Harmony calculation: Complementary ($\pm 180^\circ$), Analogous ($\pm 30^\circ$), Triadic ($\pm 120^\circ$).
    - Perceptual color distance using the $\Delta E_{00}$ (CIEDE2000) formula.
    - Contrast ratio validation against user `StyleTelemetry.contrast`.

#### 3. Deterministic Context & Role Gap Engine (`:applications:kocolor:data:usecase`)
* `RoleGapAnalyzer`:
  - `fun findMissingRoles(lockedItems: List<ClothingItem>, occasion: String): List<String>`
  - Dynamically evaluates required categories (e.g., standard: 1 TOP, 1 BOTTOM, 1 FOOTWEAR; formal adds OUTERWEAR).
* `RotationScoringUseCase`:
  - `fun calculatePenalty(lastUsedDays: Int, isSignature: Boolean): Float`
  - If `isSignature == true`, returns `0.0f` immediately. Otherwise, computes decay penalty ($>0.70$ prunes item).
* `DeterministicContextEngine`:
  - Inject `RoleGapAnalyzer`, `ColorHarmonyEngine`, and `RotationScoringUseCase`.
  - `suspend fun retrieveReasoningSet(inventory: List<ClothingItem>, context: StyleRequestContext, providerK: Int): Pair<List<ClothingItem>, List<CandidateProvenance>>`
  - Pipeline:
    1. Resolve locked items as immutable anchors (Score = 1.0f).
    2. Compute `CompositeColorProfile` from locked items.
    3. Identify missing roles via `RoleGapAnalyzer`.
    4. Hard filter remaining inventory (weather temp gating, availability, rotation penalty).
    5. Soft score eligible items against `CompositeColorProfile` and `StyleTelemetry`.
    6. Select top additions matching missing roles up to `providerK`, returning `(lockedItems, additions)`.

#### 4. Semantic Compression & Provider Abstraction (`:features:ai:core`)
* `CompactManifestSerializer`:
  - `enum class SerializationDetailLevel { MINIMAL, BALANCED, EXPANDED }`
  - `fun serialize(locked: List<ClothingItem>, additions: List<CandidateProvenance>, detailLevel: SerializationDetailLevel): String`
  - Formats:
    - `MINIMAL`: `[id|category|name|hex|LOCKED/CANDIDATE]`
    - `BALANCED`: `[id|category|name|hex|temp|depth|LOCKED/CANDIDATE]`
    - `EXPANDED`: `[id|category|name|hex|temp|depth|material|LOCKED/CANDIDATE]`
* `AiProviderCapability`: `id`, `displayName`, `maxInputTokens`, `maxOutputTokens`, `timeoutMillis`, `maxTopK: Int = 16`, `minTopK: Int = 6`, `isLocal: Boolean`, `supportsLocalImageIngestion: Boolean = false`.
* `sealed interface AiExecutionFailure`: `ContextLimitExceeded`, `QuotaExceeded`, `Timeout`, `NetworkUnavailable`, `ProviderUnavailable`, `Unknown(val t: Throwable)`.
* `interface AiProvider`:
  - `val capability: AiProviderCapability`
  - `suspend fun isAvailable(): Boolean`
  - `suspend fun countTokens(request: StylePromptRequest): Int`
  - `suspend fun execute(request: StylePromptRequest): Result<StyleBlueprint>`

#### 5. Prompt Assembler & Adaptive Waterfall Engine (`:applications:kocolor:data:usecase`)
* `PromptAssembler`:
  - `fun buildExactCompletionRequest(lockedManifest: String, additionsManifest: String, missingRoles: List<String>, context: StyleRequestContext, capability: AiProviderCapability): StylePromptRequest`
  - Enforces: If `!capability.supportsLocalImageIngestion`, nullifies `localImageBitmap`.
  - Injects strict prompt instructions: User-locked items are immutable anchors; AI must fill missing roles using candidate additions without replacing locked items.
* `StyleSimulatorEngine`:
  - Inject `DeterministicContextEngine`, `CompactManifestSerializer`, `PromptAssembler`, `CapabilityRouter`, and `DeterministicFallbackEngine`.
  - `suspend fun generateBlueprint(inventory: List<ClothingItem>, context: StyleRequestContext): StyleBlueprint`
  - Iterates ranked providers (`Local Multimodal` → `BYOK` → `Firebase Cloud` → `Deterministic Fallback`).
  - Executes the **Adaptive Step-Down Loop**:
    - Starts with `K = provider.capability.maxTopK` and `EXPANDED` detail.
    - If prompt tokens exceed `maxInputTokens`:
      1. `EXPANDED` → `BALANCED`
      2. `BALANCED` → `MINIMAL`
      3. `K -= 2` (reduces additions only; locked items remain intact).
    - Executes when fit is achieved; cascades to next provider upon failure.

#### 6. Audit & Transparency Logging (`:applications:kocolor:data:telemetry`)
* `StyleAuditLogger`:
  - Logs structured timeline for every request:
    1. `[1] USER SELECTION / ANCHORS`: Locked IDs, categories, composite color profile.
    2. `[2] DETERMINISTIC GAP ANALYSIS`: Missing roles identified, pruned inventory counts.
    3. `[3] CANDIDATE PROVENANCE`: Top-K candidates, individual score components, signature bypass markers.
    4. `[4] ADAPTIVE PREFLIGHT`: Provider chosen, final K, detail level, token audit.
    5. `[5] AI SYNTHESIS`: Tokens used, execution latency (ms), resulting blueprint rationale.
  - Implements formatted Logcat output under tag `KoColor_Audit`.

```