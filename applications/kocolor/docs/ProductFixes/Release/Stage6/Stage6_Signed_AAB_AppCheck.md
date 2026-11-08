# Stage 6 — Signed AAB & Staged Rollout (Release Runbook Split)

Source: `../Gemini_stages.md` ("Stage 6 Prompt"). Execute ONLY this stage; feed one stage at a time to the coding agent.

## Fixes applied in this split (vs. `Gemini_stages.md`)

- **Added the missing `mv` commands in 6.2 step 2:** the prompt said "Move (do NOT delete)" but only ran `mkdir` + `shasum` — the stale artifacts were never moved. Now moves `release/*.aab` / `release/mapping.txt` explicitly.
- **Added the missing-absence guard in 6.2:** no `release/` dir, `.aab`, or `mapping.txt` currently exists in the tree — if the stale artifacts are absent, record that and proceed instead of failing.
- **Added the missing native-symbols copy in 6.2 step 4:** the text promised copying the native debug symbols zip but the commands omitted it. Now copies `build/outputs/native-debug-symbols/release/*.zip` and includes it in `sha256.txt`.
- **Fixed the `shasum …/*` self-inclusion footgun:** hash explicit filenames (on re-runs the old glob would hash a stale `sha256.txt` into itself).
- Verified: fail-fast signing guard, `jarsigner -verify -strict -certs` criterion (`jar verified.`), canonical `release/production/mobile-release.aab` path, classical + hybrid cert handling, and Monitoring → Enforcement sequencing are consistent with `Open_Stage_feedback_1.md` §6.

---

# AGENT MISSION: Execute Stage 6 — Fail-Fast Release Signing, Cryptographic AAB Verification & Play Integrity / App Check Enforcement

You are executing **Stage 6 (6.1, 6.2, 6.3, and 6.4)** of `ProductionRoadMap.md`.

## STRICT SIGNING & ARTIFACT INVARIANTS
1. **FAIL-FAST ON MISSING RELEASE CREDENTIALS:** Running a production release build (`bundleRelease` / `assembleRelease`) when `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, or `RELEASE_KEY_PASSWORD` is missing MUST fail immediately with a `GradleException`. Never silently produce an unsigned or debug-signed production AAB.
2. **THREE SEPARATE KEYS:** Keep the Ed25519 catalog key (`atelier-prod-01`), the Android Upload Key (used by Gradle to sign `mobile-release.aab`), and the Google Play App-Signing Key(s) (used by Google Play to sign APKs delivered to users, and registered in Firebase App Check) strictly distinct.
3. **NEVER DELETE THE OCT 18 ARTIFACTS & NEVER COMMIT BINARIES/KEYSTORES TO GIT.**

---

## MICRO-STEP 6.1: Add Fail-Fast `signingConfigs` to `applications/kocolor/apps/mobile/build.gradle.kts`

1. Read `applications/kocolor/apps/mobile/build.gradle.kts` and inspect the `AshBike` / `PhotoDo` `build.gradle.kts` files for the monorepo's `RELEASE_STORE_FILE` / `RELEASE_KEY_ALIAS` pattern.
2. Configure `signingConfigs.create("release")` and wire it into `buildTypes.release` in `applications/kocolor/apps/mobile/build.gradle.kts`:
   - Read `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD` from environment variables (or untracked `local.properties` if used by AshBike/PhotoDo).
   - Add a fail-fast check when a release bundle/apk task (`bundleRelease` or `assembleRelease`) is requested in the task graph: if any of the 4 credentials is missing or the keystore file does not exist, throw `GradleException("FATAL: Missing required release signing credentials (RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD) for production release build.")`.
   - Ensure `lintRelease` and `*UnitTest` tasks can still run without requiring the production keystore if needed, OR ensure the release credentials are loaded in the build environment.
   - Confirm `isMinifyEnabled = true`, `proguardFiles(...)`, and `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` remain inside `buildTypes.release`.

> **CHECKPOINT 6.1:**
> 1. Paste the `signingConfigs` and `buildTypes.release` blocks from `applications/kocolor/apps/mobile/build.gradle.kts`.
> 2. Test the fail-fast guard by running `env -u RELEASE_STORE_FILE ./gradlew :applications:kocolor:apps:mobile:bundleRelease` (assuming credentials are not hardcoded in `local.properties`) and confirm it fails immediately with your `GradleException`.

---

## MICRO-STEP 6.2: Archive Oct 18 Artifacts, Build `release/production/mobile-release.aab` & Verify via `jarsigner` + `keytool`

1. Confirm `release/` is listed in `.gitignore`.
2. Move (do NOT delete) the stale Oct 18 `.aab` (~81 MB) and `mapping.txt` (~167 MB) into `release/archive/0.5-code7/` and record their hashes (if the stale artifacts are already absent, record `ABSENT — nothing to archive` with an `ls` proof and proceed):
   ```bash
   mkdir -p release/archive/0.5-code7 release/production
   ls -lh release/
   mv release/*.aab release/*.txt release/archive/0.5-code7/ 2>/dev/null || echo "NOTE: no stale top-level release artifacts to move"
   ls -lh release/archive/0.5-code7/
   shasum -a 256 release/archive/0.5-code7/app-release.aab release/archive/0.5-code7/mapping.txt > release/archive/0.5-code7/sha256.txt
   cat release/archive/0.5-code7/sha256.txt
   ```
   *(If the archived filenames differ from `app-release.aab` / `mapping.txt`, hash the actual filenames from the `ls` output above — never use a bare `*` glob, which would hash a stale `sha256.txt` into itself on re-runs.)*
3. Build the fresh signed production AAB:
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:bundleRelease
   ```
4. Copy the generated `.aab` to the exact canonical path `release/production/mobile-release.aab`, copy R8 `mapping.txt` to `release/production/mapping.txt`, and copy the native debug symbols zip into `release/production/`:
   ```bash
   BUILT_AAB=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)
   cp "$BUILT_AAB" release/production/mobile-release.aab
   cp applications/kocolor/apps/mobile/build/outputs/mapping/release/mapping.txt release/production/mapping.txt
   cp applications/kocolor/apps/mobile/build/outputs/native-debug-symbols/release/*.zip release/production/
   SYMBOLS_ZIP=$(ls release/production/*.zip | head -n 1)
   shasum -a 256 release/production/mobile-release.aab release/production/mapping.txt "$SYMBOLS_ZIP" > release/production/sha256.txt
   cat release/production/sha256.txt
   ```
5. **Cryptographic Signature & Certificate Verification (`jarsigner` + `keytool` + Native Symbols):**
   ```bash
   # 1. Cryptographically verify the AAB signature with jarsigner:
   jarsigner -verify -strict -certs release/production/mobile-release.aab | tail -n 25

   # 2. Extract the Upload Certificate SHA-256 fingerprint with keytool:
   keytool -printcert -jarfile release/production/mobile-release.aab | grep -E "Owner:|Issuer:|SHA256:"

   # 3. Confirm native debug symbols are embedded in BUNDLE-METADATA:
   unzip -l release/production/mobile-release.aab | grep "BUNDLE-METADATA/com.android.tools.build.debugsymbols"
   ```
    - **Pass Criterion:** `jarsigner -verify -strict -certs` MUST output **`jar verified.`**, `keytool` must print the Upload Key `SHA256:` fingerprint, and `BUNDLE-METADATA/com.android.tools.build.debugsymbols` must contain the `.so.sym` / `.so.dbg` entries.

> **CHECKPOINT 6.2:** Paste `release/archive/0.5-code7/sha256.txt`, `release/production/sha256.txt`, `jarsigner` (`jar verified.`), `keytool` Upload Certificate `SHA256:`, and the `BUNDLE-METADATA` native symbols list.

---

## MICRO-STEP 6.3 & 6.4: Play Integrity $\rightarrow$ App Check Registration (Classical + Hybrid Certs), Internal Track Install & Enforcement

1. Verify in code that `AppCheckInitializer` installs `PlayIntegrityAppCheckProviderFactory` in release builds (`!BuildConfig.DEBUG`):
   ```bash
   git grep -n -C 8 "PlayIntegrityAppCheckProviderFactory\|DebugAppCheckProviderFactory"
   ```
2. **Execute & Record Play Console $\leftrightarrow$ Firebase App Check Registration (6.3):**
    - In **Google Play Console > Test and release > Setup > App integrity**:
        1. Confirm **Play Integrity API** is linked to Google Cloud project `20197827893` (`buildwithai-453716`).
        2. Under **App signing**, copy **every applicable SHA-256 certificate fingerprint** listed under Play App Signing (including both classical RSA/ECDSA and post-quantum/hybrid signing certificates if quantum-ready hybrid signing is enabled for the app) — NOT the upload key certificate.
    - In **Firebase Console (`buildwithai-453716`)**:
        1. Under **Project settings > General > Your apps (`com.zoewave.probase.kocolor`)**, register all applicable **Play App-Signing SHA-256 certificate fingerprints**.
        2. Under **App Check > Apps**, confirm `com.zoewave.probase.kocolor` is registered with the **Play Integrity** provider.
3. **Execute Internal-Track Play Store Installation & App Check Enforcement Verification (6.4):**
    - Upload `release/production/mobile-release.aab` to the **Internal testing** track in Google Play Console. (If Play Console throws `Translation failed for these languages: de`, verify listing translation state and retry uploading the **identical** `release/production/mobile-release.aab`).
    - Install KoColor on a physical test device **directly from the Google Play Store Internal Testing opt-in page** (never via sideloaded `adb install`).
    - **Phase A (Monitoring Verification):** Launch the Play-installed app, verify CDN catalog download (`atelier-prod-01` Ed25519 verify + `zstd` decompress), run the AI Style Simulator via `FirebaseAiProvider`, and confirm in the Firebase App Check metrics dashboard that verified Play Integrity requests are recorded with zero invalid/unverified token errors.
    - **Phase B (Enforcement Verification):** Enable **App Check Enforcement** for the target Firebase backend service(s) in Firebase Console, wait for propagation, and execute another Style Simulator AI request from the Play-installed app to prove requests succeed under active **Enforcement** (not merely Monitoring).

## STAGE 6 MANDATORY COMPLETION GATE
Check off `[x] 6.1` through `[x] 6.4` in `ProductionRoadMap.md` ONLY after pasting:
1. `build.gradle.kts` fail-fast signing diff + test proof.
2. `release/archive/0.5-code7/sha256.txt` + `release/production/sha256.txt` + `jarsigner` (`jar verified.`) + Upload Key `SHA256` + `BUNDLE-METADATA` symbols output.
3. All registered Play App-Signing SHA-256 fingerprint(s), Internal Track Play Store installation verification, and App Check Monitoring $\rightarrow$ Enforcement verification results.
