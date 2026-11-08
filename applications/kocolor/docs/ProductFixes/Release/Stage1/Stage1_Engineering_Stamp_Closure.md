# Stage 1 — Engineering Stamp Closure (Release Runbook Split)

Source: `../Gemini_stages.md` ("Stage 1 Prompt"). Execute ONLY this stage; feed one stage at a time to the coding agent.

## Fixes applied in this split (vs. `Gemini_stages.md`)

- Corrected the Gate C2 row distribution to the verified values `7 + 7 + 5 + 76 + 7 = 102` (was `3 + 7 + 5 + 80 + 7` — wrong per-row counts; verified against `Phase4_Audit_1.md:212-216`, prose `101` still at lines 12 and 224).
- Verified all referenced paths exist: `Phase4_Audit_1.md`, `HeuristicStyleEngineTest.kt` (`data/.../usecase/`), `GreedyRehydratorTest.kt` (`features/analyzer/.../simulator/ui/`), `FashionistaEngineTest.kt`, `IntentFulfillmentEvaluator.kt`, `RotationScoringUseCase.kt`.

---

# AGENT MISSION: Execute Stage 1 — Engineering Stamp Closure (Docs + Fresh Test/Lint Evidence)

You are executing **Stage 1 (1.1, 1.2, and 1.3)** of `ProductionRoadMap.md`.

## STRICT STANDING RULES (VIOLATION = IMMEDIATE FAILURE)
1. **DO NOT MODIFY PRODUCTION CODE IN STAGE 1:** Stage 1 is strictly an audit-documentation closure and fresh test/lint verification pass. Do NOT downgrade `minSdk 34`, `targetSdk 37`, `DataStore 1.2.1`, or `zstd 1.5.7-21`.
2. **DO NOT OVERWRITE HISTORICAL TEST LOGS:** Keep the historical Phase 4 test run logs in `Phase4_Audit_1.md` intact as historical evidence; only correct the prose sum (`101` -> `102`) to match the existing Gate C2 table rows (`7 + 7 + 5 + 76 + 7 = 102`). Record the fresh Stage 1 test and lint execution results separately in `ProductionRoadMap.md` (or a dedicated Stage 1 section).
3. **WORK ONE MICRO-STEP AT A TIME:** Complete Micro-Step 1.1A, run its verification command, and paste the verbatim output before moving to Micro-Step 1.1B.
4. **NEVER INVENT GREP OR TEST OUTPUTS:** Every command output pasted must come from an actual terminal execution.

---

## MICRO-STEP 1.1: Close All 6 Findings in `Phase4_Audit_1.md`

Locate and read `Phase4_Audit_1.md` (`find . -name "Phase4_Audit_1.md"`). Execute these 6 targeted corrections in order:

### 1.1A. Reconcile Historical Prose Test Count (`101` -> `102`) Without Altering Historical Logs
1. Inspect the Gate C2 module test table in `Phase4_Audit_1.md`. Confirm the rows sum to `102`.
2. Update the prose text in `Phase4_Audit_1.md` where it claims `101` passed tests so it accurately states `102` (matching its own Gate C2 table). Do NOT replace the historical Gate C2 log block with a new run.
3. **Checkpoint 1.1A:**
   ```bash
   grep -n "101\|102" applications/kocolor/docs/ProductFixes/Directive/Phase4/Phase4_Audit_1.md
   ```

### 1.1B. Add the `compositeScore` Scale-Adjudication Note
1. Inspect `ColorModels.kt` and `WardrobeCandidateFilter.kt` to see how wardrobe `compositeScore` and cosmetic sub-scores are produced and normalized:
   ```bash
   grep -n -C 12 "totalScore\|compositeScore\|MAX_" \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt
   ```
2. In `Phase4_Audit_1.md` under Step 3 (`totalScore` Normalization), add a **Scale-Adjudication Note** with exact file:line producer evidence explaining the raw score range of wardrobe `compositeScore` and cosmetic sub-score sums, and why dividing by the theoretical maximum (`5.0f` / `MAX_*`) maps both pools monotonically into `[0.0, 1.0]` without score collapse.

### 1.1C. Paste the Missing `WardrobeCandidateFilter` Half of Gate B3
1. Run Gate B3 across **both** `ColorModels.kt` and `WardrobeCandidateFilter.kt`:
   ```bash
   grep -n -C 8 "isAnchor\|anchorPriority\|totalScore\|MAX_\|5\.0" \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt
   ```
2. Paste the verbatim output from **both** files into Gate B3 of `Phase4_Audit_1.md`, explicitly noting if a literal `5.0f` divisor was used instead of a named `MAX_*` constant.

### 1.1D. Evidence Step 1B's Third Bullet (`adaptContextToProvider` & `missingRoleRequirements`)
1. Run:
   ```bash
   grep -n -C 10 "missingRoleRequirements\|determineRoleRequirements\|slotsPerCategory" \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
   ```
2. Paste the verbatim `grep` output into `Phase4_Audit_1.md` under Step 1B, proving how `adaptContextToProvider` prioritizes unfilled roles from `RoleGapAnalyzer` while preserving the `slotsPerCategory` floor-of-1 step-down logic.

### 1.1E. Evidence `HeuristicStyleEngineTest` Expansion & `GreedyRehydratorTest` Regression Verification (Separate Commands)
1. Grep the `@Test` methods in both test files:
   ```bash
   grep -n -A 1 "@Test" \
     applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngineTest.kt \
     applications/kocolor/features/analyzer/src/test/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/GreedyRehydratorTest.kt
   ```
2. Run each test suite in its own separate Gradle invocation so the results are unambiguous:
   ```bash
   ./gradlew :applications:kocolor:data:testDebugUnitTest --tests "com.zoewave.probase.kocolor.data.usecase.HeuristicStyleEngineTest" --rerun-tasks
   ./gradlew :applications:kocolor:features:analyzer:testDebugUnitTest --tests "com.zoewave.probase.kocolor.features.analyzer.simulator.ui.GreedyRehydratorTest" --rerun-tasks
   ```
3. Paste both verbatim outputs into `Phase4_Audit_1.md` under Step 6.

### 1.1F. Add `RotationScoringUseCase.kt` Threshold Wiring to the File Modification Table
1. Verify `KoColorThresholds` usage in `RotationScoringUseCase.kt`:
   ```bash
   grep -n "KoColorThresholds" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RotationScoringUseCase.kt
   ```
2. Add the row for `RotationScoringUseCase.kt` (`:applications:kocolor:data` | `data/usecase/RotationScoringUseCase.kt` | **MODIFIED** | Wired centralized `KoColorThresholds` rotation/recency constants) to the File Modification Summary Table in `Phase4_Audit_1.md`.

---

## MICRO-STEP 1.2: Close Phase 3 Pass 2 Nits (`Phase3_Audit_1.md` & `Phase3_Audit_2.md`)

1. **Reword Banner in `Phase3_Audit_1.md`:**
    - Open `applications/kocolor/docs/ProductFixes/Directive/Phase3/Phase3_Audit_1.md`.
    - Replace the `"SUPERSEDED"` banner with:
      ```markdown
      > [!NOTE]
      > **BASE RECORD — READ WITH `Phase3_Audit_2.md`:** This document contains the base Phase 3 (Pass 1) file modification inventory and `KeywordMatcher` verification logs. Read together with [`Phase3_Audit_2.md`](./Phase3_Audit_2.md), which records the Pass 2 punchlist fixes and final verification gates.
      ```
2. **Paste Item-4 Existence Grep & Full Gate-3 Context Output into `Phase3_Audit_2.md`:**
   ```bash
   grep -n -C 3 "smartPick_topDoesNotMatchStopSignTee" applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngineTest.kt
   grep -n -C 4 "effectiveActiveDimensions" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/IntentFulfillmentEvaluator.kt
   grep -n -C 5 "goodEverydayOutfit_scoresBetween80And89AndPassesGate" applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/fashionista/scoring/FashionistaEngineTest.kt
   ```
   Paste all three verbatim outputs into `Phase3_Audit_2.md`.

---

## MICRO-STEP 1.3: Run Fresh Full Test Suite + `assembleDebug` + `lintRelease` (Record Separately)

1. Run the full unit test suite and debug assembly fresh:
   ```bash
   ./gradlew :features:ai:core:testDebugUnitTest \
     :features:ai:firebase:testDebugUnitTest \
     :features:ai:local:testDebugUnitTest \
     :applications:kocolor:data:testDebugUnitTest \
     :applications:kocolor:features:analyzer:testDebugUnitTest \
     :applications:kocolor:apps:mobile:assembleDebug --rerun-tasks
   ```
2. Run `lintRelease` fresh:
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:lintRelease
   ```
3. Record these fresh Stage 1 outputs in a Stage 1 Evidence block in `ProductionRoadMap.md` (keeping them distinct from the historical Phase 4 audit run).

## STAGE 1 MANDATORY COMPLETION GATE
Check off `[x] 1.1`, `[x] 1.2`, and `[x] 1.3` in `ProductionRoadMap.md` ONLY after pasting:
1. Confirmation and diffs for all 6 items in `Phase4_Audit_1.md`.
2. Confirmation and diffs for `Phase3_Audit_1.md` and `Phase3_Audit_2.md`.
3. Verbatim console output and exact test pass counts for the fresh Stage 1 unit test run, `assembleDebug`, and `lintRelease` (`0 errors`).
