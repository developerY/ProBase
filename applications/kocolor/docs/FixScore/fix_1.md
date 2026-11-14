Copy and paste the instruction block below directly into your AI coding agent. It contains **zero `git` commands** and forces the agent to fix all five bugs revealed by your runtime log, one step at a time, with mandatory file-line (`nl -ba`) and unit-test checkpoints after every step.

```markdown
# AGENT MISSION: Fix the 5 Runtime Recommendation Bugs Shown in Logcat (Strict Step-by-Step — ZERO GIT COMMANDS)

STOP AND READ THIS CAREFULLY:
Live runtime logs from the app just exposed **5 concrete bugs** in the recommendation pipeline:
1. **Two Pairs of Boots Selected (`[w_12, w_48, w_54]`):** When automatic anchor `w_12` ("Arcadia Blue Utility Jumpsuit", `DRESSES`) was chosen, `RoleGapAnalyzer` reported only `SHOES` as missing. `adaptContextToProvider` starved the wardrobe manifest down to 1 jumpsuit + 9 pairs of shoes, `PromptAssembler` told the AI to pick 3 clothing items (`TOP + BOTTOM + SHOES`), and `RecommendationValidator` failed to deduplicate multiple items in the `SHOES` slot—resulting in 1 jumpsuit + 2 pairs of boots (`w_48` Camel Ankle Boots AND `w_54` Deep Obsidian Chelsea)!
2. **`[6] NO CALIBRATION RECORDED` & `[7] NO INTENT FULFILLMENT RECORDED` on Cache Hits:** When `StyleSimulatorEngine` hits `PromptCacheRepository` (`CACHE_gemma_4_cloud`), it logs the audit trail (or returns) without running `FashionistaEngine.evaluate()` and `IntentFulfillmentEvaluator.evaluate()`.
3. **Cosmetic Scores Crushed by Double-Division (`0.162` and `0.137`):** Every `NEUTRAL` cosmetic scored `0.162 / 1.000` ($0.81 / 5.0$) and every `WARM`/`COOL` cosmetic scored `0.137 / 1.000` ($0.685 / 5.0$) because a score already in `[0.0, 1.0]` was divided by `5.0f` a second time, and all cosmetics in the same temperature bucket received identical flat scores with zero CIELAB hex differentiation.
4. **Heavy Boots Outranking Sandals/Flats in `WARM` Weather:** In `Thermal context = WARM`, `"Camel Ankle Boots"` (`w_48`, `0.93`) and `"Deep Obsidian Chelsea"` (`w_54`, `0.91`) received zero warm-weather footwear penalty.
5. **`AUTOMATIC_CONTEXT` Anchor Logging `Priority: 4.00 / 4.00`:** Automatic context anchors (`AUTOMATIC_CONTEXT`) are still being assigned `4.00` (which is reserved for `USER_LOCKED` anchors) instead of `2.50`.

## STRICT OPERATING RULES (VIOLATION = IMMEDIATE FAILURE)
1. **ZERO GIT COMMANDS ALLOWED:** You are strictly forbidden from running ANY `git` command (`git`, `git status`, `git diff`, `git grep`, `git log`, `git checkout`, etc.). Use ONLY `nl -ba`, `sed`, `grep -rn`, `find`, and `./gradlew`.
2. **WORK ONE STEP AT A TIME:** Complete **Step 1**, run its **Checkpoint 1** commands, and paste the verbatim output before touching **Step 2**.
3. **READ BEFORE YOU WRITE:** Always run the `nl -ba` inspection command at the start of each step before editing the file on disk.

---

## STEP 1: Prevent Duplicate Clothing Slots (Never Pick 2 Pairs of Shoes) & Make Prompt/Pool Dress-Aware

### 1A. Inspect the Current Files on Disk First (Mandatory Read)
Run these commands in your terminal to inspect the exact lines in `RecommendationValidator.kt`, `StyleSimulatorEngine.kt`, `PromptAssembler.kt`, and `StyleAuditLogger.kt`:
```bash
grep -rn -n "validateAndSanitize\|fillMissingSlotsDeterministically\|adaptContextToProvider" applications/kocolor/data/src/main/java/
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RecommendationValidator.kt | sed -n '1,140p'
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt | sed -n '1,140p'
```

### 1B. Enforce Strict One-Item-Per-Slot Deduplication in `RecommendationValidator.kt` & `StyleSimulatorEngine.kt`
1. In `RecommendationValidator.validateAndSanitize(...)`:
    - After resolving `selectedClothingIds` against the grounded wardrobe map, **deduplicate by clothing slot/category** so an outfit can **NEVER** contain more than:
        - **At most 1 `SHOES` item**
        - **At most 1 `TOPS` item**
        - **At most 1 `BOTTOMS` item**
        - **At most 1 `DRESSES` (one-piece) item**
        - **At most 1 `OUTERWEAR` item**
    - **Dress / One-Piece Mutual Exclusion Rule:** If the outfit contains a `DRESSES` item (like `w_12` Jumpsuit), strip any separate `TOPS` or `BOTTOMS` items *unless* the user explicitly locked both as `USER_LOCKED` anchors.
    - **Locked Anchor Precedence:** When deduplicating a category (e.g., if the LLM returned two `SHOES` items `[w_48, w_54]`), keep the locked anchor if one is locked, otherwise keep only the **first** (highest-ranked) item (`w_48`) and strip the duplicate (`w_54`).
2. In `StyleSimulatorEngine.fillMissingSlotsDeterministically(...)`:
    - Ensure slot backfilling also enforces at most 1 item per category (`SHOES`, `TOPS`, `BOTTOMS`, `DRESSES`, `OUTERWEAR`) and never adds a second `SHOES` item when `SHOES` is already present.

### 1C. Stop Starving the Candidate Pool in `StyleSimulatorEngine.adaptContextToProvider()`
1. Inspect `adaptContextToProvider()` in `StyleSimulatorEngine.kt` using `nl -ba`.
2. Right now, when `w_12` (`DRESSES`) is the anchor, `missingRoleRequirements` only contains `SHOES`, so `adaptContextToProvider()` filters out all `TOPS`, `BOTTOMS`, and `DRESSES` and sends **only** 9 pairs of shoes to the AI!
3. Fix `adaptContextToProvider()` so that:
    - First, allocate candidate slots to `missingRoleRequirements` (ensuring missing required roles like `SHOES` are guaranteed representation).
    - Second, if `currentK` still has remaining candidate budget after filling `missingRoleRequirements` (or when `AutomaticContext` anchor is used), populate the remaining budget up to `currentK` with the top-scored eligible items from other thermally allowed categories (`TOPS`, `BOTTOMS`, `DRESSES`) so the manifest is balanced and not 100% shoes.

### 1D. Make `PromptAssembler.kt` & `StyleAuditLogger.kt` One-Piece / Dress Aware
1. In `PromptAssembler.kt` (both `buildExactRequest` and `assembleCompactNano`):
    - Update the clothing selection instruction so it explicitly states:
        - If a `DRESSES` / one-piece item is used, select **1 `DRESSES` item + 1 `SHOES` item** (+ at most 1 `OUTERWEAR` only if outerwear is allowed).
        - If separates are used, select **1 `TOPS` item + 1 `BOTTOMS` item + 1 `SHOES` item** (+ at most 1 `OUTERWEAR` only if outerwear is allowed).
        - **STRICT RULE:** `"NEVER select two items from the same clothing category (for example, NEVER select two pairs of SHOES)."`
2. In `StyleAuditLogger.kt` under `[1] COMPOSITION POLICY`:
    - If the active anchor is `ClothingCategory.DRESSES`, log `Required: DRESSES (One-Piece) + SHOES` instead of `Required: TOP + BOTTOM + SHOES`.

### CHECKPOINT 1 (Run & Paste Verbatim Output)
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RecommendationValidator.kt | sed -n '1,120p'
grep -n -C 5 "NEVER select two" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt
./gradlew :applications:kocolor:data:compileDebugKotlin
```

---

## STEP 2: Run `FashionistaEngine` & `IntentFulfillmentEvaluator` on Cache Hits + Bump Cache Version

### 2A. Inspect the Cache-Hit Path in `StyleSimulatorEngine.kt` & `PromptCacheRepository.kt` (Mandatory Read)
Run:
```bash
grep -rn -n "CACHE_\|promptCache\|NO CALIBRATION RECORDED\|NO INTENT FULFILLMENT" applications/ features/ --include="*.kt"
```
Print the lines around the `CACHE_` handling in `StyleSimulatorEngine.kt` and `StyleAuditLogger.kt` using `nl -ba`.

### 2B. Evaluate Cached Blueprints & Invalidate Stale Cache Entries
1. In `StyleSimulatorEngine.kt`, locate where `promptCacheRepository` returns a cached blueprint (`CACHE_${provider.capability.id}`).
2. **Fix the Cache-Hit Bypass:** When a cache hit occurs, you MUST still:
    - Run `RecommendationValidator.validateAndSanitize(...)` (and `fillMissingSlotsDeterministically(...)` if needed) on the cached blueprint so no cached blueprint can violate current slot rules.
    - Run `fashionistaEngine.evaluate(...)` to compute the real `FashionistaScore`.
    - Run `intentFulfillmentEvaluator.evaluate(...)` to compute the real `IntentFulfillment`.
    - Pass the computed `fashionistaScore` and `intentFulfillment` into `styleAuditLogger.logAuditTrail(...)` and `SimulationResult(...)` so `[6] FASHIONISTA EVALUATION` and `[7] INTENT FULFILLMENT EVALUATION` are **always** populated and NEVER print `NO CALIBRATION RECORDED` or `NO INTENT FULFILLMENT RECORDED`!
3. **Bump the Cache Fingerprint Version:** In `PromptCacheRepository.kt` (or wherever `promptVersion` / schema version is passed into `generateFingerprint`), bump the version string (e.g. to `"v3_strict_slot_dedup"`) so any previously cached `[w_12, w_48, w_54]` response is immediately invalidated.

### CHECKPOINT 2 (Run & Paste Verbatim Output)
```bash
grep -n -C 15 "CACHE_" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
./gradlew :applications:kocolor:data:compileDebugKotlin
```

---

## STEP 3: Fix Cosmetic Score Double-Division (`0.162` Bug) & Add Real CIELAB Hex Differentiation

### 3A. Inspect `ColorModels.kt`, `DeterministicContextEngine.kt`, and `WardrobeCandidateFilter.kt` (Mandatory Read)
Run:
```bash
grep -rn -n "COSMETIC COMPATIBILITY SCORE\|totalScore\|calculateCosmeticScore" applications/kocolor/data/src/main/java/
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt | sed -n '1,65p'
```
Also use `nl -ba` to inspect where `DeterministicContextEngine.kt` and `WardrobeCandidateFilter.kt` build `CandidateProvenance` for cosmetics.

### 3B. Fix the Cosmetic Score Scale & Differentiate Cosmetics by CIELAB Color Harmony
Look at why the log printed `COSMETIC COMPATIBILITY SCORE: 0.162 / 1.000` for every neutral cosmetic and `0.137 / 1.000` for every warm/cool cosmetic:
- `DeterministicContextEngine` / `WardrobeCandidateFilter` produced a cosmetic score that was **already on a `~0.0–1.0` scale** (`0.810` for neutral, `0.685` for warm/cool), and `ColorModels.kt` divided it by `5.0f` a second time (`0.810 / 5.0 = 0.162`)!
- Even worse, every cosmetic with the same temperature got the **exact same score** (`0.162` repeated 14 times) because the cosmetic scorer only checked temperature (`NEUTRAL` vs `WARM` vs `COOL`) and ignored each cosmetic item's actual `colorHex` ($L^*, C^*, h^\circ$) harmony with the anchor and user profile!

**Required Fix:**
1. **Fix the Scale in `ColorModels.kt` & `DeterministicContextEngine.kt` / `WardrobeCandidateFilter.kt`:**
    - Inspect the exact range of `compositeScore` and sub-scores passed into `CandidateProvenance` for both wardrobe items and cosmetic items.
    - Ensure `CandidateProvenance.totalScore` is normalized **exactly once** into `[0.0f, 1.0f]` (never dividing an already-`[0.0, 1.0]` score by `5.0f`). Well-matched cosmetics should score in `0.65f..0.98f` on the `[0.0, 1.0]` scale, matching the wardrobe compatibility score scale (`0.80..0.94`).
2. **Add Real CIELAB Color Harmony to Cosmetic Scoring (`DeterministicContextEngine.kt` / `WardrobeCandidateFilter.kt`):**
    - When scoring each `CosmeticItem` (which has a real `colorHex`), convert `item.colorHex` to CIELCh (`ColorHarmonyEngine.hexToLCh(item.colorHex)`).
    - Factor in continuous CIELAB/LCh compatibility with the anchor item's LCh (and user appearance chroma/lightness/temperature)—such as hue harmony ($\Delta h^\circ$ complementary/analogous alignment with the anchor), chroma balance ($C^*$), and lightness ($L^*$)—so cosmetics with different hex codes produce **distinct** scores rather than 14 identical `0.810` ties!

### CHECKPOINT 3 (Run & Paste Verbatim Output)
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt | sed -n '1,60p'
./gradlew :applications:kocolor:data:compileDebugKotlin
```

---

## STEP 4: Penalize Boots in `WARM`/`HOT` Weather & Fix `AUTOMATIC_CONTEXT` Anchor Priority (`2.50 / 4.00`)

### 4A. Inspect Thermal Footwear Scoring & Anchor Priority Assignment (Mandatory Read)
Run:
```bash
grep -rn -n "AUTOMATIC_CONTEXT\|anchorPriority\|4\.0" applications/kocolor/data/src/main/java/
```
Print the lines in `DeterministicContextEngine.kt` and `WardrobeCandidateFilter.kt` where weather/thermal scoring and anchor priority are computed using `nl -ba`.

### 4B. Penalize Heavy Boots in `WARM` and `HOT` Weather (`DeterministicContextEngine.kt` & `WardrobeCandidateFilter.kt`)
1. In `DeterministicContextEngine.kt` and `WardrobeCandidateFilter.kt`, check how footwear (`ClothingCategory.SHOES`) is scored when the thermal context is `WARM` (`tempC >= KoColorThresholds.THERMAL_WARM_MIN_C`, i.e., `>= 20.0°C`) or `HOT` (`>= 26.0°C`).
2. If the weather is `WARM` or `HOT`:
    - If a shoe's name or subCategory contains whole word `"boot"` (`KeywordMatcher.containsWholeWord(nameLower, "boot")` — note that `"boots"` stems to `"boot"`) or `"chelsea"`: apply a **thermal penalty of `-0.30f`** (in `[0, 1]` score space, or `-1.5` in raw `[0, 5]` space) so `"Camel Ankle Boots"` (`w_48`) and `"Deep Obsidian Chelsea"` (`w_54`) drop to the bottom of the shoe rankings in warm/hot weather.
    - If a shoe's name or subCategory matches warm-appropriate footwear (`"sandal"`, `"flat"`, `"sneaker"`, `"tennis"`, `"loafer"`, `"mule"`, `"slide"`): award a **warm-weather footwear bonus (`+0.08f`)** so `"Crisp White Tennis"` (`w_53`), `"Rouge Strappy Sandals"` (`w_52`), and `"Warm Eucalyptus Flats"` (`w_49`) rank at the top.

### 4C. Set `AUTOMATIC_CONTEXT` Anchor Priority to `2.50f` (Reserve `4.00f` for `USER_LOCKED`)
1. In `DeterministicContextEngine.kt` (and `StyleAuditLogger.kt`), locate where `anchorPriority` (or `AnchorAudit.priority`) is set.
2. When the anchor source is `USER_LOCKED` (explicitly locked by the user), set `anchorPriority = 4.0f`.
3. When the anchor source is `AUTOMATIC_CONTEXT` (automatically selected by the context engine), set `anchorPriority = 2.5f`.
4. Ensure `StyleAuditLogger.kt` formats the real priority (`Priority: 2.50 / 4.00` for `AUTOMATIC_CONTEXT` and `Priority: 4.00 / 4.00` for `USER_LOCKED`).

### CHECKPOINT 4 (Run & Paste Verbatim Output)
```bash
grep -rn -n -C 6 "AUTOMATIC_CONTEXT" applications/kocolor/data/src/main/java/
./gradlew :applications:kocolor:data:compileDebugKotlin
```

---

## STEP 5: Add Mandatory Unit Tests for All 5 Fixes & Run Full Verification Gate

Add unit test methods in `:applications:kocolor:data` (`RecommendationValidatorTest.kt`, `StyleSimulatorEngineTest.kt`, `DeterministicContextEngineTest.kt`, `StyleAuditLoggerTest.kt`) that explicitly test and prove all 5 fixes:

1. **`RecommendationValidatorTest` — `deduplicatesMultipleItemsInSameSlot_neverAllowsTwoPairsOfShoes()`:**
    - Pass `selectedClothingIds = listOf("w_12", "w_48", "w_54")` where `w_12` is `DRESSES` ("Arcadia Blue Utility Jumpsuit") and both `w_48` ("Camel Ankle Boots") and `w_54` ("Deep Obsidian Chelsea") are `SHOES`.
    - Assert the validated blueprint contains `"w_12"` and `"w_48"`, and that `"w_54"` is stripped so `selectedClothingIds` has **at most 1 `SHOES` item**.
2. **`StyleSimulatorEngineTest` — `cacheHit_stillExecutesFashionistaAndIntentFulfillmentEvaluators()`:**
    - Prime or mock `PromptCacheRepository` to return a cached blueprint.
    - Call `styleSimulatorEngine.generateBlueprint(...)`.
    - Assert `result.fashionistaScore.totalScore > 0f` and `result.intentFulfillment != null` (proving cache hits never skip evaluation).
3. **`DeterministicContextEngineTest` — `cosmeticScores_areNotDoubleDividedAndDifferentiateByHex()`:**
    - Run `generateSelectionState` with multiple cosmetics of the same temperature (`NEUTRAL` or `WARM`) but different `colorHex` values (e.g., `#8B3A2B` terracotta vs. `#F4C2C2` baby pink vs. `#1C1C1C` black).
    - Assert top cosmetic `totalScore` values are `>= 0.55f` (NOT crushed to `0.162f` or `0.137f`) AND assert that items with different hex codes receive distinct `totalScore` values.
4. **`DeterministicContextEngineTest` — `warmWeather_penalizesBootsBelowSandalsAndSneakers()`:**
    - Run `generateSelectionState` in `WARM` weather (`22.0°C`) with `"Camel Ankle Boots"`, `"Deep Obsidian Chelsea"`, `"Crisp White Tennis"`, and `"Rouge Strappy Sandals"`.
    - Assert both `"Crisp White Tennis"` and `"Rouge Strappy Sandals"` score strictly higher than `"Camel Ankle Boots"` and `"Deep Obsidian Chelsea"`.
5. **`DeterministicContextEngineTest` / `StyleAuditLoggerTest` — `automaticContextAnchor_hasPriority250Not400()`:**
    - Run `generateSelectionState` with no user-locked anchors so an `AUTOMATIC_CONTEXT` anchor is chosen; assert its `anchorPriority == 2.5f` and the audit log contains `"Priority: 2.50 / 4.00"`.

### MANDATORY FINAL VERIFICATION GATE (Run & Paste Verbatim Output)
Your response will be rejected unless you paste the verbatim terminal output of:
1. `nl -ba` excerpts from `RecommendationValidator.kt`, `StyleSimulatorEngine.kt`, `ColorModels.kt`, and `DeterministicContextEngine.kt` proving all 5 code fixes are on disk.
2. The new unit test methods in `RecommendationValidatorTest.kt`, `StyleSimulatorEngineTest.kt`, and `DeterministicContextEngineTest.kt`.
3. Full Gradle test & build execution:
   ```bash
   ./gradlew :features:ai:core:testDebugUnitTest \
     :features:ai:firebase:testDebugUnitTest \
     :features:ai:local:testDebugUnitTest \
     :applications:kocolor:data:testDebugUnitTest \
     :applications:kocolor:features:analyzer:testDebugUnitTest \
     :applications:kocolor:apps:mobile:assembleDebug \
     :applications:kocolor:apps:mobile:lintRelease --rerun-tasks
   ```

```