This engineering remediation plan addresses all unresolved findings across token governance, color mathematics, orchestration gears, dead code, and test coverage in the KoColor assessment. Status of assessment P0s already in the tree: #4 (`ByokAiProvider` JSON serialization) is resolved (verified: `kotlinx.serialization` body builder, manual escaping gone). #3 (`UNKNOWN` defaults) is only partly resolved — `material` now emits `UNKNOWN`, but `finish` (`SATIN`), `brand` (`KoColor`), and `depth` (`Balanced`) still assert invented facts; finishing it is Phase 1 item 4 below.

## Execution Roadmap Matrix

| Phase | Priority | Focus Area | Key Target Files |
| --- | --- | --- | --- |
| **Phase 1** | **P0** | Pipeline decision, Scoring, Privacy, Validation | `StyleSimulatorEngine.kt`, `GreedyRehydrator.kt`, `AnalyzerEngine.kt`, `SuggestionEngine.kt`, `CompactManifestSerializer.kt` |

|
| **Phase 2** | **P1** | Token Budgets, Providers, Cache | `LocalNanoAiProvider.kt`, `FirebaseAiProvider.kt`, `PromptAssembler.kt`, `PromptCacheRepository.kt`

|
| **Phase 3** | **P2** | Fashion Math, Calibration, NLP | `ChromaticHarmonyEngine.kt`, `CalibrationCurve.kt`, `IntentFulfillmentEvaluator.kt`, `WardrobeCandidateFilter.kt`

|
| **Phase 4** | **P2** | Gears, Thresholds, Hygiene, Tests | `DeterministicContextEngine.kt`, `HeuristicStyleEngine.kt`, `ColorHarmonyEngine.kt`, `FashionistaModule.kt`

|

## Phase 1: P0 — Pipeline Decision, Scoring Unification, AI Governance & Privacy

**0. Gating decision: keep or kill the 6-feature scorer (blocks Phase 3.1)**
`FashionistaScorerImpl`, `DeterministicScorer`, `CalibrationCurve`, the five extractors, and the engines behind them (`ChromaticHarmonyEngine`, `GlcmTextureEngine`, `SilhouetteEngine`, `VisualMassEngine`, `ItaCalculator`) have no DI binding and no production callers. Decide before any calibration work:

* **Keep:** commit to wiring the real engines first (Phase 3.4, option A), then calibrate.
* **Kill (recommended):** delete the dead path and skip the `ChromaticHarmonyEngine` / τ items in Phase 3.1 entirely; all scoring work targets the live 3-evaluator `FashionistaEngine` + UI score.

No `ChromaticHarmonyEngine`/`CalibrationCurve` edits land until this decision is recorded.


**1. Reconcile Fashionista Scoring & Eliminate Double Evaluation**
Currently, `StyleSimulatorEngine` runs `FashionistaEngine` (40/35/25 split) for auditing, `GenerateStyleResultUseCase` immediately re-runs it (doubling blocking DB reads in `SilhouetteGrader`), and `GreedyRehydrator` computes a third score via `ColorHarmonyEngine.calculateFashionistaScore` (40/40/20 split).

* **Single Evaluation Pass:** Return the `FashionistaScore` and `IntentFulfillment` alongside the blueprint from `StyleSimulatorEngine.generateBlueprint()` (new result wrapper or extended `StyleResult` flow) so `GenerateStyleResultUseCase` reuses them instead of re-evaluating. Do not add eval fields to `StyleBlueprint` itself — it is the AI JSON contract; keep scoring results on the result channel.


* **Preload Silhouette Data:** Pass already-resolved domain models into `SilhouetteGrader` instead of executing blocking database queries on `Dispatchers.Default`.


* **Unify UI and Audit Math:** Remove `ColorHarmonyEngine.calculateFashionistaScore` from `GreedyRehydrator` and display the canonical `FashionistaEngine` score computed after final slot assignment.


* **Delete Broken Delta-E:** Delete the unused and dimensionally invalid `ColorHarmonyEngine.calculateDeltaE` (`:149`) that mixes hue degrees with $L^*/C^*$ units.



**2. Govern Side-Path Engines & Enforce Multimodal Privacy (T4, T5)**
`SuggestionEngine` and `AnalyzerEngine` call `Firebase.ai` directly without budgets, caching, retries, or audit logging, while `AnalyzerEngine` uploads up to 4 full-resolution bitmaps (including face selfies) to the cloud.

* **Route Through `AiProvider`:** Refactor `SuggestionEngine` and `AnalyzerEngine` to execute via `CapabilityRouter` and `AiProvider`, removing the unused `apiKey` parameter.


* **Downscale Bitmaps:** Resize all input bitmaps in `AnalyzerEngine` so the longest edge is at most 768px to 1024px before encoding, capping vision token spend.


* **Resolve Multimodal Policy:** Extract facial skin/hair telemetry on-device using `ItaCalculator` and `ColorQuantizer`, sending only text telemetry to cloud providers, or explicitly restrict `Multimodal` inputs to providers that support vision.



**3. Complete Validator Checklist Plumbing**
`GenerateStyleResultUseCase.kt:147` currently hardcodes all six creation-flow validation checks to `isValid = true` regardless of what `RecommendationValidator` returned.

* **Return the report:** Have `RecommendationValidator` return per-rule booleans (ID grounding, anchor retention, role firewall, rationale scrub) in a `ValidationReport` carried on the result channel next to the blueprint — not inside `StyleBlueprint`, which is the AI JSON contract.


* **Bind to UI State:** Map that report to the creation-flow checklist in `GenerateStyleResultUseCase.executeCreationResult()`, replacing the hardcoded all-`true` items.


**4. Finish `UNKNOWN` manifest defaults (assessment P0 #3, partly landed)**
`material` already emits `UNKNOWN`; `finish` (`SATIN`), `brand` (`KoColor`), and `depth` (`Balanced`) in `CompactManifestSerializer` still assert invented facts to the model.

* **Emit unknowns:** Replace the three remaining defaults with `UNKNOWN` (wardrobe + cosmetics paths) so the grounded manifest never states what the catalog does not know.


## Phase 2: P1 — Token Spend, Fit-Loop Latency & Provider Hardening

**1. Right-Size the On-Device Nano Tier (T1)**
`LocalNanoAiProvider` enforces a 768-token input limit, but `PromptAssembler`'s static scaffold (~600–750 tokens) plus minimum manifest lines overflows every request and wastes a fit cycle.

* **Nano Prompt Template:** Create `PromptAssembler.assembleCompactNano()` that strips verbose prose instructions, compresses telemetry to key-value pairs, and uses a minimal JSON schema (~180 tokens total).


* **Fast-Fail Gate:** If estimated tokens at `DetailLevel.MINIMAL` and `K = 4` still exceed 768 tokens, skip `LocalNanoAiProvider` immediately without running `adaptContextToProvider`.



**2. Remove Network I/O from the Fit Loop (T2, T10)**
`FirebaseAiProvider.countTokens` triggers network RPCs (up to 2 attempts across 3 models with 25s timeouts) on every iteration of `adaptContextToProvider` (`StyleSimulatorEngine.kt:298`). Furthermore, `slotsPerCategory` floors at 2, preventing `K` step-downs from shrinking the manifest.

* **Heuristic Step-Down:** Use a local character-based estimator (`prompt.length / 4`) inside the `adaptContextToProvider` loop, calling `provider.countTokens()` at most once on the final candidate prompt.


* **Honor the K Floor:** Allow `slotsPerCategory` to drop to `1` per required category when `K` steps down to `minCandidateAdditions = 4`, and hoist `generateSelectionState` outside the fit loop.



**3. Enforce `maxOutputTokens` & Record Billed Telemetry (T6)**
Output token limits are declared in provider capabilities but omitted from API requests, and server-reported `usageMetadata` is only written to `Log.d`.

* **Enforce Output Caps:** Pass `capability.maxOutputTokens` into `FirebaseAiClient` `generationConfig`, `ByokAiProvider` JSON payloads, and `GemmaRestFallbackProvider` requests.


* **Capture True Spend:** Extend `AiResponse` to return `TokenUsage(promptTokens, candidatesTokens, totalTokens)` from all providers and persist both input and output tokens in `StyleAuditLogger`.



**4. Add Validation Repair & Harden BYOK/Gemma/Cache (T3, T7, T8, T9)**
Validation failures currently burn up to 4 full prompts across failover providers.

* **Deterministic Repair Before Failover:** Note the validator already re-injects omitted locked anchors — do not rebuild that. Target the actually-unrepaired failures: substitute valid same-slot/same-role candidates for removed invalid IDs, backfill missing cosmetic roles deterministically, and only then spend one compact repair prompt before failing over to the next provider.


* **Provider Timeout & Temperature Parity:** Configure an 8s OkHttp timeout and pass `temperatureOverride` in `ByokAiProvider`; reduce `GemmaRestFallbackProvider` read timeout from 180s to 60s and forward `temperatureOverride`.


* **Stabilize Prompt Cache:** In `PromptCacheRepository`, join hashed fields with an explicit delimiter (`\u001F`). Do not alphabetically sort manifest lines (order carries the rank signal the model relies on) and do not round rotation scores (the manifest carries no scores, so rounding changes nothing — busts come from membership/order shifts driven by freshness timestamps). If hit rate disappoints, quantize recency upstream into day-buckets before scoring so identical days produce identical manifests.



## Phase 3: P2 — Fashion Math Discrimination, Calibration & NLP

**1. Widen Live Scoring Dynamic Range & Smooth Cliffs**

> Gate: the `ChromaticHarmonyEngine` and `CalibrationCurve` bullets below apply ONLY if the Phase 1 item-0 decision keeps the 6-feature pipeline; if killed, skip them. The baseline and single-chroma bullets target the live path either way.

`ChromaticHarmonyEngine.kt:50` restricts color harmony to `[0.55, 0.95]` with step-function cliffs at $\Delta E = 10.0$, while lenient evaluator baselines (`78±20`, `75±20`, `70±25`) cause almost every outfit to pass the 80-point gate.

* **Continuous Harmony Curve:** Remove the 0.5 hue floor in `ChromaticHarmonyEngine`, replace quantized $\Delta E_{00}$ steps (`{0.6, 0.75, 0.9}`) with a smooth Gaussian/piecewise-linear curve over `[0.0, 1.0]`, and remove dead `meanHue` (`:23`).


* **Fix Single-Chroma Scoring:** In `ColorHarmonyEngine.kt:184`, replace the automatic `38/40` score for `<= 1` chromatic item with a neutral-contrast evaluation based on $L^*$ separation.


* **Recalibrate Baselines & Logistic Curve:** Lower evaluator baselines in `FashionistaEngine` to `50` (full 0–100 spread) and adjust `CalibrationCurve.kt:8` from $\tau = 0.20$ to $\tau = 0.14$ so `EXCEPTIONAL_EDITORIAL` (`95+`) is reachable.



**2. Complete Intent Fulfillment & Numeric Appearance Matching**
`IntentFulfillmentEvaluator` hardcodes contrast, novelty, and formality to `0.5`, while `ContrastAnchorEvaluator` matches free-text strings like `contains("High")`.

* **Real Intent Metrics:** Compute outfit contrast from $\max(L^*) - \min(L^*)$, novelty from `1.0 - mean(rotationPenalty)`, and formality from mean `FormalityLevel.rank` in `IntentFulfillmentEvaluator`.


* **Typed Telemetry:** Replace string `contains()` checks in `ContrastAnchorEvaluator` and cosmetic temperature scorers with numeric `ColorTelemetry` and enum comparisons.



**3. Replace Substring Keyword Matching**
Raw `contains()` calls in `WardrobeCandidateFilter`, `HeuristicStyleEngine`, `SilhouetteGrader`, and `GreedyRehydrator` cause false positives like `"a"` matching every item or `"lip"` matching `"eclipse"`.

* **Shared Word-Boundary Matcher:** Build a shared core utility combining whole-word matching (as `IntentAnalyzer` does via split + exact match — it has no `\b` tokenizer to extract) with `\b` regex boundaries and stop-word stripping, and replace all 5 substring matchers (`WardrobeCandidateFilter`, `HeuristicStyleEngine`, `SilhouetteGrader`, `GreedyRehydrator`, cosmetic scorer).



**4. Resolve or Descope Engine Stubs**
Executes the Phase 1 item-0 decision; do not revisit the keep/kill question here.
Five classes in the 6-feature scorer are stubs or proxies (`GlcmTextureEngine`, `SilhouetteEngine`, `CosmeticIntegrationEngine`, `VisualHierarchyEngine`, `CompositionEngine`).

* **Wire Real CV Math or Prune:** If keeping the 6-feature pipeline, wire `VisualMassEngine.processBitmap` into `SilhouetteEngine`, wire `ItaCalculator` and `calculateMichelsonContrast` into `CosmeticIntegrationEngine`, and measure chroma/contrast isolation in `VisualHierarchyEngine` instead of `brand != null`.


* **Deprecate Unused Stubs:** If standardizing solely on the 3-evaluator `FashionistaEngine`, remove the dead 6-feature scorer classes and `GlcmTextureEngine` constant stub.



## Phase 4: P2 — Orchestration Gears, Taxonomy Alignment & Tests

**1. Fix Rehydrator Ordering, Deterministic Fallback & Dress Slots**
`GreedyRehydrator` can evict locked anchors and scores outfits before slot backfill, while `HeuristicStyleEngine` uses `.random()` and mishandles dresses.

* **Anchor Prepending:** In `GreedyRehydrator.kt:44`, prepend locked anchors *before* AI-resolved items prior to calling `distinctBy { it.category }`, and restrict slot backfill to eligible candidates.


* **Deterministic Heuristic Fallback:** Replace `.randomOrNull()` and random neutral padding in `HeuristicStyleEngine.kt:39` with deterministic top-ranked selection (`maxByOrNull { it.compositeScore }`) using actual item hex values.


* **First-Class `ONE_PIECE` / `DRESSES` Support:** Update `DeterministicContextEngine`, `HeuristicStyleEngine`, and `GreedyRehydrator` so a selected dress satisfies both `TOP` and `BOTTOM` slots without injecting redundant bottoms.



**2. Centralize Thresholds, Fix Audit Funnel & Normalize Provenance**
Temperature, rotation, and chroma thresholds are duplicated with conflicting cutoffs (`>17°C` vs `>=18°C` vs `>=26°C`), and audit funnel counts are overwritten.

* **Single Constants Surface:** Consolidate all thermal bands, outerwear cutoffs, rotation thresholds, and chroma gates into `KoColorTaxonomy`.


* **True Audit Funnel & Priority:** Record distinct sequential counts for `afterWeather`, `afterRotation`, and `finalEligible` in `DeterministicContextEngine.kt:58`, and log the actual anchor priority score in `StyleAuditLogger.kt:103`.


* **Normalize `totalScore` & Document Cosmetics Policy:** WARNING — do not rescale alone: anchors sort first only because 2.5/4.0 exceeds the 0–1 composite range (`sortedPartitionedPool`), and the audit display keys on `>2.0`. First add an explicit anchor-priority signal (separate `priority`/`isAnchor` field with anchors-first ranking + updated audit thresholds), *then* scale both wardrobe and cosmetic `totalScore` values to `[0.0, 1.0]` in `ColorModels.kt:27`. Document the pigment-only filter (`EYES`, `DIMENSION`, `LIPS`, `NAILS`).



**3. Dead Code Cleanup, Utility Consolidation & Test Suite**
Several engines and duplicate math helpers remain unreferenced or redundant.

* **Wire or Delete Dead Code:** Do not wire `DeterministicCatalogEngine` directly — it speaks `CatalogRecord`, the simulator speaks `ClothingItem`/`CosmeticItem`. Instead port its *idea* (slot-completeness preflight gate before AI spend) onto the simulator's models, or delete it. For `RoleGapAnalyzer`, adopt it as the single slot-diversity implementation (removing the simulator's inline equivalent) or delete it — never run both. Delete dead methods (`WardrobeCandidateFilter.getCandidates`, `DeterministicContextEngine.enforceRoleDiversity`, `FirebaseAiClient.getStyleAdvice`, unused injected fields in `DeterministicContextEngine`).


* **Consolidate Duplicates:** Merge duplicate `hexToLCh`, `calculateChroma`, chroma-weighted circular mean, and JSON brace-stripping helpers into `core/color` and `core/json`.


* **Expand Unit Tests:** Add test suites covering the live `FashionistaEngine` blend, `PromptAssembler` and `CompactManifestSerializer` detail levels, the `K`/detail step-down loop, `RecommendationValidator` sanitization, `GreedyRehydrator` anchor preservation, and `HeuristicStyleEngine` determinism.