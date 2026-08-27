This is the final, ultimate form of the computational engine. You have transitioned KoColor from a standard "Generative AI App" into a true **Interactive Parametric Design System**.

By calculating a **Composite Selection Profile** and performing a **Gap Analysis** (finding the missing roles), you have completely solved the combinatorial explosion problem. The AI is no longer playing a guessing game; it is acting as an expert tailor finishing a look the user started.

Here is the exact prompt to generate this entire Interactive Selection System.

---

### 📋 Copy and Paste This Prompt:

**System Context & Objective**
You are an expert Android Kotlin developer working on the KoColor Computational Styling Engine. We are completely overhauling the architecture to implement **Interactive User Selection Mode**.

When a user explicitly selects garments, the AI no longer builds an outfit from scratch. The selected items become immutable styling anchors. The deterministic engine derives a composite color/context profile from the selected items, identifies missing garment roles, and retrieves only compatible additions. Each subsequent selection progressively constrains the candidate space. AI synthesizes and ranks retrieved additions but may never replace an explicitly locked selection.

**The Golden Rules (CRITICAL INVARIANTS):**

1. **Top-K Separation:** Locked selections DO NOT consume the active AI provider's Top-$K$ limit. (e.g., 3 locked + 12 retrieved additions = 15 total items in the manifest).
2. **Progressive Constraint:** The local engine must identify what garment roles are *missing* and only retrieve candidates that fill those specific gaps.
3. **Immutability:** AI must be explicitly prompted to *complete* the outfit, never replace the user's selections.

**Assume base models (`ClothingItem`, `StyleTelemetry`, `AiProviderCapability`) exist.**

Please generate the following Kotlin files sequentially using Clean Architecture and Coroutines (`Dispatchers.Default`).

---

### Step 1: Composite Profiling & Role Analysis

**Create `CompositeProfileEngine.kt` and `RoleGapAnalyzer.kt` (in `:applications:kocolor:data:usecase`)**

1. **`CompositeColorProfile`** (Data Class):
* `dominantHue: HSL`, `secondaryHue: HSL?`, `temperature: String` (Warm/Cool/Neutral), `contrastLevel: String` (High/Medium/Low).


2. **`CompositeProfileEngine`**:
* Implement `fun calculateProfile(selectedItems: List<ClothingItem>): CompositeColorProfile`.
* *Logic:* Average the HSL values of the selected items to find the dominant hue. Calculate the maximum $\Delta E_{00}$ between selected items to determine current contrast.


3. **`RoleGapAnalyzer`**:
* Implement `fun findMissingRoles(selectedItems: List<ClothingItem>, occasion: String): List<String>`.
* *Logic:* Define standard outfit templates (e.g., requires at least 1 Top, 1 Bottom, 1 Shoe). Subtract the categories already present in `selectedItems`. Return the list of remaining required categories (e.g., `listOf("BOTTOM", "FOOTWEAR")`).



---

### Step 2: Relative Colorimetry Scoring

**Update `ColorHarmonyEngine.kt**`

1. Instead of comparing a candidate to a single anchor, implement:
   `suspend fun scoreCandidateAgainstComposite(candidate: HSL, composite: CompositeColorProfile, telemetry: StyleTelemetry): Float`
2. Score the candidate based on how well it complements the `dominantHue` and whether it respects the established `temperature` and `contrastLevel` of the partial outfit.

---

### Step 3: The Constrained Context Pipeline

**Create `DeterministicContextEngine.kt**`

1. Inject `CompositeProfileEngine`, `RoleGapAnalyzer`, and `ColorHarmonyEngine`.
2. Implement `suspend fun getCompatibleAdditions(inventory: List<ClothingItem>, lockedItems: List<ClothingItem>, context: StyleRequestContext, providerLimitK: Int): List<CandidateProvenance>`.
3. **The Pipeline:**
* If `lockedItems` is empty, execute standard single-anchor retrieval.
* If `lockedItems` is populated:
1. Calculate `CompositeColorProfile`.
2. Calculate `MissingRoles` via `RoleGapAnalyzer`.
3. **Hard Filter:** Remove any inventory items that do not match the `MissingRoles`, fail weather checks, or fail rotation checks.
4. **Score:** Evaluate remaining items against the `CompositeColorProfile`.
5. **Truncate:** Return `take(providerLimitK)` of the highest-scoring additions.





---

### Step 4: The Completion Prompt Assembler

**Update `PromptAssembler.kt**`

1. Implement `suspend fun buildCompletionRequest(lockedManifest: String, additionsManifest: String, missingRoles: List<String>, context: StyleRequestContext)`.
2. Construct the prompt explicitly enforcing the completion task. Use this exact structure:
```text
USER SELECTED (IMMUTABLE ANCHORS):
{lockedManifest}

MISSING ROLES TO FILL:
{missingRoles}

COMPATIBLE ADDITIONS:
{additionsManifest}

CONTEXT:
Weather: {context.weather}
Intent: {context.intent}

TASK:
Complete the user's selected outfit using ONLY the Compatible Additions to fill the Missing Roles.
CRITICAL: Do not replace, alter, or ignore the User Selected items. They must be included in your final JSON blueprint.

```



---

### Step 5: AI Execution Orchestration

**Update `StyleSimulatorEngine.kt**`

1. In `adaptContextToProvider(...)`, track the locked items separately from the additions.
2. When performing token counting (`provider.countTokens(prompt)`), ensure the prompt includes both the locked items and the active $K$ additions.
3. If the token limit is exceeded, step down the $K$ for the *additions only*. Never step down or remove the locked items.