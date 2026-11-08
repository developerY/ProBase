Phase 3 (Pass 2) is **100% complete** and verified. Every issue from the Phase 3 punchlist is resolved in the working tree:

* **Weather Regex Fix Verified (`WardrobeCandidateFilter.kt:62`):** `TEMP_REGEX` now parses numeric Celsius values (`tempC >= 26.0` for `isHot`, `tempC < 7.0` for `isCold`, matching your `ThermalBand` cutoffs), eliminating the bug where `"Temp: -"` tokenized to `["temp"]` and marked `28°C` weather as cold.


* **7-Point Piecewise-Linear Curve Verified (`ColorHarmonicsEvaluator.kt:108–120`):** All seven control points (`0f`, `12f`, `30f`, `45f..55f`, `65f`, `75f`, `85f+`) interpolate continuously without step cliffs, verified by `singleChromaLightnessCurve_matchesAllSevenControlPoints()`.
* **Strict `80f..89f` Approval Gate Restored (`FashionistaEngineTest.kt:80–82`):** Asserts `isAtLeast(80f)`, `isAtMost(89f)`, and `isApproved == true`.


* **All 4 NLP Consumers Tested (`HeuristicStyleEngineTest.kt`):** Total passing unit tests across the project reached **83 passed, 0 failed** (with `:applications:kocolor:data` up from 33 in Phase 2 to 61 in Phase 3).

---

### Final Phase: Foolproof Step-by-Step Agent Specification for Phase 4

Because your coding agent works best with small, isolated steps, explicit "Read Before You Write" gates, and concrete code patterns, this **Phase 4 Specification (P4.1–P4.3)** is broken into **6 sequential micro-checkpoints**. Copy and paste the block below into your coding agent:

```markdown
# AGENT MISSION: Execute Phase 4 (Foolproof Step-by-Step Specification: Orchestration Gears, Thresholds, Provenance Normalization & Dead Code Purge)

You are executing **Phase 4 (P4.1 through P4.3)** — the final phase of the KoColor remediation plan (`RoadMapToPro.md` / `Tasks.md` / `Tasks_2.md`).

## STRICT ANTI-SHORTCUT RULES (VIOLATION = IMMEDIATE FAILURE)
1. **WORK ONE STEP AT A TIME:** Complete **Step 1**, run its Gradle micro-checkpoint command, and confirm it passes before starting **Step 2**. Do NOT batch-edit all files at once.
2. **READ BEFORE YOU WRITE:** Before modifying any file, read its full contents first so you use the exact package names, data class fields, and method signatures present in the repository.
3. **DO NOT TOUCH `GreedyRehydrator.kt`:** `GreedyRehydrator` is already a pure ID-to-UI mapper since Phase 1.2. Do not add fallback logic back into it.
4. **PRESERVE ALL PHASE 1, 2 & 3 INVARIANTS:**
   - Keep `SilhouetteGrader.kt` free of `runBlocking`.
   - Keep `StyleSimulatorEngine.kt` at a single `provider.countTokens(` call site and preserve the `K = 4` floor-of-1 step-down logic.
   - Keep `ByokAiProvider.kt` and `GemmaRestFallbackProvider.kt` `UnsupportedOperationException` multimodal guards intact.
   - Keep `KeywordMatcher` and `TEMP_REGEX` intact in `WardrobeCandidateFilter.kt`.
5. **NEVER DELETE UNIT TEST FILES:** All existing 83 unit tests must continue to pass, and new Phase 4 unit tests must be added in Step 6.

---

## STEP 1 (P4.1): Deterministic Heuristic Fallback & Dress-Aware Slot Picking (`HeuristicStyleEngine.kt`, `DeterministicContextEngine.kt`)

**Problem:**
1. `HeuristicStyleEngine.kt:39` (`smartPick`) uses `.randomOrNull() ?: this.random()`, making the deterministic fallback random, and pads `recommendedPalette` with random hardcoded neutrals (phantom colors that downstream harmony scoring treats as real outfit colors).
2. `HeuristicStyleEngine` never selects dresses (`DRESSES` / `ONE_PIECE`) and adds redundant `BOTTOMS` even when a locked anchor is a dress.

### Required Changes:
1. **Read First:** Read `HeuristicStyleEngine.kt` and `DeterministicContextEngine.kt`.
2. **Eliminate Randomness in `HeuristicStyleEngine.kt`:**
   - Remove all calls to `.random()` and `.randomOrNull()`.
   - In `smartPick(...)` (or candidate selection), select deterministically using the highest-scored candidate (`maxByOrNull { ... }` or `firstOrNull()` on an already rank-sorted list, with stable `it.id` tie-breaking: `sortedWith(compareByDescending<...> { score }.thenBy { it.id }).firstOrNull()`).
   - Build `recommendedPalette` **strictly** from the actual hex codes of the selected clothing and cosmetic items (`selectedItems.mapNotNull { it.colorHex }.distinct()`). Remove all random neutral hex padding.
3. **First-Class `DRESSES` / `ONE_PIECE` Support in `HeuristicStyleEngine.kt` & `DeterministicContextEngine.kt`:**
   - In `HeuristicStyleEngine.kt`:
     - If a locked anchor or top-ranked candidate is a dress (`ClothingCategory.DRESSES` or `ONE_PIECE`), treat it as satisfying **both** `TOPS` and `BOTTOMS` slots — do **not** pick a separate `BOTTOMS` item alongside a dress!
     - If no `TOPS`+`BOTTOMS` pair is available (or if a dress scores higher than the best top+bottom), allow selecting a dress + `SHOES` (+ optional `OUTERWEAR` when thermally permitted).
   - In `DeterministicContextEngine.kt`:
     - Align candidate viability filtering so wardrobes whose upper/lower body coverage comes from `DRESSES` / `ONE_PIECE` are recognized as slot-viable alongside `TOPS` + `BOTTOMS`.

> **MICRO-CHECKPOINT 1:**
> 1. Run `grep -n "\.random" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt` (must return **0 matches**).
> 2. Run `./gradlew :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 2 (P4.2a & b): Centralize Thresholds into `KoColorTaxonomy.kt` & Fix Audit Funnel / Anchor Priority Logging

**Problem:**
1. Temperature, rotation, and chroma cutoffs are scattered and inconsistent across files: outerwear ban is `> 17°C` in simulator/prompt vs. `>= 18°C` in `DeterministicContextEngine` vs. `HOT >= 26°C` in `WardrobeCandidateFilter`; rotation thresholds (`0.7` hard / `0.5` soft / 3-day / 7-day) and chroma gates (`10/15/20/25/30`) are duplicated.
2. In `DeterministicContextEngine.kt:58`, `afterWeather`, `afterRotation`, and `finalEligible` are all assigned the same final count.
3. In `StyleAuditLogger.kt:103`, `"Priority: 4.00 / 4.00"` is a hardcoded string literal.

### Required Changes:
1. **Read First:** Read `KoColorTaxonomy.kt` (or locate the canonical taxonomy/constants file in `:applications:kocolor:data`), `DeterministicContextEngine.kt`, `PromptAssembler.kt`, `WardrobeCandidateFilter.kt`, and `StyleAuditLogger.kt`.
2. **Centralize Threshold Constants (`KoColorTaxonomy.kt`):**
   - Add a centralized `KoColorThresholds` object inside `KoColorTaxonomy.kt` (or the shared taxonomy package) defining the single source of truth for:
     - **Thermal Bands (°C):** `HOT_MIN_C = 26.0`, `WARM_MIN_C = 20.0`, `MILD_MIN_C = 14.0`, `COOL_MIN_C = 7.0`
     - **Outerwear Cutoffs (°C):** `OUTERWEAR_BAN_TEMP_C = 18.0` (outerwear banned when `tempC >= 18.0` and dry; unify `StyleSimulatorEngine`, `DeterministicContextEngine`, and `PromptAssembler` on this constant so `> 17` vs `>= 18` discrepancy is eliminated)
     - **Rotation Thresholds:** `ROTATION_HARD_BAN_PENALTY = 0.7`, `ROTATION_SOFT_PENALTY = 0.5`, `RECENT_WEAR_HARD_HOURS = 24`, `RECENT_WEAR_SOFT_DAYS = 3`
     - **Chroma Gates ($C^*$):** `ACHROMATIC_CHROMA_GATE = 10.0f`, `MUTED_CHROMA_GATE = 20.0f`, `HIGH_CHROMA_GATE = 30.0f`
   - Update `DeterministicContextEngine.kt`, `WardrobeCandidateFilter.kt`, `PromptAssembler.kt`, and `StyleSimulatorEngine.kt` to reference `KoColorThresholds`.
3. **Record True Sequential Audit Funnel Counts (`DeterministicContextEngine.kt`):**
   - In `DeterministicContextEngine.kt` (around line 58), split the filtering pipeline into sequential intermediate lists so the audit funnel records the true count at each stage:
     1. `initialCount = rawWardrobe.size`
     2. `afterWeatherList = rawWardrobe.filter { /* weather/thermal eligibility */ }` $\rightarrow$ `afterWeather = afterWeatherList.size`
     3. `afterRotationList = afterWeatherList.filter { /* rotation/availability eligibility */ }` $\rightarrow$ `afterRotation = afterRotationList.size`
     4. `finalEligibleList = ...` $\rightarrow$ `finalEligible = finalEligibleList.size`
   - Confirm `initialCount >= afterWeather >= afterRotation >= finalEligible` and that the three variables do NOT reference the same list `.size`.
4. **Log Real Anchor Priority (`StyleAuditLogger.kt`):**
   - In `StyleAuditLogger.kt` (around line 103), replace the hardcoded `"Priority: 4.00 / 4.00"` literal with the actual formatted priority / composite score of the resolved anchor item.

> **MICRO-CHECKPOINT 2:** Run `./gradlew :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 3 (P4.2c): Safely Normalize `ColorModels.totalScore` to `[0.0, 1.0]` & Document Pigment-Only Cosmetics Policy

**Problem:**
In `ColorModels.kt:27`, wardrobe provenance uses `compositeScore` (`0.0–1.0`, while anchors use `2.5` or `4.0`), whereas cosmetic provenance leaves `compositeScore = 0` and falls back to the sub-score sum (`~1.0–5.0`).
**CRITICAL TRAP:** Anchors currently sort first in `sortedPartitionedPool` ONLY because `2.5` / `4.0` is greater than `1.0`, and `StyleAuditLogger` checks `totalScore > 2.0`. You MUST decouple anchor priority **before** normalizing `totalScore` to `[0.0, 1.0]`.

### Required Changes:
1. **Read First:** Read `ColorModels.kt`, `DeterministicContextEngine.kt`, `WardrobeCandidateFilter.kt`, and `StyleAuditLogger.kt`.
2. **Step 3A — Add Explicit Anchor Priority Signal (`ColorModels.kt`, `DeterministicContextEngine.kt`, `StyleAuditLogger.kt`):**
   - On the provenance model in `ColorModels.kt` (e.g., `ItemColorProvenance` / `ColorProvenance`), add:
     ```kotlin
     val isAnchor: Boolean = false,
     val anchorPriority: Float = 0.0f // e.g., 4.0f for primary user lock, 2.5f for secondary anchor, 0.0f for regular candidates
     ```
   - In `DeterministicContextEngine.kt`, set `isAnchor = true` and `anchorPriority = 4.0f` (or `2.5f`) when constructing provenance for locked/resolved anchors.
   - Update `sortedPartitionedPool` (and any candidate sorting in `DeterministicContextEngine` / `StyleSimulatorEngine`) to sort by `compareByDescending<...> { it.isAnchor }.thenByDescending { it.anchorPriority }.thenByDescending { it.totalScore }`.
   - Update `StyleAuditLogger.kt` to detect and display anchors using `provenance.isAnchor` and `provenance.anchorPriority` instead of checking `totalScore > 2.0`.
3. **Step 3B — Normalize `totalScore` to `[0.0f, 1.0f]` Across Wardrobe and Cosmetics (`ColorModels.kt:27`):**
   - Once anchor sorting and audit logging use `isAnchor` / `anchorPriority`, update `totalScore` in `ColorModels.kt:27` (and `WardrobeCandidateFilter.kt` provenance builders) so **both** wardrobe and cosmetic `totalScore` values are normalized into `[0.0f, 1.0f]` (clamped via `.coerceIn(0.0f, 1.0f)`).
4. **Step 3C — Document Pigment-Only Cosmetics Policy (`WardrobeCandidateFilter.kt`):**
   - Add explicit KDoc on `WardrobeCandidateFilter` (and `RecommendationValidator`) documenting the intentional **Pigment-Only Cosmetics Policy**: only chromatic pigment categories (`EYES`, `DIMENSION`/`CHEEK`, `LIPS`, `NAILS`) are eligible for AI styling and CIELAB harmony evaluation; `PREP`, `COMPLEXION`, `HAIR`, and non-pigment categories are excluded by design so transparent skincare/prep items never distort color harmony math.

> **MICRO-CHECKPOINT 3:** Run `./gradlew :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 4 (P4.3a): Port Preflight Slot-Completeness Gate, Wire `RoleGapAnalyzer` into `adaptContextToProvider` & Delete Dead Code

**Problem:**
1. `DeterministicCatalogEngine.kt` is dead code (0 callers) and operates on `CatalogRecord`, whereas `StyleSimulatorEngine` operates on `ClothingItem`/`CosmeticItem`. However, its preflight slot-completeness gate idea is needed in `StyleSimulatorEngine` to prevent calling cloud AI when required slots cannot be filled.
2. Several dead methods and unused constructor injections remain across `WardrobeCandidateFilter`, `DeterministicContextEngine`, and `FirebaseAiClient`.

### Required Changes:
1. **Read First:** Read `DeterministicCatalogEngine.kt` (if present), `StyleSimulatorEngine.kt`, `DeterministicContextEngine.kt`, `RoleGapAnalyzer.kt`, `WardrobeCandidateFilter.kt`, and `FirebaseAiClient.kt`.
2. **Ensure Preflight Slot-Completeness Gate on `StyleSimulatorEngine` & Delete `DeterministicCatalogEngine.kt`:**
   - In `StyleSimulatorEngine.generateBlueprint()` (or `DeterministicContextEngine.generateSelectionState()`), verify that before any cloud `AiProvider` is called, a preflight check confirms the eligible wardrobe pool (plus anchors) can satisfy a complete outfit:
     - At least one `SHOES` item AND (at least one `DRESSES`/`ONE_PIECE` item OR (at least one `TOPS` item AND at least one `BOTTOMS` item)).
     - If the wardrobe is empty or cannot satisfy required slots, immediately bypass cloud AI providers (saving tokens) and fall back deterministically or return an empty/aborted result.
   - Delete the unused `DeterministicCatalogEngine.kt` file (`find applications/ -name "DeterministicCatalogEngine.kt"`).
3. **Wire `RoleGapAnalyzer` into `adaptContextToProvider` & Delete `enforceRoleDiversity`:**
   - In `StyleSimulatorEngine.adaptContextToProvider()`, ensure `categorizedPool` prioritizes the unfilled categories identified by `RoleGapAnalyzer` (`selectionState.missingRoleRequirements`), while **preserving** the Phase 2.2 `slotsPerCategory` floor-of-1 step-down logic (`val slotsPerCategory = if (currentK <= minCandidateAdditions) 1 else ...`).
   - In `DeterministicContextEngine.kt`, delete the dead `enforceRoleDiversity(...)` method.
4. **Delete Remaining Dead Methods & Unused Injections (Assessment Section 5):**
   - In `WardrobeCandidateFilter.kt`: Delete dead methods `getCandidates(...)` and `getCosmeticCandidates(...)` if they have zero production callers (update or remove any obsolete test-only call sites).
   - In `DeterministicContextEngine.kt`: Remove unused constructor parameters (`rotationScoringUseCase` and `repository` if they are never called inside `DeterministicContextEngine`, updating its `@Inject constructor` and test instantiations).
   - In `FirebaseAiClient.kt`: Delete dead `getStyleAdvice(...)` and `estimateTokens(...)` methods (and `StyleTelemetry` if it has zero remaining references), keeping `generateContent(...)` and `countTokens(...)`.

> **MICRO-CHECKPOINT 4:** Run `./gradlew :features:ai:firebase:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 5 (P4.3b): Consolidate Duplicate Color-Math & JSON Brace-Stripping Utilities

**Problem:**
Assessment Section 5 identified duplicated color-math functions (`hexToLCh` ×2, chroma-weighted circular mean ×2, `calculateChroma` ×3) and JSON markdown-fence/brace-stripping logic duplicated across 4 files.

### Required Changes:
1. **Find All Duplicates First:**
   - Run `grep -rn "fun hexToLCh\|fun calculateChroma\|indexOf('{')\|lastIndexOf('}')" applications/ features/ core/` to inspect all duplicate implementations.
2. **Consolidate JSON Brace-Stripping (`JsonSanitizer`):**
   - Create (or unify in an existing shared utility) a single `extractJsonObject(raw: String): String` helper that strips markdown code fences (````json ... ````) and extracts the substring from the first `'{'` to the last `'}'`.
   - Delegate the duplicate brace-stripping blocks in `StyleSimulatorEngine`, `AnalyzerEngine`, `SuggestionEngine`, and `RecommendationValidator` (or `FirebaseAiClient`) to this single shared helper.
3. **Consolidate Duplicate Color Math (`ColorHarmonyEngine.kt` / `ColorSpace.kt` / `CircularStatistics.kt`):**
   - Ensure `ColorHarmonyEngine.hexToLCh` and `ColorSpaceConverter` do not maintain divergent sRGB$\rightarrow$Lab$\rightarrow$LCh math; delegate one to the other (converting `Double` $\leftrightarrow$ `Float` cleanly).
   - Delegate any inline `sqrt(a*a + b*b)` chroma helpers and inline chroma-weighted circular hue averages to the canonical functions in `ColorSpace.kt` / `CircularStatistics.kt`.

> **MICRO-CHECKPOINT 5:** Run `./gradlew :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest`.

---

## STEP 6 (P4.3c): Write the Final Phase 4 Unit Test Suites

Add or expand unit tests in `:applications:kocolor:data` (`src/test/java/...`) to lock in every Phase 4 guarantee:

1. **Expand `HeuristicStyleEngineTest.kt` (Determinism & Dress Slot Satisfaction):**
   - `generateFallback_isStrictlyDeterministicAndUsesRealHexPalette()`: Call `heuristicStyleEngine` twice with the same inputs; assert `result1.selectedClothingIds == result2.selectedClothingIds`, `result1.selectedCosmeticIds == result2.selectedCosmeticIds`, and assert that every hex in `result1.recommendedPalette` comes from the selected items' actual `colorHex` (zero phantom random neutrals).
   - `generateFallback_withDressAnchor_doesNotAddRedundantBottoms()`: Provide a locked anchor with `category = ClothingCategory.DRESSES` plus available `TOPS`, `BOTTOMS`, and `SHOES`; assert the resulting outfit includes the dress and shoes and contains **zero** `BOTTOMS` items.
2. **Create/Expand `DeterministicContextEngineTest.kt` (True Audit Funnel & Normalized `totalScore`):**
   - `generateSelectionState_recordsDistinctAuditFunnelCounts()`: Pass a wardrobe containing (a) a hot-weather banned outerwear item, (b) a recently worn rotation-banned item, and (c) eligible `TOPS`, `BOTTOMS`, and `SHOES`; assert `initialCount > afterWeather`, `afterWeather > afterRotation`, and `afterRotation >= finalEligible`.
   - `provenanceTotalScore_isNormalizedToZeroOneWhileAnchorsSortFirst()`: Assert that locked anchors have `isAnchor == true` and sort before non-anchors, while both wardrobe and cosmetic `totalScore` values lie strictly in `0.0f..1.0f`.
3. **Create `CompactManifestSerializerTest.kt` (Detail Levels & `UNKNOWN` Grounding):**
   - `serialize_emitsUnknownForMissingMaterialFinishBrandAndDepth()`: Serialize items with null/missing `material`, `seasonalPalette`, `finish`, and `brand` across `EXPANDED`, `BALANCED`, and `MINIMAL` detail levels; assert `"UNKNOWN"` is emitted and `"Cotton"`, `"SATIN"`, `"KoColor"`, and `"Balanced"` never appear.
4. **Create `RecommendationValidatorTest.kt` (`ValidationReport` & Rationale Scrubbing):**
   - `validateAndSanitize_removesUngroundedIdsReinjectsAnchorsAndPopulatesReport()`: Pass a blueprint containing an ungrounded clothing ID, a missing locked anchor, a forbidden `PREP` cosmetic ID, and a decimal in the rationale; assert the ungrounded and `PREP` IDs are stripped, the anchor is re-injected, decimals are scrubbed, and `ValidationReport` accurately reflects each rule boolean.

---

## MANDATORY PHASE 4 VERIFICATION GATE (RUN & PASTE EXACT OUTPUT)

Do NOT mark Phase 4 complete or write `Phase4_Audit_final.md` until every check below is executed and its verbatim output is pasted into the audit report:

### Category A: Expected Zero Matches (MUST Return 0 Lines)
```bash
# A1. Zero .random() or .randomOrNull() calls in HeuristicStyleEngine.kt:
grep -n "\.random" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt

# A2. Hardcoded "Priority: 4.00 / 4.00" literal removed from StyleAuditLogger.kt:
grep -n "4\.00 / 4\.00" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/telemetry/StyleAuditLogger.kt

# A3. Dead methods & classes removed:
find applications/ -name "DeterministicCatalogEngine.kt"
grep -n "fun enforceRoleDiversity" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt
grep -n "fun getStyleAdvice\|fun estimateTokens" features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/FirebaseAiClient.kt

# A4. Invented manifest defaults remain gone from CompactManifestSerializer.kt:
grep -n '"SATIN"\|"KoColor"\|"Balanced"' applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/CompactManifestSerializer.kt
```

### Category B: Line-by-Line Inspected Context Matches (Paste Output)
```bash
# B1. Centralized thresholds in KoColorTaxonomy.kt:
grep -n -A 15 "object KoColorThresholds" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/domain/taxonomy/KoColorTaxonomy.kt

# B2. Distinct sequential audit funnel counts in DeterministicContextEngine.kt:
grep -n -C 5 "afterWeather\|afterRotation\|finalEligible" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt

# B3. Explicit isAnchor / anchorPriority and normalized [0.0, 1.0] totalScore in ColorModels.kt:
grep -n -C 5 "isAnchor\|anchorPriority\|totalScore" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt
```

### Category C: All Phase 1–4 Invariants & Full Gradle Build/Test Suite
```bash
# C1. Phase 1–3 invariants still intact:
grep -n "runBlocking" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt
grep -n "countTokens" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
grep -rn "UnsupportedOperationException" features/ai/core/ features/ai/firebase/

# C2. Full unit test suite across all modules and mobile debug APK assembly:
./gradlew :features:ai:core:testDebugUnitTest :features:ai:firebase:testDebugUnitTest :features:ai:local:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest :applications:kocolor:apps:mobile:assembleDebug
```

```

Hand this Phase 4 specification to your coding agent and share the resulting `Phase4_Audit_final.md` report when it completes!