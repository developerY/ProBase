Below is the revised **Phase 3 Agent Specification (v2)** 

```markdown
# AGENT MISSION: Execute Phase 3 (v2 — Fashion Math Discrimination, Active Intent Fulfillment & Canonical Word-Boundary NLP)

You are executing **Phase 3 (P3.1 through P3.5)** of the KoColor remediation plan (`RoadMapToPro.md` / `Tasks.md` / `Phase3_Open_feedback.md`).

## STRICT EXECUTION INVARIANTS (READ FIRST)
1. **Inspect existing repository types before editing:** The repository's actual domain models, enum definitions, score ranges, and call sites are authoritative. Inspect each target file before modifying signatures.
2. **Do NOT recreate any deleted 6-feature classes:** `FashionistaScorerImpl`, `DeterministicScorer`, `CalibrationCurve`, `InteractionModel`, `ChromaticHarmonyEngine`, `GlcmTextureEngine`, `TextureHarmonyEngine`, `SilhouetteEngine`, `VisualMassEngine`, `CosmeticIntegrationEngine`, `OutfitIntegrationEngine`, `VisualHierarchyEngine`, `CompositionEngine`, the 5 feature extractors, and `FeatureValue` were permanently deleted in Phase 1.0. Apply all Phase 3 scoring work strictly to the live 3-evaluator `FashionistaEngine` pipeline (`ColorHarmonyEngine`, `ColorHarmonicsEvaluator`, `SilhouetteGrader`, `ContrastAnchorEvaluator`).
3. **Do NOT regress Phase 1 or Phase 2 invariants:**
   - Keep `SilhouetteGrader` free of `runBlocking` and database queries (it must continue accepting preloaded `List<ClothingItem>`).
   - Keep `StyleSimulatorEngine.countTokens()` at a single call site outside the heuristic fit loop.
   - Keep `ByokAiProvider` and `GemmaRestFallbackProvider` `UnsupportedOperationException` multimodal guards intact.
4. **Never delete unit test files:** Update any existing unit test assertions affected by the recalibrated `[0, 100]` evaluator curves and add all new unit and integration tests required in P3.5.

---

## P3.1: Deterministic Single-Chroma Curve & 3-Evaluator Calibration

**Problem:**
1. In `ColorHarmonyEngine.kt` (`applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorHarmonyEngine.kt:184`), palettes with `<= 1` chromatic color automatically return `38f` out of `40f` (near-max score for trivial or flat grey input), and empty outfits can receive high baseline scores.
2. In the live 3-evaluator path (`ColorHarmonicsEvaluator`, `SilhouetteGrader`, `ContrastAnchorEvaluator`), lenient baselines (`78±20`, `75` with `-20/+30`, `70±25`) compress almost every outfit into `55–98`.

### Required Changes:

1. **Empty-Outfit Guard (`FashionistaEngine.kt`, `ColorHarmonyEngine.kt`, and Evaluators):**
   - If the resolved clothing item list is empty (`clothingItems.isEmpty()`), `FashionistaEngine` and each individual evaluator MUST immediately return `0` (`totalScore = 0`, `isApproved = false`, `breakdown = (0.0, 0.0, 0.0)`), ensuring empty manifests never receive a non-zero score.

2. **Deterministic Piecewise-Linear Single-Chroma Curve (`ColorHarmonyEngine.kt`):**
   - Locate the palette geometry / harmony branch around line 184 where `chromaticColors.size <= 1` currently returns `38f` (out of `40f`).
   - If total outfit colors `< 2`, return `0f`.
   - Otherwise, compute the lightness span across all outfit LCh colors:
     ```kotlin
     val lSpan = (allColors.maxOf { it.l } - allColors.minOf { it.l }).coerceAtLeast(0f)
     ```
   - Replace the hardcoded `38f` with a deterministic piecewise-linear interpolation function `evaluateNeutralLightnessSpan(lSpan: Float): Float` (returning a score out of `40f`) anchored at these exact control points:
     | `lSpan` ($L^*$ difference) | Control-Point Score (out of `40f`) | Semantic Meaning |
     | :--- | :--- | :--- |
     | `0f` | `10f` | Identical lightness; flat muddy monotony |
     | `12f` | `16f` | Still relatively flat tonal separation |
     | `30f` | `30f` | Intentional tonal separation |
     | `45f..55f` (plateau) | `36f` | Optimal monochromatic/achromatic tonal layering |
     | `65f` | `32f` | Good tonal separation |
     | `75f` | `28f` | Stark contrast |
     | `>= 85f` | `26f` | Extreme separation; moderate score |
   - Linearly interpolate between adjacent control points so there are zero step-function cliffs. Smooth any remaining discrete $\Delta E_{00}$ step cliffs in `ColorHarmonyEngine` using linear interpolation.

3. **5-Step Evaluator Calibration Protocol (`ColorHarmonicsEvaluator.kt`, `SilhouetteGrader.kt`, `ContrastAnchorEvaluator.kt`):**
   - **WARNING (Math Gate):** Lowering baselines to `50` without scaling the modifier spreads caps the blended score at $0.40(70) + 0.35(80) + 0.25(75) = 74.75 < 80$, which would cause *every* outfit to fail `isApproved`.
   - Before changing multipliers, inspect each evaluator's existing input measurements and follow this 5-step calibration for each of the 3 evaluators:
     1. Identify the exact numeric range of every raw input measurement and modifier in `ColorHarmonicsEvaluator`, `SilhouetteGrader`, and `ContrastAnchorEvaluator`.
     2. Normalize the underlying measurement into a documented range (`[0.0, 1.0]` or signed `[-1.0, +1.0]` centered at `0.0` = neutral baseline `50`).
     3. Map that normalized value onto `[0, 100]` centered at baseline `50` with full `±50` dynamic range (clamped to `[0, 100]`).
     4. Verify each evaluator independently satisfies these semantic anchors:
        - **Poor / Clashing / Unbalanced input:** `< 45`
        - **Neutral / Average / Unremarkable input:** `50–68`
        - **Good / Harmonious / Proportioned input:** `80–89`
        - **Exceptional / Editorial input:** `95–100`
     5. Verify that under the live `40% Color + 35% Silhouette + 25% Contrast` blend in `FashionistaEngine`:
        - Clashing/poor outfits score `< 60` (`isApproved == false`).
        - Solid everyday outfits score `80–89` (`isApproved == true`).
        - Exceptional editorial outfits reach `>= 95` (`EXCEPTIONAL_EDITORIAL` reachable).

---

## P3.2: Active-Dimension Intent Fulfillment & Typed Appearance Matching

**Problem:**
1. `IntentFulfillmentEvaluator.kt` observes only `colorfulness` and hardcodes `contrast = 0.5f`, `novelty = 0.5f`, and `formality = 0.5f`.
2. Unconditionally averaging all 4 dimensions penalizes outfits on dimensions the user never requested (e.g., when a user asks for `"colorful and unusual"`, `contrast` and `formality` were not requested).
3. `ContrastAnchorEvaluator.kt`, `WardrobeCandidateFilter.kt`, and `DeterministicContextEngine.kt` use fragile string `.contains("High")` / `.contains("Light")` checks on appearance fields.

### Required Changes:

1. **Compute Real Observations for All 4 Dimensions (`IntentFulfillmentEvaluator.kt`):**
   - If `clothingItems.isEmpty()`, immediately return a zero/unfulfilled result (`score = 0`, `status = UNFULFILLED` or `NOT_SPECIFIED` if no intent was given) — do **not** fabricate `observedFormality = 0.5f` for an empty outfit.
   - Update `IntentFulfillmentEvaluator.evaluate(...)` to accept the resolved `clothingItems: List<ClothingItem>`, `cosmeticItems: List<CosmeticItem>`, and `rotationScores: Map<String, Double>` (or `Float`, matching `StyleRequestContext.rotationScores`).
   - Compute the 4 observed properties in `[0.0f, 1.0f]`:
     - **`observedColorfulness`:** Existing chroma ($C^*$) calculation normalized to `[0.0f, 1.0f]`.
     - **`observedContrast`:** Convert selected items' hex colors to CIELAB $L^*$. For `>= 2` items, compute `((maxL - minL) / 85.0f).coerceIn(0.0f, 1.0f)` (`0.0f` if `< 2` items).
     - **`observedNovelty`:** Inspect `RotationScoringUseCase.kt` to verify the exact polarity of `rotationScores[itemId]`. Map the selected items' rotation scores so **unworn/fresh items yield `observedNovelty = 1.0f`** and **items worn within 24h yield `observedNovelty = 0.0f`**.
     - **`observedFormality`:** Inspect the actual `ClothingItem` domain model to confirm its canonical formality type (use the type actually stored on `ClothingItem`, not a similarly named taxonomy enum). Map its enum levels to `[0.0f, 1.0f]` using an **explicit, exhaustive `when` expression** (do **not** rely on `.ordinal`, which breaks if enum declaration order is reorganized). Compute the mean across `clothingItems` only (cosmetics do not have formality and must not dilute the garment formality average).

2. **Score ONLY Active Intent Dimensions (`IntentAnalyzer.kt`, `IntentFulfillmentEvaluator.kt`):**
   - Inspect `IntentProfile` (or `StyleIntentProfile`) and `IntentAnalyzer.kt` to see how user-specified dimensions are represented.
   - Ensure `IntentProfile` explicitly tracks which dimensions (`colorfulness`, `contrast`, `novelty`, `formality`) are **active** (either via an explicit `activeDimensions: Set<IntentDimension>` / boolean flags populated by `IntentAnalyzer` when keywords for that dimension match, or by checking non-null / non-default active targets).
   - **Fulfillment Scoring Rule:**
     - If **no** dimensions are active (blank intent or no recognized dimension keywords), report `status = NOT_SPECIFIED` and do not penalize the outfit.
     - If one or more dimensions are active, compute the fulfillment score (`0–100`) and met/unmet status **solely across the active requested dimensions** — never penalize an outfit for unrequested dimensions sitting at a neutral `0.5f` default.

3. **Replace Fragile String `.contains()` Appearance Checks with Typed Telemetry (`ContrastAnchorEvaluator.kt`, `WardrobeCandidateFilter.kt`, `DeterministicContextEngine.kt`):**
   - Inspect `ContrastAnchorEvaluator.kt`, `WardrobeCandidateFilter.kt`, and `DeterministicContextEngine.kt` for free-text `.contains("High")`, `.contains("Low")`, `.contains("Light")`, `.contains("Deep")`, `.contains("Warm")`, `.contains("Cool")`.
   - Use numeric `ColorTelemetry` (`lStar`, `cStar`, `bStar`, `itaAngle`) or typed enum comparisons (`==`) wherever available, and parse any legacy profile string through an explicit canonical enum mapper (`fromKey()` / exact token match) instead of substring `.contains()`.

---

## P3.3: Canonical-Stem `KeywordMatcher` & Consumer Integration

**Problem:**
Four live components use raw `String.contains()` substring matching (`WardrobeCandidateFilter.calculateScore`, `WardrobeCandidateFilter.calculateCosmeticScore`, `HeuristicStyleEngine.smartPick`, and `SilhouetteGrader`), causing `"a"` to match nearly every item, `"lip"` to match `"eclipse"`, and `"top"` to match `"stop"`.

### Required Changes:

1. **Create Canonical-Stem `KeywordMatcher` (`applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/util/KeywordMatcher.kt`):**
   - **CRITICAL DESIGN FIX:** Every word must normalize to **one canonical stem token** (never emit both the plural and singular into the token set, which causes double-counting and breaks asymmetric `containsAll` checks).
   - Implement `containsWholeWord()` to require a **contiguous token sequence** for multi-word phrases, while `countMatchingTokens()` performs unordered canonical-token overlap:
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
          * Normalizes a lowercase word into a single canonical stem so singular and plural
          * forms ("boot" / "boots", "dress" / "dresses") map to the exact same token.
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
          * for unordered intent-token overlap scoring.
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
          * Returns an ordered List of canonical stems for contiguous phrase matching.
          */
         fun tokenizeSequence(text: String): List<String> {
             if (text.isBlank()) return emptyList()
             return WORD_REGEX.findAll(text.lowercase())
                 .map { normalizeStem(it.value) }
                 .toList()
         }

         /**
          * Returns true if [targetText] contains [keyword] as a whole word (or contiguous
          * multi-word phrase) after canonical stem normalization.
          */
         fun containsWholeWord(targetText: String, keyword: String): Boolean {
             if (targetText.isBlank() || keyword.isBlank()) return false
             val querySeq = tokenizeSequence(keyword)
             if (querySeq.isEmpty()) return false
             val targetSeq = tokenizeSequence(targetText)
             if (targetSeq.isEmpty()) return false
             return Collections.indexOfSubList(targetSeq, querySeq) != -1
         }

         /**
          * Counts how many distinct non-stopword canonical tokens from [intentQuery]
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

2. **Wire `KeywordMatcher` Into All 4 Consumers:**
   - **`WardrobeCandidateFilter.calculateScore`:** Replace whitespace split + `.contains()` with `KeywordMatcher.countMatchingTokens(...)`.
   - **`WardrobeCandidateFilter.calculateCosmeticScore`:** Replace whitespace split + `.contains()` with `KeywordMatcher.countMatchingTokens(...)`.
   - **`HeuristicStyleEngine.smartPick`:** Replace substring `.contains()` with `KeywordMatcher.hasAnyMatchingToken(...)`.
   - **`SilhouetteGrader.kt`:** Replace garment keyword `.contains()` checks with `KeywordMatcher.containsWholeWord(...)`.

---

## P3.4: Comprehensive Dead-Code Verification

Verify that all 21 files deleted in Phase 1.0 remain absent from the repository:
- Scorers/Engines (13): `FashionistaScorerImpl`, `DeterministicScorer`, `CalibrationCurve`, `InteractionModel`, `ChromaticHarmonyEngine`, `GlcmTextureEngine`, `TextureHarmonyEngine`, `SilhouetteEngine`, `VisualMassEngine`, `CosmeticIntegrationEngine`, `OutfitIntegrationEngine`, `VisualHierarchyEngine`, `CompositionEngine`.
- Extractors (5): `ColorFeatureExtractor`, `CompositionFeatureExtractor`, `HierarchyFeatureExtractor`, `SilhouetteFeatureExtractor`, `TextureFeatureExtractor`.
- Domain Types (3): `FashionistaScorer`, `FashionistaObservation`, `FeatureValue`.

---

## P3.5: Mandatory Unit & Consumer Integration Tests

Create or expand unit test suites in `:applications:kocolor:data` covering every mathematical curve, evaluator, and consumer integration:

1. **`KeywordMatcherTest.kt` (Unit Tests for Tokenizer & Phrase Matcher):**
   - **Canonical Stem Symmetry:** Verify `tokenize("black boots") == setOf("black", "boot")` and `tokenize("Ankle Boot") == setOf("ankle", "boot")`.
   - **No Double-Counting on Plurals:** Verify `countMatchingTokens("Ankle Boots", "boots") == 1` (never `2`) and `containsWholeWord("Ankle Boot", "boots") == true`.
   - **Contiguous Phrase Matching:** Verify `containsWholeWord("Tailored Red Dress", "red dress") == true`, whereas `containsWholeWord("Red Blouse with a Dress", "red dress") == false` (while `countMatchingTokens("Red Blouse with a Dress", "red dress") == 2`).
   - **Substring & Stop-Word Protection:** Verify `"a red dress"` does not match `"Alpine Jacket"` via `"a"`, `"lip"` does not match `"Eclipse"`, and `"top"` does not match `"Stop"`.

2. **Consumer Integration Tests (Prove All 4 Consumers Use `KeywordMatcher` Correctly):**
   - **`WardrobeCandidateFilterTest.kt`:**
     1. *Wardrobe Scorer:* Verify an intent full of stop-words (`"a look for the style"`) awards `0.0` intent bonus to `"Alpine Coat"`.
     2. *Cosmetic Scorer:* Verify intent `"lip"` awards `0.0` bonus to `"Eclipse Eyeshadow"` while awarding `+0.75` to `"Velvet Lip Tint"`.
   - **`HeuristicStyleEngineTest.kt`:**
     - Verify `smartPick` with intent `"top"` does NOT match `"Stop Sign Graphic Tee"`.
   - **`SilhouetteGraderTest.kt`:**
     - Verify `"slim"` does NOT match a garment description containing `"slim"` only as a substring of another word (e.g., `"muslin"` / `"sublimated"` / `"slimy"`), while matching `"Slim Fit Trousers"`.

3. **Individual Evaluator & `ColorHarmonyEngine` Tests (`ColorHarmonyEngineTest.kt`, `ColorHarmonicsEvaluatorTest.kt`, `SilhouetteGraderTest.kt`, `ContrastAnchorEvaluatorTest.kt`, `FashionistaEngineTest.kt`):**
   - **Single-Chroma Control Points (`ColorHarmonyEngineTest`):** Test all regions of `evaluateNeutralLightnessSpan`:
     - Low end: `lSpan = 0f` $\rightarrow$ `10f`, `lSpan = 12f` $\rightarrow$ `16f`
     - Mid/Peak: `lSpan = 30f` $\rightarrow$ `30f`, `lSpan = 50f` $\rightarrow$ `36f`, `lSpan = 65f` $\rightarrow$ `32f`
     - High end: `lSpan = 75f` $\rightarrow$ `28f`, `lSpan = 90f` $\rightarrow$ `26f`
   - **Individual Evaluator Range Tests:** Test `ColorHarmonicsEvaluator`, `SilhouetteGrader`, and `ContrastAnchorEvaluator` individually to prove each evaluator outputs `0` on empty input, `< 45` on poor/clashing input, `50–68` on neutral input, and `>= 95` on optimal input (ensuring no single evaluator masks a broken sibling).
   - **Blended `FashionistaEngine` Gate Test:** Verify empty input returns `0` (`isApproved == false`), clashing outfit scores `< 60` (`isApproved == false`), good outfit scores `80–89` (`isApproved == true`), and optimal editorial outfit scores `>= 95`.

4. **`IntentFulfillmentEvaluatorTest.kt` (Active Dimensions & Observations):**
   - **Active-Only Scoring:** Verify that for an intent requesting *only* colorfulness and novelty (e.g., `"colorful bold new look"`), an outfit with high colorfulness and high novelty scores `>= 85` even if its contrast (`0.1f`) or formality (`0.9f`) diverges from `0.5f`.
   - **Unobservable / Empty Guard:** Verify an empty outfit returns `0` fulfillment rather than matching a neutral `0.5f` formality default.
   - **Formality & Novelty Polarity:** Verify `Formality.CASUAL` vs. `Formality.FORMAL` map accurately via the exhaustive `when` mapper, and unworn items yield `observedNovelty >= 0.9f` vs. `<= 0.2f` for items worn within 24h.

---

## MANDATORY PHASE 3 VERIFICATION GATE (RUN & PASTE EXACT OUTPUT)

Do NOT stamp Phase 3 complete or write `Phase3_Audit_final.md` until every check below is executed and categorized in the audit report:

### Category A: Expected Zero Matches (Must Return 0 Lines)
1. **All 21 Phase 1.0 Dead Files Remain Deleted:**
   ```bash
   find applications/kocolor/ -name "FashionistaScorerImpl.kt" -o -name "DeterministicScorer.kt" -o -name "CalibrationCurve.kt" -o -name "InteractionModel.kt" -o -name "ChromaticHarmonyEngine.kt" -o -name "GlcmTextureEngine.kt" -o -name "TextureHarmonyEngine.kt" -o -name "SilhouetteEngine.kt" -o -name "VisualMassEngine.kt" -o -name "CosmeticIntegrationEngine.kt" -o -name "OutfitIntegrationEngine.kt" -o -name "VisualHierarchyEngine.kt" -o -name "CompositionEngine.kt" -o -name "*FeatureExtractor.kt" -o -name "FashionistaScorer.kt" -o -name "FashionistaObservation.kt" -o -name "FeatureValue.kt"

```

2. **Hardcoded `38f` Single-Chroma Shortcut Gone from `ColorHarmonyEngine.kt`:**
```bash
grep -n "38f\|38\.0" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorHarmonyEngine.kt

```


3. **Zero `runBlocking` Calls in `SilhouetteGrader.kt`:**
```bash
grep -n "runBlocking" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt

```



### Category B: Inspected Context Matches (Must Paste & Justify Every Line)

4. **`KeywordMatcher` Wired Across All 4 Consumers & Tests:**
```bash
grep -rn "KeywordMatcher" applications/kocolor/data/src/

```


5. **Audit All `.contains(` Calls Across All 5 Target Files (Including `DeterministicContextEngine.kt`):**
```bash
grep -n "\.contains(" \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/ContrastAnchorEvaluator.kt

```


*(Permitted: `Set`/`List`/`Enum` collection membership checks. Prohibited: `String.contains` on intent words, garment names, or appearance strings).*
6. **Verify `observedContrast`, `observedNovelty`, and `observedFormality` Assignment Lines in `IntentFulfillmentEvaluator.kt`:**
```bash
grep -n -C 3 "observedContrast\|observedNovelty\|observedFormality" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/IntentFulfillmentEvaluator.kt

```



### Category C: Preserved Phase 2 Invariants & Full Gradle Verification

7. **Exactly One `provider.countTokens` Call in `StyleSimulatorEngine.kt`:**
```bash
grep -n "countTokens" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt

```


8. **Full Gradle Test & Assemble Verification:**
```bash
./gradlew :features:ai:core:testDebugUnitTest :features:ai:firebase:testDebugUnitTest :features:ai:local:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest :applications:kocolor:apps:mobile:assembleDebug

```



```

Hand this revised Phase 3 specification to your coding agent and share the resulting Phase 3 audit when it finishes.

```