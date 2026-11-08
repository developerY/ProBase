# Stage 3 — Release Privacy: Logging Gates (Release Runbook Split)

Source: `../Gemini_stages.md` ("Stage 3 Prompt"). Execute ONLY this stage; feed one stage at a time to the coding agent.

## Fixes applied in this split (vs. `Gemini_stages.md`)

- Standardized the completion-gate grep context flag to `-C 3` to match Checkpoint 3.1 (was `-C 2` in the gate — same command, different flag).
- No other changes: whole-repo scope, `Level.NONE` requirement, 4-flow runtime Logcat audit, and `Log.w`/`Log.e` retention verified consistent with `Open_Stage_feedback_1.md` §3.

---

# AGENT MISSION: Execute Stage 3 — Release Privacy: Logging Gates

You are executing **Stage 3 (3.1, 3.2, 3.3, and 3.4)** of `ProductionRoadMap.md`.

## STRICT PRIVACY & SECURITY INVARIANTS
1. **ZERO HTTP LOGGING IN RELEASE ACROSS THE ENTIRE REPO:** Every `HttpLoggingInterceptor` across the entire repository MUST evaluate to `HttpLoggingInterceptor.Level.NONE` in release builds. `Level.HEADERS` is **strictly forbidden** in release because `OpenFetchWeatherData.kt` passes the OpenWeatherMap API key (`appid=...`) in the request URL.
2. **USE EACH MODULE'S OWN `BuildConfig.DEBUG`:** Every module must branch on its compiled `BuildConfig.DEBUG` boolean so release builds resolve to `Level.NONE`.
3. **ZERO PROMPT OR WARDROBE LOGGING IN RELEASE, KEEP OPERATIONAL `Log.w`/`Log.e`:** Gate raw Gemini prompts, responses, and manifests on `BuildConfig.DEBUG` and strip `Log.v`/`Log.d`/`Log.i` via R8, while retaining `Log.w` and `Log.e` for non-sensitive operational diagnostics.

---

## MICRO-STEP 3.1: Gate All HTTP Logging Interceptors Across the Entire Repository to `Level.NONE` in Release

1. Search the **entire repository** (all `.kt` and `.java` files across all directories) for `HttpLoggingInterceptor` and `Level.`:
   ```bash
   git grep -n "HttpLoggingInterceptor\|Level\.BODY\|Level\.HEADERS\|Level\.BASIC" -- "*.kt" "*.java"
   ```
2. Inspect all 6 required clients (`OpenFetchWeatherData.kt:82`, `ProductsDataModule.kt`, `MakeupModule.kt`, `ChemicalModule.kt`, `ObfModule.kt`, `ColorModule.kt`) plus any other module returned by `git grep`:
    - Ensure every single site uses:
      ```kotlin
      level = if (BuildConfig.DEBUG) {
          HttpLoggingInterceptor.Level.BODY
      } else {
          HttpLoggingInterceptor.Level.NONE
      }
      ```
    - Confirm zero occurrences of `else HttpLoggingInterceptor.Level.HEADERS` or `Level.BASIC` exist anywhere in the repo.

> **CHECKPOINT 3.1:** Run:
> ```bash
> git grep -n -C 3 "Level\.BODY\|Level\.HEADERS\|Level\.BASIC" -- "*.kt" "*.java"
> ```
> Paste the verbatim output and confirm every match is strictly inside an `if (...BuildConfig.DEBUG)` branch with `else ... Level.NONE`.

---

## MICRO-STEP 3.2: Gate AI I/O Logging in `FirebaseAiClient.kt` & All AI Providers

1. Read `features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/FirebaseAiClient.kt` (around line 286 and throughout the file).
2. Wrap all `KoColorAI_IO` / `REQUEST TO GEMINI` / `RESPONSE FROM GEMINI` / `TOKEN USAGE` debug logs inside `if (BuildConfig.DEBUG) { ... }`.
3. Search the entire repository for any other `Log.` statements that log prompts, responses, API keys, or candidate manifests:
   ```bash
   git grep -n "KoColorAI_IO\|REQUEST TO GEMINI\|RESPONSE FROM GEMINI\|WARDROBE:" -- "*.kt" "*.java"
   ```
   Ensure all such diagnostic logs are gated by `if (BuildConfig.DEBUG)` and that `Log.w` / `Log.e` statements never interpolate raw prompts, wardrobe manifests, or API keys.

> **CHECKPOINT 3.2:** Run:
> ```bash
> git grep -n -C 2 "KoColorAI_IO\|REQUEST TO GEMINI\|RESPONSE FROM GEMINI" -- "*.kt" "*.java"
> ```
> Paste the verbatim output confirming `if (BuildConfig.DEBUG)` guards on every match.

---

## MICRO-STEP 3.3: Confirm Release Minification & R8 `assumenosideeffects` Rules

1. Read `applications/kocolor/apps/mobile/build.gradle.kts` and `applications/kocolor/apps/mobile/proguard-rules.pro`.
2. Confirm `buildTypes.release` sets `isMinifyEnabled = true` and includes `"proguard-rules.pro"`.
3. In `applications/kocolor/apps/mobile/proguard-rules.pro`, confirm `-assumenosideeffects` removes `Log.v`, `Log.d`, and `Log.i` while **retaining** `Log.w` and `Log.e`:
   ```proguard
   -assumenosideeffects class android.util.Log {
       public static boolean isLoggable(java.lang.String, int);
       public static int v(...);
       public static int d(...);
       public static int i(...);
   }
   ```

> **CHECKPOINT 3.3:** Paste the `buildTypes.release` block from `build.gradle.kts` and the `-assumenosideeffects` block from `proguard-rules.pro`.

---

## MICRO-STEP 3.4: Whole-Repo Static Grep & Release Runtime Logcat Verification Across All 4 Flows

1. Run the mandatory repository-wide grep check:
   ```bash
   git grep -n "Level\.BODY\|Level\.HEADERS" -- "*.kt" "*.java"
   ```
2. Build and install a release build on a connected device/emulator, clear logcat (`adb logcat -c`), and exercise all **4 required runtime flows**:
    - **Flow 1:** Weather fetch (OpenWeatherMap API request).
    - **Flow 2:** Cosmetic / Wardrobe starter catalog fetch.
    - **Flow 3:** AI Analyzer / Suggestion request.
    - **Flow 4:** Full Style Simulation (`StyleSimulatorEngine`).
3. Dump Logcat (`adb logcat -d > /tmp/release_logcat_audit.txt`) and run:
   ```bash
   grep -E -i "appid=|api.openweathermap.org|REQUEST TO GEMINI|RESPONSE FROM GEMINI|KoColorAI_IO|AVAILABLE CANDIDATES|selectedClothingIds" /tmp/release_logcat_audit.txt || echo "PASS: ZERO SENSITIVE URLS, KEYS, PROMPTS, OR WARDROBE CONTENT IN RELEASE LOGCAT"
   ```

## STAGE 3 MANDATORY COMPLETION GATE
Check off `[x] 3.1`, `[x] 3.2`, `[x] 3.3`, and `[x] 3.4` in `ProductionRoadMap.md` ONLY after pasting:
1. Verbatim output of `git grep -n -C 3 "Level\.BODY\|Level\.HEADERS" -- "*.kt" "*.java"`.
2. Verbatim output of `git grep -n -C 2 "KoColorAI_IO" -- "*.kt" "*.java"`.
3. Verbatim `proguard-rules.pro` `assumenosideeffects` block and the release Logcat audit output across all 4 flows.
