# Minimum to Production Ready (App Only)

Scope: the least work that makes the KoColor **app** shippable. Explicitly excluded: audit paperwork, roadmap stamping, catalog/CDN rotation, listing copy. Each item states its done-criterion — nothing here is done until the check passes.

## Blockers (cannot ship without these)

### 1. Fail-fast release signing + verify the actual signer
- In `applications/kocolor/apps/mobile/build.gradle.kts`: throw `GradleException` when any of `RELEASE_STORE_FILE` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD` is missing from the `bundleRelease`/`assembleRelease` task graph. Remove the silent `?: "keystore.jks"` fallback.
- Done when: `env -u RELEASE_STORE_FILE ./gradlew :applications:kocolor:apps:mobile:bundleRelease` fails with the `GradleException`; `keytool -printcert -jarfile <aab>` shows the intended upload cert; `jarsigner -verify -strict -certs` prints `jar verified.`

### 2. Zero `AD_ID` in the merged release manifest
- A transitive dependency merges `com.google.android.gms.permission.AD_ID` (source manifest is already clean). Add `<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />` to the source manifest; decide `ACCESS_ADSERVICES_AD_ID` (remove it or declare it — no silent third option).
- Done when: merged release manifest AND `bundletool dump manifest` report 0 `AD_ID` occurrences.

### 3. Finish Health Connect Option A
- Remove the 6 still-active permissions (`READ_STEPS`, `READ_SLEEP`, `READ_HYDRATION`, `READ_TOTAL_CALORIES_BURNED`, `READ_ACTIVE_CALORIES_BURNED`, `WRITE_HYDRATION`) plus the `<queries>` entry and permissions-rationale alias from the release path; rebuild.
- Done when: merged release manifest AND AAB dump report 0 health permissions; select the no-health-features declaration path in Play Console.

### 4. Strip `Log.i` in release
- Add `public static int i(...);` to the `-assumenosideeffects` block in `applications/kocolor/apps/mobile/proguard-rules.pro` (currently only `v`/`d`).
- Done when: block lists `v`/`d`/`i`; release-build Logcat spot-check shows no app log noise.

### 5. Real 16KB verification (not simulated)
- Enumerate every `.so` in the AAB: all `LOAD` segments `2**14`/`2**16`. `bundletool dump config` shows `PAGE_ALIGNMENT_16K`. `zipalign -c -P 16` on every generated APK prints `Verification successful`. Install on a real 16KB target (`getconf PAGE_SIZE` = `16384`) and exercise catalog download + signature verify + zstd decompression.
- Done when: all four outputs exist from real runs.

## Submission requirements

### 6. Canonical artifacts
- `release/production/mobile-release.aab` + `mapping.txt` + native-symbols zip + `sha256.txt`; move (never delete) any old artifacts to `release/archive/`.
- Done when: the uploaded AAB is the hashed file at that path.

### 7. Screenshots + version check
- Populate `PlayStore/Screen/` (required for the listing; currently missing entirely).
- Confirm `versionCode 8` exceeds the highest code on ALL Play Console tracks (Internal/Closed/Open/Production), not just the repo's last value.

### 8. Play Integrity → App Check → Internal-track install
- Link the Play Integrity API to GCP project `20197827893`; register every Play app-signing SHA-256 in Firebase App Check; upload to the Internal track; install **from Play** (never sideload); confirm Monitoring traffic, then enable Enforcement and re-verify an AI request succeeds under it.
- Done when: a Play-installed build passes catalog + AI calls with Enforcement on.

### 9. Data Safety answers from the verified artifact
- Fill the Play Console questionnaire strictly from the release build's real behavior (HTTPS-only, local-vs-cloud AI handling, no health data per Option A).

## Final lock

### 10. Commit, rebuild, re-verify on one revision
- Commit the tree clean; run the full unit-test suite + `lintRelease` (0 failures); rebuild `bundleRelease`; re-verify merged manifest (`AD_ID`/health zero), signer identity, and `jarsigner`.
- Done when: tests, lint, and the uploaded AAB all come from the same commit.

## Cheap hardening (do while in there, not blocking)

- Align the six HTTP log gates to each module's own `BuildConfig.DEBUG` (they currently branch on `core.network`'s — functionally correct in release, but spec-noncompliant).
