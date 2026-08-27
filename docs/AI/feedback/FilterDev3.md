This is the definitive, implementation-ready architecture. By shifting from a global Top-$K$ to a **Role-Allocated Candidate Budget**, and introducing proper chroma-weighting for neutrals, you guarantee the AI receives a structurally complete and aesthetically balanced puzzle every single time.

Here is the finalized Master Implementation Prompt to feed into your AI coding assistant.

---

### 📋 Master Implementation Prompt

```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the **Deterministic-First Computational Styling Engine** for KoColor. 

The architecture strictly separates responsibilities:
*   **KoColor Local (Retrieval):** Determines what works, what is relevant, and what is missing.
*   **Generative AI (Synthesis):** Determines how to creatively assemble and explain the final style.

### Information Elimination Principle
Token optimization begins before tokenization. KoColor does not attempt to compress an unnecessarily large wardrobe prompt; it first eliminates information that has no reasonable bearing on the current styling decision. 
The deterministic engine progressively transforms: 
**Entire Inventory → Eligible Inventory → Compatible Inventory → Ranked Inventory → Role-Complete Candidate Set → Compact AI Context.** 
AI therefore receives not a compressed representation of the wardrobe, but a compressed representation of the *relevant* wardrobe.

---

### Non-Negotiable System Invariants
1.  **Role-Complete Candidate Budget:** The AI provider dictates the maximum candidate budget ($K$). The deterministic engine does NOT just take the global highest scores; it allocates that budget across missing roles (e.g., 4 Tops, 3 Bottoms) and selects the highest-value slice *within those allocations* to ensure a structurally complete reasoning set.
2.  **Hard Constraints vs. Soft Scoring:** Hard constraints (weather, availability, rotation) strictly eliminate impossible/inappropriate items. Soft constraints (color mathematics, appearance compatibility) continuously score and rank the viable eligible items.
3.  **Chroma-Weighted Hues & Neutrals:** `CompositeColorProfile` MUST calculate dominant hues using circular statistics weighted by chroma ($x = \Sigma (\text{chroma} \times \cos(\theta))$). Neutrals (gray/black/white) have near-zero chroma and must not distort the dominant hue vector, but their lightness/value MUST strongly contribute to contrast and composite scoring.
4.  **Type-Safe Privacy Boundary:** Cloud AI providers receive only derived `StyleTelemetry` and semantic text manifests. Raw image data is encapsulated in a sealed interface and strictly excluded unless routed to a local on-device provider (`supportsLocalImageIngestion = true`).

---

### Implementation Instructions: Generate the Following Modules Sequentially

#### 1. Domain Models & State (`:features:ai:core` & `:applications:kocolor:domain`)
*   `ColorTelemetry`: `undertoneScore: Float`, `depthScore: Float`, `contrastScore: Float`.
*   `AppearanceProfile`: Categorical (`undertone: String`, `depth: String`, `contrast: String`).
*   `AiInput`: Sealed interface (`TextOnly` and `Multimodal`).
*   `RoleRequirement`: `val role: String`, `val minCount: Int`, `val maxCount: Int? = null`.
*   `LockedConstraint`: `itemId: String`, `category: String`, `isUserForced: Boolean`.
*   `StyleSelectionState`:
    *   `val lockedAnchors: List<ClothingItem>`
    *   `val missingRoleRequirements: List<RoleRequirement>`
    *   `val compositeProfile: CompositeColorProfile`
    *   `val fullRankedCandidatePool: List<CandidateProvenance>`

#### 2. Mathematical Color Harmony Engine (`:applications:kocolor:data:color`)
*   `CompositeColorProfile`: `dominantHues: List<Float>`, `temperatureDistribution: Map<String, Float>`, `contrastRange: Float`.
*   `ColorHarmonyEngine`:
    *   `fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile` (Applies chroma-weighted circular vectors. Neutrals bypass hue distortion but factor into contrast/lightness).
    *   `fun scoreCandidate(candidateHsl: Triple<Float, Float, Float>, composite: CompositeColorProfile, telemetry: ColorTelemetry): Float`
    *   Treat $\Delta E_{00}$ as a continuous feature, not a binary clash detector.

#### 3. Deterministic Context Orchestration (`:applications:kocolor:data:usecase`)
*   `RoleGapAnalyzer`: `fun determineRoleRequirements(lockedItems: List<ClothingItem>, occasion: String): List<RoleRequirement>`.
*   `DeterministicContextEngine`:
    *   `suspend fun generateSelectionState(inventory: List<ClothingItem>, lockedConstraints: List<LockedConstraint>, context: StyleRequestContext): StyleSelectionState`
    *   **Pipeline:**
        1. Resolve Anchors (User locked, or deterministically chosen if empty).
        2. Calculate `CompositeColorProfile`.
        3. Identify `MissingRoleRequirements` via `RoleGapAnalyzer`.
        4. **HARD CONSTRAINTS:** Eliminate weather/availability/rotation violations (unless `isUserForced == true`).
        5. **SOFT SCORING:** Score remaining eligible inventory via `ColorHarmonyEngine`. Return `fullRankedCandidatePool`.

#### 4. Provider Abstraction (`:features:ai:core`)
*   `AiProviderCapability`: `id`, `maxInputTokens`, `maxCandidateBudget: Int = 12`, `minCandidateBudget: Int = 4`, `isLocal: Boolean`, `supportsLocalImageIngestion: Boolean`.
*   `interface AiProvider`: `suspend fun countTokens(input: AiInput): Int`, `suspend fun execute(input: AiInput): Result<StyleBlueprint>`.

#### 5. Adaptive Preflight & Waterfall (`:applications:kocolor:data:usecase`)
*   `PromptAssembler`: 
    *   `fun buildRequest(state: StyleSelectionState, candidateBudget: Int, detail: SerializationDetailLevel, supportsImage: Boolean): AiInput`. 
    *   **Crucial Logic:** Do not take global Top-K. Allocate `candidateBudget` across `state.missingRoleRequirements` (e.g., K=12 distributed as 4 Tops, 4 Bottoms, 3 Shoes, 1 Accessory) and take the highest-scoring candidates *within* those buckets.
*   `StyleSimulatorEngine`:
    *   **Adaptive Step-Down Loop**: Start with `K = maxCandidateBudget` and `EXPANDED`.
    *   Step downs: `EXPANDED` $\to$ `BALANCED` $\to$ `MINIMAL` $\to$ `K -= 2`. Recalculate exact token count on every step.

#### 6. Core Verification (`:applications:kocolor:data:test`)
*   Generate `SelectionCascadeTest.kt`.
*   **Test Case:** Lock "Charcoal Trousers" (neutral). Verify hue vector remains stable while contrast constraints activate. Lock "Burgundy Jacket". Verify composite profile updates, global pool is re-ranked, and `PromptAssembler` correctly allocates the remaining budget exclusively to Tops, Footwear, and Accessories.

```