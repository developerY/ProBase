# Recommendation Bugs Remediation Audit (NeedsFix)

This document tracks the resolution of the runtime recommendation bugs surfaced in `NeedsFix.md`.

## STEP 1: Eliminate the 25–47s `gemini-3.8-flash` Hang
- **What was done:** Edited `FirebaseAiClient.kt` to map `gemini-3.8-flash` to `gemini-flash-lite-latest` natively, ensuring fast responses. Reduced fallback retries for timeouts to failover faster by skipping the jitter wait on overloaded capacity errors.

## STEP 2: Fix `SilhouetteGrader.kt`
- **What was done:** 
  - Added support for `ClothingCategory.DRESSES` (awarding 25f for structure, +10f for cuts, +10f for shoes). 
  - Expanded `ACTIVEWEAR` to trigger top-slot checks. 
  - Expanded the `KeywordMatcher` logic for tags like `volume`, `cut`, `cropped`, and `high_waisted` (e.g. `oversized`, `puffer`, `relaxed`, `culottes`, `leggings`, etc.).

## STEP 3: Treat ACTIVEWEAR as Satisfying TOPS
- **What was done:** Updated `RoleGapAnalyzer.kt` to classify `ACTIVEWEAR` as satisfying the `TOPS` slot. 
- **What was done:** Updated `StyleAuditLogger.kt` to accurately log the `DRESSES (One-Piece) + SHOES` role intent instead of always printing it for non-dress anchors.

## STEP 4: Normalize Prefix-Stripped IDs
- **What was done:** In `RecommendationValidator.kt`, updated `filteredCosmeticIds` and `filteredClothingIds` with a `mapNotNull` normalization function `c_${rawId.trim()}` and `w_${rawId.trim()}`.
- **What was done:** Implemented `fillMissingCosmeticRolesDeterministically` in `StyleSimulatorEngine.kt` to automatically repair missing cosmetic roles (EYE, CHEEK, LIP, NAIL).

## STEP 5: Mandatory Unit Tests for All Fixes
- Added `dressAndSneakersEnsemble_scoresAtLeast82` and `croppedHoodieAndCulottesEnsemble_scoresAtLeast82` in `SilhouetteGraderTest`.
- Evaluated `RecommendationValidatorTest` with ungrounded ids, prep removals, string replacements and `normalizesBareNumericCosmeticId_93_to_c_93` and `activewearHoodie_satisfiesTopSlotWithoutAddingExtraBlouse`.
- Successfully ran all tests.
