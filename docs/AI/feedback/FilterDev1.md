**System Context & Architecture Contract**
You are an expert Android Kotlin developer and Principal Software Architect implementing the **Interactive Parametric Design System** for KoColor.

The architecture is built on the principle of **Information Elimination**: all computable constraints, color science, anchor profiling, and gap retrieval are executed locally in Kotlin, reserving Generative AI exclusively for high-order aesthetic coordination, drape/texture reasoning, and visual synthesis.

---

**I. Non-Negotiable System Invariants**

1. **Top-K Candidate Separation:** Locked anchors NEVER consume the active AI provider's Top-$K$ limit. The formula is strictly: `Locked Context + Candidate Budget (K) = Total Prompt Inventory`.
2. **Type-Safe Privacy Boundary:** The AI input model uses sealed interfaces. Cloud AI providers strictly accept `TextInput`. Raw images/bitmaps are encapsulated in `MultimodalInput` and can only be routed to local on-device providers (`supportsLocalImageIngestion = true`).
3. **Retrieval Invariant:** The AI never performs wardrobe retrieval. The local engine ranks the *entire* eligible candidate pool. The AI provider merely dictates how much of that ranked pool it can afford to see (the $K$-slice) based on its token budget.
4. **Circular Hue Mathematics:** `CompositeColorProfile` must use circular statistics to calculate dominant hues. Arithmetic averaging of hues ($359^\circ$ and $1^\circ \to 180^\circ$) is mathematically invalid and strictly forbidden.
5. **Progressive Selection State:** Every user selection updates a first-class `StyleSelectionState`, dynamically recalculating the composite profile, missing roles, and the ranked candidate pool before the AI is invoked.

---

**II. Implementation Instructions: Generate the Following Modules Sequentially**

**1. Domain Models & State (`:features:ai:core` & `:applications:kocolor:domain`)**

* `ColorTelemetry`: Mathematical representation (`undertoneScore: Float`, `depthScore: Float`, `contrastScore: Float`).
* `AppearanceProfile`: Categorical semantic representation (`undertone: String`, `depth: String`, `contrast: String`).
* `AiInput`: Sealed interface enforcing privacy.
* `data class TextOnly(val prompt: String) : AiInput`
* `data class Multimodal(val prompt: String, val localImage: Bitmap) : AiInput`


* `StyleSelectionState`:
* `val lockedAnchors: List<ClothingItem>`
* `val missingRoles: List<String>`
* `val compositeProfile: CompositeColorProfile?`
* `val fullRankedCandidatePool: List<CandidateProvenance>`



**2. Mathematical Color Harmony Engine (`:applications:kocolor:data:color`)**

* `CompositeColorProfile`:
* `dominantHues: List<Float>`, `secondaryHues: List<Float>`
* `temperatureDistribution: Map<String, Float>`, `contrastRange: Float`


* `ColorHarmonyEngine`:
* `fun calculateCompositeProfile(items: List<ClothingItem>): CompositeColorProfile` (Must use circular mean for hue calculations).
* `fun scoreCandidateAgainstComposite(candidateHsl: Triple<Float, Float Float,>, composite: CompositeColorProfile, telemetry: ColorTelemetry): Float`
* Implement $\Delta E_{00}$ (CIEDE2000) for perceptual distance scoring.



**3. Deterministic Context & State Orchestration (`:applications:kocolor:data:usecase`)**

* `RoleGapAnalyzer`:
* `fun findMissingRoles(lockedItems: List<ClothingItem>, occasion: String): List<String>`


* `DeterministicContextEngine`:
* `suspend fun generateSelectionState(inventory: List<ClothingItem>, lockedItems: List<ClothingItem>, context: StyleRequestContext): StyleSelectionState`
* Pipeline:
1. Calculate `CompositeColorProfile` from `lockedItems`.
2. Identify `missingRoles` via `RoleGapAnalyzer`.
3. Hard Filter remaining inventory (weather, availability, rotation penalty).
4. Soft Score all eligible items against the composite profile and telemetry.
5. Return the full `StyleSelectionState` containing the comprehensively ranked pool.





**4. Semantic Compression & Provider Abstraction (`:features:ai:core`)**

* `CompactManifestSerializer`: `enum class SerializationDetailLevel { MINIMAL, BALANCED, EXPANDED }`.
* `sealed interface AiExecutionFailure`: `Unavailable`, `ContextTooLarge`, `QuotaExceeded`, `Timeout`, `NetworkError`, `ExecutionError(val t: Throwable)`.
* `interface AiProvider`:
* `val capability: AiProviderCapability` (Includes `supportsLocalImageIngestion: Boolean`).
* `suspend fun isAvailable(): Boolean`
* `suspend fun countTokens(input: AiInput): Int`
* `suspend fun execute(input: AiInput): Result<StyleBlueprint>`



**5. Adaptive Preflight & Waterfall Engine (`:applications:kocolor:data:usecase`)**

* `PromptAssembler`:
* `fun buildRequest(state: StyleSelectionState, additionsK: Int, detail: SerializationDetailLevel, supportsImage: Boolean): AiInput`
* Takes the Top-$K$ slice from `state.fullRankedCandidatePool`.
* Generates `TextOnly` or `Multimodal` strictly based on `supportsImage`.
* Explicitly prompts the AI to synthesize the additions to *complete* the locked anchors.


* `StyleSimulatorEngine`:
* Executes provider waterfall (`Local Multimodal` $\to$ `BYOK` $\to$ `Firebase AI Logic` $\to$ `Deterministic`).
* **Adaptive Step-Down Loop**:
* Start with `K = provider.maxTopK` and `EXPANDED`.
* While token limit exceeded (must `countTokens()` on the exact assembled `AiInput` every loop):
1. Step down detail (`EXPANDED` $\to$ `BALANCED` $\to$ `MINIMAL`).
2. If `MINIMAL` is too large, reduce additions `K -= 2`.
3. If $K$ hits `minTopK` and still fails, return `AiExecutionFailure.ContextTooLarge` and route to the next provider.