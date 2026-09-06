# Implementation Plan: User Intent Fulfillment & Validator Hardening

Based on the architectural review in `ArchRev54.md`, this document outlines the step-by-step implementation plan to resolve the critical validator failure and bridge the gap between user intent and deterministic candidate scoring.

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
  * If `errors.isNotEmpty()`, `isValid` must be `false`.

### 3. Implement Automatic Retry/Fallback in the Engine
* **File**: `StyleSimulatorEngine.kt`
* **Implementation**:
  * In `executeAndCache`, check `validation.isValid`.
  * If `!validation.isValid`, do **not** return the blueprint. Treat it as an execution failure (`reason = "VALIDATION_FAILED"`).
  * The engine's loop will automatically try the next detail level or next provider, eventually hitting the deterministic fallback if the LLM repeatedly fails the invariant.

---

## Part 2: Typed Intent Profiling & Scoring

**The Problem**: "Super fun colorful outfit" yielded a Khaki/Ivory/Camel outfit because deterministic candidate ranking favored safe, neutral appearance harmony over the explicit intent.
**The Goal**: Translate free-form user intent into deterministic, mathematically enforceable candidate scoring modifiers before Gemini sees them.

### 1. Create `StyleIntentProfile` Model
* **File**: `StyleModels.kt`
* **Implementation**:
  ```kotlin
  data class StyleIntentProfile(
      val colorfulness: Float = 0.5f, // 0.0 (monochrome/neutral) to 1.0 (vibrant/high-chroma)
      val novelty: Float = 0.5f,
      val formality: Float = 0.5f
  )
  ```

### 2. Implement Deterministic Intent Analyzer
* **File**: `IntentAnalyzer.kt` (New)
* **Implementation**:
  * Parse `context.intent` string for keywords.
  * "colorful", "bright", "fun", "vibrant", "neon" $\rightarrow$ `colorfulness = 1.0f`
  * "neutral", "minimalist", "muted", "subtle" $\rightarrow$ `colorfulness = 0.0f`

### 3. Inject Intent Profile into Candidate Scoring
* **File**: `WardrobeCandidateFilter.kt`
* **Implementation**:
  * In `calculateScore`, parse the candidate's `colorHex` into HSL to evaluate its saturation/chroma.
  * If the `StyleIntentProfile.colorfulness` is high ($> 0.8$), apply a significant multiplier/bonus to candidates with high saturation (e.g., Electric Coral `#FF5F1F`).
  * Penalize highly neutral/muted colors (Khaki, Camel) when a high colorfulness intent is detected.

---

## Part 3: Intent Fulfillment Scoring (Separate from FASHIONISTA)

**The Problem**: An 88.2 FASHIONISTA score on a neutral outfit masks the fact that the recommendation completely failed the "colorful" user request.
**The Goal**: Expose an "Intent Fulfillment" metric explicitly defining how well the recommendation answered the user's prompt.

### 1. Define Intent Fulfillment Result
* **File**: `StyleModels.kt` or `GenerateStyleResultUseCase.kt`
* **Implementation**:
  ```kotlin
  data class IntentFulfillment(
      val score: Float, // 0.0 to 100.0
      val feedback: String // e.g., "Missed the mark on 'colorful'."
  )
  ```
  Add this to `StyleResult` alongside `fashionistaScore`.

### 2. Create `IntentFulfillmentEvaluator`
* **File**: `IntentFulfillmentEvaluator.kt` (New)
* **Implementation**:
  * Takes the final `StyleBlueprint` and `StyleIntentProfile`.
  * If `intent.colorfulness` is high, but the `blueprint.recommendedPalette` averages low saturation/chroma, return a low score (`30/100`).

### 3. Update the UI State
* **File**: `StyleResultUiState.kt` & `StyleResultScreen.kt`
* **Implementation**:
  * Expose `intentFulfillmentScore`.
  * Display a dedicated UI component: `"Intent Fulfillment: 35/100 - Aesthetically pleasing, but lacked the requested colorful elements."`
  * This explicitly separates *what was asked for* (Recommendation Fulfillment) from *how good it looks* (FASHIONISTA).
