My assessment: this is a strong Phase 3 specification, and I agree with the overall direction. I would make a few important corrections before handing it to the coding agent. The biggest risks are not the fashion mathematics themselves; they are a couple of subtle implementation problems in the proposed `KeywordMatcher`, the intent-fulfillment calculation, and the calibration instructions.

The scope is substantially consistent with `RoadMapToPro.md`. In particular, killing the unused six-feature scorer and focusing on the live three-evaluator `FashionistaEngine` is consistent with the roadmap's recommended decision. Excluding `GreedyRehydrator` from the new keyword matcher is also correct because Phase 1 moves its keyword responsibilities out of that class.&#x20;


## 1. Critical: Fix the proposed KeywordMatcher before implementation

This is the one part I would definitely change.

The proposed tokenizer preserves both the original plural and its normalized stem:

```
.flatMap { word ->
    val stem = normalizeStem(word)
    if (stem != word) sequenceOf(word, stem) else sequenceOf(word)
}
```

That creates two problems.

First, it can count one concept twice.

For example:

```
Query:  boots
Tokens: boots, boot

Target: Ankle Boot
Tokens: ankle, boot
```

That happens to match once with `countMatchingTokens`, but:

```
Query:  boots
Target: Ankle Boots
```

can count both `boots` and `boot`. One keyword has now produced two scoring matches.

Second, matching is asymmetric.

`containsWholeWord("Ankle Boot", "boots")` can fail because it requires both `boots` and `boot` to be present in the target tokens.

My recommendation: normalize each word into one canonical token, rather than returning both the original and stem.

Conceptually:

```
tokenize("black boots") 
// setOf("black", "boot")

tokenize("Ankle Boot")
// setOf("ankle", "boot")
```

This gives you consistent singular/plural matching and prevents duplicate scoring.

I would also clarify what `containsWholeWord()` means for multiword keywords. The current set-based implementation checks whether all tokens exist, not whether a phrase exists contiguously. Those are different operations.

For example, a query for `"red dress"` could match `"red blouse with a dress"` under set containment. That might be acceptable for intent matching, but it is not strictly whole-phrase matching.

My recommendation: Make `containsWholeWord()` require a contiguous token sequence for multiword phrases, while `countMatchingTokens()` remains an unordered token-overlap operation.

## 2. Critical: Intent fulfillment should only score active dimensions

There is an inconsistency in P3.2.

The specification says:

> Compute the fulfillment score across all active intent dimensions.

But the supplied implementation always calculates:

```
val diffs = listOf(
    abs(intentProfile.targetColorfulness - observedColorfulness),
    abs(intentProfile.targetContrast - observedContrast),
    abs(intentProfile.targetNovelty - observedNovelty),
    abs(intentProfile.targetFormality - observedFormality)
)
```

That always scores all four dimensions, even when the user has only expressed an intent concerning one or two of them.

Imagine someone asks for:

> Something colorful and unusual.

Colorfulness and novelty are active. Contrast and formality might not be.

If the profile defaults those other targets to `0.5`, the evaluator can penalize the outfit for attributes the user never requested.

I would require the agent to inspect `IntentProfile` and establish how active dimensions are represented.

The resulting rule should be:

- Score dimensions the user actually requested.
- Do not penalize unrequested dimensions.
- Apply a documented weighting policy if dimensions have different importance.
- Define what happens when no dimensions are active.
- Do not use a fabricated observation to make an unavailable dimension look satisfied.

There is another small issue here: returning `observedFormality = 0.5f` when no clothing items exist makes an unobservable property look neutral. An empty outfit should be handled by the empty-outfit guard, not receive a potentially perfect formality match.

## 3. Resolve the Formality model discrepancy

There is one disagreement between the Phase 3 prompt and the roadmap.

The roadmap specifies:

`FormalityLevel.rank`

The new prompt specifies:

`ClothingItem.formality` using the core-model `Formality` enum and explicitly warns against confusing it with taxonomy `FormalityLevel`.&#x20;




The new instruction may be correct based on the current domain model, but the coding agent should verify the actual `ClothingItem` definition before implementing it.

I would make this explicit:

Use the canonical formality representation actually stored on the resolved `ClothingItem`. Do not substitute a similarly named taxonomy enum. Map its semantic levels explicitly to `[0.0, 1.0]`.

I would also avoid relying on `ordinal` unless the enum explicitly guarantees that its declaration order is its permanent semantic ranking. An exhaustive mapper is safer if the enum might be reorganized.

## 4. Be more precise about evaluator calibration

The mathematical warning in P3.1 is excellent.

The roadmap correctly identifies that lowering the baselines to 50 without expanding the scoring ranges can make the 80-point approval threshold mathematically unreachable.&#x20;



However, I would not let the coding agent implement `avg * 25` or `±50` without first establishing the actual ranges and meaning of the existing modifiers.

For each evaluator, require:

1. Identify the range of every input measurement.
2. Normalize the underlying measurement into a documented range.
3. Map that normalized value to a score in `[0, 100]`.
4. Establish what poor, average, good, and exceptional inputs should produce.
5. Verify the resulting blended score using the real 40/35/25 weights.

Otherwise, the agent could simply enlarge multipliers until the tests pass, without making the scoring more mathematically meaningful.

I would also insist on individual evaluator tests, not just aggregate `FashionistaEngine` tests. Otherwise, one evaluator could compensate for another evaluator's bad scoring.

## 5. Make the single-chroma curve deterministic

I like replacing the `38/40` shortcut with lightness separation. This is a meaningful correction: an outfit of three identical grey items should not receive a near-perfect harmony score.

But the specification currently describes ranges rather than a fully defined function:

- Below 12: 10–16.
- Between 30 and 65: peak near 36.
- Above 75: 26–30.

That leaves the agent to choose the actual curve.

I would require explicit interpolation control points and tests for:

| L\* span | Expected behavior                  |
| -------- | ---------------------------------- |
| 0        | Flat monotony; low score           |
| 12       | Still relatively flat              |
| 30       | Intentional tonal separation       |
| 45–55    | Near-optimal tonal layering        |
| 65       | Good tonal separation              |
| 75       | Stark contrast                     |
| Above 85 | Extreme separation; moderate score |

The exact control-point scores can be selected after inspecting the existing color model, but they should be documented and fixed rather than left to interpretation.

Also, test both ends of the curve and its middle. The current grey-versus-black-and-white test only proves relative improvement, not that the desired curve actually exists.

## 6. Strengthen the verification gate

There are three omissions I would correct.

A. `DeterministicContextEngine.kt` is missing from the substring audit.

P3.2 explicitly requires typed appearance matching in `DeterministicContextEngine`, but the verification command does not inspect that file.

Add:

```
applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt
```

The roadmap explicitly identifies this engine as part of the typed appearance matching work.&#x20;

B. The dead-code list is incomplete.

P3.4 should include `VisualMassEngine`, which is mentioned in the original deleted-engine list but omitted from the verification list. I would also verify the status of `FashionistaScorerImpl`, `DeterministicScorer`, and the associated extractors identified in the Phase 1 gating decision.&#x20;

C. Some grep checks are too broad.

For example:

```
grep -n "\.contains("
```

can flag legitimate collection membership checks. The requirement is to eliminate raw string-substring matching, not every use of `contains()`.

Similarly, searching the entire intent evaluator for `0.5f` could flag a legitimate neutral default or fallback. Target the specific hardcoded contrast and novelty assignments instead.

I would make the verification gate distinguish between:

- Expected zero matches.
- Matches that are permitted but must be reviewed.
- Exactly one invocation of `provider.countTokens()`.
- A preserved Phase 2 implementation invariant.

That gives the audit much more credibility.

## 7. Add integration tests for the four keyword consumers

`KeywordMatcherTest` is necessary, but it doesn't prove that all four consumers use the matcher correctly.

I would add targeted tests for:

| Consumer                  | Required behavioral test                                                               |
| ------------------------- | -------------------------------------------------------------------------------------- |
| `WardrobeCandidateFilter` | Stop-words do not increase wardrobe scores                                             |
| Cosmetic scorer           | `"lip"` does not match `"Eclipse Eyeshadow"`                                           |
| `HeuristicStyleEngine`    | `"top"` does not match `"Stop Sign Red Tee"`                                           |
| `SilhouetteGrader`        | `"slim"` does not match a description containing only `"slim"` as part of another word |

The exact last test can be adjusted to actual garment descriptions. The point is to verify the actual integration, not just the utility.

## My final assessment

Approve the architecture and scope

Revise before execution

| Area                        | Assessment                                                 |
| --------------------------- | ---------------------------------------------------------- |
| P3.1 scoring discrimination | Strong direction; define calibration more precisely        |
| P3.2 intent fulfillment     | Important correction; active dimensions need clarification |
| P3.3 keyword matching       | Good architecture; fix canonical stemming first            |
| P3.4 dead-code cleanup      | Correct direction; expand the verification list            |
| P3.5 testing                | Good coverage foundation; add consumer integration tests   |
| Phase 1/2 protections       | Excellent and worth retaining                              |

One final point: I would preserve the instruction that the coding agent must inspect the existing implementation before changing signatures or inventing new abstractions. The prompt contains a lot of implementation detail, but the repository's actual types, enum definitions, score ranges, and call sites must remain authoritative.

The roadmap already establishes a sensible progression: Phase 1 resolves the architecture, Phase 2 governs the AI pipeline, Phase 3 improves the mathematical and linguistic quality of recommendations, and Phase 4 handles orchestration and remaining hygiene.&#x20;



I would make the corrections above and then proceed. Phase 3 is addressing something important: not merely making KoColor generate outfits, but making its recommendations meaningfully discriminate between a genuinely good outfit, an ordinary outfit, and a bad one—while actually honoring what the user asked for.