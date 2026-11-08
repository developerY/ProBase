# KoColor Production Roadmap (Release Checklist — Current State 2026-11-08)

**Status: NOT READY.** Engineering remediation (Phases 1–4) is code-complete per team reports but not fully stamped; release/ops blockers below are still open. Work the stages in order — each stage's boxes must all be checked before the next. For every verification step, paste the verbatim command output into the release record (same discipline as the phase audits).

**Standing policies (do not "fix"):** `minSdk 34` is intentional; cutting-edge library versions are intentional — verify 16KB forward, never downgrade to resolve it. We build for the future: adopt Google Play requirements and target versions EARLY, ahead of enforcement deadlines (16KB, target API), and resolve findings by moving forward.

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

**Key boundary:** the Ed25519 catalog signing key is NOT the Android application upload key — different credentials, purposes, and security boundaries. All five must agree: private seed, key ID, `SecurityConstants.kt` public key, every signed `.kpkg`, CDN manifest. Preparation and publication are separate operations — never publish before 2.4–2.5 pass.

- [ ] **2.1 Generate the production Ed25519 seed**, store it in the secret manager (never in git). The old `atelier-dev-01` seed is burned.
- [ ] **2.2 Set `KOCOLOR_KEY_ID` to the production id** (e.g. `atelier-prod-01`) and update `SecurityConstants.kt` to the production public key.
- [ ] **2.3 Sign the full catalog in staging** with `KOCOLOR_SIGNING_SEED_HEX` from the environment (both pipelines exit unsigned if it is missing — by design, no fallback). Verify every package and the manifest locally BEFORE building the release AAB.
- [ ] **2.4 Key-transition compatibility test** (mandatory, before CDN publication): fresh install trusting the production key; upgrade from the previous app version; existing cached dev catalog vs. new production catalog; invalid-signature rejection; wrong-key-ID rejection; missing-seed rejection; manifest references only verified production packages. Resolve existing-client coexistence explicitly before switching the live manifest (or record N/A with zero-install evidence — pre-release, no users shipped).
- [ ] **2.5 Build the release AAB and validate it trusts the production catalog** end-to-end (download → verify → decompress) before publishing.
- [ ] **2.6 Publish to the CDN: upload all `.kpkg` files BEFORE `manifest.json`** (zstd level 19 without `--ultra`, sha256 + Ed25519 per pack), then verify the live CDN from an external client. Follow `server/Inventory/docs/Guides/Deployment_Pipeline_Manual.md` rotation procedure.

## Stage 3 — Release Privacy: Logging Gates

- [ ] **3.1 HTTP logging fully OFF in release.** All six clients (`OpenFetchWeatherData.kt:82`, ProductsDataModule, MakeupModule, ChemicalModule, ObfModule, ColorModule) must use `level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE` with each module's own `BuildConfig` — `Level.HEADERS` is NOT acceptable for the weather client because the `appid` travels in the URL itself.
- [ ] **3.2 Gate AI I/O logging on debug.** `FirebaseAiClient.kt:286` logs full Gemini prompts/responses unconditionally — wrap in `BuildConfig.DEBUG`.
- [ ] **3.3 Strip debug/info logs in release, keep diagnostics.** Confirm release minification is actually enabled and the `assumenosideeffects` rule for `android.util.Log` (`Log.d`/`Log.i`/`Log.v`) in `applications/kocolor/apps/mobile/proguard-rules.pro` removes the intended calls; retain warning/error logging for operational diagnostics.
- [ ] **3.4 Verify:** `grep -rn "Level.BODY\|Level.HEADERS" core/ features/` returns only debug-branched sites; install a release build and confirm Logcat shows no URLs, keys, prompts, or wardrobe content.

## Stage 4 — 16KB Page-Size Compatibility (Mandatory Production Readiness Gate)

Context: apps targeting API 35+ must support 16KB pages; Google blocks non-compliant updates from Feb 1, 2027. Per standing policy we verify early at current versions — never downgrade (targetSdk 37 already exceeds Play's API 36 requirement).

- [ ] **4.1 ELF segment alignment:** inspect the actual resolved AAB dependencies (not just the version catalog); enumerate every packaged `.so` across supported ABIs; record PASS/FAIL with checker output. Pins (DataStore 1.2.1 / zstd 1.5.7-21) stay unless evidence identifies a problem.
- [ ] **4.2 APK ZIP alignment (separate check):** generate APKs from the actual AAB with bundletool and run `zipalign -c -P 16` — ELF alignment alone is insufficient. Record output.
- [ ] **4.3 16KB environment run:** install on a 16KB-page device/env and exercise catalog download, signature verification, AND zstd decompression — not just app startup. Record device model + build fingerprint.

## Stage 5 — Listing vs. Manifest + Store Assets

- [ ] **5.1 AD_ID contradiction (check the MERGED manifest, not just source):** dependencies can merge `com.google.android.gms.permission.AD_ID` in undeclared. Inspect the final merged release manifest + AAB: either the permission is absent everywhere (and the "explicitly remove AD_ID" listing claim `GooglePlayStoreListing.md:59` stands) or remove the claim. Listing, manifest, and Play declaration must agree — reviewers check.
- [ ] **5.2 Cleartext exception:** the `makeup-api.herokuapp.com` exception permits cleartext but doesn't prove plaintext is sent. If unnecessary, remove it; if the app actually uses HTTP, migrate the call to HTTPS instead of weakening config. Then make the Data Safety "all traffic HTTPS" answer match the verified reality (config + runtime traffic).
- [ ] **5.3 Waterfall-chart bullet** (`GooglePlayStoreListing.md:36`): replace "interaction synergies / unresolved conflict penalties" copy — it describes the deleted 6-feature engine, not the live 3-axis engine.
- [ ] **5.4 Screenshots:** populate `PlayStore/Screen/` (currently empty — required for the listing).
- [ ] **5.5 Version:** bump from 0.5/code 7 to the release version (`versionCode` + `versionName`).
- [ ] **5.6 Tree hygiene:** commit or revert the uncommitted `MessagingStep.kt` navigation rework so the release build matches a clean tree.
- [ ] **5.7 Health Connect eligibility gate (potential blocker — decide BEFORE declaring):** determine whether KoColor has a qualifying user-facing health/fitness feature per Play policy (fashion recommendations informed by activity data do NOT automatically qualify). If YES: document each data type individually with its user-facing benefit, complete the Health Apps declaration AND the separate data-type permission declaration (demo video only where that form requires it), and verify the privacy policy covers health-data access/processing/sharing/retention/deletion/encryption. If NO: remove the functionality and permissions from the release artifact and use the no-health-features declaration path. In both cases inspect the FINAL MERGED release manifest + AAB — never rely on the source manifest or "transitively stripped" claims; if the runtime still requests absent permissions, that's a separate defect.
- [ ] **5.8 Data Safety cross-check:** independently reconcile the Play Console Data Safety answers against the release build — data collected, local-vs-cloud processing, AI prompt/response handling, third-party processors, health data if applicable, encryption in transit, retention/deletion — and confirm listing + privacy policy + Data Safety + actual provider behavior all agree.

## Stage 6 — Signed AAB & Staged Rollout

- [ ] **6.1 Add `signingConfigs` to the KoColor release build** (`apps/mobile/build.gradle.kts:42` area), mirroring the AshBike/PhotoDo `RELEASE_STORE_FILE` / `RELEASE_KEY_ALIAS` env pattern. Confirm `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` stays inside `buildTypes release`.
- [ ] **6.2 Archive (do NOT delete) the stale Oct 18 artifacts, then build fresh.** Move the 81 MB AAB + 167 MB `mapping.txt` to `release/archive/0.5-code7/` (with `sha256.txt`); build fresh into `release/production/` via `./gradlew :applications:kocolor:apps:mobile:bundleRelease`. For every release retain: version code/name, commit SHA, AAB SHA-256, R8 mapping, native symbols, signing-cert fingerprint. The archive lives in protected artifact storage, not git.
- [ ] **6.3 Play Integrity → App Check registration** (project `20197827893` / `buildwithai-453716`): in Play Console open Release → App integrity → Play Integrity API and LINK it to the Firebase/Google Cloud project; in Firebase Console go to Security → App Check and register the Android app (`com.zoewave.probase.kocolor`) with the Play Integrity provider, supplying the SHA-256 of the PLAY APP-SIGNING certificate (not the upload cert, not debug — Play installs verify against the app-signing cert). No API-key-restriction step — Firebase API keys are project identifiers, not auth credentials.
- [ ] **6.4 Upload to the INTERNAL track first** and verify with an ACTUAL PLAY-STORE INSTALLATION (not just a sideloaded APK) so the Play-signing + Play Integrity + App Check path is exercised together; then promote through closed testing. If Play Console throws `Translation failed for these languages: de`, treat as potentially transient: verify the listing translation state and the artifact's validity, then retry the IDENTICAL AAB.

## Stage 7 — Go / No-Go

- [ ] Every box in Stages 1–6 checked with evidence recorded (command outputs, file hashes, screenshots of Console declarations).
- [ ] Fresh full-suite green + fresh `lintRelease` + fresh AAB hash all recorded in the same session as the upload — final test, lint, and signed AAB MUST correspond to the same source revision (Stages 3/5/6 change code after Stage 1, so Stage 1 output alone is not release evidence).
- [ ] **FINAL RELEASE RECORD** completed and filed:
  `commit SHA / tree state / Gradle tests / lintRelease / versionCode / versionName / AAB SHA-256 / upload cert fp / Play signing cert fp / catalog key ID / catalog verify / 16KB ELF / 16KB APK align / Health Connect status / Data Safety status / internal-track result`
- [ ] Master Architect stamps this roadmap COMPLETE.

---

## Appendix — Already Passed (no action, do not regress)

Deterministic engine + preflight gate, 15 centralized thresholds, true score normalization, `JsonSanitizer`, App Check client integration (Play Integrity provider wired in code — Console registration/enforcement is Stage 6.3), CDN verify-before-decompress (sha256 + Ed25519, zstd-19, size caps), R8 + symbols + targetSdk 37, git hygiene (no secrets tracked), 3-axis listing radar (matches the live engine).
