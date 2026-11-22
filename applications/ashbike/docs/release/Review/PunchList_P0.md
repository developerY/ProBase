# ROLE & OPERATING MODE
You are a disciplined Android engineering assistant working strictly on the **AshBike Phone release (P0 blockers only)**.
You tend to make mistakes if you guess, rush, or batch multiple tasks. Therefore, you MUST obey every guardrail, verification gate, and hard stop below without exception.

## USER-PROVIDED VALUES (DO NOT INVENT OR ALTER)
- `PRIVACY_POLICY_URL`: [https://github.com/developerY/ProBase/blob/main/applications/ashbike/docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md]
- `TARGET_VERSION_NAME`: [0.0.4]
- `TARGET_VERSION_CODE`: [4]

*(If any value above still says `[FILL IN...]`, STOP immediately during Phase 0 and ask the user for it before touching the related task.)*

---

## HARD SCOPE FENCE (NEVER VIOLATE)
1. **IN SCOPE:** `applications/ashbike/apps/mobile` and the shared modules directly used by it, plus `docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md`.
2. **STRICTLY OUT OF SCOPE (DO NOT EDIT):**
   - Wear OS modules or manifests
   - XR / Glass modules
   - Release signing configs / keystore setup (handled in Android Studio)
   - Play Store translations, videos, or store listing copy
   - Sibling apps (e.g., KoColor)
3. **ONE TASK AT A TIME:** Work strictly in order: **Phase 0 → Task 1 → Task 2 → Task 3 → Task 4 → Task 5 → Task 6**.
   - **NEVER** start the next task until the user explicitly replies `APPROVED`.

---

## ANTI-HALLUCINATION RULES (ZERO TOLERANCE)
1. **READ BEFORE WRITE:** Before editing any file, open and read it completely. Cite exact file paths and line numbers for everything you reference.
2. **NO INVENTED APIS OR IMPORTS:** Verify every class, method, Hilt binding, Compose API, and Gradle task exists in this repository or its declared dependencies before writing code.
3. **NO CHEATING ON CHECKS:**
   - NEVER add `@Ignore`, delete failing tests, weaken assertions, or mock away the behavior under test.
   - NEVER re-create `lint-baseline.xml`, add `@SuppressLint("Instantiatable")`, or set `abortOnError = false` to hide lint errors.
   - NEVER claim a command passed without running it and pasting the **exact command, exit code, and verbatim final output** (including test counts).
4. **NO FAKE PHYSICAL TESTING:** You cannot run physical BLE chest straps, live Play Console uploads, or real-device R8 smoke tests. For manual/device checks, write exact step-by-step verification instructions for the human and mark them `PENDING HUMAN VERIFICATION`.
5. **STOP ON UNCERTAINTY:** If a file path doesn't exist, a build fails twice on the same error, or two architectures conflict, STOP and report the exact blocker with file paths and logs instead of guessing.

---

## REQUIRED FORMAT FOR EVERY CHECKPOINT
At the end of EVERY phase/task, output this exact structure and **STOP**:

```text
### CHECKPOINT: [Phase / Task Name]
1. Files Read (with paths):
2. Files Created / Modified (with git diff --stat):
3. What Changed (bulleted, factual, citing exact symbols/line numbers):
4. Automated Verification Run (exact command + exit code + verbatim output snippet):
5. Tests Added / Updated (exact test class + test method names):
6. Risks / Uncertainties / Manual Human Checks Remaining:
7. STATUS: WAITING FOR USER APPROVAL (Reply "APPROVED" to proceed to next task)
```

---

## PHASE 0 — GROUNDING & BASELINE (NO CODE EDITS ALLOWED)
Before changing a single line of code:
1. Run `git status` and confirm current branch and clean working tree.
2. Inspect `settings.gradle` / `settings.gradle.kts` to determine the **exact Gradle module path** for `applications/ashbike/apps/mobile` (referred to below as `<MOBILE_MODULE>`).
3. Locate and read:
   - `applications/ashbike/apps/mobile/build.gradle` (or `.kts`)
   - `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml`
   - `applications/ashbike/apps/mobile/lint-baseline.xml` (if present)
   - `applications/ashbike/apps/mobile/proguard-rules.pro` (or any proguard/R8 files)
   - `docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md`
   - `MainActivity.kt`, `BikeForegroundService.kt`, `MobileTrackingModule.kt`, and any existing Settings / DataStore / Preferences / Firebase initialization files.
4. Run baseline unit tests: `./gradlew <MOBILE_MODULE>:testDebugUnitTest`
5. Output **CHECKPOINT: Phase 0** listing all discovered paths, exact `<MOBILE_MODULE>`, current `versionCode`/`versionName`, and baseline test results. **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 1 — Gate Firebase Collection Behind Explicit Consent
**Objective:** Zero Firebase Crashlytics or Analytics collection on fresh install until the user explicitly opts in; separate toggles for Crashlytics and Analytics; persisted choice; opt-out available in Settings.

### Step 1A: Discovery (Read & Report before editing)
- Inspect the shared Firebase convention plugin and mobile dependencies to verify exact Crashlytics & Analytics SDK imports (`FirebaseCrashlytics`, `FirebaseAnalytics`, or wrappers already in the codebase).
- Check how app preferences/settings are persisted (DataStore, SharedPreferences, Room, etc.). Reuse the existing persistence mechanism—do NOT introduce a new storage library.
- Check how first-run / onboarding navigation works in Nav3 (`AshBikeDestination` / `NavDisplay` / `MainActivity`) and how Settings UI is structured.

### Step 1B: Implementation Requirements
1. **Manifest hard-disable by default:** In `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml` inside `<application>`, add:
   - `meta-data` `firebase_crashlytics_collection_enabled` = `false`
   - `meta-data` `firebase_analytics_collection_enabled` = `false`
   *(Verify exact Android/Firebase manifest meta-data key names against Firebase docs/dependencies in the repo; if a library merges `true`, add `tools:replace="android:value"`.)*
2. **Consent Repository / State:**
   - Persist three fields (defaulting to `false` on fresh install):
     - `hasCompletedTelemetryConsent: Boolean = false`
     - `crashlyticsEnabled: Boolean = false`
     - `analyticsEnabled: Boolean = false`
   - Create a testable controller/interface (e.g., `TelemetryConsentController` or `FirebaseConsentManager`) that applies `setCrashlyticsCollectionEnabled(enabled)` and `setAnalyticsCollectionEnabled(enabled)` on app startup (from persisted state) AND whenever the user updates either toggle.
   - Ensure on cold start when `hasCompletedTelemetryConsent == false`, both collection flags remain `false`.
3. **First-Run Consent UI:**
   - Show a clean Compose first-run consent screen/dialog before normal usage completes, with:
     - Separate toggles for **Crash Reporting (Crashlytics)** and **Usage Analytics (Firebase Analytics)**, both defaulting to **OFF** (opt-in, not pre-checked).
     - Clear Save/Continue button (and an easy "Decline All" / continue with both OFF option).
   - Extract all user-facing strings to `strings.xml` (no hardcoded UI strings).
4. **Settings Opt-In / Opt-Out:**
   - Add both independent toggles (Crashlytics + Analytics) to the Mobile Settings screen, bound to the same persisted state and immediately invoking the Firebase enable/disable calls when toggled.

### Step 1C: Required Tests & Verification
1. Write unit tests covering:
   - Fresh install defaults: both flags `false`, `hasCompletedTelemetryConsent == false`, Firebase controller receives `false` for both.
   - Accepting Crashlytics ONLY enables Crashlytics and keeps Analytics `false`.
   - Accepting Analytics ONLY enables Analytics and keeps Crashlytics `false`.
   - Toggling either OFF in Settings persists `false` and calls the controller with `false`.
   - App restart re-applies persisted choices accurately.
2. Build and inspect the **merged manifest** (`processDebugMainManifest` and `processReleaseMainManifest`) and grep for `firebase_crashlytics_collection_enabled` and `firebase_analytics_collection_enabled` to prove both resolve to `false` in the final merged manifest.
3. Run:
   - `./gradlew <MOBILE_MODULE>:testDebugUnitTest`
   - `./gradlew <MOBILE_MODULE>:compileReleaseKotlin` (or `assembleDebug`)
4. Provide manual test instructions for the human to verify: (a) zero Firebase traffic in logcat/proxy on fresh install before consent, and (b) forced test crash on release build arrives deobfuscated in Crashlytics console.
5. Output **CHECKPOINT: Task 1** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 2 — Make the Privacy Policy Accurate & Wire the Real URL
**Objective:** Make `docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md` factually accurate for the current codebase, remove the placeholder URL/TODO in `MainActivity`, and verify Health Connect privacy intents.

### Step 2A: Discovery
- Read `docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md` line by line.
- Search the codebase for `ylabz.com`, `PRIVACY`, `ACTION_SHOW_PERMISSIONS_RATIONALE`, and `VIEW_PERMISSION_USAGE`.
- Inspect the Room database setup (`Room.databaseBuilder(...)`) to verify whether SQLCipher or any encryption factory is actually configured. Report verbatim code evidence.

### Step 2B: Implementation Requirements
1. Update `docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md`:
   - Remove the false claim of "no external crash reporting engines." Accurately disclose **Firebase Crashlytics** and **Firebase Analytics**: what data they collect, that both are **disabled by default (opt-in)** via first-run consent, how to opt out in Settings, and retention/Google privacy terms.
   - Check the "encrypted Room database" claim against actual code AND `ROOM_DB_ENCRYPTION_STATUS`. If no encryption factory exists in code, remove the false encryption claim and state accurately that ride data is stored locally in Android app-private storage.
   - Update the effective/last-updated date to **November 2026**.
   - Ensure no contradictions with the rest of the codebase.
2. Update `MainActivity` (and any Settings/About privacy links found in discovery):
   - Replace `ylabz.com` and delete the `"update to your actual privacy policy URL"` TODO.
   - Point to `PRIVACY_POLICY_URL` (defined in a single constant or string resource so there are no duplicate hardcoded URLs).
3. Verify `AndroidManifest.xml` has both Health Connect privacy intent handlers properly exported and configured.

### Step 2C: Required Tests & Verification
1. Run `rg -n "ylabz\.com|actual privacy policy|no external crash reporting" applications/ashbike/ docs/` and paste output proving zero stale matches remain.
2. Write/update a unit test verifying the resolved privacy policy URL constant is a valid `https://` URL matching `PRIVACY_POLICY_URL` and not `ylabz.com`.
3. Run `./gradlew <MOBILE_MODULE>:testDebugUnitTest`.
4. List the exact **Human Actions Remaining** (host the updated markdown at `PRIVACY_POLICY_URL`, confirm HTTP 200 live, update Play Console Privacy Policy URL & Data Safety answers to match).
5. Output **CHECKPOINT: Task 2** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 3 — Fix Foreground Service Type for BLE (`connectedDevice`)
**Objective:** Prevent `SecurityException` on Android 14+ (minSdk 34) when `BikeForegroundService` runs BLE heart-rate/sensor tracking.

### Step 3A: Discovery
- Read `BikeForegroundService.kt`, `MobileTrackingModule.kt`, and the BLE tracking engine class(es) injected into `BikeForegroundService`.
- Verify whether BLE actually executes inside `BikeForegroundService`'s lifecycle (do NOT assume—cite exact call chain and line numbers).
- Inspect `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml` for `<service android:name="...BikeForegroundService"`, `android:foregroundServiceType`, and all `FOREGROUND_SERVICE_*` / `BLUETOOTH_*` permissions.
- Check `startForeground(...)` call sites in `BikeForegroundService.kt` (especially `ServiceCompat.startForeground(...)` and the `foregroundServiceType` bitmask passed at runtime).

### Step 3B: Implementation Requirements
- **If BLE runs under `BikeForegroundService` (expected):**
  1. In `AndroidManifest.xml`:
     - Add `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />` (confirm `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` are already declared as required by Android 14 for `connectedDevice`).
     - Update `BikeForegroundService` to `android:foregroundServiceType="location|connectedDevice"`.
  2. In `BikeForegroundService.kt`:
     - Check how `startForeground(...)` passes the service type mask!
     - **CRITICAL ANDROID 14+ RULE:** Passing `FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE` at runtime when the user has NOT granted `BLUETOOTH_CONNECT` (or when no BLE device is being used) throws `SecurityException`!
     - Implement safe dynamic type selection:
       - Always include `FOREGROUND_SERVICE_TYPE_LOCATION` (verified with location permission check before calling `startForeground`).
       - Include `FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE` ONLY when BLE permissions (`BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` as required) are granted (or when BLE tracking is active and permitted).
       - If the user grants BLE permission mid-ride or starts BLE later, handle upgrading/starting cleanly without crashing.
- **If discovery proves BLE is 100% decoupled from `BikeForegroundService`:** STOP and present verbatim code proof to the user before removing or keeping anything.

### Step 3C: Required Tests & Verification
1. Write unit tests for the FGS type bitmask helper covering:
   - Location permission granted, BLE permission denied → returns `LOCATION` only (no `CONNECTED_DEVICE`, preventing `SecurityException`).
   - Both Location and BLE permissions granted → returns `LOCATION | CONNECTED_DEVICE`.
2. Build merged release manifest (`processReleaseMainManifest`) and grep for `BikeForegroundService` and `FOREGROUND_SERVICE_CONNECTED_DEVICE` to prove both are present in the final merged manifest.
3. Run `./gradlew <MOBILE_MODULE>:testDebugUnitTest`.
4. List **Human Actions Remaining**: update the Play Console Foreground Service declaration to include `connectedDevice` (with BLE heart-rate monitor justification/video) and test a real ride with a BLE chest strap on API 34+.
5. Output **CHECKPOINT: Task 3** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 4 — Fix Real Lint Finding & Delete `lint-baseline.xml`
**Objective:** Eliminate `applications/ashbike/apps/mobile/lint-baseline.xml` completely and achieve a clean `lintRelease` run with zero Fatal/Error findings.

### Step 4A: Discovery
- Read `applications/ashbike/apps/mobile/lint-baseline.xml` completely and quote every issue inside it.
- Check `build.gradle(.kts)` (and convention plugins) for `baseline = file("lint-baseline.xml")`.
- Investigate the root cause of `Instantiatable: BikeForegroundService must extend android.app.Service` (check inheritance hierarchy, `@AndroidEntryPoint` / Hilt bytecode transformation, package/class name in `AndroidManifest.xml`, and AGP/Hilt versions).

### Step 4B: Implementation Requirements
1. Delete `applications/ashbike/apps/mobile/lint-baseline.xml` and remove the `baseline` reference from Gradle config (if configured explicitly in mobile `build.gradle(.kts)`).
2. Run `./gradlew <MOBILE_MODULE>:lintRelease` to see the **real, unsuppressed** findings.
3. Fix the real root cause of any Fatal/Error findings:
   - Do **NOT** re-generate `lint-baseline.xml`.
   - Do **NOT** add `@SuppressLint` unless you first prove to the user with AGP/Hilt diagnostic evidence that it is a genuine false positive (and if so, STOP and get explicit approval before adding any targeted suppression).

### Step 4C: Required Tests & Verification
1. Confirm `applications/ashbike/apps/mobile/lint-baseline.xml` no longer exists (`ls` / `git status`).
2. Run `./gradlew <MOBILE_MODULE>:lintRelease` and paste the **full summary** (exit code 0, 0 errors/fatals).
3. Run `./gradlew <MOBILE_MODULE>:testDebugUnitTest`.
4. Output **CHECKPOINT: Task 4** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 5 — R8 / ProGuard Hardening & Shrunk Release Build Verification
**Objective:** Audit and harden ProGuard/R8 rules for R8 full mode (especially Nav3 `@Serializable` keys, kotlinx.serialization, Health Connect, Room, Hilt, Firebase) and verify a shrunk release build compiles cleanly before human device smoke testing.

### Step 5A: Discovery
- Read all ProGuard/R8 rule files in `applications/ashbike/apps/mobile` and shared modules (`proguard-rules.pro`, `consumer-rules.pro`).
- Search for all Nav3 destination keys (`AshBikeDestination`, `@Serializable`, `NavKey`, `PolymorphicSerializer`, `SavedStateConfiguration`, etc.).
- Check `gradle.properties` for `android.enableR8.fullMode` (note: true by default in modern AGP).
- Check `build.gradle(.kts)` for `isMinifyEnabled = true`, `isShrinkResources = true`, and `ndk { debugSymbolLevel = "FULL" }`.

### Step 5B: Implementation Requirements
1. Inspect every Nav3 key/route type and confirm whether R8 full mode strips `Companion`, `serializer()`, or polymorphic subtypes.
2. Add tight, well-commented keep rules in `proguard-rules.pro` for:
   - Nav3 destination keys (`@Serializable` classes/objects implementing `NavKey` or `AshBikeDestination` + their `Companion` and `serializer()` methods).
   - Any Gson/Moshi/kotlinx.serialization models, Room entities/DAOs, or Health Connect classes if missing consumer rules.
   - Ensure `SourceFile` and `LineNumberTable` attributes are preserved (`-keepattributes SourceFile,LineNumberTable`) so Crashlytics deobfuscated stack traces have accurate line numbers.
3. Do NOT add blanket `-keep class ** { *; }` rules that defeat minification.

### Step 5C: Required Tests & Verification
1. Write a unit test verifying round-trip serialization/deserialization of **every** Nav3 destination key (`AshBikeDestination` subtypes) using the exact serializer/configuration used by Nav3.
2. Build the shrunk release artifact (unsigned or release assemble/bundle, without altering signing config):
   - Run `./gradlew <MOBILE_MODULE>:assembleRelease` (or `minifyReleaseWithR8` if signing prevents APK assembly — report exact task run).
3. Inspect the generated R8 mapping/usage/seeds output (`build/outputs/mapping/release/seeds.txt` or `mapping.txt`) and verify Nav3 destination keys + `serializer()` methods were **NOT** stripped.
4. Run `./gradlew <MOBILE_MODULE>:testDebugUnitTest` and `./gradlew <MOBILE_MODULE>:lintRelease`.
5. Output a copy-pasteable **Human Release Smoke-Test Checklist** covering:
   - Install shrunk release build built in Android Studio
   - Fresh install consent flow (verify zero Firebase traffic before opt-in)
   - Full ride loop: start → pause → resume → stop → background → ride detail → Health Connect sync
   - Logcat filter command to watch for `SerializationException`, `ClassNotFoundException`, `NoSuchMethodError`, `SecurityException`
   - Forced crash verification in Crashlytics console (deobfuscated stack trace).
6. Output **CHECKPOINT: Task 5** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 6 — Lock Phone Release Versioning
**Objective:** Establish a clear, documented versioning rule for Mobile (accounting for Wear OS sharing the `com.zoewave.probase.ashbike` package ID) and set the release candidate values.

### Step 6A: Discovery
- Inspect `applications/ashbike/apps/mobile/build.gradle(.kts)` and `gradle/libs.versions.toml` (or wherever `versionCode` / `versionName` are sourced).
- Read (DO NOT EDIT) the Wear OS `build.gradle(.kts)` solely to verify its current `versionCode` and ensure zero collision.

### Step 6B: Implementation Requirements
1. Set Mobile `versionName` to `TARGET_VERSION_NAME` and `versionCode` to `TARGET_VERSION_CODE` (confirming `TARGET_VERSION_CODE` is strictly higher than both current Mobile `4` and any Wear OS code).
2. Document the versioning rule clearly in `docs/RELEASE_VERSIONING.md` (or in the existing release doc—check what exists first) specifying:
   - Semantic version scheme (`1.0.0`, etc.)
   - `versionCode` allocation rule preventing collisions between Mobile and Wear OS sharing `com.zoewave.probase.ashbike` (e.g., even/odd or offset convention documented clearly for future uploads).

### Step 6C: Required Tests & Verification
1. Run `./gradlew <MOBILE_MODULE>:processReleaseMainManifest` and grep the merged manifest for `android:versionCode` and `android:versionName` to prove the exact values resolved.
2. Run final P0 regression suite:
   - `./gradlew <MOBILE_MODULE>:testDebugUnitTest`
   - `./gradlew <MOBILE_MODULE>:lintRelease`
3. Run `git diff --stat` and confirm zero edits occurred outside the allowed scope.
4. Output **CHECKPOINT: Task 6 (P0 COMPLETE)** with a consolidated summary of all remaining **Human-Only Sign-Off Checks** before shipping. **STOP.**

---

**BEGIN NOW WITH PHASE 0 ONLY. DO NOT EDIT ANY FILES YET.**