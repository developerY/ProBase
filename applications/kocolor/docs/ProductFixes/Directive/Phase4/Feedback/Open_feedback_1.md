My assessment: Phase 4 is well structured and is close to execution-ready. I like the six micro-checkpoints, the read-before-write requirement, and the explicit protections against undoing the previous phases.

I would not hand it to the coding agent quite yet, however. There are five important corrections I would make, particularly around score normalization, threshold completeness, preflight eligibility, and shared utility design.

The overall scope is consistent with `RoadMapToPro.md`. Phase 4 is intended to finish deterministic orchestration, dress handling, threshold consolidation, provenance normalization, dead-code cleanup, utility consolidation, and test coverage. Your specification addresses all of those areas.&#x20;

RoadMapToPro.md



## 1. Critical: `totalScore` normalization must not just clamp

This is the biggest mathematical risk in Step 3.

You correctly identify the original problem:

- Wardrobe scores use a composite score around 0–1.
- Anchor priority currently uses values such as 2.5 and 4.0.
- Cosmetic provenance can fall back to sub-score sums around 1–5.

You correctly want to separate anchor priority before normalizing scores.

However, this instruction is dangerous:

```
totalScore.coerceIn(0.0f, 1.0f)
```

Suppose cosmetic scores are:

```
1.5
2.2
3.1
4.0
4.8
```

Clamping produces:

```
1.0
1.0
1.0
1.0
1.0
```

You have technically normalized the values into the permitted range, but destroyed their ranking and discrimination.

Required correction:

Normalize the underlying scores using their actual mathematical ranges, then clamp floating-point rounding:

```
val normalizedScore = normalizedRawScore.coerceIn(0.0f, 1.0f)
```

The agent must establish the real score ranges for both wardrobe and cosmetics before implementing the conversion.

Add a test that verifies:

- All scores are between 0 and 1.
- Higher-quality candidates retain higher scores.
- Different source scores do not all collapse to 1.0.
- Anchors still sort ahead of ordinary candidates.

I would explicitly prohibit using `coerceIn()` as the normalization algorithm.

## 2. Threshold centralization is missing five existing thresholds

Step 2 is directionally correct, but its proposed constants do not fully cover the roadmap.

The original roadmap explicitly identifies:

- Rotation: `0.7`, `0.5`, 3-day and 7-day thresholds.
- Chroma: `10`, `15`, `20`, `25`, `30`.
- Temperature: several inconsistent thermal and outerwear cutoffs.&#x20;

  RoadMapToPro.md



Your proposed `KoColorThresholds` includes only 10, 20 and 30 for chroma, and only a 3-day rotation threshold.

I would add the missing values:

| Category     | Constants to account for |
| ------------ | ------------------------ |
| Rotation     | 0.7 hard, 0.5 soft       |
| Wear history | 24 hours, 3 days, 7 days |
| Chroma       | 10, 15, 20, 25, 30       |
| Temperature  | 7, 14, 20, 26 °C         |
| Outerwear    | 18 °C                    |

One important additional instruction:

Do not invent new thresholds while centralizing existing ones.

Have the agent inventory every existing occurrence, document its current behavior, identify discrepancies, and then establish the canonical value and inclusive/exclusive boundary.

For example:

```
tempC >= OUTERWEAR_BAN_TEMP_C
```

must behave consistently wherever the outerwear policy applies.

But do not inadvertently remove rain, wind, or other legitimate outerwear exceptions simply because the temperature threshold has been unified.

Also preserve the Phase 3 `TEMP_REGEX` behavior and its tested 26°C and 7°C boundaries.

## 3. Dress support and preflight need to share the same definition of outfit completeness

I like Step 1 and Step 4, but they need a stronger architectural rule.

The same definition of outfit completeness should be used by:

- `HeuristicStyleEngine`
- `DeterministicContextEngine`
- `RoleGapAnalyzer`
- `StyleSimulatorEngine` preflight

Otherwise, you could have a situation where `DeterministicContextEngine` believes an outfit is incomplete, `HeuristicStyleEngine` believes it is complete, and `StyleSimulatorEngine` still sends an unnecessary AI request.

I would explicitly define the required clothing roles:

```
Complete Outfit:

SHOES AND (
    DRESSES / ONE_PIECE
    OR
    (TOPS AND BOTTOMS)
)
```

A dress satisfies both upper and lower body coverage.

There are two additional details I would include.

Locked anchor precedence

A locked dress must satisfy the missing TOPS and BOTTOMS roles without automatically injecting additional clothing.

However, if the user explicitly locks both a dress and a separate bottom, do not silently discard either lock. Preserve the explicit anchors and handle any incompatibility through the existing validation/reporting mechanism.

This is important because Phase 1 established anchor preservation as an architectural invariant.&#x20;

RoadMapToPro.md



Preflight must use the actual eligible pool.

Do not count hidden, unavailable, forbidden, or otherwise ineligible items merely because they exist in Room.

The preflight must use the same eligibility rules as the eventual selection process. A failed preflight must not invoke cloud providers.

I would add two tests:

- Complete dress-based wardrobe: cloud preflight passes.
- Incomplete wardrobe: cloud provider invocation count remains exactly zero.

This protects the token budget as well as outfit correctness.

## 4. JSON and color utility consolidation needs a stronger implementation contract

Step 5 is a good idea and directly follows the roadmap's requirement to consolidate duplicate color math and JSON helpers in shared modules.&#x20;

RoadMapToPro.md



However, I would change the proposed implementation.

### JSON: Do not rely on first-brace/last-brace extraction

This approach:

```
val start = raw.indexOf('{')
val end = raw.lastIndexOf('}')
```

can produce incorrect results when:

- JSON contains braces inside string values.
- The response contains multiple JSON objects.
- The model includes explanatory text around the JSON.
- The response is malformed or truncated.
- Escaped quotes occur inside JSON strings.

I would require a balanced-brace scanner that understands quoted strings and escaping, followed by actual JSON parsing and validation.

Also, prefer the existing `core/json` module if it is accessible to all consumers. Do not introduce a shared utility into the application data module if doing so creates an undesirable dependency direction.

Add `JsonSanitizerTest` covering fenced JSON, surrounding prose, nested objects, escaped quotes, braces inside strings, and malformed input.

### Color: Preserve mathematical equivalence

The objective should not simply be to remove duplicate functions.

Make sure the canonical color conversion uses consistent:

- sRGB gamma handling.
- D65 reference white.
- CIELAB conversion.
- LCh hue convention.
- Chroma calculation.
- Circular hue averaging.

I would specifically add a test for circular averaging around the hue boundary:

```
359° + 1° should produce approximately 0°,
not 180°.
```

Add golden-value tests for representative hex colors and verify that the consolidated conversions do not unexpectedly change existing color-scoring results.

This is particularly important because KoColor relies on CIELAB/LCh math throughout its wardrobe and cosmetics pipeline.

## 5. Phase 4 test coverage is not quite comprehensive enough

The four proposed test suites are good, but they don't yet cover everything the roadmap assigned to Phase 4.

The roadmap specifically calls out tests for the live scoring pipeline, prompt/detail-level behavior, the K/detail step-down loop, recommendation validation, rehydrator anchor preservation, and deterministic heuristic selection.&#x20;

RoadMapToPro.md



I would expand your Step 6 as follows:

| Test suite                       | Additional coverage                                                                               |
| -------------------------------- | ------------------------------------------------------------------------------------------------- |
| `HeuristicStyleEngineTest`       | Repeated deterministic results, dress anchor, no redundant bottoms, stable tie-breaking           |
| `DeterministicContextEngineTest` | True filtering funnel, threshold boundaries, normalized score ranking, anchor priority            |
| `CompactManifestSerializerTest`  | All detail levels, unknown defaults, no fabricated manifest facts                                 |
| `RecommendationValidatorTest`    | ID grounding, role firewall, locked-anchor preservation, rationale sanitization, ValidationReport |
| `RoleGapAnalyzerTest`            | Correct missing-role identification and prioritization                                            |
| `StyleSimulatorEngineTest`       | Preflight prevents unnecessary provider calls; K/detail step-down remains intact                  |
| `JsonSanitizerTest`              | Fences, escaped strings, nested objects, malformed JSON                                           |
| `ColorSpaceConverterTest`        | Golden color values, chroma, circular hue, conversion consistency                                 |
| `StyleAuditLoggerTest`           | Real anchor priority rather than hardcoded priority                                               |

I would also add:

Compact manifest regression tests

The instruction to test `UNKNOWN` values across `EXPANDED`, `BALANCED`, and `MINIMAL` is good. Just make sure assertions account for fields intentionally omitted at lower detail levels rather than requiring every detail level to emit every field.

RecommendationValidator report semantics

Define whether each `ValidationReport` boolean means:

1. The original AI response satisfied the rule; or
2. The final sanitized and repaired result satisfies the rule.

I recommend reporting final validation status, with separate diagnostics for violations detected and repairs performed. That avoids telling the UI that validation failed merely because the validator successfully repaired an invalid response.

GreedyRehydrator

I agree with your instruction not to modify `GreedyRehydrator.kt`. But its existing anchor-preservation behavior should still have a regression test. Not touching a component and not testing it are two different things.

## 6. A few improvements to the verification gate

Your three verification categories are well organized. I would make these changes:

A3 — Dead code

Before deleting methods, search for references across the entire repository, including tests, DI bindings and other modules.

For example, finding zero callers in `FirebaseAiClient.kt` does not prove that another module doesn't call `getStyleAdvice()`.

A4 — Invented defaults

The grep command can flag legitimate occurrences in comments or mappings.

Use the serializer tests as the definitive behavioral check, and inspect the remaining grep results rather than treating every textual occurrence as a failure.

B3 — Normalization

The grep command proves that the new fields exist, not that the values are mathematically normalized.

The unit tests must establish the actual `[0,1]` range and preserve ranking discrimination.

C1 — Previous phase invariants

I would explicitly inspect the actual multimodal guards in both providers, rather than merely checking that `UnsupportedOperationException` appears somewhere in the directories.

Also add verification that the Phase 3 `KeywordMatcher` and `TEMP_REGEX` behavior still passes its regression tests.

C2 — Full build

Keep your existing full Gradle command. It is the correct final integration gate.

## 7. One additional architectural protection: Do not let Phase 4 become a refactoring project

This is worth adding to your anti-shortcut rules.

Phase 4 contains several potentially large changes:

- Reworking provenance models.
- Changing candidate sorting.
- Introducing outfit preflight.
- Consolidating shared utilities.
- Deleting unused methods.
- Altering dependency injection.

The coding agent must not take this opportunity to redesign working Phase 1–3 architecture.

Add this rule:

> SCOPE CONTROL: Implement only the Phase 4 requirements. Do not redesign the Fashionista scoring pipeline, AI provider router, manifest contract, Room schema, or navigation architecture. Preserve existing public APIs unless a documented Phase 4 change requires modification. Before deleting or moving any shared utility, verify all production callers and module dependency direction. Each micro-checkpoint must leave the project in a compilable, testable state.

## Final assessment

Architecture approved

Needs five corrections

| Phase 4 component                        | Assessment                                       |
| ---------------------------------------- | ------------------------------------------------ |
| P4.1 Deterministic selection and dresses | Very good                                        |
| P4.2 Threshold consolidation             | Missing some existing thresholds                 |
| P4.2 Provenance normalization            | Needs proper mathematical normalization          |
| P4.2 Audit funnel                        | Good                                             |
| P4.2 Anchor priority                     | Good, needs regression tests                     |
| P4.3 Preflight and RoleGapAnalyzer       | Very important; strengthen shared slot semantics |
| P4.3 Dead-code removal                   | Good, verify all repository references           |
| P4.3 Shared color/JSON utilities         | Good, improve JSON extraction and golden tests   |
| Final tests                              | Needs broader coverage                           |

My recommendation is to retain your six-step execution sequence. It is sensible to establish deterministic behavior first, centralize thresholds, separate anchor priority, normalize scores, then clean up architecture and shared utilities before running the final integration suite.

One final distinction: Phase 4 should not simply make the code cleaner. It should make the system more predictable and measurable.

At the end, KoColor should have:

1. Deterministic outfit selection.
2. Correct dress and one-piece outfit semantics.
3. One authoritative set of fashion thresholds.
4. Consistent, comparable wardrobe and cosmetic scores.
5. Explicit anchor priority independent of scoring.
6. No unnecessary AI calls when a complete outfit is impossible.
7. One canonical implementation of shared color and JSON mathematics.
8. Regression tests protecting everything from Phases 1–3.

I would make these amendments and then authorize Phase 4. This is a good final remediation phase, and the roadmap has a coherent stopping point after it.