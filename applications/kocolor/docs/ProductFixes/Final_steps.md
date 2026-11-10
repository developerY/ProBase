# AGENT MISSION: Fix the Last 2 Open Code Blockers (Strict File Inspection & Gradle Proof — NO GIT COMMANDS)

STOP AND READ THIS CAREFULLY:
Your previous claim that everything was finished was **FALSE**. Inspection of the files on disk revealed two items you did NOT fix:
1. `applications/kocolor/apps/mobile/build.gradle.kts:44` STILL has the silent `?: "keystore.jks"` fallback and has ZERO `GradleException` fail-fast guard.
2. `HealthUiRoute.kt:87` STILL launches Health Connect's permission-request contract, and `HealthConnectCard` is STILL reachable in Settings—even though all Health Connect permissions were removed from the manifest under Option A.

## STRICT OPERATING RULES (VIOLATION = IMMEDIATE FAILURE)
1. **ZERO GIT COMMANDS ALLOWED:** You are strictly forbidden from running ANY `git` command (`git`, `git status`, `git diff`, `git grep`, `git add`, `git commit`, `git checkout`, `git reset`, etc.). Do NOT invoke `git` under any circumstances. Use ONLY standard file-inspection tools (`nl`, `sed`, `grep -rn`, `find`) and `./gradlew`.
2. **YOU MUST EDIT THE FILES ON DISK:** Do NOT say "Done" or write a summary until you have modified the actual files on disk and pasted the exact line-numbered file contents (`nl -ba`) and terminal outputs required at each checkpoint.
3. **WORK ONE STEP AT A TIME:** Execute **Step 1**, run all **Checkpoint 1** terminal commands, and verify their output before touching **Step 2**.

---

## STEP 1: Fix the Fail-Fast Release Signing Guard in `apps/mobile/build.gradle.kts`

### 1A. Inspect the Current Broken Lines on Disk First (Mandatory Read)
Run this exact command in your terminal to print lines 25 to 75 of `applications/kocolor/apps/mobile/build.gradle.kts` with line numbers:
```bash
nl -ba applications/kocolor/apps/mobile/build.gradle.kts | sed -n '25,75p'
```
Locate line ~44 where `?: "keystore.jks"` (and any silent fallback for `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) is currently defined.

### 1B. Edit `applications/kocolor/apps/mobile/build.gradle.kts`
Modify `applications/kocolor/apps/mobile/build.gradle.kts` on disk so that:
1. **Delete `?: "keystore.jks"` completely.** Never fall back to `"keystore.jks"` or any default password/alias string.
2. Read the 4 credentials from environment variables (or `local.properties` if loaded, with NO fallback default literals):
   ```kotlin
   val releaseStoreFile = System.getenv("RELEASE_STORE_FILE")?.takeIf { it.isNotBlank() }
   val releaseStorePassword = System.getenv("RELEASE_STORE_PASSWORD")?.takeIf { it.isNotBlank() }
   val releaseKeyAlias = System.getenv("RELEASE_KEY_ALIAS")?.takeIf { it.isNotBlank() }
   val releaseKeyPassword = System.getenv("RELEASE_KEY_PASSWORD")?.takeIf { it.isNotBlank() }
   ```
3. **Add an Explicit `GradleException` Fail-Fast Guard for Release Signing Tasks:**
   Enforce that whenever any release build/signing task (`bundleRelease`, `assembleRelease`, `packageRelease`, `signReleaseBundle`) is requested:
   ```kotlin
   val isReleaseTaskRequested = gradle.startParameter.taskNames.any { taskName ->
       taskName.contains("Release", ignoreCase = true) &&
           !taskName.contains("lint", ignoreCase = true) &&
           !taskName.contains("UnitTest", ignoreCase = true) &&
           !taskName.contains("Manifest", ignoreCase = true)
   }

   if (isReleaseTaskRequested) {
       if (releaseStoreFile == null || releaseStorePassword == null || releaseKeyAlias == null || releaseKeyPassword == null) {
           throw GradleException(
               "FATAL: Missing required release signing credentials (RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD). Refusing to fall back to keystore.jks."
           )
       }
       if (!file(releaseStoreFile).exists()) {
           throw GradleException(
               "FATAL: RELEASE_STORE_FILE does not exist at path: $releaseStoreFile"
           )
       }
   }
   ```
4. Inside `signingConfigs.create("release")`, only assign `storeFile = file(releaseStoreFile)` when `releaseStoreFile != null` (so non-signing tasks like `testDebugUnitTest`, `assembleDebug`, `processReleaseMainManifest`, and `lintRelease` configure cleanly without crashing).
5. Ensure `buildTypes { getByName("release") { ... } }` still sets `signingConfig = signingConfigs.getByName("release")`, `isMinifyEnabled = true`, and `ndk { debugSymbolLevel = "SYMBOL_TABLE" }`.

### CHECKPOINT 1 (Run All 4 Commands & Paste Verbatim Output)
Do NOT proceed to Step 2 until all 4 commands below succeed:
```bash
# 1. Prove "keystore.jks" fallback is GONE from build.gradle.kts (only allowed inside the GradleException error message string, NEVER as a ?: fallback):
grep -n "keystore\.jks" applications/kocolor/apps/mobile/build.gradle.kts || echo "PASS: keystore.jks removed"

# 2. Print the exact updated lines 25 to 90 of build.gradle.kts from disk:
nl -ba applications/kocolor/apps/mobile/build.gradle.kts | sed -n '25,90p'

# 3. NEGATIVE TEST — Prove bundleRelease FAILS FAST with GradleException when env vars are unset:
env -u RELEASE_STORE_FILE -u RELEASE_STORE_PASSWORD -u RELEASE_KEY_ALIAS -u RELEASE_KEY_PASSWORD \
  ./gradlew :applications:kocolor:apps:mobile:bundleRelease 2>&1 | grep -E "FATAL:|GradleException|BUILD FAILED"

# 4. POSITIVE CONFIG TEST — Prove non-signing tasks (processReleaseMainManifest) still configure and run cleanly without env vars:
env -u RELEASE_STORE_FILE -u RELEASE_STORE_PASSWORD -u RELEASE_KEY_ALIAS -u RELEASE_KEY_PASSWORD \
  ./gradlew :applications:kocolor:apps:mobile:processReleaseMainManifest
```
*(Checkpoint 1.3 MUST print `FATAL: Missing required release signing credentials...` and `BUILD FAILED`, and Checkpoint 1.4 MUST print `BUILD SUCCESSFUL`!)*

---

## STEP 2: Remove/Gate the Orphaned Health Connect Runtime Flow (`HealthUiRoute.kt:87` & `HealthConnectCard` in Settings)

**Context:** We chose Option A (no Health Connect features in this release) and already stripped all Health Connect permissions from `AndroidManifest.xml`. However, `HealthUiRoute.kt:87` still registers and launches Health Connect's permission contract, and `HealthConnectCard` in Settings still lets users tap into that dead flow.

### 2A. Locate and Read the Exact Files on Disk First (Mandatory Read)
Run these commands (using `find` and `grep -rn`, NOT `git`) to locate `HealthUiRoute.kt` and every file referencing `HealthConnectCard`:
```bash
grep -rn "HealthUiRoute\|HealthConnectCard\|createRequestPermissionResultContract" applications/ features/ core/ --include="*.kt"
```
Then display lines 1 to 130 of `HealthUiRoute.kt` with line numbers:
```bash
HEALTH_ROUTE_FILE=$(find applications/ features/ core/ -name "HealthUiRoute.kt" | head -n 1)
echo "Found HealthUiRoute at: $HEALTH_ROUTE_FILE"
nl -ba "$HEALTH_ROUTE_FILE" | sed -n '1,130p'
```

### 2B. Edit `HealthUiRoute.kt` to Remove the Orphaned Permission Launcher
In `HealthUiRoute.kt` (around line 87):
1. Remove the Health Connect permission launcher registration (`rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract())` or `requestPermissions` contract) and remove the `.launch(...)` call at line ~87 so the app **never** requests undeclared Health Connect permissions at runtime.
2. Replace the action callback in `HealthUiRoute.kt` with a safe no-op (or navigate back) and remove any unused `PermissionController` imports.

### 2C. Remove or Gate `HealthConnectCard` from the Settings Screen
1. Open the Settings screen `.kt` file(s) returned by the `grep -rn "HealthConnectCard"` search in Step 2A and print the surrounding lines with `nl -ba`.
2. Remove the `HealthConnectCard(...)` call site from the Settings screen UI composition (or gate it off behind `private const val IS_HEALTH_CONNECT_ENABLED = false` so `HealthConnectCard` is never rendered or reachable by users in Settings).
3. If any unit test references `HealthUiRoute` or `HealthConnectCard`, update it so all tests compile and pass.

### CHECKPOINT 2 (Run All 4 Commands & Paste Verbatim Output)
Do NOT proceed to Step 3 until all 4 commands below succeed:
```bash
# 1. Prove createRequestPermissionResultContract is GONE across the codebase (MUST return 0 matches):
grep -rn "createRequestPermissionResultContract" applications/ features/ core/ --include="*.kt" || echo "PASS: Zero Health Connect permission contract launchers"

# 2. Print lines 45 to 110 of HealthUiRoute.kt from disk to prove line 87 no longer launches permissions:
HEALTH_ROUTE_FILE=$(find applications/ features/ core/ -name "HealthUiRoute.kt" | head -n 1)
nl -ba "$HEALTH_ROUTE_FILE" | sed -n '45,110p'

# 3. Inspect all references to HealthConnectCard across .kt files to prove it is removed or gated off in Settings:
grep -rn -C 4 "HealthConnectCard" applications/ features/ core/ --include="*.kt" || echo "PASS: HealthConnectCard completely removed"

# 4. Compile the modified UI/Settings and Health modules to prove zero syntax or unresolved reference errors:
./gradlew :applications:kocolor:apps:mobile:compileDebugKotlin
```

---

## STEP 3: Final Rebuild, Merged Release Manifest Audit & Full Test Suite Verification

Now that both code items have landed on disk, run the final verification sequence:

### 3A. Process the Release Merged Manifest & Verify ZERO `AD_ID` and ZERO `health` Entries
Run these exact commands and paste the verbatim output:
```bash
./gradlew :applications:kocolor:apps:mobile:processReleaseMainManifest --rerun-tasks

MERGED_MANIFEST=$(find applications/kocolor/apps/mobile/build/intermediates/merged_manifest/release -name "AndroidManifest.xml" | head -n 1)
echo "Checking Merged Release Manifest at: $MERGED_MANIFEST"

grep -n -i "AD_ID" "$MERGED_MANIFEST" || echo "PASS: ZERO AD_ID IN MERGED RELEASE MANIFEST"
grep -n -i "health" "$MERGED_MANIFEST" || echo "PASS: ZERO HEALTH ENTRIES IN MERGED RELEASE MANIFEST"
```
*(Both greps MUST output `PASS: ZERO ...` with 0 matching lines in `$MERGED_MANIFEST`).*

### 3B. Run the Full Unit Test Suite, `assembleDebug`, and `lintRelease`
Run:
```bash
./gradlew :features:ai:core:testDebugUnitTest \
  :features:ai:firebase:testDebugUnitTest \
  :features:ai:local:testDebugUnitTest \
  :applications:kocolor:data:testDebugUnitTest \
  :applications:kocolor:features:analyzer:testDebugUnitTest \
  :applications:kocolor:apps:mobile:assembleDebug \
  :applications:kocolor:apps:mobile:lintRelease
```

### 3C. Build Signed `bundleRelease` (If Release Keystore Env Vars Are Exported in This Shell)
```bash
if [ -n "${RELEASE_STORE_FILE:-}" ]; then
    ./gradlew :applications:kocolor:apps:mobile:bundleRelease
    AAB_PATH=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)
    shasum -a 256 "$AAB_PATH"
    bundletool dump manifest --bundle="$AAB_PATH" | grep -E -i "AD_ID|health" || echo "PASS: ZERO AD_ID OR HEALTH IN BUILT AAB"
else
    echo "NOTE: RELEASE_STORE_FILE not set in this shell; fail-fast guard verified in Checkpoint 1.3."
fi
```

---

## MANDATORY COMPLETION GATE (DO NOT CLAIM SUCCESS WITHOUT ALL 5 OUTPUTS)
Your response will be rejected unless it pastes the verbatim terminal output of:
1. `nl -ba applications/kocolor/apps/mobile/build.gradle.kts | sed -n '25,90p'` showing `?: "keystore.jks"` is removed and `GradleException` is present.
2. Checkpoint 1.3 output showing `bundleRelease` failing with `FATAL: Missing required release signing credentials` when env vars are unset, and Checkpoint 1.4 showing `processReleaseMainManifest` succeeding.
3. `nl -ba` output of `HealthUiRoute.kt` (lines 45–110) and `grep -rn -C 4 "HealthConnectCard"` showing the permission launcher and Settings card are removed/gated.
4. The `grep -n -i "AD_ID"` and `grep -n -i "health"` outputs on `build/intermediates/merged_manifest/release/.../AndroidManifest.xml` showing **0 matches**.
5. The Gradle `BUILD SUCCESSFUL` output for all 5 unit test suites, `assembleDebug`, and `lintRelease`.