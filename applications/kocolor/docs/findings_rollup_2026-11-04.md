# KoColor Findings Rollup — 2026-11-04

Consolidates everything found across this session: the release-pipeline
verification, the release-checklist audit, and the fashion/token assessment.
Related docs:

- `PlayStore/checklist.md` — the original 3-blocker release review (still the
  working gate list; this rollup does not replace it).
- `fashion_algorithms_assessment.md` — full fashion-math and token deep-dive
  (summarized in Section C, not duplicated here).
- `server/Inventory/docs/Guides/Deployment_Pipeline_Manual.md` — pipeline manual
  (rewritten earlier today; covers the key-rotation procedure).

## A. Release-pipeline verification — PASS (code), rotation pending (ops)

The pre-crash session moved both catalog pipelines off the hardcoded signing seed;
this session re-ran the interrupted end-to-end verification after the IDE crash
killed the final signed run (SIGKILL, before any conclusion was recorded).

**Verified this session (disposable test seed, isolated `/tmp` copies):**

- Both crates build offline with no new warnings; both test suites green
  (zero unit tests exist — green by vacuity, noted as a gap).
- Both binaries fail closed with `KOCOLOR_SIGNING_SEED_HEX` unset (hard error,
  no ephemeral fallback, no silent output).
- `kc-optimizer` full run on all 232 real inputs: exit 0 in ~55 s, 20 `.kpkg`
  bundles + signed `manifest.json` envelope stamped `test-verify-01` + search index.
- `prog_rs` full run with `--resume`: exit 0, fully offline (232/232 skipped,
  dummy Gemini key never used), 232 approved / 0 quarantined, 2 `.kpkg` + signed
  manifest with the test key id.
- Tracked input tree untouched; test seed and test-signed bundles confined to
  `/tmp`, never near the CDN path. Burned seed absent from the worktree.

**Bug found and fixed during verification** (uncommitted, in working tree):
`server/Inventory/package/kc-optimizer/src/indexer.rs` parsed the 232 `.meta.json`
enrichment artifacts as product sources and crashed the run. It now ignores
`.meta.json` (with `.notes.json`); the passing run above confirms the fix.

**Still pending — owner action, blocks release:**
Generate + store the production seed in the secret manager, pin the new public key
in `SecurityConstants.kt` (still the original value, file unmodified), re-sign the
full catalog with `KOCOLOR_KEY_ID=atelier-prod-01`, redeploy (`.kpkg` before
`manifest.json`). The old seed is burned and can never be trusted again.

## B. Release-checklist audit — "we updated all the code"

Audited `PlayStore/checklist.md` against the current tree. Verdict: **not done**.
The team's code updates resolved most of it; the remainder is one listing
contradiction, one new policy inconsistency, the Section A rotation ops, and four
should-fix leftovers.

**Must-fix 1 — signing key: code done, ops pending.** Both pipelines read the seed
from the environment and exit without signing when it is missing; scripts fail
fast; manual rewritten. Operational half outstanding per Section A.

**Must-fix 2 — Logcat leaks: done.** All six okhttp clients are now `BODY` in
debug / `NONE` in release (gated on `core.model`'s `BuildConfig.DEBUG` —
equivalent in standard builds); the raw Gemini prompt/response logs are `Log.d`,
stripped in release by the new R8 `assumenosideeffects` (release has minify
enabled, so the rule applies); surviving `Log.w/e` lines carry no user content
or secrets.

**Must-fix 3 — listing vs manifest: partial.**
- Fixed: `AD_ID` removed from the manifest (matches the "explicitly removed"
  claim); cleartext exception removed (config is HTTPS-only, matches Data Safety).
- Not fixed: both listing files still advertise heart-rate sync
  (`Operations/Legal/PlayStore/GooglePlayStoreListing.md:50`,
  `StoreListingDescription.md:50`) while the manifest strips `READ_HEART_RATE`;
  `DataSafety.md:108` still instructs checking the fitness box including heart
  rate, contradicting `HealthPermissionsJustification.md`.
- New inconsistency surfaced by the fix: `PrivacyPolicy.md:40` says Analytics
  "utilizes the Advertising ID (`AD_ID`)", contradicting the listing's removal
  claim now that the permission is gone. One of the two needs correcting.

**Should-fix:** signing block (env-based, wired), version bump (1.0.0 / code 8),
and MessagingStep commit are done. Still open: screenshots (no `Screen/` dir or
listing images exist), stale Oct 18 AAB + 167 MB mapping in `release/`, 16 KB
page-size re-verification (DataStore 1.2.1 / zstd 1.5.7-21 unchanged), minSdk 34
intentional-confirm (unchanged — product decision).

## C. Fashion math, tokens, and gears — summary

Full analysis in `fashion_algorithms_assessment.md` (~45 files, live-vs-dead traced
via DI bindings and caller search). Headlines:

**Tokens — disciplined core, leaky edges.** The simulator has a real budget loop
(adaptive detail/K step-down, exact counting, SHA-256 prompt cache, zero-token
fallback). Leaks: the 768-token on-device tier cannot fit the ~700-token fixed
scaffold (local-first is decorative); Firebase `countTokens` does retried network
calls inside the fit loop; validation failures re-spend full prompts per provider;
`SuggestionEngine`/`AnalyzerEngine` bypass all governance and the latter sends up
to 4 full-res face photos to the cloud; billed output tokens are unobservable
(`maxOutputTokens` declared everywhere, enforced nowhere); BYOK hand-builds JSON
(crash risk on backslashes) and ignores temperature.

**Math — correct foundations, weak discrimination, stubs.** CIEDE2000, circular
hue statistics, and sRGB→Lab→LCh verify against the references. But live scores
cluster ~55–98 with lenient baselines, tier 95+ is mathematically unreachable
(logistic μ=0.5/τ=0.2 caps at ~92.4), two "engines" return hard constants, one
"delta-E" mixes hue degrees with Lab units (dead — delete it), and the manifest
invents facts for unknown fields (`"Cotton"`/`"SATIN"`).

**Gears — two pipelines, one alive.** The tested 6-feature scorer has no DI
binding; the live 3-evaluator path is scored twice per request while the UI shows
a third implementation's number; the creation-flow validation checklist is
hardcoded all-pass; the "deterministic" fallback picks randomly with phantom
palette colors; the rehydrator's anchor fail-safe can drop the anchor it protects.

**Dead inventory (confirmed):** 6-feature scorer path, `DeterministicCatalogEngine`,
`VisualMassEngine`/`ItaCalculator`/Michelson (the only real CV math),
`calculateDeltaE`, `getCandidates`/`getCosmeticCandidates`, `enforceRoleDiversity`,
`missingRoleRequirements` consumers, the `StyleTelemetry` AI path, two dead
injections in `DeterministicContextEngine`. Duplicated: `hexToLCh` ×2, circular
mean ×2, `calculateChroma` ×3, thermal banding ×3, fashionista score ×3.

See the assessment doc for the ranked P0/P1/P2 fix list.

## D. Consolidated open items (release blockers first)

1. Rotate the catalog signing key and redeploy (Section A) — ops, blocks release.
2. Fix heart-rate listing claims + AD_ID policy contradiction (Section B) —
   policy risk at submission.
3. Reconcile the three fashionista scores; delete `calculateDeltaE`; fix
   `UNKNOWN` manifest defaults; fix BYOK JSON construction; plumb the real
   validator verdict into the creation UI (Section C, P0s).
4. Screenshots, fresh AAB (drop the stale Oct 18 artifacts), 16 KB verification,
   minSdk confirm (Section B leftovers).
5. Token-spend hardening: Nano viability, heuristic-first counting, output-token
   telemetry + enforcement, validator-feedback repair (Section C, P1s).
