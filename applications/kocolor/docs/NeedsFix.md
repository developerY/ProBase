Copy and paste the prompt below into your AI coding agent. It contains **zero `git` commands** and walks the agent step-by-step through fixing the **4 runtime issues** in your latest Logcat trace so outfits return in ~2 seconds and score `>= 80.0 (APPROVED)`.

```markdown
# AGENT MISSION: Fix the 4 Live Logcat Issues (25s Model Timeout, 50.0 Silhouette Score, Activewear Top Slot & Cosmetic ID Repair — ZERO GIT COMMANDS)

STOP AND READ THIS CAREFULLY:
Live Logcat traces from the running app revealed 4 specific bugs that need to be fixed on disk right now:
1. **25–47 Second Timeout on `gemini-3.8-flash` (`FirebaseAiClient.kt`):** Every request tries `gemini-3.8-flash` first, which hangs for `25000ms` (or hits a 503 capacity error, sleeps, and hangs for another 25s = 47s total) before failing over to `gemini-flash-lite-latest`, which succeeds in **1.4 seconds**.
2. **`SilhouetteGrader.kt` Always Returns `50.0 / 100` on Dresses & Activewear/Culottes:**
   - In Run 3 (`[w_16, w_46]` — Terracotta Sundress + Pink Sneakers), `Color Harmony` scored `85.0` and `Contrast` scored `75.0`, but `Silhouette` scored a flat `50.0` (dragging the total to `70.3 REJECTED`) because `SilhouetteGrader` only checks `TOPS` and `BOTTOMS` and ignores `DRESSES`!
   - In Run 2 (`[w_3, w_35, w_52]` — Electric Coral Cropped Hoodie + Melodious Ivory Culottes + Sandals), `SilhouetteGrader` also returned `50.0` because `w_3` has category `ACTIVEWEAR` (not `TOPS`) and `"culottes"` was not recognized as a volume/high-waisted bottom keyword.
3. **`ACTIVEWEAR` Anchor (`w_3` Cropped Hoodie) Had a Extra Blouse (`w_37`) Stacked on Top of It:** Because `w_3` has `category = ClothingCategory.ACTIVEWEAR`, `fillMissingSlotsDeterministically()` and `RoleGapAnalyzer` thought the outfit had no top and injected `w_37` ("Olive Spruce Blouse") onto `[w_3, w_35, w_52]`. Also, `StyleAuditLogger` hardcoded `Required: DRESSES (One-Piece) + SHOES` even when the anchor was `w_3` (`ACTIVEWEAR`).
4. **Cosmetic ID `"93"` (Missing `"c_"` Prefix) & Missing Cheek Repair Triggered a Failover:** `gemini-flash-lite-latest` returned `"selectedCosmeticIds": ["c_123", "93", "c_114", "c_133"]`. `RecommendationValidator` stripped `"93"` instead of normalizing it to `"c_93"`, and deterministic repair failed to backfill the missing `CHEEK` role, wasting 6.6s failing over to Gemma 4.

## STRICT OPERATING RULES (VIOLATION = IMMEDIATE FAILURE)
1. **ZERO GIT COMMANDS ALLOWED:** You are strictly forbidden from running ANY `git` command (`git`, `git status`, `git diff`, `git grep`, `git log`, `git checkout`, etc.). Use ONLY `nl -ba`, `sed`, `grep -rn`, `find`, and `./gradlew`.
2. **WORK ONE STEP AT A TIME:** Complete **Step 1**, run its **Checkpoint 1** commands, and paste the verbatim output before touching **Step 2**.
3. **READ BEFORE YOU WRITE:** Run the `nl -ba` inspection command at the start of every step before editing any file.

---

## STEP 1: Eliminate the 25–47s `gemini-3.8-flash` Hang in `FirebaseAiClient.kt`

### 1A. Inspect `FirebaseAiClient.kt` and Model Resolution First (Mandatory Read)
Run these commands in your terminal:
```bash
grep -rn "gemini-3.8-flash\|gemini-flash-lite\|25000" features/ applications/ core/ --include="*.kt"
nl -ba features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/FirebaseAiClient.kt | sed -n '1,180p'
```

### 1B. Edit `FirebaseAiClient.kt` (and Any Default Model Config) for Fast Execution
In `FirebaseAiClient.kt`:
1. **Promote Fast Model / Sanitize Stale Override:** If `modelName` (from Settings Flow or local override) resolves to `"gemini-3.8-flash"`, map it directly to `"gemini-flash-lite-latest"` (or place `"gemini-flash-lite-latest"` first in the fallback model chain) so the app calls the working 1.4-second model immediately instead of hanging for 25–47 seconds on `gemini-3.8-flash`.
2. **Fail-Over Immediately on Timeout or 503 Capacity Spikes:**
    - Reduce the per-model `withTimeout` from `25000L` (25s) to `10000L` (10s).
    - When a model throws a `503` / `"high demand"` / `ServerException` or `TimeoutCancellationException`, do **NOT** sleep and retry the same overloaded model—immediately break/continue to the next fallback model in the list!

### CHECKPOINT 1 (Run & Paste Verbatim Output)
```bash
nl -ba features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/FirebaseAiClient.kt | sed -n '1,160p'
./gradlew :features:ai:firebase:testDebugUnitTest
```

---

## STEP 2: Fix `SilhouetteGrader.kt` so Dresses (`DRESSES`), `ACTIVEWEAR` Tops & Culottes Score `82–95` Instead of `50.0`

### 2A. Inspect `SilhouetteGrader.kt` on Disk First (Mandatory Read)
Run:
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt | sed -n '1,120p'
```

### 2B. Update `SilhouetteGrader.kt` to Grade `DRESSES`, `ACTIVEWEAR`, and Expanded Silhouette Keywords
Edit `SilhouetteGrader.kt` so that:
1. **Empty Guard Preserved:** If `resolvedClothing.isEmpty()`, return `0f`.
2. **Support `DRESSES` (One-Piece) Ensembles:**
    - Identify `val dress = resolvedClothing.find { it.category == ClothingCategory.DRESSES }`.
    - Identify `val shoes = resolvedClothing.find { it.category == ClothingCategory.SHOES }`.
    - If `dress != null`:
        - Extract text from the dress (`"${dress.name} ${dress.material.orEmpty()} ${dress.subCategory.orEmpty()}"`).
        - A dress/jumpsuit inherently defines a unified upper-and-lower vertical column (`+25f` base structural cohesion).
        - If the dress text matches tailored/defined/tiered features via `KeywordMatcher.containsWholeWord` (`"tiered"`, `"smocked"`, `"utility"`, `"belted"`, `"tailored"`, `"fitted"`, `"structured"`, `"drape"`, `"sundress"`, `"jumpsuit"`, `"linen"`, `"silk"`), add `+10f`.
        - If paired with `shoes != null` (a complete one-piece + footwear ensemble), add `+10f` complete-ensemble credit.
        - This brings a well-paired dress + shoes ensemble (like `w_16` Sundress + `w_46` Sneakers, or `w_12` Jumpsuit + `w_53` Tennis shoes) to **`85.0f..95.0f`** instead of `50.0f`!
3. **Support `ACTIVEWEAR` Upper Garments & Expanded Top/Bottom Vocabulary:**
    - When finding `top`, check `it.category == ClothingCategory.TOPS || it.category == ClothingCategory.ACTIVEWEAR` (so `w_3` "Electric Coral Cropped Hoodie" is graded as the top!).
    - Expand `extractSilhouetteTags(item: ClothingItem)` using `KeywordMatcher.containsWholeWord`:
        - **`VOLUME` (Top):** `"oversized"`, `"puffer"`, `"relaxed"`, `"boxy"`, `"hoodie"`, `"loose"`
        - **`CUT` (Top):** `"cropped"`, `"fitted"`, `"slim"`, `"tailored"`, `"ribbed"`, `"blouse"`, `"camisole"`, `"turtleneck"`, `"oxford"`, `"structured"`
        - **`CROPPED` (Top):** `"cropped"`, `"crop"`
        - **`VOLUME` (Bottom):** `"wide leg"`, `"wide"`, `"cargo"`, `"culottes"`, `"culotte"`, `"pleated"`, `"flared"`, `"relaxed"`, `"slacks"`
        - **`CUT` (Bottom):** `"fitted"`, `"leggings"`, `"slim"`, `"tailored"`, `"straight"`, `"denim"`, `"jeans"`, `"leather"`
        - **`HIGH_WAISTED` (Bottom):** `"high waisted"`, `"high"`, `"culottes"`, `"pleated"`, `"flared"`
    - Ensure that:
        - **Box Penalty (`isTopVolume && isBottomVolume && !isTopCropped`):** Still applies `-35.0f` (score `15.0f` — preserving the existing `poorOrClashingInput_scoresBelow45` unit test!).
        - **Cropped Volume Top + Volume/High-Waisted Bottom (e.g., `w_3` Cropped Hoodie + `w_35` Ivory Culottes):** Because `isTopCropped` is true, a cropped hoodie paired with high-waisted/wide culottes follows the rule-of-thirds! Award `+32.0f` proportion balance (and `+10.0f` when `shoes != null`), scoring **`82.0f..92.0f`** instead of `50.0f`!
        - **Tapered Harmony (`(isTopVolume && isBottomCut) || (isTopCut && isBottomVolume)`):** Awards `+32.0f`.
        - **Classic Tailored Harmony (`isTopCut && isBottomCut`):** Awards `+22.0f` (so a fitted/tailored top + tailored bottom + shoes scores `50 + 22 + 10 = 82.0f`).

### CHECKPOINT 2 (Run & Paste Verbatim Output)
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/fashionista/scoring/SilhouetteGrader.kt | sed -n '1,130p'
./gradlew :applications:kocolor:data:testDebugUnitTest --tests "*.SilhouetteGraderTest" --tests "*.FashionistaEngineTest"
```

---

## STEP 3: Treat `ACTIVEWEAR` as Satisfying the `TOPS` Slot & Fix `[1] COMPOSITION POLICY` Audit Log

### 3A. Inspect `RoleGapAnalyzer.kt`, `RecommendationValidator.kt`, `StyleSimulatorEngine.kt`, and `StyleAuditLogger.kt` (Mandatory Read)
Run:
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RoleGapAnalyzer.kt | sed -n '1,90p'
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/telemetry/StyleAuditLogger.kt | sed -n '1,95p'
```

### 3B. Recognize `ACTIVEWEAR` as Satisfying Upper-Body (`TOPS`) Coverage
1. **`RoleGapAnalyzer.kt`:**
    - When checking whether the `TOPS` role is already satisfied by anchors or selected items, treat `ClothingCategory.TOPS` **or** `ClothingCategory.ACTIVEWEAR` as satisfying the `TOPS` slot!
2. **`RecommendationValidator.kt` & `StyleSimulatorEngine.kt` (`fillMissingSlotsDeterministically` & `isOutfitSlotComplete`):**
    - Treat `ClothingCategory.ACTIVEWEAR` as belonging to the `TOPS` slot bucket during slot deduplication and missing-slot backfill.
    - That way, when `w_3` ("Electric Coral Cropped Hoodie", `ACTIVEWEAR`) is selected in `["w_3", "w_35", "w_52"]`, the outfit already has a top (`w_3`), a bottom (`w_35`), and shoes (`w_52`)—so `fillMissingSlotsDeterministically` will **NOT** append a second top (`w_37` Olive Spruce Blouse)!
3. **`StyleAuditLogger.kt` (`[1] COMPOSITION POLICY`):**
    - Look at `[1] COMPOSITION POLICY` in `StyleAuditLogger.kt`. Right now it logs `Required: DRESSES (One-Piece) + SHOES` even when the anchor is `w_3` (`ACTIVEWEAR`)!
    - Fix that check so it logs `Required: DRESSES (One-Piece) + SHOES` **only** when the resolved anchor (or selected outfit) actually contains a `ClothingCategory.DRESSES` item; otherwise log `Required: TOP + BOTTOM + SHOES`.

### CHECKPOINT 3 (Run & Paste Verbatim Output)
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RoleGapAnalyzer.kt | sed -n '1,90p'
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/telemetry/StyleAuditLogger.kt | sed -n '50,95p'
./gradlew :applications:kocolor:data:testDebugUnitTest --tests "*.RoleGapAnalyzerTest" --tests "*.RecommendationValidatorTest"
```

---

## STEP 4: Normalize Prefix-Stripped IDs (`"93"` $\rightarrow$ `"c_93"`) & Backfill Missing Cosmetic Roles in Deterministic Repair

### 4A. Inspect `RecommendationValidator.kt` & `StyleSimulatorEngine.kt` Repair Logic (Mandatory Read)
Run:
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RecommendationValidator.kt | sed -n '1,160p'
grep -n -C 20 "Attempting deterministic local repair" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
```

### 4B. Normalize Bare Numeric IDs (`"93"` $\rightarrow$ `"c_93"`, `"12"` $\rightarrow$ `"w_12"`) in `RecommendationValidator.kt`
In `RecommendationValidator.validateAndSanitize(...)`:
1. Before filtering `blueprint.selectedCosmeticIds` against `validCosmeticsMap`, normalize each raw ID:
   ```kotlin
   val normalizedCosmeticId = when {
       rawId in validCosmeticsMap -> rawId
       "c_${rawId.trim()}" in validCosmeticsMap -> "c_${rawId.trim()}"
       else -> rawId
   }
   ```
   *(If `"93"` is normalized to `"c_93"`, it matches `c_93` "Starlight Liquid Glow" in `validCosmeticsMap` and is preserved instead of stripped!)*
2. Do the exact same normalization for `blueprint.selectedClothingIds` (`"w_${rawId.trim()}"`).

### 4C. Backfill Missing Cosmetic Roles During Deterministic Local Repair in `StyleSimulatorEngine.kt`
In `StyleSimulatorEngine.kt` (around `"Attempting deterministic local repair"`):
1. Right now, when validation flags `Missing required cosmetic roles [Cheek]`, deterministic repair only calls `fillMissingSlotsDeterministically(...)` for clothing and never repairs `selectedCosmeticIds`!
2. Add `fillMissingCosmeticRolesDeterministically(...)` (and call it alongside `fillMissingSlotsDeterministically(...)` during deterministic repair and upstream completion):
    - Check which required pigment cosmetic roles (`EYE`, `CHEEK`/`DIMENSION`, `LIP`, `NAIL`) are present in `blueprint.selectedCosmeticIds`.
    - For any required cosmetic role that is missing (and has at least one eligible candidate in the cosmetic pool), deterministically append the highest-scored eligible cosmetic item ID for that role.
    - Also deduplicate `selectedCosmeticIds` by role so at most 1 item per cosmetic role (`EYE`, `CHEEK`, `LIP`, `NAIL`) is kept.

### CHECKPOINT 4 (Run & Paste Verbatim Output)
```bash
nl -ba applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RecommendationValidator.kt | sed -n '1,140p'
grep -n -C 25 "Attempting deterministic local repair" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
./gradlew :applications:kocolor:data:compileDebugKotlin
```

---

## STEP 5: Add Unit Tests for All 4 Fixes & Run Full Verification Suite

Add unit tests in `:applications:kocolor:data` covering these exact scenarios from the live Logcat trace:
1. **`SilhouetteGraderTest`:**
    - `dressAndSneakersEnsemble_scoresAtLeast82()`: Pass `w_16` ("Warm Terracotta Linen Sundress", `DRESSES`, `"100% European Flax Linen, Smocked Back, Tiered Skirt"`) + `w_46` ("Aesthetic Pink Sneakers", `SHOES`) and assert `SilhouetteGrader` scores `>= 82.0f` (NOT `50.0f`).
    - `croppedHoodieAndCulottesEnsemble_scoresAtLeast82()`: Pass `w_3` ("Electric Coral Cropped Hoodie", `ACTIVEWEAR`) + `w_35` ("Melodious Ivory Culottes", `BOTTOMS`) + `w_52` ("Rouge Strappy Sandals", `SHOES`) and assert `SilhouetteGrader` scores `>= 82.0f`.
2. **`RecommendationValidatorTest`:**
    - `normalizesBareNumericCosmeticId_93_to_c_93()`: Pass `selectedCosmeticIds = listOf("c_123", "93", "c_114", "c_133")` where `c_93` is a valid `CHEEK` item; assert `"93"` is normalized to `"c_93"`, all 4 roles are satisfied, and `validation.isValid == true`.
    - `activewearHoodie_satisfiesTopSlotWithoutAddingExtraBlouse()`: Pass `selectedClothingIds = listOf("w_3", "w_35", "w_52")` where `w_3` is `ACTIVEWEAR`, `w_35` is `BOTTOMS`, and `w_52` is `SHOES`; assert `fillMissingSlotsDeterministically` / `RecommendationValidator` keeps `["w_3", "w_35", "w_52"]` of size `3` and does NOT inject a 4th `TOPS` item.

### MANDATORY COMPLETION GATE (Paste All Outputs)
Run and paste the verbatim terminal output of:
```bash
./gradlew :features:ai:core:testDebugUnitTest \
  :features:ai:firebase:testDebugUnitTest \
  :features:ai:local:testDebugUnitTest \
  :applications:kocolor:data:testDebugUnitTest \
  :applications:kocolor:features:analyzer:testDebugUnitTest \
  :applications:kocolor:apps:mobile:assembleDebug
```

```