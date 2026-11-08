# KoColor Production Roadmap (Release Checklist — Current State 2026-11-08)

**Status: NOT READY.** Engineering remediation (Phases 1–4) is code-complete per team reports but not fully stamped; release/ops blockers below are still open. Work the stages in order — each stage's boxes must all be checked before the next. For every verification step, paste the verbatim command output into the release record (same discipline as the phase audits).

**Standing policies (do not "fix"):** `minSdk 34` is intentional; cutting-edge library versions are intentional — verify 16KB forward, never downgrade to resolve it.

---

## Stage 1 — Engineering Stamp Closure (docs + evidence, no code risk)

- [ ] **1.1 Close the 6 Phase 4 audit findings** (`Directive/Phase4/Phase4_Audit_1.md` review):
  1. Reconcile test count (C2 rows sum to 102, text claims 101).
  2. Add the `compositeScore` scale-adjudication note (producer evidence for divide-both-by-5 vs. spec 3B/3C passthrough).
  3. Paste the missing WardrobeCandidateFilter `MAX_*` half of gate B3 (or document the `5.0f` deviation).
  4. Evidence Step 1B's third bullet (`adaptContextToProvider` / `missingRoleRequirements` / unfilled-roles filtering).
  5. Evidence `HeuristicStyleEngineTest` expansion and `GreedyRehydratorTest` regression verification.
  6. Add `RotationScoringUseCase` threshold wiring to the file table.
- [ ] **1.2 Close Phase 3 Pass 2 nits** (`Directive/Phase3/Phase3_Audit_2.md`): paste item-4 existence grep + correct gate-3 output; reword the Audit_1 banner from "superseded" to "base record — read with Audit_2".
- [ ] **1.3 Team runs the full suite + lint fresh and records output** (independent of audit pastes):
  `./gradlew :features:ai:core:testDebugUnitTest :features:ai:firebase:testDebugUnitTest :features:ai:local:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest :applications:kocolor:apps:mobile:assembleDebug` and `:applications:kocolor:apps:mobile:lintRelease` — 0 failures.

## Stage 2 — Catalog Signing & Production Seed (critical path)

- [ ] **2.1 Generate the production Ed25519 seed**, store it in the secret manager (never in git). The old `atelier-dev-01` seed is burned.
- [ ] **2.2 Set `KOCOLOR_KEY_ID` to the production id** (e.g. `atelier-prod-01`) and update `SecurityConstants.kt` to the production public key.
- [ ] **2.3 Re-sign the full catalog** with `KOCOLOR_SIGNING_SEED_HEX` from the environment (both pipelines exit unsigned if it is missing — by design, no fallback).
- [ ] **2.4 Redeploy to the CDN: upload all `.kpkg` files BEFORE `manifest.json`** (zstd level 19 without `--ultra`, sha256 + Ed25519 per pack). Follow `server/Inventory/docs/Guides/Deployment_Pipeline_Manual.md` rotation procedure.

## Stage 3 — Release Privacy: Logging Gates

- [ ] **3.1 Gate HTTP logging on debug.** The six clients hardcoding `Level.BODY` (`OpenFetchWeatherData.kt:82`, ProductsDataModule, MakeupModule, ChemicalModule, ObfModule, ColorModule) must drop to `Level.NONE`/headers-only in release — the weather `appid` travels in the URL query string.
- [ ] **3.2 Gate AI I/O logging on debug.** `FirebaseAiClient.kt:286` logs full Gemini prompts/responses unconditionally — wrap in `BuildConfig.DEBUG`.
- [ ] **3.3 Strip `Log.d` in release.** Add `assumenosideeffects` for `android.util.Log` to `applications/kocolor/apps/mobile/proguard-rules.pro`.
- [ ] **3.4 Verify:** `grep -rn "Level.BODY" core/ features/` returns only debug-gated sites; install a release build and confirm Logcat shows no URLs, keys, prompts, or wardrobe content.

## Stage 4 — 16KB Page-Size Verdict (Play is rejecting non-compliant apps)

- [ ] **4.1 Re-verify ELF 16KB alignment with the CURRENT pins** (DataStore 1.2.1 / zstd 1.5.7-21) — record a PASS/FAIL verdict with the checker output. If FAIL, resolve by upgrading forward, never by downgrading.
- [ ] **4.2 On-device install + launch check** on a 16KB-page device; record device model + build fingerprint.

## Stage 5 — Listing vs. Manifest + Store Assets

- [ ] **5.1 AD_ID contradiction:** either remove the permission from `apps/mobile/src/main/AndroidManifest.xml:9` or delete the "explicitly remove AD_ID" claim (`GooglePlayStoreListing.md:59`). Listing and manifest must agree — reviewers check.
- [ ] **5.2 Cleartext contradiction:** either remove the `makeup-api.herokuapp.com` cleartext exception from `network_security_config.xml` or correct the "all traffic HTTPS" Data Safety claim.
- [ ] **5.3 Waterfall-chart bullet** (`GooglePlayStoreListing.md:36`): replace "interaction synergies / unresolved conflict penalties" copy — it describes the deleted 6-feature engine, not the live 3-axis engine.
- [ ] **5.4 Screenshots:** populate `PlayStore/Screen/` (currently empty — required for the listing).
- [ ] **5.5 Version:** bump from 0.5/code 7 to the release version (`versionCode` + `versionName`).
- [ ] **5.6 Tree hygiene:** commit or revert the uncommitted `MessagingStep.kt` navigation rework so the release build matches a clean tree.
- [ ] **5.7 Health Connect:** file the Play Console data-type declaration + record the demo video (manifest declares 5 types, all transitively stripped).

## Stage 6 — Signed AAB & Staged Rollout

- [ ] **6.1 Add `signingConfigs` to the KoColor release build** (`apps/mobile/build.gradle.kts:42` area), mirroring the AshBike/PhotoDo `RELEASE_STORE_FILE` / `RELEASE_KEY_ALIAS` env pattern. Confirm `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` stays inside `buildTypes release`.
- [ ] **6.2 Delete the stale Oct 18 AAB (81 MB) + 167 MB `mapping.txt`** from `release/`; build fresh: `./gradlew :applications:kocolor:apps:mobile:bundleRelease`. Record the AAB sha256.
- [ ] **6.3 Firebase / Play Integrity wiring** (project `20197827893` / `buildwithai-453716`): add the Play Console **App signing key certificate (SHA-256)** to Firebase Console → Project settings → app `com.zoewave.probase.kocolor`; confirm Play Integrity API enabled and in API-key restrictions.
- [ ] **6.4 Upload to the INTERNAL track first**, then promote through closed testing. If Play Console throws `Translation failed for these languages: de`, re-upload the same `.aab` to clear the transient timeout.

## Stage 7 — Go / No-Go

- [ ] Every box in Stages 1–6 checked with evidence recorded (command outputs, file hashes, screenshots of Console declarations).
- [ ] Fresh full-suite green + fresh `lintRelease` + fresh AAB hash all recorded in the same session as the upload.
- [ ] Master Architect stamps this roadmap COMPLETE.

---

## Appendix — Already Passed (no action, do not regress)

Deterministic engine + preflight gate, 15 centralized thresholds, true score normalization, `JsonSanitizer`, App Check (Play Integrity in release), CDN verify-before-decompress (sha256 + Ed25519, zstd-19, size caps), R8 + symbols + targetSdk 37, git hygiene (no secrets tracked), 3-axis listing radar (matches the live engine).
