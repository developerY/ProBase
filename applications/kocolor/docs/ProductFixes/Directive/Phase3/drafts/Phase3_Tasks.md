Phase 2 is verified and complete. Your Phase 2 audit confirms that the compact Nano scaffold sits at 82 tokens (328 chars), `provider.countTokens()` is called at most once per provider attempt (`StyleSimulatorEngine.kt:528`), `maxOutputTokens` and `temperatureOverride` are wired across Firebase, BYOK, and Gemma, `PromptCacheRepository` uses the `"\u001F"` delimiter inside a thread-safe `Collections.synchronizedMap`, `RotationScoringUseCase` quantizes timestamps into `86_400_000L` UTC day-buckets, and all 55 unit tests pass across all 5 modules.

Below is the complete, copy-pasteable agent specification for **Phase 3 (P2 — Fashion Math Discrimination, Calibration, Intent Fulfillment & Word-Boundary NLP)** based on `fashion_algorithms_assessment.md`, `Tasks.md`, and `Tasks_2.md`.

```markdown
# AGENT MISSION: Execute Phase 3 (P2 — Fashion Math Discrimination, Intent Fulfillment & Word-Boundary NLP)

You are executing **Phase 3 (P3.1 through P3.5)** of the KoColor remediation plan (`RoadMapToPro.md` / `Tasks.md` / `Tasks_2.md`).

## STRICT EXECUTION INVARIANTS (READ FIRST)
1. **Do NOT recreate deleted 6-feature classes:** `ChromaticHarmonyEngine`, `CalibrationCurve`, `GlcmTextureEngine`, `SilhouetteEngine`, `VisualMassEngine`, `CosmeticIntegrationEngine`, `VisualHierarchyEngine`, and `CompositionEngine` were permanently deleted in Phase 1.0. Skip the gated `ChromaticHarmonyEngine` / `CalibrationCurve` bullets and apply all Phase 3 scoring work strictly to the live 3-evaluator `FashionistaEngine` pipeline (`ColorHarmonyEngine`, `ColorHarmonicsEvaluator`, `SilhouetteGrader`, `ContrastAnchorEvaluator`).
2. **Do NOT regress Phase 1 or Phase 2 invariants:**
   - Keep `SilhouetteGrader` free of `runBlocking` and database queries (it must continue accepting preloaded `List<ClothingItem>`).
   - Keep `StyleSimulatorEngine.countTokens()` at a single call site outside the heuristic fit loop.
   - Keep `ByokAiProvider` and `GemmaRestFallbackProvider` `UnsupportedOperationException` multimodal guards intact.
3. **Never delete unit test files:** Update any existing unit test assertions that relied on the old lenient evaluator baselines (`78 / 75 / 70`) and add all required Phase 3 unit tests.

---

## P3.1: Widen Live Scoring Dynamic Range & Fix Single-Chroma Scoring

**Problem:**
1. In `ColorHarmonyEngine.kt` (`applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorHarmonyEngine.kt:184`), palettes with `<= 1` chromatic color automatically return `38f` out of `40f` (near-perfect score for trivial or flat monotone input), and empty input can still earn high baseline points.
2. In the live 3-evaluator path (`ColorHarmonicsEvaluator`, `SilhouetteGrader`, `ContrastAnchorEvaluator`), lenient baselines (`78±20`, `75` with `-20/+30`, `70±25`) compress almost every outfit into `55–98`, making the `80`-point `isApproved` gate pass by construction and leaving poor outfits unpenalized.

### Required Changes:

1. **Empty-Outfit Guard (`FashionistaEngine.kt`, `ColorHarmonyEngine.kt`):**
   - If the resolved clothing item list is empty (`items.isEmpty()`), `FashionistaEngine` and all three evaluators MUST immediately return a score of `0` (`totalScore = 0`, `isApproved = false`, `breakdown = (0.0, 0.0, 0.0)`), preventing empty manifests from ever receiving a non-zero score.

2. **Fix Single-Chroma / Achromatic Neutral-Contrast Scoring (`ColorHarmonyEngine.kt`):**
   - Locate the palette geometry / harmony calculation around line 184 where `chromaticColors.size <= 1` currently returns a hardcoded `38f` (out of `40f`).
   - Replace the hardcoded `38f` with a continuous lightness-separation ($L^*$) evaluation across all colors in the outfit:
     - If `allColors.size <= 1`, return `0f` (a single item has no multi-item harmony).
     - Compute the lightness span across the outfit's LCh colors: `val lSpan = (allColors.maxOf { it.l } - allColors.minOf { it.l })`.
     - Reward intentional monochromatic/achromatic tonal layering (`lSpan` in `30f..65f` peaks up to `36f / 40f`), penalize flat muddy monotony (`lSpan < 12f` scores `10f..16f / 40f`), and moderately score extreme starkness (`lSpan > 75f` scores `26f..30f / 40f`) using a smooth piecewise-linear curve with no step-function cliffs.
   - Smooth any remaining discrete $\Delta E$ step cliffs in `ColorHarmonyEngine` using linear interpolation between band thresholds.

3. **Recalibrate Live Evaluator Baselines to `50` AND Scale Spreads (`ColorHarmonicsEvaluator.kt`, `SilhouetteGrader.kt`, `ContrastAnchorEvaluator.kt`, `FashionistaEngine.kt`):**
   - **WARNING (Math Gate):** Lowering baselines to `50` without widening the modifier spreads caps the blended score at `74.75 < 80`, which would cause *every* outfit to fail `isApproved`. You MUST scale the spreads proportionally alongside the `50` baseline so each evaluator spans `[0, 100]` and the blended total ($0.40 \times \text{Color} + 0.35 \times \text{Silhouette} + 0.25 \times \text{Contrast}$) can reach $\ge 95$:
     - **`ColorHarmonicsEvaluator.kt`:** Recenter the baseline from `78` to `50` and widen the harmony delta multiplier (`avg * 10` -> `avg * 25`, or map normalized `[0.0, 1.0]` harmony across `[0, 100]`) so clashing palettes score `< 45`, neutral/average palettes score `~50–70`, and harmonious multi-item palettes with strong skin-temperature alignment reach `90–100`.
     - **`SilhouetteGrader.kt`:** Recenter the baseline from `75` to `50`. Scale positive proportion modifiers (e.g., tapered balance `+15` -> `+25`, rule-of-thirds / volume contrast `+15` -> `+25`) so an ideal proportioned silhouette reaches `100`, and scale negative penalties (e.g., all-oversized / conflicting volume `-20` -> `-35`) so boxy/unbalanced silhouettes drop to `15–35`. Clamp to `[0, 100]`.
     - **`ContrastAnchorEvaluator.kt`:** Recenter the baseline from `70` to `50` and scale the contrast/depth alignment modifiers from `±25` to `±50` so ideal user-to-outfit contrast alignment reaches `95–100` and severe contrast mismatches drop to `0–35`. Clamp to `[0, 100]`.
   - Verify that `FashionistaScore.isApproved` (`totalScore >= 80`) passes for well-matched outfits (`totalScore >= 85`, with top editorial combinations reaching `>= 95`) and fails (`totalScore < 60`) for clashing/unbalanced outfits.

---

## P3.2: Complete `IntentFulfillmentEvaluator` & Numeric Appearance Matching

**Problem:**
1. `IntentFulfillmentEvaluator.kt` observes only `colorfulness` and hardcodes `contrast = 0.5f`, `novelty = 0.5f`, and `formality = 0.5f`. Any requested novelty `> 0.6` is *always* reported unmet, and the `0–100` score ignores contrast, novelty, and formality.
2. `ContrastAnchorEvaluator.kt` and cosmetic temperature scorers in `WardrobeCandidateFilter.kt` / `DeterministicContextEngine.kt` match free-text appearance strings via `.contains("High")` / `.contains("Light")`.

### Required Changes:

1. **Implement Real `contrast`, `novelty`, and `formality` Observation in `IntentFulfillmentEvaluator.kt`:**
   - Update `IntentFulfillmentEvaluator.evaluate(...)` to accept:
     - The resolved `clothingItems: List<ClothingItem>` and `cosmeticItems: List<CosmeticItem>`
     - `rotationScores: Map<String, Double>` (or `Map<String, Float>`, matching `StyleRequestContext.rotationScores` / `RotationScoringUseCase` output)
   - Update all callers of `IntentFulfillmentEvaluator.evaluate(...)` (`StyleSimulatorEngine.kt` and unit tests) to pass `context.rotationScores`.
   - Compute the 4 observed dimensions in `[0.0f, 1.0f]`:
     - **`observedColorfulness`:** Keep the existing chroma ($C^*$) calculation normalized to `[0.0f, 1.0f]`.
     - **`observedContrast`:** Convert selected items' hex colors to CIELAB $L^*$ (via `ColorHarmonyEngine.hexToLCh` or `ColorSpaceConverter`). If `>= 2` items exist, compute `((maxL - minL) / 85.0f).coerceIn(0.0f, 1.0f)` (so an $L^*$ span of $\ge 85$ maps to `1.0f` and identical lightness maps to `0.0f`; fallback to `0.0f` if `< 2` items).
     - **`observedNovelty`:** Inspect `RotationScoringUseCase.kt` to confirm the polarity of `rotationScores[itemId]`:
       - Note: In `RotationScoringUseCase.kt`, verify whether `1.0` represents a *penalty* (worn within 24h) or *freshness* (unworn).
       - Map the selected items' rotation values so **unworn/fresh items yield `observedNovelty = 1.0f`** and **items worn within 24h yield `observedNovelty = 0.0f`** (i.e., `1.0f - meanPenalty` if `rotationScores` stores penalty, or `meanFreshness` if it stores freshness; default to `1.0f` for items with no wear history).
     - **`observedFormality`:** Compute from the resolved `clothingItems` ONLY (cosmetics do not have a formality field and must not dilute the average). Map the core-model `ClothingItem.formality` enum (`Formality` — do NOT confuse with taxonomy `FormalityLevel`) to `[0.0f, 1.0f]` using its rank/ordinal normalized by the maximum enum rank:
       ```kotlin
       val formalityValues = clothingItems.map { item ->
           // Map CASUAL -> 0.0f up to FORMAL/GALA -> 1.0f
           item.formality.ordinal.toFloat() / (Formality.entries.size - 1).coerceAtLeast(1).toFloat()
       }
       val observedFormality = if (formalityValues.isNotEmpty()) formalityValues.average().toFloat().coerceIn(0f, 1f) else 0.5f
       ```
   - **Multi-Dimensional Fulfillment Score (`0–100`):**
     - Compute the fulfillment score across all active intent dimensions (`colorfulness`, `contrast`, `novelty`, `formality`) rather than `colorfulness` alone:
       ```kotlin
       val diffs = listOf(
           kotlin.math.abs(intentProfile.targetColorfulness - observedColorfulness),
           kotlin.math.abs(intentProfile.targetContrast - observedContrast),
           kotlin.math.abs(intentProfile.targetNovelty - observedNovelty),
           kotlin.math.abs(intentProfile.targetFormality - observedFormality)
       )
       val meanError = diffs.average().toFloat()
       val fulfillmentScore = ((1.0f - meanError) * 100.0f).coerceIn(0.0f, 100.0f)
       ```

2. **Replace Fragile String `.contains()` Appearance Checks with Typed Telemetry (`ContrastAnchorEvaluator.kt`, `WardrobeCandidateFilter.kt`, `DeterministicContextEngine.kt`):**
   - Inspect `ContrastAnchorEvaluator.kt` and cosmetic/wardrobe temperature scorers for free-text `.contains("High")`, `.contains("Low")`, `.contains("Light")`, `.contains("Deep")`, `.contains("Warm")`, `.contains("Cool")`.
   - Where numeric `ColorTelemetry` (`lStar`, `cStar`, `bStar`, `itaAngle`) or typed enums (`ColorTemperature`, `ContrastLevel`, `SkinDepth`) are available on the context/profile, evaluate them directly (e.g., numeric ITA / $L^*$ thresholds or enum equality `==`).
   - Where a legacy string field on `UserProfile` / `StyleRequestContext` is still read as a fallback, parse it into a canonical enum via an explicit mapper (`ContrastLevel.fromKey(raw)` / `ColorTemperature.fromKey(raw)` using exact token/enum matching) instead of raw substring `.contains()`.

---

## P3.3: Shared Word-Boundary Keyword Matcher & Stop-Word Stripping

**Problem:**
Four live scorers split intent or item names on whitespace and call raw `String.contains()`:
1. `WardrobeCandidateFilter.calculateScore` (+2.0 per match)
2. `WardrobeCandidateFilter.calculateCosmeticScore` (+0.75 per match)
3. `HeuristicStyleEngine.smartPick`
4. `SilhouetteGrader` (volume/proportion keyword classification)

Because there are no word boundaries or stop-words, an intent like `"a red dress"` matches nearly every item via `"a"`, and `"top"` matches `"stop"`. (`GreedyRehydrator` is already off this list because Phase 1.2 deleted its matchers).

### Required Changes:

1. **Create Shared `KeywordMatcher` Utility (`applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/util/KeywordMatcher.kt`):**
   - Implement a pure, allocation-light word-boundary tokenizer and matcher:
     ```kotlin
     package com.zoewave.probase.kocolor.data.util

     object KeywordMatcher {
         private val WORD_REGEX = Regex("\\b[a-z0-9]+\\b")

         val STOP_WORDS: Set<String> = setOf(
             "a", "an", "the", "in", "on", "for", "with", "to", "of",
             "and", "or", "is", "are", "at", "by", "from", "as", "into",
             "my", "me", "i", "want", "need", "something", "style", "look", "outfit"
         )

         /**
          * Extracts lowercase alphanumeric whole words of length >= 2, excluding stop-words.
          * Also normalizes simple English plurals (trailing "es" / "s" for words >= 4 chars)
          * so "boots" matches "boot" and "dresses" matches "dress".
          */
         fun tokenize(text: String): Set<String> {
             if (text.isBlank()) return emptySet()
             return WORD_REGEX.findAll(text.lowercase())
                 .map { it.value }
                 .filter { it.length >= 2 && it !in STOP_WORDS }
                 .flatMap { word ->
                     val stem = normalizeStem(word)
                     if (stem != word) sequenceOf(word, stem) else sequenceOf(word)
                 }
                 .toSet()
         }

         private fun normalizeStem(word: String): String = when {
             word.length > 4 && word.endsWith("resses") -> word.removeSuffix("es")
             word.length > 4 && word.endsWith("ches") -> word.removeSuffix("es")
             word.length > 4 && word.endsWith("shes") -> word.removeSuffix("es")
             word.length > 4 && word.endsWith("xes") -> word.removeSuffix("es")
             word.length > 3 && word.endsWith("s") && !word.endsWith("ss") -> word.removeSuffix("s")
             else -> word
         }

         /**
          * Returns true if [targetText] contains [keyword] as a whole word (or multi-word phrase)
          * respecting word boundaries.
          */
         fun containsWholeWord(targetText: String, keyword: String): Boolean {
             if (targetText.isBlank() || keyword.isBlank()) return false
             val queryTokens = tokenize(keyword)
             if (queryTokens.isEmpty()) return false
             val targetTokens = tokenize(targetText)
             return targetTokens.containsAll(queryTokens)
         }

         /**
          * Counts how many distinct non-stopword tokens from [intentQuery] appear as whole words
          * in [itemText].
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

2. **Replace All 4 Substring Matchers with `KeywordMatcher`:**
   - **`WardrobeCandidateFilter.kt` (`calculateScore` & `calculateCosmeticScore`):**
     - Replace `intent.split(" ")` + `.contains(...)` with `KeywordMatcher.countMatchingTokens(...)` (or `KeywordMatcher.hasAnyMatchingToken(...)`) so stop-words like `"a"` or `"for"` never award `+2.0` / `+0.75` points, and `"lip"` never matches `"eclipse"`.
   - **`HeuristicStyleEngine.kt` (`smartPick`):**
     - Replace raw substring `.contains()` with `KeywordMatcher.hasAnyMatchingToken(itemSearchableText, intent)`.
   - **`SilhouetteGrader.kt`:**
     - Replace raw `.contains(...)` checks on garment names/materials/notes (e.g., `"crop"`, `"slim"`, `"fitted"`, `"oversized"`, `"wide"`, `"tailored"`, `"structured"`) with `KeywordMatcher.containsWholeWord(...)` so `"top"` never matches `"stop"` and `"slim"` only matches whole tokens.

---

## P3.4: Confirm Engine Stub Cleanup (Phase 1.0 Cross-Check)

Verify that zero references or dead imports remain to `GlcmTextureEngine`, `SilhouetteEngine`, `CosmeticIntegrationEngine`, `VisualHierarchyEngine`, `CompositionEngine`, `ChromaticHarmonyEngine`, or `CalibrationCurve`.

---

## P3.5: Mandatory Unit Tests for Phase 3

Create or expand unit test suites in `:applications:kocolor:data` to verify every Phase 3 requirement:

1. **`KeywordMatcherTest.kt` (NEW):**
   - Verify `"a red dress for a party"` strips `"a"` and `"for"`, and does **not** match `"Alpine Jacket"` via `"a"`.
   - Verify `"lip"` does **not** match `"Eclipse Eyeshadow"`, and `"top"` does **not** match `"Stop Sign Red Tee"`.
   - Verify plural normalization: `"black boots"` matches `"Ankle Boot"` and `"summer dresses"` matches `"Silk Dress"`.
2. **`FashionistaEngineTest.kt` (NEW or EXPANDED):**
   - **Empty Outfit Guard:** Verify `evaluate(emptyList(), ...)` returns `totalScore == 0` and `isApproved == false`.
   - **Dynamic Range & Approval Gate:**
     - Verify a harmonious, well-proportioned, contrast-aligned outfit scores `>= 85` (`isApproved == true`), and an ideal editorial combination reaches `>= 95` (`EXCEPTIONAL_EDITORIAL`).
     - Verify a clashing, poorly proportioned, contrast-mismatched outfit scores `< 60` (`isApproved == false`).
   - **Single-Chroma Neutral Contrast:** Verify that an outfit of 3 identical `#808080` grey items ($L^*$ span = `0`) scores significantly lower on color harmony than an intentional high-contrast monochromatic outfit (`#1A1A1A` charcoal + `#F5F5F5` ivory, $L^*$ span > `70`), proving the hardcoded `38/40` shortcut is gone.
3. **`IntentFulfillmentEvaluatorTest.kt` (NEW or EXPANDED):**
   - Verify `observedContrast` reflects actual $L^*$ separation (`> 0.7` for black+white vs. `< 0.1` for two identical greys).
   - Verify `observedNovelty` reflects `rotationScores` (unworn items yield `observedNovelty >= 0.9f`, whereas recently worn items yield `observedNovelty <= 0.2f`, and a high-novelty intent `targetNovelty = 0.8f` is **met** when unworn items are selected).
   - Verify `observedFormality` differentiates `Formality.CASUAL` (`<= 0.25f`) from `Formality.FORMAL` (`>= 0.75f`).

---

## MANDATORY PHASE 3 VERIFICATION GATE (RUN & PASTE EXACT OUTPUT)

Do NOT stamp Phase 3 complete or write `Phase3_Audit_final.md` until every command below passes and its verbatim output is included in the audit report:

1. **`KeywordMatcher` Created & Wired into All 4 Target Files:**
   ```bash
   grep -rn "KeywordMatcher" applications/kocolor/data/src/

```

*(Must show usage in `WardrobeCandidateFilter.kt`, `HeuristicStyleEngine.kt`, `SilhouetteGrader.kt`, and `KeywordMatcherTest.kt`).*

2. **No Raw `.contains(` Substring Matchers Left in the 4 Target Files or `ContrastAnchorEvaluator.kt`:**
```bash
grep -n "\.contains(" \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/ContrastAnchorEvaluator.kt

```


*(Must return zero `String.contains` substring checks; `Set`/`List`/`Collection.contains` on typed enums/IDs is fine).*
3. **Hardcoded `0.5f` Removed from `IntentFulfillmentEvaluator.kt`:**
```bash
grep -n "0\.5f" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/IntentFulfillmentEvaluator.kt

```


*(Must show that `observedContrast` and `observedNovelty` are no longer hardcoded to `0.5f`).*
4. **Hardcoded `38f` Single-Chroma Shortcut Removed from `ColorHarmonyEngine.kt`:**
```bash
grep -n "38f\|38\.0" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorHarmonyEngine.kt

```


*(Must return 0 matches).*
5. **Phase 1 & Phase 2 Invariants Preserved:**
```bash
grep -n "runBlocking" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt
grep -n "countTokens" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt

```


*(Must show 0 `runBlocking` matches in `SilhouetteGrader.kt` and at most 1 `countTokens` call in `StyleSimulatorEngine.kt`).*
6. **Full Gradle Test & Assemble Verification:**
```bash
./gradlew :features:ai:core:testDebugUnitTest :features:ai:firebase:testDebugUnitTest :features:ai:local:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest :applications:kocolor:apps:mobile:assembleDebug

```



```

### Key Protections Built Into This Phase 3 Prompt

* **Fixes the Empty-Outfit 85.4/100 Bug:** Explicitly enforces a zero-item guard at the top of `FashionistaEngine` and `ColorHarmonyEngine` so an empty selection immediately returns `0 / 100` (`isApproved = false`)[cite: 4].
* **Prevents the `rotationScores` Polarity Bug in P3.2:** Forces the agent to check how `RotationScoringUseCase.kt` defines `rotationScores` and prove via a unit test (`IntentFulfillmentEvaluatorTest`) that unworn items produce `observedNovelty >= 0.9f` and fulfill a `targetNovelty = 0.8f` intent[cite: 1, 2, 3].
* **Enforces Stem-Aware Whole-Word NLP in P3.3:** Gives the agent the complete `KeywordMatcher` implementation with stop-word stripping (`"a"`, `"the"`, `"for"`) and plural stem normalization (`"boots"` $\leftrightarrow$ `"boot"`, `"dresses"` $\leftrightarrow$ `"dress"`) so fixing the `"a"` and `"top" ⊂ "stop"` bugs doesn't break legitimate plural user queries[cite: 1, 2, 3].

Hand this prompt to your coding agent for Phase 3, and share the resulting Phase 3 audit when it finishes!

```