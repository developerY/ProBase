`Open_feedback_1.md` caught five subtle traps that a weaker coding agent would otherwise get wrong in Phase 4:

1. **Preventing the `coerceIn(0f, 1f)` Score-Collapse Bug (Point 1):** If cosmetic provenance sub-scores sum to `1.5, 2.2, 3.1, 4.0, 4.8`, calling `.coerceIn(0.0f, 1.0f)` collapses all five items to `1.0f` and destroys candidate ranking. The updated specification below explicitly forbids using `coerceIn()` as the normalization formula and requires dividing by the true theoretical maximum scale (`rawScore / maxPossibleScore`) before clamping floating-point rounding.


2. **All 13 Threshold Constants Accounted For (Point 2):** `KoColorThresholds` now includes the missing chroma gates (`15f` and `25f`, completing `10, 15, 20, 25, 30`) and the `7-day` wear history window (completing `24h, 3d, 7d`, plus `0.7` hard / `0.5` soft rotation and `7, 14, 20, 26°C` thermal / `18°C` outerwear cutoffs) while preserving rain/wind outerwear exceptions and Phase 3's `TEMP_REGEX`.


3. **Unified Outfit-Completeness Rule & Explicit Dual-Anchor Preservation (Point 3):** Defines a single shared completeness rule—`SHOES AND (DRESSES/ONE_PIECE OR (TOPS AND BOTTOMS))`—across `RoleGapAnalyzer`, `DeterministicContextEngine`, `HeuristicStyleEngine`, and `StyleSimulatorEngine` preflight on the **eligible pool**, while preserving both items if a user explicitly locks both a dress and a separate bottom.


4. **String-Aware Balanced-Brace JSON Scanner & Circular Hue Boundary Math (Point 4):** Replaces naive `indexOf('{')`/`lastIndexOf('}')` with a drop-in balanced-brace state machine that handles braces inside quoted strings (`"Use {slot}"`), escaped quotes (`\"`), nested objects, and truncated JSON, plus golden hex tests and the `359° + 1° ≈ 0°` circular hue test.


5. **All 9 Test Suites & Strict Scope Control (Points 5–7):** Expands Step 6 to all 9 required test suites (including `RoleGapAnalyzerTest`, `JsonSanitizerTest`, `ColorSpaceConverterTest`, `StyleAuditLoggerTest`, and `GreedyRehydratorTest` regression verification) and locks in the `SCOPE CONTROL` anti-refactoring guardrail.



Copy and paste the complete **Phase 4 Agent Specification (v2)** below into your coding agent:

```markdown
# AGENT MISSION: Execute Phase 4 (v2 — Foolproof 6-Step Runbook: Deterministic Gears, Thresholds, True Score Normalization & Shared Math/JSON)

You are executing **Phase 4 (P4.1 through P4.3)** — the final phase of the KoColor remediation plan (`RoadMapToPro.md` / `Tasks.md` / `Open_feedback_1.md`).

## STRICT SCOPE CONTROL & ANTI-SHORTCUT RULES (READ FIRST)
1. **SCOPE CONTROL:** Implement only the Phase 4 requirements. Do not redesign the Fashionista scoring pipeline, AI provider router, manifest contract, Room schema, or navigation architecture. Preserve existing public APIs unless a documented Phase 4 change requires modification. Before deleting or moving any shared utility, verify all production callers and module dependency direction. Each micro-checkpoint must leave the project in a compilable, testable state.
2. **WORK ONE STEP AT A TIME:** Complete **Step 1**, run its Gradle micro-checkpoint command, and confirm it passes before touching **Step 2**. Never batch-edit all files at once.
3. **READ BEFORE YOU WRITE:** Read the full target file before editing so you use the exact package names, imports, and data models in the repository.
4. **PROHIBITED NORMALIZATION SHORTCUT:** You are strictly forbidden from normalizing `totalScore` by merely calling `.coerceIn(0.0f, 1.0f)` on a multi-point sum (which collapses `1.5, 2.2, 3.1, 4.0` all to `1.0`). You MUST divide by the actual maximum theoretical score first, and only use `.coerceIn(0.0f, 1.0f)` at the very end for floating-point rounding safety.
5. **DO NOT MODIFY `GreedyRehydrator.kt`:** `GreedyRehydrator.kt` is already a pure ID-to-UI mapper from Phase 1.2. Do not modify it (though you will verify its anchor-preservation test in Step 6).
6. **PRESERVE ALL PHASE 1–3 INVARIANTS:**
   - Zero `runBlocking` in `SilhouetteGrader.kt`.
   - At most ONE `provider.countTokens(` call in `StyleSimulatorEngine.kt`.
   - Keep `UnsupportedOperationException` multimodal guards at line 1 of `ByokAiProvider.execute()` and `GemmaRestFallbackProvider.execute()`.
   - Keep `KeywordMatcher` and `TEMP_REGEX` (`26.0°C` and `7.0°C` boundaries) intact in `WardrobeCandidateFilter.kt`.

---

## STEP 1 (P4.1 & P4.3a): Unified Outfit-Completeness Rule, Dress Semantics, Preflight Gate & Deterministic Fallback

### 1A. Read First
Read the full contents of:
1. `RoleGapAnalyzer.kt`
2. `DeterministicContextEngine.kt`
3. `HeuristicStyleEngine.kt`
4. `StyleSimulatorEngine.kt`

### 1B. Establish One Canonical Definition of Outfit Completeness
All four classes (`RoleGapAnalyzer`, `DeterministicContextEngine`, `HeuristicStyleEngine`, and `StyleSimulatorEngine`) MUST share the exact same outfit-completeness rule:
```kotlin
/**
 * Canonical KoColor Outfit Completeness Rule:
 * A wardrobe/selection can form a complete outfit if and only if it contains:
 *   SHOES  AND  ( (DRESSES / ONE_PIECE)  OR  (TOPS AND BOTTOMS) )
 */
fun isOutfitSlotComplete(categories: Iterable<ClothingCategory>): Boolean {
    val set = categories.toSet()
    val hasShoes = ClothingCategory.SHOES in set
    val hasOnePiece = ClothingCategory.DRESSES in set // (include ONE_PIECE if defined on ClothingCategory)
    val hasTwoPiece = ClothingCategory.TOPS in set && ClothingCategory.BOTTOMS in set
    return hasShoes && (hasOnePiece || hasTwoPiece)
}
```
1. **`RoleGapAnalyzer.kt`:**
    - If the locked anchors (or current selection) contain a `DRESSES` item, both `TOPS` and `BOTTOMS` are satisfied; only `SHOES` (and optional `OUTERWEAR` when thermally required) is reported as a missing role requirement.
    - **Locked Anchor Precedence Rule:** If a user locks a `DRESSES` anchor, do NOT auto-inject a separate `TOPS` or `BOTTOMS` item. **However**, if the user *explicitly locks both* a `DRESSES` anchor AND a `BOTTOMS` (or `TOPS`) anchor, do **NOT** silently discard either user lock — preserve all explicit user anchors and let validation/reporting surface any feedback.
2. **Eligible-Pool Preflight Gate in `StyleSimulatorEngine.kt`:**
    - Before invoking **any** `AiProvider` (local or cloud) in `StyleSimulatorEngine.generateBlueprint()`, evaluate `isOutfitSlotComplete(...)` over the union of `selectionState.resolvedAnchors` and `selectionState.eligibleCandidates` (the **actual eligible pool** after hidden, thermal, and rotation filtering — NEVER raw Room inventory).
    - If the eligible pool + anchors cannot form a complete outfit, abort the AI provider loop immediately (making **zero** `provider.execute()` or `provider.countTokens()` calls) and return the deterministic fallback / empty-pool result.
    - In `StyleSimulatorEngine.adaptContextToProvider()`, wire `selectionState.missingRoleRequirements` (from `RoleGapAnalyzer`) so `categorizedPool` prioritizes unfilled roles while preserving the Phase 2.2 `slotsPerCategory` floor-of-1 step-down logic (`if (currentK <= minCandidateAdditions) 1 else ...`).

### 1C. Eliminate All Randomness & Add Dress Support in `HeuristicStyleEngine.kt`
1. Remove every call to `.random()` and `.randomOrNull()` in `HeuristicStyleEngine.kt`.
2. In `smartPick(...)` and slot fallback selection, select deterministically using the highest provenance/composite score with a stable `item.id` tie-breaker:
   ```kotlin
   candidates.sortedWith(
       compareByDescending<ClothingItem> { scoreMap[it.id] ?: 0.0 }
           .thenBy { it.id }
   ).firstOrNull()
   ```
3. **Dress-Aware Slot Picking in `HeuristicStyleEngine.kt`:**
    - Preserve all explicit locked anchors first.
    - If a locked anchor is `DRESSES`, do not pick a separate `TOPS` or `BOTTOMS` item (unless the user explicitly locked both).
    - If neither `TOPS` nor `BOTTOMS` is locked, allow selecting the highest-scoring eligible `DRESSES` item (satisfying both upper and lower body) when either (a) `TOPS` or `BOTTOMS` is unavailable in the eligible pool, or (b) the best `DRESSES` candidate outscores the average of the best `TOPS` + `BOTTOMS` pair.
4. **Real Hex Palette Only:** Build `recommendedPalette` strictly from the actual `colorHex` values of the selected clothing and cosmetic items (`selectedItems.map { it.colorHex }.filter { it.isNotBlank() }.distinct()`). Delete all random neutral hex padding.

> **MICRO-CHECKPOINT 1:**
> 1. Run `grep -n "\.random" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt` (must return **0 matches**).
> 2. Run `./gradlew :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 2 (P4.2a & b): Inventory & Centralize All 13 Thresholds + Fix Sequential Audit Funnel & Anchor Priority Logging

### 2A. Inventory Existing Thresholds Before Editing
Read `KoColorTaxonomy.kt`, `DeterministicContextEngine.kt`, `WardrobeCandidateFilter.kt`, `RotationScoringUseCase.kt`, `PromptAssembler.kt`, `StyleSimulatorEngine.kt`, and `StyleAuditLogger.kt`. Inspect every existing temperature, rotation, wear-recency, and chroma cutoff.

### 2B. Define All 13 Canonical Thresholds in `KoColorTaxonomy.kt`
Add `object KoColorThresholds` inside `KoColorTaxonomy.kt` containing all 13 existing domain thresholds (do not invent new cutoffs or drop existing weather conditions like rain/wind):
```kotlin
object KoColorThresholds {
    // 1. Thermal Bands (°C) — matches KoColorTaxonomy.ThermalBand & Phase 3 TEMP_REGEX
    const val THERMAL_HOT_MIN_C: Double = 26.0
    const val THERMAL_WARM_MIN_C: Double = 20.0
    const val THERMAL_MILD_MIN_C: Double = 14.0
    const val THERMAL_COOL_MIN_C: Double = 7.0

    // 2. Outerwear Ban Cutoff (°C) — unified across ContextEngine, Simulator, and PromptAssembler
    const val OUTERWEAR_BAN_TEMP_C: Double = 18.0

    // 3. Rotation Penalty Cutoffs
    const val ROTATION_HARD_BAN_PENALTY: Double = 0.7
    const val ROTATION_SOFT_PENALTY: Double = 0.5

    // 4. Wear History Windows
    const val RECENT_WEAR_HARD_HOURS: Int = 24
    const val RECENT_WEAR_SOFT_DAYS: Int = 3
    const val ROTATION_WINDOW_DAYS: Int = 7

    // 5. CIELAB Chroma (C*) Gates
    const val CHROMA_ACHROMATIC_MAX: Float = 10.0f
    const val CHROMA_LOW_GATE: Float = 15.0f
    const val CHROMA_MUTED_GATE: Float = 20.0f
    const val CHROMA_STATEMENT_MIN: Float = 25.0f
    const val CHROMA_HIGH_GATE: Float = 30.0f
}
```
Wire `KoColorThresholds` into `DeterministicContextEngine.kt`, `WardrobeCandidateFilter.kt`, `RotationScoringUseCase.kt`, `PromptAssembler.kt`, and `StyleSimulatorEngine.kt`:
- Unify the outerwear temperature cutoff on `tempC >= KoColorThresholds.OUTERWEAR_BAN_TEMP_C` (eliminating the `> 17°C` vs `>= 18°C` mismatch) while **preserving** any rain/wind/precipitation outerwear exceptions.
- Preserve `TEMP_REGEX` and the `26.0°C` (`THERMAL_HOT_MIN_C`) and `7.0°C` (`THERMAL_COOL_MIN_C`) checks in `WardrobeCandidateFilter.kt`.

### 2C. Fix Sequential Audit Funnel Counts in `DeterministicContextEngine.kt`
In `DeterministicContextEngine.kt` (around line 58), `afterWeather`, `afterRotation`, and `finalEligible` currently all read the same final list size.
Split the filtering into distinct sequential steps so each funnel metric records the true count at that stage:
1. `val initialCount = availableWardrobe.size` (after hidden/unavailable removal, or raw wardrobe size per existing convention)
2. `val afterWeatherItems = ...` (items passing thermal/weather rules) $\rightarrow$ `afterWeatherCount = afterWeatherItems.size`
3. `val afterRotationItems = ...` (subset of `afterWeatherItems` passing the rotation hard-ban cutoff `< KoColorThresholds.ROTATION_HARD_BAN_PENALTY` unless locked as an anchor) $\rightarrow$ `afterRotationCount = afterRotationItems.size`
4. `val finalEligibleItems = ...` (final candidate pool) $\rightarrow$ `finalEligibleCount = finalEligibleItems.size`
   Verify that `initialCount >= afterWeatherCount >= afterRotationCount >= finalEligibleCount`.

### 2D. Log Real Anchor Priority in `StyleAuditLogger.kt`
In `StyleAuditLogger.kt` (around line 103), replace the hardcoded literal `"Priority: 4.00 / 4.00"` with the actual formatted priority of the anchor (`String.format(Locale.US, "Priority: %.2f / 4.00", anchorProvenance.anchorPriority)`).

> **MICRO-CHECKPOINT 2:** Run `./gradlew :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 3 (P4.2c): Decouple Anchor Priority & Mathematically Normalize `totalScore` to `[0.0, 1.0]`

### 3A. Read First
Read `ColorModels.kt`, `DeterministicContextEngine.kt`, `WardrobeCandidateFilter.kt`, and `StyleAuditLogger.kt`.
Inspect how wardrobe `compositeScore` and cosmetic sub-scores (`seasonalMatchScore`, `temperatureAlignmentScore`, `undertoneHarmonyScore`, `intentBonus`, etc.) are computed and summed.

### 3B. Add Explicit Anchor Priority Fields First (`ColorModels.kt`)
1. On the provenance model in `ColorModels.kt` (e.g., `ColorProvenance` / `ItemColorProvenance`), add:
   ```kotlin
   val isAnchor: Boolean = false,
   val anchorPriority: Float = 0.0f // 4.0f for primary locked anchor, 2.5f for secondary anchor, 0.0f for non-anchor
   ```
2. In `DeterministicContextEngine.kt`, when creating provenance for locked/resolved anchors, set `isAnchor = true` and `anchorPriority = 4.0f` (or `2.5f` for secondary anchors), while keeping `compositeScore` in `[0.0f, 1.0f]`.
3. Update all candidate sorting (`sortedPartitionedPool`, etc.) to sort by:
   ```kotlin
   compareByDescending<ProvenanceType> { it.isAnchor }
       .thenByDescending { it.anchorPriority }
       .thenByDescending { it.totalScore }
   ```
4. Update `StyleAuditLogger.kt` to check `it.isAnchor` (instead of `it.totalScore > 2.0f`).

### 3C. Mathematically Normalize `totalScore` into `[0.0f, 1.0f]` (NO Raw Clamping!)
Inspect `ColorModels.kt:27` and `WardrobeCandidateFilter.kt`:
1. Determine the exact maximum theoretical raw score for wardrobe candidates (`MAX_WARDROBE_RAW_SCORE`) and for cosmetic candidates (`MAX_COSMETIC_RAW_SCORE`, e.g., if sub-scores sum up to `5.0f` or `4.75f`).
2. Scale the raw sum by dividing by its theoretical maximum **before** clamping for floating-point safety:
    - If `compositeScore > 0f` is already normalized in `[0.0f, 1.0f]`, use `compositeScore.coerceIn(0.0f, 1.0f)`.
    - When falling back to the sub-score sum (`rawSubScoreSum`), compute:
      ```kotlin
      val normalized = (rawSubScoreSum / MAX_SUB_SCORE_SUM).coerceIn(0.0f, 1.0f)
      ```
    - **Self-Check:** Verify that five cosmetic items with raw sums `1.5f, 2.2f, 3.1f, 4.0f, 4.8f` produce five **strictly increasing** normalized `totalScore` values in `(0.0f, 1.0f)` (e.g., `0.30f < 0.44f < 0.62f < 0.80f < 0.96f`) and do **NOT** collapse to `1.0f`!
3. Add KDoc on `WardrobeCandidateFilter.kt` documenting the **Pigment-Only Cosmetics Policy** (`EYES`, `DIMENSION`, `LIPS`, `NAILS` are included for chromatic styling; `PREP`, `COMPLEXION`, `HAIR` are intentionally excluded).

> **MICRO-CHECKPOINT 3:** Run `./gradlew :applications:kocolor:data:testDebugUnitTest`.

---

## STEP 4 (P4.3a): Whole-Repo Reference Audit & Safe Dead-Code Removal

### 4A. Search the Entire Repository Before Deleting Any Method
Run these whole-repository searches across `applications/`, `features/`, and `core/` first:
```bash
grep -rn "DeterministicCatalogEngine" applications/ features/ core/
grep -rn "enforceRoleDiversity" applications/ features/ core/
grep -rn "getCandidates\|getCosmeticCandidates" applications/ features/ core/
grep -rn "getStyleAdvice\|estimateTokens" applications/ features/ core/
```
1. **`DeterministicCatalogEngine.kt`:** After verifying Step 1B's preflight slot-completeness gate is active in `StyleSimulatorEngine.kt` and zero callers reference `DeterministicCatalogEngine`, delete `DeterministicCatalogEngine.kt`.
2. **`DeterministicContextEngine.enforceRoleDiversity`:** Verify zero callers exist and delete `enforceRoleDiversity(...)`.
3. **Unused Constructor Injections in `DeterministicContextEngine.kt`:** Inspect `DeterministicContextEngine`'s constructor. If `rotationScoringUseCase` and/or `repository` have zero references inside the class body, remove those unused constructor parameters and update test constructor calls.
4. **`WardrobeCandidateFilter.getCandidates` / `getCosmeticCandidates`:** If they have zero production callers, delete them (and migrate any test calling them to test the live `filterAndRank*` / `calculateScore` methods instead).
5. **`FirebaseAiClient.getStyleAdvice` & `estimateTokens`:** Only delete if the whole-repo `grep` above confirms zero callers across all modules (including `:applications:seaweed` or diagnostic delegates). If any module still calls `estimateTokens`, keep that method!

> **MICRO-CHECKPOINT 4:** Run `./gradlew :features:ai:firebase:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:apps:mobile: compileDebugKotlin`.

---

## STEP 5 (P4.3b): String-Aware Balanced-Brace `JsonSanitizer` & Golden-Value Color Math Consolidation

### 5A. Create String-Aware Balanced-Brace `JsonSanitizer`
Do **NOT** use `raw.indexOf('{')` / `raw.lastIndexOf('}')`, which breaks when braces appear inside JSON string values or when trailing commentary contains `{...}`.
1. Check module dependencies: place `JsonSanitizer` in `core/json` (if accessible to `:features:ai:*` and `:applications:kocolor:*`) or in `:features:ai:core` (`com.zoewave.probase.features.ai.core.util.JsonSanitizer`) so `:applications:kocolor:data`, `:applications:kocolor:features:analyzer`, and `:applications:kocolor:features:suggestions` can all call it without circular dependencies.
2. Implement `JsonSanitizer` with a balanced-brace state machine that tracks string literals (`"..."`) and backslash escapes (`\\`, `\"`):

```kotlin
object JsonSanitizer {
    private val CODE_FENCE_REGEX = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)

    /**
     * Extracts the first complete, balanced JSON object "{...}" from [raw],
     * stripping markdown code fences and ignoring '{' or '}' characters that
     * occur inside quoted JSON string literals. Returns null if no balanced
     * JSON object exists.
     */
    fun extractFirstJsonObject(raw: String): String? {
        if (raw.isBlank()) return null
        val unfenced = CODE_FENCE_REGEX.find(raw)?.groupValues?.getOrNull(1)?.trim() ?: raw.trim()

        var startIndex = -1
        var depth = 0
        var inString = false
        var isEscaped = false

        for (i in unfenced.indices) {
            val ch = unfenced[i]
            if (isEscaped) {
                isEscaped = false
                continue
            }
            if (ch == '\\' && inString) {
                isEscaped = true
                continue
            }
            if (ch == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (ch == '{') {
                    if (depth == 0) startIndex = i
                    depth++
                } else if (ch == '}') {
                    if (depth > 0) {
                        depth--
                        if (depth == 0 && startIndex != -1) {
                            return unfenced.substring(startIndex, i + 1)
                        }
                    }
                }
            }
        }
        return null
    }
}
```
3. Delegate the duplicate JSON fence/brace-extraction logic in `StyleSimulatorEngine`, `AnalyzerEngine`, and `SuggestionEngine` to `JsonSanitizer.extractFirstJsonObject(raw)`.

### 5B. Consolidate Duplicate Color Math While Preserving Exact D65 CIELAB Equivalence
1. Inspect `ColorHarmonyEngine.kt` (`data/color/ColorHarmonyEngine.kt`), `ColorSpace.kt` (`fashionista/color/ColorSpace.kt`), and `CircularStatistics.kt` (`fashionista/math/CircularStatistics.kt`).
2. Ensure there is **one canonical implementation** of:
    - sRGB $\rightarrow$ linear RGB (`0.04045` pivot, $\gamma = 2.4$) $\rightarrow$ XYZ (D65 reference white) $\rightarrow$ CIELAB (`0.008856` pivot) $\rightarrow$ CIELCh ($L^*, C^* = \sqrt{a^{*2} + b^{*2}}, h^\circ \in [0^\circ, 360^\circ)$).
    - Delegate `ColorHarmonyEngine.hexToLCh` to the canonical converter in `ColorSpaceConverter` (or vice versa) so only one copy of the matrix/pivot math exists, preserving exact numerical outputs.
    - Delegate any duplicate inline `calculateChroma` or chroma-weighted circular hue averaging to `ColorSpaceConverter` / `CircularStatistics`.

> **MICRO-CHECKPOINT 5:** Run `./gradlew :features:ai:core:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest`.

---

## STEP 6 (P4.3c): Comprehensive 9-Suite Unit & Regression Test Expansion

Create or expand the following **9 unit test suites** to lock in every Phase 1–4 guarantee:

1. **`JsonSanitizerTest.kt` (`:features:ai:core` or `:applications:kocolor:data`):**
    - Extracts clean JSON from ` ```json ... ``` ` markdown fences and surrounding prose.
    - Correctly extracts nested objects (`{"a":{"b":1}}`).
    - Preserves braces `{}` and escaped quotes `\"` *inside* JSON string literals (e.g., `{"rationale": "Pairs {\"key\"} with {slot} nicely"}`) without premature truncation.
    - Returns `null` on truncated/unbalanced input (`{"selectedClothingIds": ["w_1"`).
2. **`ColorSpaceConverterTest.kt` (`:applications:kocolor:data`):**
    - **Golden Hex Values:** Verify pure black `#000000` ($L^* \approx 0, C^* \approx 0$), pure white `#FFFFFF` ($L^* \approx 100, C^* \approx 0$), and sRGB red `#FF0000` ($L^* \approx 53.2, C^* \approx 104.5, h^\circ \approx 40.0^\circ$).
    - **Circular Hue Boundary (`359° + 1° ≈ 0°`):** Verify `CircularStatistics` chroma-weighted mean of two equal-chroma colors at `359.0°` and `1.0°` evaluates to `0.0°` ($\pm 0.5^\circ$, or `360.0°`), NEVER `180.0°`.
    - **Conversion Consistency:** Verify `ColorHarmonyEngine.hexToLCh(hex)` and `ColorSpaceConverter.hexToLCh(hex)` agree within `1e-4`.
3. **`HeuristicStyleEngineTest.kt` (Expand):**
    - Repeated calls with identical inputs return identical `selectedClothingIds`, `selectedCosmeticIds`, and `recommendedPalette` (strictly from real item hexes, zero phantom neutrals).
    - Single locked `DRESSES` anchor + available `TOPS`, `BOTTOMS`, `SHOES` selects the dress + shoes with **zero** `BOTTOMS`.
    - Explicit dual lock (user locks *both* a `DRESSES` item and a `BOTTOMS` item) preserves **both** locked anchor IDs.
4. **`RoleGapAnalyzerTest.kt` (Create):**
    - Empty anchors require `TOPS`, `BOTTOMS`, `SHOES` (or `DRESSES` + `SHOES`).
    - Locked `DRESSES` anchor marks upper + lower body satisfied, leaving only `SHOES` (and thermal outerwear if cold) as missing.
5. **`DeterministicContextEngineTest.kt` (Create):**
    - **True Audit Funnel:** Given a wardrobe with 1 hot-weather banned coat (`28°C`), 1 rotation-banned top (worn 2h ago, penalty `>= 0.7`), and 3 eligible items (`TOPS`, `BOTTOMS`, `SHOES`), assert `initialCount > afterWeather > afterRotation == finalEligible`.
    - **Mathematical `[0.0, 1.0]` Normalization Without Collapse:** Given 4 cosmetic candidates with distinct raw sub-score sums (e.g., `1.5, 2.5, 3.5, 4.5`), assert all 4 `totalScore` values lie in `(0.0f, 1.0f)` AND are strictly ordered (`score1 < score2 < score3 < score4`, none collapsed to `1.0f`).
    - **Anchor Sorting:** Assert locked anchors have `isAnchor == true`, `anchorPriority > 0f`, `totalScore in 0.0f..1.0f`, and sort ahead of non-anchors even when a non-anchor has a higher `totalScore`.
6. **`CompactManifestSerializerTest.kt` (Create):**
    - Test `EXPANDED`, `BALANCED`, and `MINIMAL` serialization on items with missing/null `material`, `seasonalPalette`, `finish`, and `brand`.
    - Assert `"UNKNOWN"` is emitted in every detail level that includes those fields (accounting for fields intentionally omitted in `MINIMAL`), and assert `"Cotton"`, `"SATIN"`, `"KoColor"`, and `"Balanced"` never appear.
7. **`RecommendationValidatorTest.kt` (Create):**
    - Verify ungrounded IDs and `PREP` cosmetics are stripped, missing locked anchors are re-injected, rationale decimals are scrubbed, and `ValidationReport` booleans accurately report rule compliance.
8. **`StyleSimulatorEngineTest.kt` (Expand):**
    - **Dress Wardrobe Preflight Passes:** A wardrobe with only `DRESSES` + `SHOES` passes preflight and invokes the AI provider.
    - **Incomplete Wardrobe Preflight Aborts with Zero Cloud Spend:** A wardrobe missing `SHOES` (or missing both `BOTTOMS` and `DRESSES`) aborts at preflight with **zero** calls to `provider.execute()` or `provider.countTokens()`.
9. **`StyleAuditLoggerTest.kt` (Create) & `GreedyRehydratorTest.kt` (Verify Existing):**
    - In `StyleAuditLoggerTest`: Assert an anchor with `anchorPriority = 2.5f` logs `"Priority: 2.50 / 4.00"` (proving the hardcoded `4.00 / 4.00` literal is gone).
    - Confirm `GreedyRehydratorTest` still passes all anchor-preservation and pure-mapping assertions without touching `GreedyRehydrator.kt`.

---

## MANDATORY PHASE 4 VERIFICATION GATE (RUN & PASTE VERBATIM OUTPUT)

Do NOT stamp Phase 4 complete or write `Phase4_Audit_final.md` until every check below is executed and its verbatim output is included in the audit report:

### Category A: Expected Zero Matches (MUST Return 0 Lines)
```bash
# A1. Zero .random() or .randomOrNull() calls in HeuristicStyleEngine.kt:
grep -n "\.random" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngine.kt

# A2. Hardcoded "Priority: 4.00 / 4.00" literal removed from StyleAuditLogger.kt:
grep -n "4\.00 / 4\.00" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/telemetry/StyleAuditLogger.kt

# A3. Dead classes/methods removed across the entire repo:
find applications/ features/ core/ -name "DeterministicCatalogEngine.kt"
grep -rn "fun enforceRoleDiversity" applications/ features/ core/

# A4. Zero naive indexOf('{') / lastIndexOf('}') left in simulator, analyzer, or suggestion engines:
grep -n "indexOf('{')\|lastIndexOf('}')" \
  applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt \
  applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/data/AnalyzerEngine.kt \
  applications/kocolor/features/suggestions/src/main/java/com/zoewave/probase/kocolor/features/suggestions/data/SuggestionEngine.kt
```

### Category B: Line-by-Line Inspected Context Matches (Paste Verbatim Output)
```bash
# B1. All 13 centralized thresholds in KoColorTaxonomy.kt:
grep -n -A 25 "object KoColorThresholds" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/domain/taxonomy/KoColorTaxonomy.kt

# B2. Distinct sequential audit funnel counts in DeterministicContextEngine.kt:
grep -n -C 5 "afterWeather\|afterRotation\|finalEligible" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt

# B3. Mathematical division normalization (NOT bare coerceIn) + isAnchor / anchorPriority in ColorModels.kt & WardrobeCandidateFilter.kt:
grep -n -C 6 "isAnchor\|anchorPriority\|totalScore" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt

# B4. Inspect any remaining occurrences of "SATIN"|"KoColor"|"Balanced" in CompactManifestSerializer.kt:
grep -n '"SATIN"\|"KoColor"\|"Balanced"' applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/CompactManifestSerializer.kt
```

### Category C: Preserved Phase 1–3 Invariants & Full Gradle Build/Test Suite
```bash
# C1. Verify Phase 1–3 invariants with context:
grep -n "runBlocking" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt
grep -n "countTokens" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
grep -n -C 2 "UnsupportedOperationException" features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/ByokAiProvider.kt features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/GemmaRestFallbackProvider.kt
grep -n "TEMP_REGEX" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt

# C2. Execute full unit test suite across all modules and mobile debug APK assembly:
./gradlew :features:ai:core:testDebugUnitTest :features:ai:firebase:testDebugUnitTest :features:ai:local:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest :applications:kocolor:apps:mobile:assembleDebug
```

```

Hand this revised Phase 4 specification to your coding agent and share the resulting `Phase4_Audit_final.md` output when it finishes.