# Implementation Plan: User Intent Fulfillment & Validator Hardening

Based on the architectural review in `ArchRev54.md`, `ArchRev55.md`, and `ArchRev57.md`, this document outlines the step-by-step implementation plan to resolve critical validator failures and bridge the gap between user intent and deterministic candidate scoring without conflating recommendation intent with FASHIONISTA aesthetics.

---

## Part 1: Hardening the Deterministic Validator (Critical Bug Fix)

**The Problem**: The `RecommendationValidator` allowed Gemini to return 3 cosmetics (dropping the CHEEK role) instead of the required 4, erroneously passing the state to `RESULT`.
**The Goal**: Enforce strict cardinality and role coverage, triggering a rejection/retry if the LLM drops a requested category.

### 1. Enforce Cosmetic Role Coverage
* **File**: `RecommendationValidator.kt`
* **Implementation**:
  * Track the explicitly requested cosmetic roles (based on the candidate manifest).
  * After mapping `finalCosmeticIds` to their `CosmeticRole` via `CosmeticRole.fromMacroCategory`, assert that **all requested roles are present**.
  * If a role is missing (e.g., `CHEEK`), append an error to `validationErrors`.

### 2. Enforce Strict Cardinality
* **File**: `RecommendationValidator.kt`
* **Implementation**:
  * Assert that exactly 3 clothing items (or 2 if shoes are unavailable) are selected.
  * Assert that the number of cosmetic items matches the number of requested roles.
  * If `errors.isNotEmpty()`, `isValid` must evaluate to `false`.

### 3. Implement Bounded Automatic Retry
* **File**: `StyleSimulatorEngine.kt`
* **Implementation**:
  * In `executeAndCache`, check `validation.isValid`.
  * If `!validation.isValid`, treat it as an execution failure (`reason = "VALIDATION_FAILED"`) and trigger a retry.
  * **Constraint**: Limit retries to a bounded count (e.g., `maxRetries = 2`). Changing provider or detail level must **not** silently weaken the structural composition requirements.
  * **Invariant**: Every retry must validate against the same `RecommendationComposition` derived before the first LLM call.

---

## Part 2: Typed Intent Profiling & Scoring

**The Problem**: "Super fun colorful outfit" yielded a Khaki/Ivory/Camel outfit because deterministic candidate ranking overly favored safe, neutral appearance harmony over the explicit user intent.
**The Goal**: Translate free-form user intent into deterministic, mathematically enforceable candidate scoring modifiers before Gemini sees them.

### 1. Create a Richer `StyleIntentProfile` Model
* **File**: `StyleModels.kt`
* **Implementation**:
  ```kotlin
  data class StyleIntentProfile(
      val colorfulness: Float = 0.5f, 
      val colorContrast: Float = 0.5f,
      val novelty: Float = 0.5f,
      val formality: Float = 0.5f
  )
  ```

### 2. Implement Deterministic Intent Analyzer
* **File**: `IntentAnalyzer.kt` (New)
* **Implementation**:
  * Parse `context.intent` string for expressive keywords using weighted lexical evidence to map to the multi-dimensional profile.
  * `colorful` $\rightarrow$ `+0.8 colorfulness`
  * `vibrant` $\rightarrow$ `+0.9 colorfulness`
  * `bright` $\rightarrow$ `+0.7 colorfulness`
  * `fun` $\rightarrow$ `+0.4 colorfulness, +0.5 novelty`
  * `minimalist` $\rightarrow$ `-0.7 colorfulness, -0.6 novelty`
  * `professional` $\rightarrow$ `+0.8 formality`
  * `casual` $\rightarrow$ `-0.4 formality`
  * Clamp each dimension to `[0,1]`.

### 3. Perceptual Chroma Candidate Scoring
* **File**: `WardrobeCandidateFilter.kt`
* **Implementation**:
  * Extract CIELAB / $L^*C^*h^\circ$ Chroma ($C^*$) from `item.colorHex`.
  * Apply a preference curve rather than absolute penalties:
    * High colorfulness intent + high chroma $\rightarrow$ strong bonus
    * High colorfulness intent + medium chroma $\rightarrow$ mild bonus
    * High colorfulness intent + neutral $\rightarrow$ neutral / slight penalty (allows neutrals as supporting bases)
  * Evaluate the ensemble distribution (maximum chroma, mean chroma, percentage of chromatic items, hue diversity) rather than a simple average.
  * **Invariant 1**: Intent-derived scores must influence candidate retrieval/ranking **before** Top-K candidate selection and before Gemini sees the candidate set.
  * **Invariant 2**: Intent scoring may influence candidate ranking, but it must **never override** hard constraints (mandatory anchors, required clothing slots, required cosmetic roles, wardrobe availability, or validator requirements). Soft intent preferences are optimized only after satisfying hard constraints.

---

## Part 3: Intent Fulfillment Scoring (Separate from FASHIONISTA)

**The Problem**: An 88.2 FASHIONISTA score on a neutral outfit masks the fact that the recommendation completely failed the "colorful" user request.
**The Goal**: Expose a multidimensional "Intent Fulfillment" metric explicitly defining how well the recommendation answered the user's prompt based on structured evidence.

### 1. Define Intent Fulfillment Result
* **File**: `StyleModels.kt` or `GenerateStyleResultUseCase.kt`
* **Implementation**:
  ```kotlin
  data class IntentFulfillment(
      val score: Float, // 0.0 to 100.0
      val dimensions: IntentFulfillmentDimensions,
      val unmetIntent: List<String>
  )

  data class IntentFulfillmentDimensions(
      val colorfulness: Float,
      val novelty: Float,
      val formality: Float
  )
  ```
  Add this to `StyleResult` alongside `fashionistaScore`.

### 2. Create `IntentFulfillmentEvaluator`
* **File**: `IntentFulfillmentEvaluator.kt` (New)
* **Implementation**:
  * Evaluates structured properties of the validated selected ensemble (e.g., CIELAB chroma, categories, materials).
  * **Invariant**: `IntentFulfillmentEvaluator` is 100% deterministic and does **not** inspect LLM rationale text.
  * Evaluates independently from FASHIONISTA. (Aesthetic quality $\neq$ Request satisfaction).

### 3. Update the UI State
* **File**: `StyleResultUiState.kt` & `StyleResultScreen.kt`
* **Implementation**:
  * Expose `intentFulfillment`.
  * Display a dedicated UI component: `"Intent Fulfillment: 35/100"` with breakdown dimensions and `unmetIntent` tags.
  * Clearly separates *what was asked for* (Recommendation Fulfillment) from *how good it looks* (FASHIONISTA).
