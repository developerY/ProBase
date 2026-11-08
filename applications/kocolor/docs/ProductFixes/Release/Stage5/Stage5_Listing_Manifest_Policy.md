# Stage 5 — Listing vs. Manifest + Store Assets & Policy Gates (Release Runbook Split)

Source: `../Gemini_stages.md` ("Stage 5 Prompt"). Execute ONLY this stage; feed one stage at a time to the coding agent.

## Fixes applied in this split (vs. `Gemini_stages.md`)

- **Made Stage 5 self-contained across shells:** inlined the portable `run_bundletool()` resolver (the prompt invoked `bundletool` directly but only Stage 4 defined the resolver) and added `AAB_PATH` / `MERGED_MANIFEST` re-export notes — shell variables do not survive across terminal sessions.
- Verified referenced paths exist: `MakeupModule.kt` (`features/makeupapi/.../di/`), `network_security_config.xml` (`apps/mobile/.../res/xml/`), `MessagingStep.kt` (`features/analyzer/.../messaging/`), `GooglePlayStoreListing.md`, `Deployment_Pipeline_Manual.md`.

---

# AGENT MISSION: Execute Stage 5 — Listing vs. Manifest, Store Assets, Version Bump & Health Connect / Data Safety Reconciliation

You are executing **Stage 5 (5.1 through 5.8)** of `ProductionRoadMap.md`.

## STRICT EXECUTION RULES
1. **ALWAYS INSPECT THE MERGED RELEASE MANIFEST AND BUILT AAB:** Never rely solely on source `AndroidManifest.xml`.
2. **RESOLVE PERMISSIONS BY ACTUAL SDK & PRODUCT BEHAVIOR:** Do not lazily edit documentation to hide an unintended transitive permission, and do not make privacy claims without tracing every code path.

## SHELL PREAMBLE (run once per fresh shell — all micro-steps below depend on these)

```bash
AAB_PATH=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)
echo "AAB: $AAB_PATH"
MERGED_MANIFEST=$(find applications/kocolor/apps/mobile/build/intermediates/merged_manifest/release -name "AndroidManifest.xml" | head -n 1)
echo "MERGED MANIFEST: $MERGED_MANIFEST"

run_bundletool() {
    if command -v bundletool >/dev/null 2>&1; then
        bundletool "$@"
    else
        BT_JAR=$(find "$HOME" -name "bundletool*.jar" 2>/dev/null | head -n 1)
        [ -n "$BT_JAR" ] || { echo "FATAL: bundletool binary and bundletool*.jar both missing"; exit 1; }
        java -jar "$BT_JAR" "$@"
    fi
}
```

---

## MICRO-STEP 5.1: Resolve `AD_ID` Based on Actual SDK Usage + Merged Manifest + AAB

1. Check whether KoColor's codebase actually accesses Google Advertising ID:
   ```bash
   git grep -n "AdvertisingIdClient\|AD_ID" -- "*.kt" "*.java" "*.xml"
   ```
2. Inspect the merged release manifest and built AAB:
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:processReleaseMainManifest
   MERGED_MANIFEST=$(find applications/kocolor/apps/mobile/build/intermediates/merged_manifest/release -name "AndroidManifest.xml" | head -n 1)
   grep -n "AD_ID" "$MERGED_MANIFEST" || echo "ZERO AD_ID IN MERGED MANIFEST"
   ```
3. **Resolution Rule:**
    - Since KoColor does NOT display ads or use `AdvertisingIdClient`, `com.google.android.gms.permission.AD_ID` MUST NOT be present in the release artifact.
    - If any transitive dependency merges `AD_ID` into `$MERGED_MANIFEST`, add `<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />` to `applications/kocolor/apps/mobile/src/main/AndroidManifest.xml`, rebuild the release manifest + AAB, and verify that both `$MERGED_MANIFEST` and `run_bundletool dump manifest --bundle="$AAB_PATH"` report **0 occurrences of `AD_ID`**.
    - Verify `GooglePlayStoreListing.md:59` and the Play Console Advertising ID declaration ("Does your app use advertising ID? No") agree 100%.

> **CHECKPOINT 5.1:** Paste the `git grep`, merged manifest check, and `bundletool dump manifest` check proving zero `AD_ID` in the final AAB.

---

## MICRO-STEP 5.2: Verify 100% HTTPS Across Source Endpoints, Merged Config & Runtime Traffic

1. **Layer 1 — Source Endpoint Audit:**
   ```bash
   git grep -n "makeup-api\|cleartextTrafficPermitted\|networkSecurityConfig\|http://" applications/ core/ features/ -- "*.kt" "*.java" "*.xml" | grep -v "schemas.android.com"
   ```
    - Confirm `MakeupModule.kt` and all other Retrofit/OkHttp clients use `https://` URLs exclusively.
2. **Layer 2 — Network Security Config Audit:**
    - Read `network_security_config.xml` (and check the merged release resources).
    - Remove the unnecessary `makeup-api.herokuapp.com` `cleartextTrafficPermitted="true"` exception so cleartext HTTP is disabled across all domains in release builds.
3. **Layer 3 — Runtime Verification:**
    - Exercise the Makeup API / catalog / weather / AI network flows in the release build and confirm zero plaintext `http://` requests are attempted or blocked in Logcat.

> **CHECKPOINT 5.2:** Paste the updated `network_security_config.xml`, `MakeupModule.kt` base URL, and the `git grep` output confirming zero non-schema `http://` URLs.

---

## MICRO-STEP 5.3 & 5.4: Fix Waterfall-Chart Listing Copy & Verify Screenshots

1. **5.3 Waterfall-Chart Bullet (`GooglePlayStoreListing.md:36`):**
    - Read `GooglePlayStoreListing.md` around line 36 (and any sibling store listing files).
    - Replace `"interaction synergies / unresolved conflict penalties"` (which described the deleted 6-feature engine) with accurate copy describing the live 3-axis `FashionistaEngine` (`40% CIELAB Color Harmony, 35% Silhouette Proportions, and 25% Contrast & Depth alignment`).
    - Run `git grep -n "interaction synergies\|6-axis\|6-Axis"` across the repo to confirm zero stale 6-axis references remain in active listing files.
2. **5.4 Screenshots (`PlayStore/Screen/`):**
    - Check `PlayStore/Screen/` (`find . -path "*PlayStore/Screen*"` and list all files with dimensions/file sizes). Confirm required Play Store screenshots are populated.

> **CHECKPOINT 5.3–5.4:** Paste lines 30–45 of `GooglePlayStoreListing.md` and the `ls -lh` output of `PlayStore/Screen/`.

---

## MICRO-STEP 5.5 & 5.6: Check Play Console Highest `versionCode`, Bump Version & Clean `MessagingStep.kt`

1. **5.5 Version Bump (`applications/kocolor/apps/mobile/build.gradle.kts`):**
    - **CRITICAL:** Check the highest previously uploaded `versionCode` across **ALL tracks** (Internal, Closed, Open, Production) in Google Play Console (and check `release/` archives).
    - Set `versionCode` in `applications/kocolor/apps/mobile/build.gradle.kts` to an integer **strictly greater than the highest version code ever uploaded to Play Console** (and `> 7`), and update `versionName` to the target release version string.
2. **5.6 Tree Hygiene (`MessagingStep.kt`):**
    - Run `git status` and `git diff` on `MessagingStep.kt`. Commit or revert the uncommitted `MessagingStep.kt` navigation rework so the working tree contains no half-finished uncommitted experiments.

> **CHECKPOINT 5.5–5.6:** Paste the `versionCode` and `versionName` lines from `build.gradle.kts` and `git status`.

---

## MICRO-STEP 5.7: Health Connect Eligibility Decision Gate (Source + Merged Manifest + AAB)

1. Inspect all Health Connect references across code, merged release manifest, and built AAB (re-run the Shell Preamble first in a fresh shell):
   ```bash
   git grep -n "HealthConnect\|android\.permission\.health" applications/ core/ features/
   grep -n -i "health" "$MERGED_MANIFEST" || echo "ZERO HEALTH PERMISSIONS IN MERGED MANIFEST"
   ```
2. **Apply the Policy Decision Before Declaring in Play Console:**
    - Google Play policy restricts `android.permission.health.*` to approved primary health/fitness use cases with direct user-facing health benefits.
    - **If Option A (No qualifying Health Connect feature in this release):** Remove all `android.permission.health.*` permissions, `<queries>`, activity-aliases, and runtime permission launch calls from the release build (using `tools:node="remove"` if merged transitively), rebuild the AAB, and verify via `run_bundletool dump manifest --bundle="$AAB_PATH" | grep -i health` that **zero** health permissions exist in the final AAB. Select the "No health features" declaration path in Play Console.
    - **If Option B (Retaining Health Connect as a qualified health/wellness feature):** Document every single declared `android.permission.health.*` permission in the final AAB, its exact user-facing health benefit screen, in-app consent/disclosure flow, privacy policy clauses (access, processing, sharing, retention, deletion, encryption), and complete both the Health Apps declaration and Data-Type Permission declaration (with demo video) in Play Console.

> **CHECKPOINT 5.7:** Paste the merged manifest + `bundletool dump manifest` output for `health` and the recorded Option A or Option B evidence.

---

## MICRO-STEP 5.8: End-to-End Image Pipeline Trace & Data Safety Reconciliation

1. **Mandatory Face/Image Pipeline Trace (Do NOT Assume Local-Only Without Proof):**
    - Inspect every class that touches user bitmaps or `AiInput.Multimodal`:
      ```bash
      git grep -n "Bitmap\|Multimodal\|analyzeStyle" applications/ features/ core/ -- "*.kt"
      ```
    - Read `AnalyzerEngine.kt`, `FirebaseAiProvider.kt`, `FirebaseAiClient.kt`, `ByokAiProvider.kt`, `GemmaRestFallbackProvider.kt`, and `LocalNanoAiProvider.kt`.
    - Record exact `File:Line` evidence proving:
        1. What `AnalyzerEngine` sends to `CapabilityRouter` / `AiProvider` (`AiInput.TextOnly` with on-device `ItaCalculator` / `ColorQuantizer` numeric telemetry vs. `AiInput.Multimodal`).
        2. How `FirebaseAiProvider`, `ByokAiProvider`, and `GemmaRestFallbackProvider` handle `AiInput.Multimodal` (confirming all three cloud providers reject `AiInput.Multimodal` with `UnsupportedOperationException` or `Result.failure`, so raw face photographs can **never** be transmitted to any cloud endpoint).
2. **Complete the 7-Point Data Safety Reconciliation Table:**
    - Cross-check and record the exact verified answers for: (1) Data collected vs. shared, (2) On-device biometric image processing (backed by the trace above), (3) Cloud AI text prompt/response handling, (4) Third-party processors, (5) Health data status (matching 5.7), (6) Encryption in transit (backed by 5.2), and (7) User data retention/deletion controls.

## STAGE 5 MANDATORY COMPLETION GATE
Check off `[x] 5.1` through `[x] 5.8` in `ProductionRoadMap.md` ONLY after pasting:
1. Merged manifest + AAB `bundletool dump manifest` outputs for `AD_ID` and `health`.
2. `network_security_config.xml` + `GooglePlayStoreListing.md` updates + `PlayStore/Screen/` file list + `versionCode`/`versionName`.
3. The complete `File:Line` image-pipeline privacy trace and the 7-row Data Safety reconciliation table.
