Because a weaker coding agent can easily get overwhelmed by multi-file refactors, invent non-existent types, or write tautological tests (`assertTrue(true)`) just to pass a gate, this final **Phase 3 Agent Specification** is structured as a **strict 6-step micro-checkpoint runbook**:

* **Pre-Verified Code & Formulas:** Instead of asking the agent to derive stemming rules or calibration bands from scratch, the prompt provides the exact Kotlin code for `KeywordMatcher.kt` (all 6 behaviors pre-traced), the rank-normalized formality formula (verified against test thresholds), and exact result-construction snippets using field names confirmed against the current tree.

* **Mandatory "Read Before Write" Commands:** Before modifying `IntentFulfillmentEvaluator`, `SilhouetteGrader`, `ColorHarmonicsEvaluator`, or `ContrastAnchorEvaluator`, the agent is forced to inspect the existing file signatures and domain models (`ClothingItem`, `StyleIntentProfile`, `RotationScoringUseCase`) so it never hallucinates enum or field names.

* **No Default-Parameter Shortcuts:** New parameters (such as `rotationScores` in `IntentFulfillmentEvaluator.evaluate()`) are required to have **no default values**, forcing the Kotlin compiler to catch every un-updated call site.

* **Compile Gate After Every Single Step:** The agent must compile and test after each individual step (`Step 1` $\rightarrow$ `Step 6`) rather than editing 10 files at once and failing at the end.

Copy and paste the entire block below into your coding agent:

````markdown
# AGENT MISSION: Execute Phase 3 (Foolproof Step-by-Step Specification with Micro-Checkpoints)

You are executing **Phase 3 (P3.1 through P3.5)** of the KoColor remediation plan.

## STRICT ANTI-SHORTCUT RULES (VIOLATION = IMMEDIATE FAILURE)
1. **WORK ONE STEP AT A TIME:** Complete **Step 1**, run its Gradle verification command, and confirm it compiles before touching **Step 2**. Do NOT batch-edit all files at once.
2. **READ BEFORE YOU WRITE:** Before editing any existing Kotlin file, you MUST read the full file first so you use the exact package names, imports, data class properties, and enum names that exist in the repository.
3. **NO DEFAULT PARAMETER WORKAROUNDS:** When updating `IntentFulfillmentEvaluator.evaluate(...)` to accept `rotationScores`, do **NOT** add a default value (`= emptyMap()`). Let the Kotlin compiler force you to update `StyleSimulatorEngine.kt` and all test call sites.
4. **NO FAKE OR TAUTOLOGICAL TESTS:** Every unit test in Step 6 MUST instantiate the real production class and call its real production method. Writing `assertTrue(true)`, testing local dummy variables, or deleting existing test files is strictly forbidden.
5. **DO NOT RECREATE DELETED PHASE 1.0 FILES:** `FashionistaScorerImpl`, `DeterministicScorer`, `CalibrationCurve`, `InteractionModel`, `ChromaticHarmonyEngine`, `GlcmTextureEngine`, `TextureHarmonyEngine`, `SilhouetteEngine`, `VisualMassEngine`, `CosmeticIntegrationEngine`, `OutfitIntegrationEngine`, `VisualHierarchyEngine`, `CompositionEngine`, the 5 `*FeatureExtractor` classes, `FashionistaScorer`, `FashionistaObservation`, `FashionistaFeatureVector` (6-field version), and `FeatureValue` must remain deleted, as must `FashionistaScorerTest`.
6. **PRESERVE PHASE 1 & 2 INVARIANTS:**
   - Zero `runBlocking` calls in `SilhouetteGrader.kt`.
   - At most ONE `provider.countTokens(` call in `StyleSimulatorEngine.kt`.
   - Keep `UnsupportedOperationException` multimodal guards as the first statement of `ByokAiProvider.execute()` and `GemmaRestFallbackProvider.execute()`.

---

## STEP 1 (P3.3a): Create `KeywordMatcher.kt` & `KeywordMatcherTest.kt`

### 1A. Create `KeywordMatcher.kt`
Create `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/util/KeywordMatcher.kt` with this **exact** implementation (canonical single-stem normalization per word + contiguous sequence matching for phrases):

```kotlin
package com.zoewave.probase.kocolor.data.util

import java.util.Collections

object KeywordMatcher {
    private val WORD_REGEX = Regex("\\b[a-z0-9]+\\b")

    val STOP_WORDS: Set<String> = setOf(
        "a", "an", "the", "in", "on", "for", "with", "to", "of",
        "and", "or", "is", "are", "at", "by", "from", "as", "into",
        "my", "me", "i", "want", "need", "something", "style", "look", "outfit"
    )

    /**
     * Normalizes a lowercase word into ONE canonical stem so singular and plural
     * forms ("boot"/"boots", "dress"/"dresses", "accessory"/"accessories")
     * map to the exact same token without double-counting.
     */
    fun normalizeStem(word: String): String = when {
        word.length > 4 && word.endsWith("resses") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("ches") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("shes") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("xes") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("ies") -> word.removeSuffix("ies") + "y"
        word.length > 3 && word.endsWith("s") && !word.endsWith("ss") && !word.endsWith("us") -> word.removeSuffix("s")
        else -> word
    }

    /**
     * Returns a Set of unique canonical stems (length >= 2, stop-words removed)
     * for unordered token-overlap scoring.
     */
    fun tokenize(text: String): Set<String> {
        if (text.isBlank()) return emptySet()
        return WORD_REGEX.findAll(text.lowercase())
            .map { it.value }
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .map { normalizeStem(it) }
            .toSet()
    }

    /**
     * Returns an ordered List of canonical stems (preserving word positions, including
     * intervening words) so multi-word phrase checks require contiguous adjacency.
     */
    fun tokenizeSequence(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return WORD_REGEX.findAll(text.lowercase())
            .map { normalizeStem(it.value) }
            .toList()
    }

    /**
     * Returns true if [targetText] contains [keyword] as a whole word or contiguous
     * multi-word phrase after canonical stem normalization.
     * Rejects queries that consist solely of stop-words or single-character tokens.
     */
    fun containsWholeWord(targetText: String, keyword: String): Boolean {
        if (targetText.isBlank() || keyword.isBlank()) return false
        val rawWords = WORD_REGEX.findAll(keyword.lowercase()).map { it.value }.toList()
        if (rawWords.isEmpty() || rawWords.all { it.length < 2 || it in STOP_WORDS }) return false
        val querySeq = rawWords.map { normalizeStem(it) }
        val targetSeq = tokenizeSequence(targetText)
        if (targetSeq.isEmpty()) return false
        return Collections.indexOfSubList(targetSeq, querySeq) != -1
    }

    /**
     * Counts how many distinct non-stopword canonical stems from [intentQuery]
     * appear in [itemText]. Each concept is counted at most once.
     */
    fun countMatchingTokens(itemText: String, intentQuery: String): Int {
        val queryTokens = tokenize(intentQuery)
        if (queryTokens.isEmpty()) return 0
        val targetTokens = tokenize(itemText)
        return queryTokens.count { it in targetTokens }
    }

    fun hasAnyMatchingToken(itemText: String, intentQuery: String): Boolean =
        countMatchingTokens(itemText, intentQuery) > 0
}
```

### 1B. Create `KeywordMatcherTest.kt`
Create `applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/data/util/KeywordMatcherTest.kt` and verify all 6 behaviors:
```kotlin
package com.zoewave.probase.kocolor.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordMatcherTest {

    @Test
    fun `tokenize normalizes plurals to single canonical stem`() {
        assertEquals(setOf("black", "boot"), KeywordMatcher.tokenize("black boots"))
        assertEquals(setOf("ankle", "boot"), KeywordMatcher.tokenize("Ankle Boot"))
        assertEquals(setOf("silk", "dress"), KeywordMatcher.tokenize("Silk Dresses"))
    }

    @Test
    fun `countMatchingTokens counts plural match exactly once without double counting`() {
        assertEquals(1, KeywordMatcher.countMatchingTokens("Ankle Boots", "boots"))
        assertEquals(1, KeywordMatcher.countMatchingTokens("Ankle Boot", "boots"))
        assertEquals(1, KeywordMatcher.countMatchingTokens("Ankle Boots", "boot"))
    }

    @Test
    fun `containsWholeWord matches singular and plural symmetrically`() {
        assertTrue(KeywordMatcher.containsWholeWord("Ankle Boot", "boots"))
        assertTrue(KeywordMatcher.containsWholeWord("Ankle Boots", "boot"))
        assertTrue(KeywordMatcher.containsWholeWord("Summer Dresses", "dress"))
    }

    @Test
    fun `containsWholeWord requires contiguous sequence for multiword phrases`() {
        assertTrue(KeywordMatcher.containsWholeWord("Tailored Red Dress", "red dress"))
        assertFalse(KeywordMatcher.containsWholeWord("Red Blouse with a Dress", "red dress"))
        assertEquals(2, KeywordMatcher.countMatchingTokens("Red Blouse with a Dress", "red dress"))
    }

    @Test
    fun `stop words are stripped and never match substrings`() {
        assertEquals(0, KeywordMatcher.countMatchingTokens("Alpine Jacket", "a for the in on"))
        assertFalse(KeywordMatcher.containsWholeWord("Alpine Jacket", "a"))
    }

    @Test
    fun `substrings inside larger words do not match`() {
        assertFalse(KeywordMatcher.containsWholeWord("Eclipse Eyeshadow", "lip"))
        assertFalse(KeywordMatcher.containsWholeWord("Stop Sign Red Tee", "top"))
        assertFalse(KeywordMatcher.containsWholeWord("Sublimated Muslin Shirt", "slim"))
        assertTrue(KeywordMatcher.containsWholeWord("Slim Fit Trousers", "slim"))
    }
}
```

> **MICRO-CHECKPOINT 1:** Run `./gradlew :applications:kocolor:data:testDebugUnitTest --tests "com.zoewave.probase.kocolor.data.util.KeywordMatcherTest"`. Do NOT proceed to Step 2 until this test passes.

---

## STEP 2 (P3.3b & P3.2b): Wire `KeywordMatcher` into All 4 Consumers & Replace Fragile String `.contains(` Appearance Checks

### 2A. Read the 5 Target Files First
Read the full contents of:
1. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt`
2. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt`
3. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt`
4. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/ContrastAnchorEvaluator.kt`
5. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt`

### 2B. Replace Substring Keyword Matchers with `KeywordMatcher`
1. **`WardrobeCandidateFilter.kt`:**
    - In `calculateScore(...)`: Replace any `intent.split(...)` + `.contains(...)` loop with `val matchCount = KeywordMatcher.countMatchingTokens("${item.name} ${item.category} ${item.material.orEmpty()} ${item.notes.orEmpty()}", intent)` and add `matchCount * 2.0` (preserving the `+2.0` weight per matched concept).
    - In `calculateCosmeticScore(...)`: Replace any `intent.split(...)` + `.contains(...)` loop with `val matchCount = KeywordMatcher.countMatchingTokens("${item.name} ${item.macroCategory} ${item.shadeName.orEmpty()}", intent)` and add `matchCount * 0.75` (preserving the `+0.75` weight per matched concept).
2. **`HeuristicStyleEngine.kt`:**
    - `smartPick` is a local generic function using `nameSelector`/`notesSelector` with a random fallback. Extract its matching predicate into an `internal fun <T> findMatches(items: List<T>, intent: String, nameSelector: (T) -> String, notesSelector: (T) -> String?): List<T>` that returns items where `KeywordMatcher.hasAnyMatchingToken("${nameSelector(item)} ${notesSelector(item).orEmpty()}", intent)` is true. Reimplement `smartPick` as `findMatches(...).randomOrNull() ?: this.random()` (identical selection behavior, deterministically testable matcher).
3. **`SilhouetteGrader.kt`:**
    - Replace every `String.contains(...)` check on garment names, materials, silhouettes, or notes (e.g., `"crop"`, `"slim"`, `"fitted"`, `"tailored"`, `"oversized"`, `"wide"`, `"relaxed"`, `"structured"`, `"heels"`, `"high"`) with `KeywordMatcher.containsWholeWord(targetText, keyword)`.

### 2C. Replace Fragile String `.contains()` Appearance & Temperature Checks
1. Inspect `ContrastAnchorEvaluator.kt`, `WardrobeCandidateFilter.kt`, and `DeterministicContextEngine.kt` for any `String.contains("High")`, `contains("Low")`, `contains("Light")`, `contains("Deep")`, `contains("Warm")`, `contains("Cool")`.
2. Replace them with:
    - Numeric `ColorTelemetry` fields (`undertoneScore`, `depthScore`, `contrastScore`) or typed enum comparisons (`==`) where available on the context/profile.
    - Where a profile string field (`AppearanceProfile.undertone`/`depth`/`contrast`, `item.seasonalPalette` — confirm exact names when reading) is parsed, use `KeywordMatcher.containsWholeWord(fieldValue, "high")` / exact canonical enum resolution instead of raw substring `.contains(...)`.
3. Verify via `grep -n "\.contains("` across all 5 files above that **zero** `String.contains` calls remain (only `Set`/`List`/`Collection` membership checks are permitted).

> **MICRO-CHECKPOINT 2:** Run `./gradlew :applications:kocolor:data:compileDebugKotlin :applications:kocolor:data:testDebugUnitTest`. Fix any compile or test issues before Step 3.

---

## STEP 3 (P3.1a): Delete Dead Palette-Geometry Privates in `ColorHarmonyEngine.kt`

The `38`-point single-chroma branch lives in `evaluatePaletteGeometry`, which is `private` with zero callers (orphaned when Phase 1 deleted `calculateFashionistaScore`), along with `evaluateContrastAdherence` and `evaluateNeutralGrounding`. There is nothing to fix — delete, do not rehabilitate.

### 3A. Confirm Deadness First
Run and record the output of:
```bash
grep -rn "evaluatePaletteGeometry\|evaluateContrastAdherence\|evaluateNeutralGrounding\|calculateFashionistaScore" --include="*.kt" applications core features
```
Proceed only if matches are confined to the private declarations inside `ColorHarmonyEngine.kt` itself. Single-chroma tonal evaluation for the live path is specified fresh in Step 4 (`ColorHarmonicsEvaluator` L\*-span handling below), not salvaged from this dead code.

### 3B. Delete the Three Dead Privates
Delete `evaluatePaletteGeometry`, `evaluateContrastAdherence`, and `evaluateNeutralGrounding` from `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorHarmonyEngine.kt`. Keep every live method intact (`scoreCandidate`, `hexToLCh`, `calculateCompositeProfile`, `processGarment` and their callers).

> **MICRO-CHECKPOINT 3:** Run `./gradlew :applications:kocolor:data:compileDebugKotlin`.

---

## STEP 4 (P3.1b): 5-Step Calibration of the 3 Live Evaluators & `FashionistaEngine` Empty Guard

### 4A. Read All 4 Scoring Files First
Read the full contents of:
1. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/FashionistaEngine.kt`
2. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/ColorHarmonicsEvaluator.kt`
3. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt`
4. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/ContrastAnchorEvaluator.kt`

### 4B. Enforce the Empty-Outfit Guard
1. In `FashionistaEngine.kt`: If `blueprint.selectedClothingIds.isEmpty()`, immediately return `FashionistaScore(totalScore = 0f, isApproved = false, breakdown = FashionistaFeatureVector(colorHarmony = 0.0, silhouette = 0.0, contrast = 0.0))` without invoking sub-evaluators (confirm `totalScore`/`isApproved` are constructor parameters when reading; `FashionistaFeatureVector` is the 3-`Double` breakdown type in `FashionistaScore.kt`).
2. In `ColorHarmonicsEvaluator.kt` (`evaluate` returns `Float`), `SilhouetteGrader.kt` (`grade` returns `Float`), and `ContrastAnchorEvaluator.kt` (`evaluate` returns `Float`): If the clothing input is empty (`blueprint.selectedClothingIds` / `resolvedClothing` as applicable), immediately return `0f`.

### 4C. Calibrate Each Evaluator to `[0, 100]` Centered at `50`
Follow this exact calibration contract for each evaluator so the `40% Color + 35% Silhouette + 25% Contrast` blend in `FashionistaEngine` spans `[0, 100]` and can reach `>= 95` (above the `80`-point `isApproved` gate):

1. **`ColorHarmonicsEvaluator.kt` (Weight: 40%):**
    - NOTE: this evaluator is currently HSL hue-distance logic only and does NOT use `ColorHarmonyEngine` — do not wire the deleted Step 3 code. Add undertone alignment (thread user context through, mirroring `ContrastAnchorEvaluator.evaluate(blueprint, context)`'s signature — extend `evaluate(blueprint)` to also accept the context and update `FashionistaEngine` + call sites) and L\*-span tonal evaluation (compute CIELAB L\* per item via the `ColorUtils.colorToLAB` pattern already used in `IntentFulfillmentEvaluator.calculateChroma`).
    - Recenter the neutral baseline from `78` to `50` and widen the positive and negative modifiers to `±50` (clamped to `[0.0, 100.0]`):
        - Severe chromatic clash (e.g., high-chroma non-complementary ΔE clash + wrong undertone) must score `<= 40.0`.
        - Flat / unremarkable neutral single-color outfit ($L^*$ span = `0`) must score `50.0..62.0`.
        - Good harmonious multi-item outfit must score `80.0..89.0`.
        - Optimal complementary/analogous or `45..55` $L^*$-span tonal outfit with matching undertone must score `95.0..100.0`.

2. **`SilhouetteGrader.kt` (Weight: 35%):**
    - Recenter the baseline from `75.0` to `50.0` and widen the proportion modifiers to `±50.0` (clamped to `[0.0, 100.0]`):
        - Unbalanced / conflicting volume (e.g., oversized top + oversized/wide bottom with no structure) must apply a `-30.0` to `-35.0` penalty $\rightarrow$ score `15.0..20.0`.
        - Unremarkable neutral basics with no proportion modifiers must score `50.0..60.0`.
        - One strong proportion principle (e.g., fitted top + wide-leg/tailored bottom OR structured outerwear) must add `+32.0` $\rightarrow$ score `82.0`.
        - Optimal rule-of-thirds + tapered/structured balance (e.g., cropped/fitted top + high-waist/tailored bottom + elevating footwear/outerwear) must add `+46.0..+50.0` $\rightarrow$ score `96.0..100.0`.

3. **`ContrastAnchorEvaluator.kt` (Weight: 25%):**
    - Recenter the baseline from `70.0` to `50.0` and scale the contrast/depth alignment modifiers from `±25.0` to `±50.0` (clamped to `[0.0, 100.0]`):
        - High-contrast user paired with flat low-contrast outfit (or low-contrast user overwhelmed by extreme stark contrast) must penalize by `-25.0` to `-40.0` $\rightarrow$ score `10.0..25.0`.
        - Neutral / uncalibrated profile or moderate alignment must score `50.0..65.0`.
        - Good alignment between user contrast/depth and outfit lightness span must score `82.0..90.0`.
        - Ideal contrast and depth alignment must score `95.0..100.0`.

> **MICRO-CHECKPOINT 4:** Run `./gradlew :applications:kocolor:data:compileDebugKotlin`.

---

## STEP 5 (P3.2a): Active-Dimension `IntentFulfillmentEvaluator.kt` & Exhaustive Formality Mapping

### 5A. Read the Domain Models & Evaluator First
Read the full contents of:
1. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/IntentFulfillmentEvaluator.kt`
2. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/IntentAnalyzer.kt`
3. `applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RotationScoringUseCase.kt`
4. The `ClothingItem` model definition (to confirm the exact enum type and entries of `clothingItem.formality`: `Formality(LOUNGE(1), CASUAL(2), SMART_CASUAL(3), PROFESSIONAL(4), FORMAL(5), GALA(6))` with `rank: Int`).

### 5B. Track Active Requested Dimensions in `StyleIntentProfile` / `IntentAnalyzer.kt`
1. Inspect how `IntentAnalyzer.analyze(intent: String)` constructs `StyleIntentProfile`.
2. Add an explicit property on the profile model tracking which dimensions were actually triggered by the user's intent text, e.g.:
   ```kotlin
   enum class IntentDimension { COLORFULNESS, CONTRAST, NOVELTY, FORMALITY }
   ```
   and add `val activeDimensions: Set<IntentDimension> = emptySet()` to `StyleIntentProfile`.
3. In `IntentAnalyzer.analyze(intent: String)`, whenever a keyword shifts `colorfulness`, `colorContrast`, `novelty`, or `formality` away from the neutral default (or matches that dimension's vocabulary), add the corresponding `IntentDimension` to `activeDimensions`. Additionally, if a test constructs `StyleIntentProfile` directly with a non-`0.5f` target (`abs(target - 0.5f) > 0.01f`) and leaves `activeDimensions` empty, treat any dimension with `abs(target - 0.5f) > 0.01f` as active so direct unit-test constructions also work seamlessly!

### 5C. Compute Real Observations & Active-Only Score in `IntentFulfillmentEvaluator.kt`
1. **Update Signature (No Default Value on `rotationScores`):**
    - Update `IntentFulfillmentEvaluator.evaluate(...)` to require `rotationScores: Map<String, Double>` (or `Float`, matching `StyleRequestContext.rotationScores`) alongside the resolved `clothingItems: List<ClothingItem>` and `cosmeticItems: List<CosmeticItem>`.
    - Update `StyleSimulatorEngine.kt` to pass `context.rotationScores` into `intentFulfillmentEvaluator.evaluate(...)`.
2. **Empty-Outfit Guard:**
    - If `clothingItems.isEmpty()`, immediately return `IntentFulfillment(state = <specified-or-not per intent>, score = 0f, observedMetrics = ObservedEnsembleMetrics(0f, 0f, 0f, 0f), unmetIntent = <active dimension labels>)`. Never return `formality = 0.5f` for an empty outfit. Preserve the existing early-return behavior when intent is `StyleIntentState.NotSpecified` (score `null`).
3. **Compute All 4 Observed Metrics (`[0.0f, 1.0f]`):**
    - **`colorfulness`:** Existing chroma ($C^*$) calculation normalized to `[0.0f, 1.0f]`.
    - **`colorContrast`:** Convert all resolved items' hex codes to CIELAB $L^*$. If `allLStars.size >= 2`, compute `((allLStars.max() - allLStars.min()) / 85.0f).coerceIn(0.0f, 1.0f)`, else `0.0f`.
    - **`novelty`:** Check `RotationScoringUseCase.kt` to confirm whether `rotationScores[id]` is a penalty (`1.0` = worn in last 24h, `0.0` = fresh) or a freshness score (`1.0` = fresh, `0.0` = worn). Compute `novelty` across the selected items so **unworn items (`rotationScores` empty or fresh) produce `1.0f`** and **recently worn items (24h penalty) produce `0.0f`**.
    - **`formality`:** Compute across `clothingItems` ONLY (ignore cosmetics so they do not dilute garment formality). Prefer the rank formula `((item.formality.rank - 1) / 5f)` averaged over items (`LOUNGE`→`0.0`, `CASUAL`→`0.2`, `SMART_CASUAL`→`0.4`, `PROFESSIONAL`→`0.6`, `FORMAL`→`0.8`, `GALA`→`1.0`) — `rank` is an explicit `Int` property, NOT `.ordinal`, so this complies with the no-ordinal gate. Fall back to an exhaustive `when (item.formality)` only if rank semantics change.
4. **Score ONLY Active Dimensions:**
    - Determine the effective active dimensions:
      ```kotlin
      val effectiveActiveDimensions = intentProfile.activeDimensions.ifEmpty {
          buildSet {
              if (kotlin.math.abs(intentProfile.colorfulness - 0.5f) > 0.01f) add(IntentDimension.COLORFULNESS)
              if (kotlin.math.abs(intentProfile.colorContrast - 0.5f) > 0.01f) add(IntentDimension.CONTRAST)
              if (kotlin.math.abs(intentProfile.novelty - 0.5f) > 0.01f) add(IntentDimension.NOVELTY)
              if (kotlin.math.abs(intentProfile.formality - 0.5f) > 0.01f) add(IntentDimension.FORMALITY)
          }
      }
      ```
    - If `effectiveActiveDimensions.isEmpty()`, return `state = StyleIntentState.NotSpecified` with `score = null` (existing behavior) and do not penalize the outfit.
    - Otherwise, compute the fulfillment error **only** over `effectiveActiveDimensions` so unrequested dimensions sitting at `0.5f` never penalize the score!

> **MICRO-CHECKPOINT 5:** Run `./gradlew :applications:kocolor:data:compileDebugKotlin :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 6 (P3.5): Write the Complete Unit & Consumer Integration Test Suites

Create or update the following test classes in `applications/kocolor/data/src/test/java/...` and verify every single test passes:

1. **Individual Evaluator Tests (`ColorHarmonicsEvaluatorTest.kt`, `SilhouetteGraderTest.kt`, `ContrastAnchorEvaluatorTest.kt`):**
    - For **each** of the 3 evaluators independently, write 4 test methods:
        1. `emptyOutfit_returnsZero()` $\rightarrow$ asserts score is `0f`.
        2. `poorOrClashingInput_scoresBelow45()` $\rightarrow$ asserts score `< 45.0`.
        3. `neutralInput_scoresBetween50And68()` $\rightarrow$ asserts score in `50.0..68.0`.
        4. `exceptionalInput_scoresAtLeast95()` $\rightarrow$ asserts score `>= 95.0`.

2. **Blended `FashionistaEngineTest.kt`:**
    - `emptyOutfit_returnsZeroAndUnapproved()` $\rightarrow$ `totalScore == 0`, `isApproved == false`.
    - `clashingUnbalancedOutfit_scoresBelow60AndFailsGate()` $\rightarrow$ `totalScore < 60`, `isApproved == false`.
    - `goodEverydayOutfit_scoresBetween80And89AndPassesGate()` $\rightarrow$ `totalScore in 80..89`, `isApproved == true`.
    - `exceptionalEditorialOutfit_scoresAtLeast95()` $\rightarrow$ `totalScore >= 95`, `isApproved == true`.

3. **`IntentFulfillmentEvaluatorTest.kt`:**
    - `emptyOutfit_returnsZeroWithoutFabricatingFormality()` $\rightarrow$ with a specified intent (e.g. `StyleIntentProfile(colorfulness = 0.9f)`), empty clothing returns `observedMetrics.formality == 0f` and `score == 0f`.
    - `activeDimensionsOnly_unrequestedContrastAndFormalityDoNotPenalizeScore()` $\rightarrow$ Given an intent requesting only high colorfulness and high novelty (`colorfulness = 0.85f, novelty = 0.90f, activeDimensions = setOf(COLORFULNESS, NOVELTY)`), an unworn colorful formal outfit with low lightness contrast (`observedMetrics.colorContrast = 0.1f`, `observedMetrics.formality = 0.95f`) scores `>= 85`.
    - `noveltyAndFormality_reflectRotationAndRankMapping()` $\rightarrow$ Unworn items yield `observedMetrics.novelty >= 0.9f`, items worn within 24h yield `observedMetrics.novelty <= 0.2f`, and `CASUAL` vs. `FORMAL` clothing items produce `observedMetrics.formality <= 0.25f` vs. `>= 0.80f`.

4. **4 Consumer Keyword Integration Tests:**
    - In **`WardrobeCandidateFilterTest.kt`:**
        - `wardrobeScorer_stopWordsDoNotIncreaseScore()`: Score `"Alpine Jacket"` against intent `"a look for the style"` vs. `""` and assert the scores are identical (`0.0` intent bonus).
        - `cosmeticScorer_lipDoesNotMatchEclipse()`: Score `"Eclipse Eyeshadow"` against intent `"lip"` and assert it receives `0.0` intent bonus, while `"Velvet Lip Tint"` receives `+0.75`.
    - In **`HeuristicStyleEngineTest.kt`:**
        - `findMatches_topDoesNotMatchStopSignTee()`: Call `findMatches(listOf(stopSignTee, silkBlouse), "top", { it.name }, { it.notes })` directly and assert the result contains neither item (no whole-word `"top"` present); call with `"silk"` and assert exactly `silkBlouse` is returned.
    - In **`SilhouetteGraderTest.kt`:**
        - `silhouetteGrader_slimDoesNotMatchSublimatedMuslin()`: Assert a garment named `"Sublimated Muslin Shirt"` does not trigger the `"slim"` fitted modifier, whereas `"Slim Fit Trousers"` does.

---

## MANDATORY PHASE 3 VERIFICATION GATE (RUN & PASTE EXACT OUTPUT)

Do NOT mark Phase 3 complete or write `Phase3_Audit_final.md` until every command below is run and its verbatim output is pasted into the audit report:

### Category A: Expected Zero Matches (MUST Return 0 Lines)
```bash
# A1. All Phase 1.0 dead files remain deleted (plus the 6-field vector):
find applications/kocolor/ -name "FashionistaScorerImpl.kt" -o -name "DeterministicScorer.kt" -o -name "CalibrationCurve.kt" -o -name "InteractionModel.kt" -o -name "ChromaticHarmonyEngine.kt" -o -name "GlcmTextureEngine.kt" -o -name "TextureHarmonyEngine.kt" -o -name "SilhouetteEngine.kt" -o -name "VisualMassEngine.kt" -o -name "CosmeticIntegrationEngine.kt" -o -name "OutfitIntegrationEngine.kt" -o -name "VisualHierarchyEngine.kt" -o -name "CompositionEngine.kt" -o -name "*FeatureExtractor.kt" -o -name "FashionistaScorer.kt" -o -name "FashionistaObservation.kt" -o -name "FeatureValue.kt" -o -name "FashionistaFeatureVector.kt"

# A2. Dead palette-geometry privates are gone from ColorHarmonyEngine.kt:
grep -rn "evaluatePaletteGeometry\|evaluateContrastAdherence\|evaluateNeutralGrounding\|calculateFashionistaScore" --include="*.kt" applications core features

# A3. Zero runBlocking calls in SilhouetteGrader.kt:
grep -n "runBlocking" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt

# A4. Zero .ordinal usage in IntentFulfillmentEvaluator.kt:
grep -n "\.ordinal" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/IntentFulfillmentEvaluator.kt
```

### Category B: Line-by-Line Inspected Context Matches (Paste Output & Justify Every Line)
```bash
# B1. KeywordMatcher wired across all 4 production consumers + tests:
grep -rn "KeywordMatcher" applications/kocolor/data/src/

# B2. Audit all .contains( calls across all 5 target files (ONLY Collection/Set/Enum membership permitted; ZERO String.contains allowed):
grep -n "\.contains(" \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/ContrastAnchorEvaluator.kt

# B3. Confirm observedMetrics, formality rank mapping & activeDimensions in IntentFulfillmentEvaluator.kt:
grep -n -C 2 "observedMetrics\|formality.rank\|activeDimensions" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/IntentFulfillmentEvaluator.kt
```

### Category C: Preserved Phase 2 Invariants & Full Gradle Suite
```bash
# C1. Exactly one provider.countTokens call in StyleSimulatorEngine.kt:
grep -n "countTokens" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt

# C2. Multimodal rejection guards intact in BYOK and Gemma:
grep -rn "UnsupportedOperationException" features/ai/core/ features/ai/firebase/

# C3. Execute full unit test suite and mobile debug assembly:
./gradlew :features:ai:core:testDebugUnitTest :features:ai:firebase:testDebugUnitTest :features:ai:local:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest :applications:kocolor:apps:mobile:assembleDebug
```
````

Paste this into your agent, and once it finishes running the 6 steps, send over the `Phase3_Audit_final.md` output so we can verify every line of its `grep` and test results.
