> Phase 1 punchlist: closes the verified gaps between `Phase1_Audit_final.md` and the tree. Feed to the coding agent after Tasks.md Phase 1. Each item states the exact required end-state; run the gate at the end before stamping Phase 1 complete.

# Tasks — Phase 1 Corrections (Punchlist 1)

## [CODE] 1. Land the Multimodal rejection for real (BYOK + Gemma)

The audit claims explicit `AiInput.Multimodal` rejection, but neither provider has it — BYOK never mentions `Multimodal`, and Gemma's `execute()` still strips images to `promptString`.

- `features/ai/core/.../ByokAiProvider.kt`, `features/ai/firebase/.../GemmaRestFallbackProvider.kt`: at the top of `execute()`, return `Result.failure(UnsupportedOperationException("..."))` when `input is AiInput.Multimodal`, before any prompt extraction.
- Leave `countTokens()` text-length estimation as-is (record: image tokens uncounted by design).
- Verify: `grep -rn "UnsupportedOperationException" <both files>` returns both `execute()` guards; no `Multimodal -> promptString` path remains in either `execute()`.

## [CODE] 2. Finish the UNKNOWN defaults: `depth`

`CompactManifestSerializer.kt:33` still falls back to `"Balanced"`. Change it to `"UNKNOWN"`.

- Verify: `grep -n '"SATIN"\|"KoColor"\|"Balanced"' data/usecase/CompactManifestSerializer.kt` returns zero default literals (`ColorQuantizer` fallbacks are computed values, not invented facts — leave them).

## [CODE] 3. Make the rehydrator score param required; record Int as decided

`GreedyRehydrator.mapToVisualBlueprintData` takes `fashionistaScore: Int = 85` — a silent passing grade above the 80 gate. Decision: keep `Int` (it precisely matches the single consumer, `koColorScore`), but it must be required.

- Remove the `= 85` default; update callers (`StyleSimulatorViewModel`, `BlueprintDetailContent`) and `GreedyRehydratorTest` to always pass the result-channel score explicitly.
- Amend Tasks.md 1.2 item 5 wording from "accept the pre-computed `FashionistaScore`" to "accept the pre-computed score `Int`" so the directive matches the decided contract.

## [CODE] 4. Finish the standardId unification

`FashionistaScore.kt` has `standardVersion = "v1"` but still `standardId = "FASHIONISTA_STD"`. Change to `"FASHIONISTA"` per Tasks.md 1.2 item 4; grep for `FASHIONISTA_STD` consumers (UI/tests) and update them in the same pass.

## [DOC] 5. Correct `Phase1_Audit_final.md`

- Kill list: add the deleted `domain/FashionistaFeatureVector.kt` and `FashionistaScorerTest.kt` (both verified gone); state that `ItaCalculator` was **moved** from `fashionista/integration/` to `fashionista/math/` and rewritten as an injectable class (not purely "created").
- File table: fix `ColorHarmonyEngine.kt` path to `data/color/` (not `fashionista/color/`); fix the "`fashionista/scoring/*` (13 files)" label to the actual cross-package paths; add the missing domain-file rows.
- Logging section: add `OpenFetchWeatherData.kt` (both interceptors, verified on `core.network.BuildConfig.DEBUG`) to the restored-guard account.
- Rehydrator section: record the decided `Int` (required, no default) contract instead of implying the object.
- Either add real RESOLVED markers to `RoadMapToPro.md` / `findings_rollup_2026-11-04.md` or delete the sentence claiming they were updated — today that sentence is false.

## [DOC] 6. Retire the old audit

Mark `Phase1_Audit[...].md` superseded by `Phase1_Audit_final.md` (dated header) or move it to `Archive/`. Two competing audit records must not coexist unmarked.

## Gate

Run `:applications:kocolor:data:testDebugUnitTest`, `:applications:kocolor:features:analyzer:testDebugUnitTest`, and `:applications:kocolor:apps:mobile:assembleDebug`. All green, then refresh the test counts in `Phase1_Audit_final.md`'s verification section and stamp Phase 1.
