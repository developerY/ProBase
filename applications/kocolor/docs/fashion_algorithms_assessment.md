# KoColor Fashion Math & Algorithms Assessment

Date: 2026-11-04. Method: static review of ~45 files across `applications/kocolor/data`,
`applications/kocolor/features/{analyzer,suggestions}`, `features/ai/*`, and supporting
`core/*` color utilities. Live-vs-dead status was determined from Hilt bindings plus
caller search, not from naming or comments. Nothing was executed; token figures are
by-inspection estimates. Paths below are relative to the repo root.

## TL;DR

- **Tokens: disciplined core, leaky edges.** The simulator pipeline has a real token
  budget loop (adaptive detail/K step-down, exact counting, prompt cache, deterministic
  fallback). But the on-device-first tier almost certainly never fits its 768-token
  budget, Firebase token-counting does network calls inside the fit loop, validation
  failures re-spend full prompts on every provider, two engines bypass all token
  governance, and billed output tokens are invisible in production telemetry.
- **Fashion math: correct foundations, weak discrimination, several stubs.** CIEDE2000,
  circular hue statistics, and sRGB→Lab→LCh are implemented correctly. But the live
  scoring path is dominated by lenient baselines (everything scores 55–98), two
  "engines" return hard constants, one "delta-E" is dimensionally wrong (dead, but
  present), and the top semantic tier (95+) is mathematically unreachable.
- **Gears: two pipelines, one alive.** The 6-feature logistic scorer is fully tested
  and fully unreachable (no DI binding). The live 3-evaluator path works but is
  scored twice per request, and the score the user sees comes from a *third*
  implementation at the UI layer. The creation-flow "validation checklist" is
  hardcoded to all-pass.

## 1. Live pipeline map

```
GenerateStyleResultUseCase  (Dispatchers.Default)
 └─ StyleSimulatorEngine.generateBlueprint()
     ├─ CapabilityRouterImpl: Nano → BYOK → Firebase → Gemma(proxy) → Heuristic fallback
     ├─ adaptContextToProvider()            token-budget step-down loop
     │    ├─ DeterministicContextEngine     anchors + ranked candidate pool
     │    ├─ WardrobeCandidateFilter        cosmetic candidates (K-capped, role-diverse)
     │    ├─ CompactManifestSerializer      pipe-delimited manifest, 3 detail levels
     │    └─ PromptAssembler                grounded prompt + JSON schema
     ├─ PromptCacheRepository               SHA-256 exact-match cache (LRU-50, memory-only)
     ├─ provider.execute()                  AI call
     ├─ RecommendationValidator             ID grounding, anchor/role firewall, rationale scrub
     ├─ FashionistaEngine                   color 40% + silhouette 35% + contrast 25% (audit only)
     └─ IntentFulfillmentEvaluator          colorfulness match (audit only)
UI: GreedyRehydrator  re-resolves IDs, re-injects anchors, assigns slots,
                      computes displayed koColorScore via ColorHarmonyEngine
Side paths (bypass everything above): SuggestionEngine, AnalyzerEngine
```

## 2. Tokens

### What is done well

- **Adaptive fit loop** (`StyleSimulatorEngine.kt:298`): EXPANDED→BALANCED→MINIMAL,
  then K−2, re-checked against `provider.countTokens() <= maxInputTokens`. Genuine
  per-provider budget discipline.
- **Compact manifest** (`CompactManifestSerializer.kt`): pipe-delimited single-line
  items instead of JSON; MINIMAL is ~4 fields. Cosmetics capped by the same K.
- **Exact-match cache** (`PromptCacheRepository.kt`): SHA-256 over tier, prompt
  version, retrieval-policy version, telemetry, weather, intent, and full manifest.
  Version pins prevent stale-format hits.
- **Deterministic fallback** (`HeuristicStyleEngine.kt`): zero-token indestructible
  baseline when all providers fail.
- **Low temperature default** (0.2) on cloud paths; `thinkingLevel MINIMAL` on the
  Gemma direct path; JSON MIME type on Firebase paths (fewer runaway outputs).

### Findings (ranked)

**T1. The on-device tier cannot fit its budget, so "local-first" is decorative.**
`LocalNanoAiProvider` allows 768 input tokens, but `PromptAssembler`'s fixed scaffold
(appearance, thermal, UV, circadian, intent, wardrobe-intelligence, color-relations,
goal, JSON schema ≈ 600–750 tokens by inspection) plus the minimum manifest floor
(~6 wardrobe + up-to-K cosmetic lines) exceeds it before any real content. Expect
`CONTEXT_OVERFLOW` on Nano for virtually every request, then cloud failover.
Consequence: the privacy story ("85/15 local-first") does not hold in practice, and
every request pays a wasted fit cycle. Fix options: a Nano-specific minimal prompt
scaffold, or route Nano only for short flows.

**T2. Token counting does network I/O inside the fit loop.** `FirebaseAiProvider`
delegates `countTokens` to `FirebaseAiClient.countTokens`, which calls the cloud
`countTokens` API wrapped in the full retry machine (up to 2 attempts × 3 models,
25 s timeout each). `adaptContextToProvider` calls it once per step-down iteration.
Worst case is many sequential billable-latency round-trips *before* the real call.
Prefer the len/4 heuristic during adaptation and one exact count at most — or cache
the count per fingerprint.

**T3. Validation failure burns a full prompt per provider with no repair.**
`executeAndCache` returns null on `VALIDATION_FAILED`, and the engine moves to the
next provider with a freshly fitted prompt — up to 4 full input spends for one user
request, plus per-provider retries. Consider one feedback-repair attempt (re-prompt
with the validator errors) before failing over, or fail faster to the heuristic.

**T4. Two engines bypass all token governance.** `SuggestionEngine` and
`AnalyzerEngine` call `Firebase.ai` directly: no budgets, no `countTokens`, no cache,
no audit, no retry policy, and the `apiKey` parameter they accept is ignored. Their
spend is invisible. Either route them through `AiProvider` or accept them as
explicitly ungoverned.

**T5. Up to 4 full-resolution bitmaps per analyzer call.** `AnalyzerEngine`
attaches face/hair/shoes/clothes bitmaps with no downscale; Gemini image tokens
scale with resolution. Downscale to a fixed bound (e.g. 768–1024 px) before sending.
Privacy note: this path sends face selfies to the cloud backend, bypassing the
`AiInput` sealed-type invariant that otherwise keeps images on-device
(`FirebaseAiProvider` loudly rejects `Multimodal`; the analyzer never goes through
it). BYOK/Gemma silently *drop* multimodal images — three different behaviors for
one input type.

**T6. Billed tokens are unobservable.** `usageMetadata` is captured in
`FirebaseAiClient` but only written to `Log.d` (stripped in release). The audit
trail's `tokens_used` is the *input estimate*; output tokens are never recorded, and
BYOK/Gemma capture no usage at all. `maxOutputTokens` is declared in every
capability but enforced nowhere (Firebase `generationConfig` omits it; BYOK sends no
`generationConfig`; Gemma direct sends none). Add server-reported usage to the audit
trail and set `maxOutputTokens` on all paths.

**T7. BYOK request-body bug.** `ByokAiProvider` hand-escapes JSON with two `replace`
calls (`"` and `\n`). A backslash, tab, or control character in a wardrobe name or
note produces malformed JSON → HTTP 400 → failover burn. Build the body with
`kotlinx.serialization` like `GemmaRestFallbackProvider` already does. BYOK also
ignores `temperatureOverride` and sets no OkHttp timeouts despite declaring 8 s.

**T8. Gemma proxy is the silent always-on last tier.** `isAvailable()` returns true
via the `"PROXY_ACTIVE_NO_LOCAL_KEY"` sentinel even with no user key, and the router
adds Gemma unconditionally — fine as a backstop, but every exhausted chain then hits
a hardcoded proxy URL with a 180 s read timeout (capability claims 60 s) and drops
`temperatureOverride` on the proxy path.

**T9. Cache limits.** LRU-50, process-lifetime only (no disk persistence), and the
fingerprint covers the full prompt string, so any manifest reorder or freshness
change busts the hit. Rotation/freshness scores shift daily, bounding the real hit
rate. Also: fingerprint fields are concatenated without delimiters before hashing —
cosmetically sloppy (ambiguous segmentation), cryptographically irrelevant.

**T10. Fit-loop floor ignores K.** `slotsPerCategory` has a minimum of 2 per
category, so the manifest cannot shrink below ~6–8 items + anchors + cosmetics no
matter how small K gets; `minCandidateAdditions = 4` is unreachable and the K
step-down is ineffective at the floor. Only `detailLevel` still saves tokens there.
Also, `generateSelectionState` re-runs every loop iteration (small-n, but wasteful).

## 3. Fashion math

### Verified correct

- **CIEDE2000** (`fashionista/math/Distance.kt:22`): matches the Sharma et al.
  reference — 25⁷ constant, G correction, T weighting, rotation term, and the
  achromatic `h̄'`/Δh′ branches. Correct.
- **Circular hue statistics** (`CircularStatistics.kt`): chroma-weighted mean with a
  C*≥10 neutral gate and standard `1−R` variance. Correct (neutral-only input falls
  back to hue 0.0/red — biased but documented behavior).
- **sRGB→XYZ→Lab→LCh** (`ColorSpace.kt`, `ColorHarmonyEngine.kt:22`): D65 matrices,
  0.04045/0.008856 pivots, cbrt branch. Correct in both copies (see duplication).
- **Michelson contrast and ITA** (`CosmeticIntegrationEngine.kt:16`,
  `ItaCalculator.kt:12`): textbook formulas — but neither has production callers.
- **Bitmap center-of-gravity** (`VisualMassEngine.kt:16`): sane 64×128 alpha×darkness
  mass model — but unreachable (see stubs).
- **ΔE76 family snapping** (`ColorQuantizer.kt:13`) and the r−b/g−b temperature
  heuristic (`:50`): crude but appropriate for coarse bucketing.
- **Rotation penalty** (`RotationScoringUseCase.kt:31`): cold-start gate, 35% share
  and 3-day/24-h recency rules combined by max. Coarse (outputs ≈ {0, 0.85, 1.0})
  but predictable and documented.
- **Intent keyword mapping** (`IntentAnalyzer.kt:19`): whole-word matching with
  clamped 0–1 outputs. Small vocabulary (~20 stems) but sound.

### Weak discrimination (live path)

- **Live color harmony range is ~[0.55, 0.95]** (`ChromaticHarmonyEngine.kt:50`):
  hue term floors at 0.5, ΔE term is quantized to {0.6, 0.75, 0.9}. Nearly every
  palette scores "good". The ΔE bands also have cliffs (9.99→0.75 vs 10.0→0.9).
  The computed `meanHue` (line 23) is never used — dead computation.
- **Evaluator baselines are lenient**: color 78±20, silhouette 75±20, contrast
  70±25. Combined with the 80-point approval gate, rejection is rare by construction.
- **Top semantic tier unreachable**: logistic calibration μ=0.5, τ=0.2 maps q∈[0,1]
  to ≈[7.6, 92.4], so `EXCEPTIONAL_EDITORIAL` (95+) in `CalibrationCurve.kt:8` can
  never fire. Either recenter μ/τ or relabel the tiers.
- **Palette geometry gives near-max for trivial input**
  (`ColorHarmonyEngine.kt:184`): ≤1 chromatic color returns 38/40.
- **Intent fulfillment observes only colorfulness** (`IntentFulfillmentEvaluator.kt`):
  contrast/novelty/formality are hardcoded 0.5, so any requested novelty >0.6 is
  *always* reported unmet, and the 0–100 score is colorfulness-only.
- **String-typed appearance matching** (`ContrastAnchorEvaluator.kt`,
  cosmetic temperature scoring): `contains("High")` / `contains("Light")` on
  free-text profile fields. Works while upstream strings are stable; one rename
  silently shifts scores. Prefer the numeric `ColorTelemetry`.

### Stubs and placeholders (named like real engines)

| File | Claim | Reality |
|---|---|---|
| `texture/GlcmTextureEngine.kt:21` | GLCM order/entropy | Ignores input; returns constants 0.75/0.40 → fixed 0.69 |
| `silhouette/SilhouetteEngine.kt:19` | Garment proportions | Ignores input; fixed 0.70/0.80. Injected `VisualMassEngine` never called |
| `integration/CosmeticIntegrationEngine.kt:25` | Face/outfit integration | Average cosmetic lightness ÷ 100. Lighter makeup = higher "integration" |
| `hierarchy/VisualHierarchyEngine.kt:17` | Focal-point isolation | Counts items with `brand != null` as "statement pieces" |
| `composition/CompositionEngine.kt:12` | Composition | Category-completeness counting (fine as coverage proxy; misnamed) |
| `data/color/ColorHarmonyEngine.kt:149` | "Simplified Delta E" | `√(dL²+dC²+dH²)` mixes hue *degrees* with L/C units, no circular wrap. No callers — delete it |

Net effect on the 6-feature Q pipeline: of six features, one is real math (color),
one is counting (composition), two are constants (silhouette, texture), one is a
brand proxy (hierarchy), and one is usually zero-availability (integration). That
pipeline is dead in production anyway (Section 5) — but its tests pass, which can
mislead.

### Fabricated grounding facts

`CompactManifestSerializer` substitutes defaults for unknown fields: material
`"Cotton"`, finish `"SATIN"`, brand `"KoColor"`, depth `"Balanced"`. The manifest is
sold to the model as strictly grounded; these defaults assert facts that may be
false (a silk dress presented as cotton changes thermal/material reasoning).
Emit `"UNKNOWN"` instead.

### Keyword-NLP hazards (substring, no stop-words)

Three scorers split intent on whitespace and use `contains()`:
`DeterministicContextEngine` is safe (matches long category names), but
`WardrobeCandidateFilter.calculateScore` (+2.0) and `calculateCosmeticScore` (+0.75)
fire on any word — intent "a red dress" matches nearly every item via "a".
`HeuristicStyleEngine.smartPick` has the same flaw, then picks *randomly* among
matches. English-only name/material keyword systems also drive `SilhouetteGrader`,
weather gating, and `GreedyRehydrator` fallbacks (`"lip"` matches "eclipse";
`"top"` matches "stop"). Use word boundaries + stop-word filtering, or reuse
`IntentAnalyzer`'s whole-word approach.

## 4. Orchestration gears

- **Double scoring per request.** `StyleSimulatorEngine` evaluates Fashionista +
  intent for the audit trail; `GenerateStyleResultUseCase` immediately re-evaluates
  both for the result. That doubles the `SilhouetteGrader` blocking DB read (both
  run on `Dispatchers.Default`, so this is pool-blocking, not an ANR).
- **The user-visible score differs from the audited score.** The audit logs
  `FashionistaEngine.totalScore` (40/35/25 blend); the UI's `koColorScore` comes
  from `GreedyRehydrator` → `ColorHarmonyEngine.calculateFashionistaScore`
  (40/40/20 split). Same blueprint, two numbers, no documented relationship.
- **Creation-flow validation UI is theater** (`GenerateStyleResultUseCase.kt:147`):
  all six checklist items (`isValid = true`, "All selected IDs grounded", …) are
  hardcoded regardless of `RecommendationValidator`'s actual verdict. Either plumb
  the real result through or remove the checklist.
- **Rehydrator anchor fail-safe can silently fail** (`GreedyRehydrator.kt:44`):
  anchors are appended *after* resolved items and `distinctBy(category)` keeps the
  *first* — a present-but-different item evicts the locked anchor it was meant to
  protect. Prepend anchors or dedupe in their favor.
- **Rehydrator score ≠ displayed outfit.** `koColorScore` is computed before slot
  assignment, but slots then backfill from the whole inventory (any TOP/SHOES,
  ignoring hidden/rotation/thermal/validation state). The score does not describe
  what the user sees.
- **Fallback is random, not deterministic** (`HeuristicStyleEngine.kt:39`):
  `smartPick` returns `matches.randomOrNull() ?: this.random()`, and the palette is
  padded with random neutrals — phantom colors that downstream harmony scoring
  treats as real outfit colors.
- **Dresses are handled inconsistently.** `OutfitSlot` maps DRESSES→TOP, but the
  simulator's slot universe is TOP/BOTTOM/SHOES only (no DRESSES/ONE_PIECE), the
  fallback never picks dresses yet still adds BOTTOMS beside a dress anchor, and the
  rehydrator maps dress→both slots. One-piece wardrobes cannot complete the
  diversity guarantee.
- **Threshold sprawl.** Outerwear ban: >17 °C (simulator/prompt) vs ≥18 °C (context
  engine) vs HOT≥26 °C (taxonomy/catalog) vs `"Temp: 2x"` string parse (dead
  filter). Thermal bands are defined three different ways
  (`KoColorTaxonomy.ThermalBand`, `DeterministicContextEngine`, `PromptAssembler`).
  Rotation: 0.7 hard / 0.5 soft / 3-day / 7-day. Chroma gates at 10/15/20/25/30.
  Centralize into one constants surface.
- **Audit funnel is fake granularity** (`DeterministicContextEngine.kt:58`):
  after-weather, after-rotation, and final-eligible are all assigned the same value;
  the "Priority 4.00 / 4.00" line in `StyleAuditLogger.kt:103` is a literal.
- **Dual-scale `totalScore`** (`ColorModels.kt:27`): wardrobe provenance uses
  `compositeScore` (0–1, anchors 2.5/4.0); cosmetic provenance leaves it 0 and falls
  back to the sub-score sum (~1–5). Consistent within each pool, incomparable
  across them — the audit paper over this with ad-hoc normalization.
- **`FashionistaScore` serves two pipelines badly**: `isApproved` derives from
  `totalScore`, so it is always false on the 6-feature path; `breakdown` defaults
  to fake all-0.85 vectors on the 3-evaluator path; `standardVersion` default
  "v1.1" disagrees with calibration v1.
- **Cosmetic universe silently narrows.** `MacroCategory` has 13 values, but
  retrieval, roles, prompts, and validation admit only EYES/DIMENSION/LIPS/NAILS
  (+PREP to forbid). COMPLEXION/HAIR/etc. map to null and vanish. The rehydrator's
  COMPLEXION-cheeks fallback is therefore dead. If intentional, document it as the
  pigment-only policy; the validator firewall and the catalog-engine allow-list
  agree today, which is good.

## 5. Dead and duplicated code

Confirmed by caller search / DI bindings (production code; tests excluded):

| Item | Status |
|---|---|
| 6-feature scorer (`FashionistaScorerImpl`, `DeterministicScorer`, `CalibrationCurve`, 5 extractors) | Dead — `FashionistaModule` binds only `FashionistaEvaluator`; tested but unreachable |
| `DeterministicCatalogEngine` (provenance preflight) | Dead — zero callers. Good design; wire it up or delete it |
| `VisualMassEngine.processBitmap`, `ItaCalculator`, `calculateMichelsonContrast` | Dead — the only real CV math in the repo |
| `ColorHarmonyEngine.calculateDeltaE` | Dead — and wrong; delete |
| `WardrobeCandidateFilter.getCandidates`, `getCosmeticCandidates` | Dead (tests-only / none) |
| `DeterministicContextEngine.enforceRoleDiversity` | Dead — partitioning reimplemented inline in the simulator |
| `RoleGapAnalyzer` output (`missingRoleRequirements`) | Computed, consumed only by tests |
| `FirebaseAiClient` StyleTelemetry path (`getStyleAdvice`/`estimateTokens`) | Dead — simulator uses `PromptAssembler` + `generateContent` |
| `rotationScoringUseCase` + `repository` in `DeterministicContextEngine` | Injected, never called (uses `context.rotationScores`) |
| `ChromaticHarmonyEngine.meanHue` | Computed, never used |

Duplication to consolidate: `hexToLCh` ×2 (Double vs Float), chroma-weighted
circular mean ×2 (util vs inline), `calculateChroma` ×3, thermal banding ×3,
"fashionista score" ×3 (one dead, two live-but-different), JSON brace-stripping ×4,
keyword matchers in 5+ files.

## 6. Test coverage (as found)

- `FashionistaScorerTest`: invariants, zero-availability, determinism — but covers
  the *dead* scorer. The live `FashionistaEngine` blend has no dedicated test.
- `StyleSimulatorEngineTest` / `StyleSimulatorIntegrationTest`: provider success,
  fallback-to-deterministic, nested-JSON decode. No token-budget, validation-fail
  failover, or cache-hit tests.
- `SuggestionEngineTest`, `WardrobeCandidateFilterTest`,
  `SelectionCascadeTest`, `GreedyRehydratorTest`: narrow happy-path coverage.
- `FirebaseAiClientTest` (59 lines): thin for the retry/fallback/token core.
- Nothing tests prompt assembly, the K/detail step-down, serializer detail levels,
  validator sanitization, rehydrator slot logic, or the heuristic fallback.

## 7. Recommendations

**P0 (correctness / money / privacy)**
1. Reconcile the three fashionista scores into one documented number, or explicitly
   designate audit vs display scores. Delete `calculateDeltaE`.
2. Route `SuggestionEngine`/`AnalyzerEngine` through `AiProvider` (budgets, cache,
   audit) or formally exempt them; downscale analyzer bitmaps; resolve the
   face-photo-to-cloud vs text-only-cloud contradiction as a product decision.
3. Replace `UNKNOWN` manifest defaults (Cotton/SATIN/KoColor/Balanced) — the model
   is reasoning over invented facts today.
4. Fix BYOK JSON body construction (use `kotlinx.serialization`).
5. Plumb the real validator verdict into the creation-flow checklist (or remove it).

**P1 (token spend)**
6. Make Nano viable (minimal scaffold) or stop routing to it; it currently only
   adds a doomed fit cycle.
7. Heuristic-first token counting for Firebase during adaptation; enforce
   `maxOutputTokens` on all providers; record billed usage in the audit trail.
8. Add validator-feedback repair before provider failover; honor the K floor in the
   step-down (or remove the minimum-2-per-category).

**P2 (quality / hygiene)**
9. Widen live-score dynamic range (recenter the logistic so 95+ is reachable, or
   relabel tiers); smooth the ΔE cliffs; implement or descoped-label the GLCM /
   silhouette / texture / hierarchy stubs (names promise what code does not do).
10. Centralize thermal/rotation/chroma thresholds; fix rehydrator anchor ordering
    and score-before-slots; make the heuristic fallback deterministic and
    dress-aware; word-boundary + stop-word intent matching.
11. Delete or wire up the dead inventory in Section 5; consolidate the duplicated
    color-math utils; add tests for the live blend, validator, step-down, and cache.
