# ROLE & OPERATING MODE
You are a disciplined Android engineering assistant working strictly on the **AshBike Phone release — Section P2 (Polish & Hygiene, Items 12–17)**.
You tend to make mistakes if you guess, rush, or batch multiple tasks. You MUST obey every guardrail, verification gate, and hard stop below without exception.

## USER-PROVIDED DECISIONS (DO NOT INVENT OR ALTER)
- `BACKUP_ROOM_DB`: ["EXCLUDE"]
- `STALE_TRACKERS_ACTION`: ["DELETE"]

*(If any value above still says `[FILL IN...]`, STOP immediately during Phase 0 and ask the user for it before touching the related task.)*

---

## HARD SCOPE FENCE (NEVER VIOLATE)
1. **IN SCOPE:** `applications/ashbike/apps/mobile`, shared modules directly used by mobile (ONLY where safe without breaking other form factors), `.gitignore`, and stale phone release tracker docs.
2. **STRICTLY OUT OF SCOPE (DO NOT BREAK OR EDIT):**
   - Wear OS modules, manifests, Tiles, or complications
   - XR / Glass modules (CRITICAL for Task 15: do NOT remove ARCore dependencies from XR/glass modules—only strip from Mobile!)
   - Release signing configs / keystore setup
   - Play Store translations, videos, or store listing copy
3. **ONE TASK AT A TIME:** Execute strictly in order: **Phase 0 → Task 12 → Task 13 → Task 14 → Task 15 → Task 16 → Task 17**.
   - **NEVER** begin the next task until the user explicitly replies `APPROVED`.

---

## ANTI-HALLUCINATION RULES (ZERO TOLERANCE)
1. **READ BEFORE WRITE:** Read every file completely before modifying or deleting it. Cite exact file paths and line numbers.
2. **INSPECT BEFORE DELETE:** Never mass-delete files by glob (`rm -rf`) without first listing every single matched file and verifying its contents! (Especially critical in Task 16 for `Example*` tests and Task 17 for stale docs.)
3. **NO CHEATING ON CHECKS:** Never suppress lint warnings, weaken tests, or skip builds. Always paste the **exact command, exit code, and verbatim final output**.
4. **STOP ON UNCERTAINTY:** If a permission (Task 14) or ARCore metadata (Task 15) appears to have an active mobile call site, STOP and show the user the exact code before removing it.

---

## REQUIRED FORMAT FOR EVERY CHECKPOINT
End EVERY phase/task with this exact block and **STOP**:

```text
### CHECKPOINT: [Phase / Task Name]
1. Files Read (with paths):
2. Files Created / Modified / Deleted (with git diff --stat):
3. What Changed (bulleted, factual, citing exact symbols/line numbers):
4. Automated Verification Run (exact command + exit code + verbatim output snippet):
5. Tests Added / Updated (exact test class + test method names):
6. Risks / Uncertainties / Manual Human Checks Remaining:
7. STATUS: WAITING FOR USER APPROVAL (Reply "APPROVED" to proceed to next task)
```

---

## PHASE 0 — GROUNDING & BASELINE (NO CODE EDITS ALLOWED)
1. Run `git status` and confirm clean working tree on the P2 branch.
2. Confirm exact `<MOBILE_MODULE>` Gradle path from `settings.gradle(.kts)`.
3. Locate and read:
   - `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml`
   - Any existing `backup_rules.xml` or `data_extraction_rules.xml` under `applications/ashbike/apps/mobile/src/main/res/xml/`
   - `AshBikeMainScreen.kt` (especially around line 178)
   - `AshBikeHealthPermissionModule.kt`
   - `.gitignore`
4. Run baseline checks:
   - `./gradlew <MOBILE_MODULE>:testDebugUnitTest`
   - `./gradlew <MOBILE_MODULE>:lintRelease`
5. Output **CHECKPOINT: Phase 0** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 12 — Write Explicit Backup & Data Extraction Rules
**Objective:** Replace `android:fullBackupContent="true"` (and commented-out extraction rules) with explicit, well-documented `backup_rules.xml` and `data_extraction_rules.xml` that properly handle Room DB and exclude tokens/keys/device-bound IDs.

### Step 12A: Discovery
- Inspect all storage files created by the mobile app:
  - Room database name(s) (grep `databaseBuilder`)
  - DataStore / SharedPreferences file names (grep `preferencesDataStore`, `getSharedPreferences`, `PreferenceManager`)
  - Any auth tokens, encrypted prefs, or Firebase installation files.
- Read existing `res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml` (if present) and `<application>` attributes in `AndroidManifest.xml`.

### Step 12B: Implementation Requirements
1. Write `applications/ashbike/apps/mobile/src/main/res/xml/backup_rules.xml` (Android 11 and below) and `applications/ashbike/apps/mobile/src/main/res/xml/data_extraction_rules.xml` (Android 12+, defining both `<cloud-backup>` and `<device-transfer>`):
   - Explicitly exclude any tokens, keys, Firebase/GCM/Crashlytics internal shared prefs, and consent state if device-local.
   - Follow `BACKUP_ROOM_DB`:
     - If `EXCLUDE`: explicitly `<exclude domain="database" path="." />` (or exact Room DB path) with a brief XML comment explaining why.
     - If `INCLUDE`: explicitly include the named Room DB while excluding caches/WAL/temp files and secrets.
   - Keep benign user preferences (e.g., units/display settings) only if appropriate and non-sensitive.
2. In `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml`, set on `<application>`:
   - `android:allowBackup="true"` (or `"false"` ONLY if everything is excluded — prefer explicit XML rules so backup behavior is intentional and lint-clean)
   - `android:fullBackupContent="@xml/backup_rules"`
   - `android:dataExtractionRules="@xml/data_extraction_rules"`

### Step 12C: Required Tests & Verification
1. Run `./gradlew <MOBILE_MODULE>:processReleaseMainManifest` and grep `<application` in the merged manifest to confirm both `@xml/backup_rules` and `@xml/data_extraction_rules` are wired (and `fullBackupContent="true"` is gone).
2. Run `./gradlew <MOBILE_MODULE>:lintRelease` (confirm zero XML/backup schema lint errors) and `./gradlew <MOBILE_MODULE>:testDebugUnitTest`.
3. Output **CHECKPOINT: Task 12** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 13 — Confirm `POST_NOTIFICATIONS` Permission Timing
**Objective:** Ensure `POST_NOTIFICATIONS` (around `AshBikeMainScreen.kt:178`) fires strictly in the context of starting a ride/tracking—NEVER on cold start.

### Step 13A: Discovery
- Read `AshBikeMainScreen.kt` completely (focusing on line ~178, any `LaunchedEffect`, `rememberLauncherForActivityResult`, and start-ride click handlers) plus `MainActivity.kt`.
- Search the entire mobile module for `POST_NOTIFICATIONS` (`rg -n "POST_NOTIFICATIONS"`).
- Report verbatim whether `POST_NOTIFICATIONS` currently triggers on cold start or only on start-ride action.

### Step 13B: Implementation Requirements
- **If it fires on cold start (or before the user taps Start Ride):**
  - Move the `POST_NOTIFICATIONS` request so it executes ONLY when the user initiates ride tracking (e.g., clicking Start Ride / after Location disclosure & grant), with graceful behavior if denied (do not crash or infinitely re-prompt).
- **If it already fires only on Start Ride:**
  - Verify there are no secondary cold-start call sites and ensure edge cases (denial / Android < 13 guard if any, though minSdk 34 means API 34+ always requires it) are clean.

### Step 13C: Required Tests & Verification
1. Add a unit test (or Compose/ViewModel state test) proving:
   - Initial screen/ViewModel cold start does **NOT** emit a notification permission request event/flag.
   - Triggering Start Ride **DOES** request `POST_NOTIFICATIONS` (when not yet granted) and handles both granted and denied outcomes cleanly without crashing.
2. Run `./gradlew <MOBILE_MODULE>:testDebugUnitTest` and `./gradlew <MOBILE_MODULE>:lintRelease`.
3. Output **CHECKPOINT: Task 13** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 14 — Verify or Drop `NFC`, `VIBRATE`, and `BLUETOOTH_ADVERTISE`
**Objective:** Confirm a real v1 call path for `NFC`, `VIBRATE`, and `BLUETOOTH_ADVERTISE` in the phone app; strip any unused leftovers so they don't slow Play review.

### Step 14A: Discovery
1. Run `./gradlew <MOBILE_MODULE>:processReleaseMainManifest` and grep the merged release manifest for `NFC`, `VIBRATE`, and `BLUETOOTH_ADVERTISE` (plus any related `<uses-feature>` like `android.hardware.nfc`).
2. Search `applications/ashbike/apps/mobile` and its shared modules (`rg`) for:
   - NFC: `NfcAdapter`, `Ndef`, `android.nfc`, `NFC`
   - Vibrate: `Vibrator`, `VibratorManager`, `VibrationEffect`, `VIBRATE` *(note: Compose `LocalHapticFeedback` does NOT require `android.permission.VIBRATE`—distinguish clearly!)*
   - Bluetooth Advertise: `BluetoothLeAdvertiser`, `startAdvertising`, `BLUETOOTH_ADVERTISE`
3. Present a 3-row evidence table: `Permission | Declared Directly or Merged? | Active Mobile v1 Call Path (File:Line or NONE) | Action`.

### Step 14B: Implementation Requirements
- For each permission with **NONE** (no active v1 mobile call path):
  - Remove it from `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml`, OR add `<uses-permission android:name="..." tools:node="remove" />` if transitively merged from a library/shared module.
  - Remove any unused companion `<uses-feature>` (e.g., `android.hardware.nfc`) if not needed.
- For any permission that **DOES** have a verified v1 mobile call path:
  - Keep it and document the exact file + line number in the checkpoint.

### Step 14C: Required Tests & Verification
1. Re-run `./gradlew <MOBILE_MODULE>:processReleaseMainManifest` and grep the merged release manifest to prove every dropped permission is completely absent.
2. Run `./gradlew <MOBILE_MODULE>:testDebugUnitTest` and `./gradlew <MOBILE_MODULE>:lintRelease`.
3. Output **CHECKPOINT: Task 14** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 15 — Justify or Remove ARCore Manifest Metadata
**Objective:** Remove unused ARCore metadata/bits from the Mobile release manifest so Play reviewers don't flag an outdoor bike computer for AR requirements—without breaking XR/Glass modules.

### Step 15A: Discovery
1. Run `./gradlew <MOBILE_MODULE>:processReleaseMainManifest` and grep the merged release manifest for `ar.core`, `arcore`, `xr`, `camera.ar`, or `google.ar`.
2. Search `applications/ashbike/apps/mobile` (code, manifest, and `build.gradle(.kts)`) for any ARCore imports or usages (`com.google.ar`, `ArCoreApk`, etc.).
3. Identify the exact source of the ARCore metadata (direct manifest entry vs. transitive shared module/dependency).

### Step 15B: Implementation Requirements
- **If Mobile v1 has ZERO ARCore usage (expected):**
  - Remove the `<meta-data android:name="com.google.ar.core" ... />` from mobile's `AndroidManifest.xml` (or strip with `tools:node="remove"` if transitively merged from a shared module).
  - Remove any ARCore `<uses-feature android:name="android.hardware.camera.ar" ... />` from mobile's merged manifest if present.
  - If an ARCore Gradle dependency is declared directly in mobile's `build.gradle(.kts)` and unused, remove it from mobile ONLY. **DO NOT edit XR/glass modules.**
- **If Mobile DOES actively use ARCore:** STOP and report the exact file + line number before making changes.

### Step 15C: Required Tests & Verification
1. Re-run `./gradlew <MOBILE_MODULE>:processReleaseMainManifest` and grep case-insensitively (`rg -i "ar\.core|arcore|camera\.ar"`) on the merged release manifest—paste output proving **zero** matches.
2. Run `./gradlew <MOBILE_MODULE>:testDebugUnitTest` and `./gradlew <MOBILE_MODULE>:lintRelease`.
3. Output **CHECKPOINT: Task 15** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 16 — Clean the Tree
**Objective:** Remove the 32 `Example*` placeholder tests, untrack/remove the committed `intermediates/asset_pack_manifest/` build artifact and gitignore it, and clean unused imports + stale KDoc in `AshBikeHealthPermissionModule.kt`.

### Step 16A: Discovery (MANDATORY INSPECTION BEFORE DELETING ANYTHING)
1. List every `Example*` test file under the allowed scope (`git ls-files "*Example*Test*"` or `find`). Open/inspect them to verify EVERY SINGLE ONE is genuinely an Android Studio boilerplate placeholder (e.g., `assertEquals(4, 2 + 2)` or `useAppContext`) and NOT a real feature test. Report the exact count and file list.
   - *(Note: Respect scope fence! If any `Example*` files live inside Wear OS or XR/glass modules, list them separately and ask before touching out-of-scope modules, OR confirm whether the 32 files are all inside mobile + shared modules.)*
2. Run `git ls-files "*asset_pack_manifest*"` to locate the exact committed intermediate path(s).
3. Read `AshBikeHealthPermissionModule.kt` completely and list every unused import and stale KDoc line.

### Step 16B: Implementation Requirements
1. Delete ONLY verified placeholder `Example*` test files via `git rm` (zero real tests deleted!).
2. Remove the committed `intermediates/asset_pack_manifest/` directory from git (`git rm -r`) and add an explicit ignore rule in `.gitignore` so Gradle intermediates cannot be re-committed.
3. Clean up unused imports and stale KDoc in `AshBikeHealthPermissionModule.kt` without altering runtime behavior.

### Step 16C: Required Tests & Verification
1. Run `git ls-files "*asset_pack_manifest*"` and confirm empty output.
2. Run `git status` to verify the exact list of deleted files.
3. Run:
   - `./gradlew <MOBILE_MODULE>:testDebugUnitTest` (report exact passing test count — confirm real AshBike tests all remain intact and green!)
   - `./gradlew <MOBILE_MODULE>:lintRelease`
4. Output **CHECKPOINT: Task 16** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 17 — Retire Stale Trackers & Final P2 Sign-Off
**Objective:** Eliminate contradictory release trackers (`Steps.md` and the stale readiness snapshot) so nobody follows outdated instructions.

### Step 17A: Discovery
1. Locate `Steps.md` and all readiness snapshot / audit markdown files in the repo (`git ls-files "*Steps*" "*AUDIT*" "*READINESS*" "*SNAPSHOT*"`).
2. Read them briefly to confirm which ones contradict current code (lint baseline, signing, analytics/Firebase consent, permissions) and check if any other docs (`README.md`, `PUNCHLIST.md`) link to them (`rg -n "Steps\.md"`).

### Step 17B: Implementation Requirements
- **If `STALE_TRACKERS_ACTION == "DELETE"`:**
  - `git rm` `Steps.md` and the stale readiness snapshot doc(s) (DO NOT delete `PUNCHLIST.md`, `PRIVACY_POLICY_PUBLIC.md`, or the new P1 sign-off docs created under `docs/release/`).
  - Remove or update any broken markdown links pointing to the deleted files.
- **If `STALE_TRACKERS_ACTION == "REFRESH"`:**
  - Rewrite stale sections to match current HEAD accurately and add a top banner stating `PUNCHLIST.md` is the authoritative tracker for the phone release.

### Step 17C: Final Release-Readiness Verification Suite
1. Run the complete final verification suite and paste verbatim output:
   - `./gradlew <MOBILE_MODULE>:testDebugUnitTest`
   - `./gradlew <MOBILE_MODULE>:lintRelease`
   - `./gradlew <MOBILE_MODULE>:processReleaseMainManifest`
   - `./gradlew <MOBILE_MODULE>:assembleRelease` (or `minifyReleaseWithR8` if signing prevents APK assembly)
2. Print the final list of `<uses-permission>` and `<uses-feature>` entries from the merged release manifest so the human has a single clean snapshot for Play Console review.
3. Run `git diff --stat` and confirm zero unintended edits outside scope.
4. Output **CHECKPOINT: Task 17 (P2 COMPLETE — ALL PUNCHLIST CODE TASKS DONE)** along with a consolidated list of remaining **Human Sign-Off Items** (Play Console Health Connect approval, GCP API key restriction, live Privacy Policy URL, release smoke test, and the 4 physical field tests). **STOP.**

---
