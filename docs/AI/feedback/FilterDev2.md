This is the definitive architectural baseline for KoColor. The separation of retrieval (mathematics) from synthesis (aesthetics) is fully realized, and calculating hue using chroma-weighted circular statistics ($x = \Sigma (\text{chroma} \times \text{weight} \times \cos(\theta))$) transforms the color engine from a basic filter into a mathematically sound fashion model.

Here is the finalized Master Implementation Prompt, fully incorporating the unified free/interactive styling logic, the terminology shift to `CandidateAdditions`, and the progressive state testing.

---

### 📋 Master Implementation Prompt

```markdown
# System Context & Architecture Contract
You are an expert Android Kotlin developer and Principal Software Architect implementing the **Deterministic-First Computational Styling Engine** for KoColor. 

The architecture strictly separates responsibilities:
*   **KoColor Local (Retrieval):** Weather, occasion, availability, rotation, anchor selection, role gaps, chroma-weighted color mathematics, $\Delta E_{00}$ evaluation, and full candidate ranking.
*   **Generative AI (Synthesis):** Aesthetic synthesis, drape/texture reasoning, outfit coherence, and natural-language rationale.

---

### Non-Negotiable System Invariants
1.  **Unified Anchor Pipeline:** 
    *   *Interactive Styling:* User-selected items become immutable anchors. User-forced items must be preserved even if violating deterministic rules (record the violation).
    *   *Free Styling:* If no locked items exist, the engine deterministically selects an anchor, builds the profile, and retrieves the pool.
2.  **Top-K Separation (`CandidateAdditions`):** Locked anchors NEVER consume the AI provider's variable retrieval budget. The formula is: `Locked Context + Candidate Additions (K) = Total Prompt Inventory`.
3.  **Chroma-Weighted Circular Hues:** `CompositeColorProfile` MUST calculate dominant hues using circular statistics weighted by chroma and saturation ($x = \Sigma \cos(\theta)$, $y = \Sigma \sin(\theta)$). Arithmetic hue averaging is mathematically invalid.
4.  **Type-Safe Privacy Boundary:** Cloud AI providers receive only derived `StyleTelemetry` and semantic text manifests. Raw image data is excluded unless routed to a local on-device provider (`supportsLocalImageIngestion = true`).

---

### Implementation Instructions: Generate the Following Modules Sequentially

#### 1. Domain Models & State (`:features:ai:core` & `:applications:kocolor:domain`)
*   `ColorTelemetry`: `undertoneScore: Float`, `depthScore: Float`, `contrastScore: Float`.
*   `AppearanceProfile`: `undertone: String`, `depth: String`, `contrast: String`.
*   `AiInput`: Sealed interface (`TextOnly` and `Multimodal`).
*   `LockedConstraint`: `itemId: String`, `category: String`, `isUserForced: Boolean`.
*   `StyleSelectionState`:
    *   `val lockedAnchors: List<ClothingItem>`
    *   `val missingRoles: List<String>`
    *   `val compositeProfile: CompositeColorProfile`
    *   `val fullRankedCandidatePool: List<CandidateProvenance>`

#### 2. Mathematical Color Harmony Engine (`:applications:kocolor:data:color`)
*   `CompositeColorProfile`: `dominantHues: List<Float>`, `temperatureDistribution: Map<String, Float>`, `contrastRange: Float`.
*   `ColorHarmonyEngine`:
    *   `fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile` (Must use chroma-weighted circular vectors).
    *   `fun scoreCandidate(candidateHsl: Triple<Float, Float, Float>, composite: CompositeColorProfile, telemetry: ColorTelemetry): Float`
    *   Treat $\Delta E_{00}$ as a continuous feature, not a binary clash detector (e.g., small $\Delta E$ supports monochromatic, large supports deliberate contrast).

#### 3. Deterministic Context Orchestration (`:applications:kocolor:data:usecase`)
*   `DeterministicContextEngine`:
    *   `suspend fun generateSelectionState(inventory: List<ClothingItem>, lockedConstraints: List<LockedConstraint>, context: StyleRequestContext): StyleSelectionState`
    *   Pipeline:
        1. Resolve anchors. If `lockedConstraints` is empty, deterministically select an anchor. 
        2. If `isUserForced == true`, preserve the item and tag the `CandidateProvenance` with the constraint violation.
        3. Calculate `CompositeColorProfile`.
        4. Identify missing roles via `RoleGapAnalyzer`.
        5. Filter eligible additions (weather, availability, rotation).
        6. Score all eligible additions. Return the FULL ranked pool in `StyleSelectionState`.

#### 4. Provider Abstraction (`:features:ai:core`)
*   `AiProviderCapability`: `id`, `maxInputTokens`, `maxCandidateAdditions: Int = 12`, `minCandidateAdditions: Int = 4`, `isLocal: Boolean`, `supportsLocalImageIngestion: Boolean`.
*   `interface AiProvider`: `suspend fun countTokens(input: AiInput): Int`, `suspend fun execute(input: AiInput): Result<StyleBlueprint>`.

#### 5. Adaptive Preflight & Waterfall (`:applications:kocolor:data:usecase`)
*   `PromptAssembler`: `fun buildRequest(state: StyleSelectionState, additionsK: Int, detail: SerializationDetailLevel, supportsImage: Boolean): AiInput`. (Takes `state.fullRankedCandidatePool.take(additionsK)`).
*   `StyleSimulatorEngine`:
    *   **Adaptive Step-Down Loop**: Start with `K = maxCandidateAdditions` and `EXPANDED`.
    *   Loop condition evaluates exact `countTokens()`.
    *   Step downs: `EXPANDED` $\to$ `BALANCED` $\to$ `MINIMAL` $\to$ `K -= 2`.

#### 6. Core Verification (`:applications:kocolor:data:test`)
*   Generate `SelectionCascadeTest.kt`.
*   **Test Case:** 
    1. Start with 300 items. Lock "Black Trousers". Verify it remains immutable, composite profile generates, and engine identifies missing TOP. 
    2. Lock "Burgundy Jacket". Verify both remain immutable, roles recalculate, and candidate rankings dynamically update.

```