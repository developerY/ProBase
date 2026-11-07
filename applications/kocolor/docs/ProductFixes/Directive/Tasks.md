> Agent execution prompt for RoadMapToPro.md Phases 1–4. Feed this file to the coding agent; run the checkpoint after each phase before proceeding.


# AGENT MISSION: Execute `RoadMapToPro.md` (Phases 1–4) for KoColor

You are tasked with executing the full engineering remediation plan in `applications/kocolor/docs/ProductFixes/RoadMapToPro.md` based on `applications/kocolor/docs/ProductFixes/fashion_algorithms_assessment.md`.

Work sequentially through **Phase 1 → Phase 2 → Phase 3 → Phase 4**. After completing each phase, run `./gradlew testDebugUnitTest` and verify zero compilation or test regressions before starting the next phase.

---

## PHASE 1: P0 — Pipeline Decision, Scoring Unification, AI Governance & Privacy

### 1.0 Execute the "Kill" Decision on the Dead 6-Feature Scorer
We are standardizing on the live 3-evaluator `FashionistaEngine`.
1. Delete the unreachable 6-feature scorer classes, dead extractors/engines, and dead domain types:
   - `FashionistaScorerImpl`, `DeterministicScorer`, `CalibrationCurve`, `InteractionModel`, `ChromaticHarmonyEngine`, `GlcmTextureEngine`, `TextureHarmonyEngine`, `SilhouetteEngine`, `VisualMassEngine`, `CosmeticIntegrationEngine`, `OutfitIntegrationEngine`, `VisualHierarchyEngine`, `CompositionEngine`.
   - All five feature extractors (`Color/Composition/Hierarchy/Silhouette/TextureFeatureExtractor`).
   - Dead domain types: `FashionistaScorer` (interface), `FashionistaFeatureVector`, `FeatureValue`, `FashionistaObservation`.
   - Test: `FashionistaScorerTest`; then grep all test sources for references to any deleted type and update them.
   - **DO NOT DELETE** the live path: `FashionistaEngine`, `ColorHarmonicsEvaluator`, `SilhouetteGrader`, `ContrastAnchorEvaluator`, `FashionistaEvaluator`, `FashionistaModule`, `FashionistaScore.kt` (reshaped in 1.2 item 4), and `ItaCalculator` (needed for on-device skin telemetry in Phase 1.4).
   - **DEFER, do not delete:** `fashionista/math/*` and `fashionista/color/ColorSpace.kt` — shared color-math utils whose fate is decided in Phase 4.3 consolidation (`ItaCalculator` needs `LabColor`; Phase 1.4 needs Lab conversion).
   - Expected transient breakage: `FashionistaScore.kt` will not compile until 1.2 item 4 lands — continue through it; the phase checkpoint is the atomic gate.
2. Delete the unused and dimensionally broken `ColorHarmonyEngine.calculateDeltaE` (`data/color/ColorHarmonyEngine.kt:149`).

### 1.1 Finish `UNKNOWN` Manifest Defaults (`CompactManifestSerializer.kt`)
1. `material` already defaults to `"UNKNOWN"`.
2. Replace the remaining invented fallback literals—`finish` (`"SATIN"`), `brand` (`"KoColor"`), and `depth` (`"Balanced"`)—with `"UNKNOWN"` across both wardrobe and cosmetic serialization paths (EXPANDED, BALANCED, MINIMAL).

### 1.2 Reconcile Fashionista Scoring, Move Slot Completion Upstream & Strip `GreedyRehydrator`
1. **Move Deterministic Slot Completion Into `StyleSimulatorEngine.generateBlueprint()`:**
   - Before `FashionistaEngine` and `IntentFulfillmentEvaluator` run, complete any missing required slots strictly from the **eligible candidate pool** (never from raw unfiltered inventory, respecting hidden, thermal, rotation, and anchor constraints). Source the missing-slot requirements from `RoleGapAnalyzer.determineRoleRequirements()` (this is its adoption — Phase 4.3 additionally wires its output into manifest budget partitioning).
   - Implement dress-aware satisfaction here: a selected `DRESSES`/`ONE_PIECE` item fills both `TOP` and `BOTTOM` (no redundant bottoms). Phase 4.1 extends only the heuristic fallback's own slot picking with the same rule.
   - **Anchor-First Ordering:** Place locked anchors into their slots *before* AI-resolved or backfilled items so anchors win category conflicts by construction without any `distinctBy` race.
2. **Preload Silhouette Data:**
   - Refactor `SilhouetteGrader` to accept already-resolved domain models (`List<ClothingItem>`) rather than executing blocking DB queries on `Dispatchers.Default`.
3. **Single Evaluation Pass via Result Channel (Do Not Mutate `StyleBlueprint`):**
   - Keep `StyleBlueprint` strictly as the AI JSON wire contract.
   - Return `FashionistaScore`, `IntentFulfillment`, and `ValidationReport` (see 1.3) on the result wrapper returned by `StyleSimulatorEngine.generateBlueprint()` so `GenerateStyleResultUseCase` reuses the single evaluation instead of re-running `FashionistaEngine` and `IntentFulfillmentEvaluator`.
4. **Reshape `FashionistaScore`:**
   - Remove the fake all-`0.85` default `breakdown` vector and the 6-feature `FashionistaFeatureVector` breakdown type. Replace it with a 3-field breakdown snapshot populated from the evaluated sub-scores (`colorHarmonyScore/100`, `silhouetteScore/100`, `contrastScore/100`) as primitive `Double` values. Unify on single constants `standardId="FASHIONISTA"`, `standardVersion="v1"` (deleting the `"v1.1"` default). Update any UI/Compose radar or breakdown call sites referencing `FashionistaScore.breakdown` to read the 3-axis snapshot. This un-breaks `FashionistaScore.kt` after the Phase 1.0 deletions.
5. **Turn `GreedyRehydrator` into a Pure ID-to-UI Mapper:**
   - Delete all 5 legacy fallback/rescoring steps from `GreedyRehydrator.kt`:
     1. Keyword recategorization (`applyClothingKeywordFallbacks` at line ~75, eliminating `"top" ⊂ "stop"`).
     2. Anchor fail-safe append + `distinctBy { it.category }` (line ~44).
     3. Inventory-wide slot backfill (`assignToSlots` `inventory.find` fallbacks).
     4. Name-based cosmetic fallbacks (lines ~129–136, eliminating `"lip" ⊂ "eclipse"`).
     5. `ColorHarmonyEngine.calculateFashionistaScore` rescoring — delete `calculateFashionistaScore` from `ColorHarmonyEngine.kt`, change `mapToVisualBlueprintData` to accept the pre-computed score `Int` as a parameter (no internal scoring and no default value), update its callers (`StyleSimulatorViewModel`, `BlueprintDetailContent`) to pass the result-channel score through to `koColorScore`, and update `GreedyRehydratorTest` in place to match the new signature (do NOT delete `GreedyRehydratorTest`).

### 1.3 Complete Validator Checklist Plumbing (`RecommendationValidator.kt`, `GenerateStyleResultUseCase.kt`)
1. Update `RecommendationValidator` to produce a structured `ValidationReport` containing per-rule booleans (ID grounding, anchor retention, role firewall, rationale scrub).
2. Carry `ValidationReport` on the simulator result channel alongside `StyleBlueprint`.
3. In `GenerateStyleResultUseCase.executeCreationResult()` (block at `:150`), bind the creation-flow UI checklist items directly to `ValidationReport` instead of hardcoding `isValid = true`.

### 1.4 Govern Side-Path Engines & Enforce Multimodal Privacy (`SuggestionEngine.kt`, `AnalyzerEngine.kt`, Providers)
1. Refactor `SuggestionEngine` and `AnalyzerEngine` to route requests through `CapabilityRouter` and `AiProvider` instead of calling `Firebase.ai` directly. Remove their unused `apiKey` parameters.
2. In `AnalyzerEngine`, downscale any input `Bitmap` so its longest edge is `<= 1024px` (target 768–1024px) before processing.
3. Extract skin/hair color telemetry on-device: sample skin/hair regions from the (downscaled) bitmap, convert to CIELAB (`ColorUtils.colorToLAB` or the retained `ColorSpaceConverter`), and compute ITA via `ItaCalculator` (plus `ColorQuantizer` temperature) so text-only cloud providers receive numeric telemetry rather than raw face selfies.
4. In `ByokAiProvider` and `GemmaRestFallbackProvider` (and/or `CapabilityRouter` via `supportsLocalImageIngestion`), explicitly reject `AiInput.Multimodal` with a clear failure result instead of silently dropping images.

> **CHECKPOINT 1:** Run `./gradlew testDebugUnitTest` and fix any broken imports or test assertions before proceeding to Phase 2.

---

## PHASE 2: P1 — Token Spend, Fit-Loop Latency & Provider Hardening

### 2.1 Right-Size the On-Device Nano Tier (`PromptAssembler.kt`, `StyleSimulatorEngine.kt`)
1. Add `PromptAssembler.assembleCompactNano()` with stripped prose instructions, key-value telemetry, and a minimal JSON schema (~180 tokens).
2. In `StyleSimulatorEngine`, add a fast-fail gate for `LocalNanoAiProvider`: if the heuristic token estimate at `DetailLevel.MINIMAL` and `K = 4` exceeds `maxInputTokens` (768), skip Nano immediately without running the `adaptContextToProvider` loop.

### 2.2 Remove Network I/O from the Fit Loop & Honor the K Floor (`StyleSimulatorEngine.kt`, `DeterministicContextEngine.kt`)
1. Hoist `generateSelectionState` outside the `adaptContextToProvider` step-down loop so it runs once per request.
2. Inside `adaptContextToProvider` (`:298`), replace `provider.countTokens()` with a local character heuristic (`prompt.length / 4`) during step-down iterations, calling `provider.countTokens()` at most **once** on the final adapted prompt.
3. Allow `slotsPerCategory` to step down to `1` per required category when `K` reaches `minCandidateAdditions = 4` so the manifest actually shrinks at the floor.

### 2.3 Enforce `maxOutputTokens` & Record Billed Telemetry (`FirebaseAiClient.kt`, `ByokAiProvider.kt`, `GemmaRestFallbackProvider.kt`, `StyleAuditLogger.kt`)
1. Pass `capability.maxOutputTokens` into `FirebaseAiClient` `generationConfig`, `ByokAiProvider` JSON request bodies, and `GemmaRestFallbackProvider` payloads.
2. Introduce a provider-common execution result carrying usage: `AiResponse` exists only on the Firebase path while BYOK/Gemma return `Result<String>`, so add a shared carrier (e.g. `AiExecutionResult(text, usage: TokenUsage)`) returned by `AiProvider.execute`, parse `usageMetadata` in the BYOK/Gemma REST responses (treat absent usage as zeros), update all `execute()` call sites (incl. `StyleSimulatorEngine`), and log both input and output tokens in `StyleAuditLogger`.

### 2.4 Validation Repair, Provider Timeouts & Cache Stabilization
1. **Deterministic Validation Repair (`StyleSimulatorEngine.kt`):**
   - When `RecommendationValidator` flags removed invalid IDs or missing cosmetic roles (note: locked anchors are already re-injected by the validator), deterministically substitute the top-ranked valid candidate of the same slot/role from the eligible pool (or run at most 1 compact repair prompt) before failing over to the next provider.
2. **Provider Timeout & Temperature Parity:**
   - In `ByokAiProvider`, configure an 8s OkHttp timeout and forward `temperatureOverride` into the request body.
   - In `GemmaRestFallbackProvider`, reduce the OkHttp read timeout from 180s to 60s (matching capability declarations) and forward `temperatureOverride` on the proxy path.
3. **Prompt Cache Stabilization (`PromptCacheRepository.kt`):**
   - Join fingerprint fields with the explicit unit separator delimiter `"\u001F"` before SHA-256 hashing.
   - Do **not** alphabetically sort manifest lines (preserve rank order). Quantize recency timestamps into day-buckets upstream so rotation scores remain stable within the same calendar day.

> **CHECKPOINT 2:** Run `./gradlew testDebugUnitTest` and verify all provider and simulator tests pass.

---

## PHASE 3: P2 — Fashion Math Discrimination, Calibration & NLP

### 3.1 Widen Live Scoring Dynamic Range & Fix Single-Chroma Scoring (`ColorHarmonyEngine.kt`, `FashionistaEngine.kt`)
*(Note: `ChromaticHarmonyEngine` and `CalibrationCurve` were deleted in Phase 1.0).*
1. **Single-Chroma Neutral Contrast (`ColorHarmonyEngine.kt:184`):**
   - Replace the automatic `38/40` score when `<= 1` chromatic item is present with a neutral-contrast evaluation based on $L^*$ separation (`max(L*) - min(L*)`) across the outfit.
2. **Recalibrate Evaluator Baselines AND Spreads (`FashionistaEngine.kt` & Evaluators):**
   - Lower evaluator baselines to `50` AND widen the modifier spreads proportionally (e.g., color harmony `avg * 10` → `avg * 25`, silhouette and contrast modifiers scaled toward `±50`) so that individual evaluators span `[0, 100]` and the maximum achievable blended score is `>= 95` (well above the `80`-point `isApproved` gate). Verify via unit test that a strong outfit scores `>= 85` and a clashing outfit scores `< 60`.

### 3.2 Complete Intent Fulfillment & Numeric Appearance Matching (`IntentFulfillmentEvaluator.kt`, `ContrastAnchorEvaluator.kt`)
1. In `IntentFulfillmentEvaluator`, replace hardcoded `0.5` values:
   - Compute `contrast` from normalized $\max(L^*) - \min(L^*)$ over selected items.
   - Compute `novelty` from `1.0 - mean(rotationPenalty)` — thread rotation data into `evaluate()` via a new parameter (it currently receives only profile + selected items).
   - Compute `formality` from the normalized mean of core-model `ClothingItem.formality.rank` (`Formality` enum, NOT taxonomy `FormalityLevel`, which does not exist on these items); cosmetics without a formality field contribute neutrally.
2. In `ContrastAnchorEvaluator` and cosmetic temperature scorers, replace fragile `String.contains("High")` / `contains("Light")` checks with typed `ColorTelemetry` numeric fields and enums.

### 3.3 Shared Word-Boundary Keyword Matcher
1. Create a shared word-boundary keyword matcher utility in `core/` that tokenizes on `\b` word boundaries, lowercases, and strips English stop-words (`"a"`, `"an"`, `"the"`, `"in"`, `"on"`, `"for"`, `"with"`, `"to"`, `"of"`).
2. Replace the 4 remaining raw `String.contains()` substring matchers in:
   - `WardrobeCandidateFilter.calculateScore`
   - `WardrobeCandidateFilter.calculateCosmeticScore`
   - `HeuristicStyleEngine.smartPick`
   - `SilhouetteGrader`

> **CHECKPOINT 3:** Run `./gradlew testDebugUnitTest` and verify all math and evaluator tests pass.

---

## PHASE 4: P2 — Orchestration Gears, Taxonomy Alignment & Tests

### 4.1 Deterministic Heuristic Fallback & Fallback Dress Support (`HeuristicStyleEngine.kt`, `DeterministicContextEngine.kt`)
1. In `HeuristicStyleEngine.kt:39`, replace `.randomOrNull()` / `.random()` with deterministic selection (`maxByOrNull { it.compositeScore }` or stable ID tie-breaker), and build `recommendedPalette` strictly from the selected items' actual hex codes (no random neutral padding).
2. Extend the heuristic fallback's own slot picking with the dress rule from Phase 1.2 (a selected dress satisfies both `TOP` and `BOTTOM`, no redundant bottoms) and align `DeterministicContextEngine` viability filtering with it. Do not touch `GreedyRehydrator` — it is a pure mapper since Phase 1.2 and inherits dress handling from upstream completion.

### 4.2 Centralize Thresholds, Fix Audit Funnel & Safely Normalize `totalScore`
1. Consolidate all thermal bands, outerwear temperature cutoffs, rotation thresholds, and chroma gates into `KoColorTaxonomy`.
2. In `DeterministicContextEngine.kt:58`, record distinct sequential counts for `afterWeather`, `afterRotation`, and `finalEligible` instead of assigning the final count to all three. In `StyleAuditLogger.kt:103`, log the real anchor priority value instead of the literal `"Priority: 4.00 / 4.00"`.
3. **Safely Normalize `ColorModels.totalScore` (`ColorModels.kt:27`):**
   - **Step A:** Add an explicit `isAnchor: Boolean` / `anchorPriority: Int` field to provenance models and update `sortedPartitionedPool` and `StyleAuditLogger` to sort and display anchors using that explicit field rather than relying on `totalScore > 2.0`.
   - **Step B:** Once anchor sorting and audit logging are decoupled, normalize both wardrobe and cosmetic `totalScore` values to `[0.0, 1.0]`.
   - Document the pigment-only cosmetic policy (`EYES`, `DIMENSION`, `LIPS`, `NAILS`) in `WardrobeCandidateFilter` / taxonomy docs.

### 4.3 Dead Code Cleanup, Utility Consolidation & Unit Test Suite
1. **Preflight Gate & Role Diversity:**
   - Port the preflight slot-completeness gate concept from `DeterministicCatalogEngine` onto `StyleSimulatorEngine`'s `ClothingItem` pool (aborting before AI spend if required slots cannot be filled), then delete the unused `DeterministicCatalogEngine` class.
   - Wire `selectionState.missingRoleRequirements` (from `RoleGapAnalyzer`) into `adaptContextToProvider`'s `categorizedPool`/`slotsPerCategory` partitioning (preserving the Phase 2.2 floor-of-1 step-down logic) so candidate slots only allocate budget to unfilled roles.
   - Delete dead methods/fields: `WardrobeCandidateFilter.getCandidates`, `WardrobeCandidateFilter.getCosmeticCandidates`, `DeterministicContextEngine.enforceRoleDiversity`, `FirebaseAiClient.getStyleAdvice`, `FirebaseAiClient.estimateTokens`, and unused `rotationScoringUseCase`/`repository` constructor injections in `DeterministicContextEngine`.
2. **Consolidate Duplicates:**
   - Merge duplicate `hexToLCh`, `calculateChroma`, chroma-weighted circular mean, and JSON brace-stripping helpers into shared utilities in `core/color` and `core/json`.
3. **Expand Unit Tests:**
   - Add unit tests covering:
     - Live `FashionistaEngine` 40/35/25 blend and recalibrated `[0, 100]` spread (verifying approval gate behavior).
     - `PromptAssembler` (standard vs. Nano compact) and `CompactManifestSerializer` detail levels (`UNKNOWN` defaults).
     - `StyleSimulatorEngine` `K`/detail step-down loop, single `countTokens` call, validation repair, and cache hits.
     - `RecommendationValidator` `ValidationReport` generation and rationale scrubbing.
     - Upstream anchor-first slot completion and `GreedyRehydrator` pure mapping.
     - `HeuristicStyleEngine` determinism and dress slot satisfaction.

> **FINAL VERIFICATION:** Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` and report a summary of all modified/deleted files and test results.


