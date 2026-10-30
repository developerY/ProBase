Paste the prompt below into Android Studio’s **Agent Mode** chat window or save it in **Settings > Tools > AI > Prompt Library** (or `.agents/skills/google-play-deploy/SKILL.md`) to run an automated codebase audit before generating and uploading your `mobile-release.aab` bundle.

---

### Copy-Paste Prompt for the Android Studio AI Agent

> **Role & Objective:**
> Act as the Release & Security Verification Agent for **KoColor (`com.zoewave.probase.kocolor`)**. Before we build and upload `mobile-release.aab` to the Google Play Store, inspect the codebase, Gradle configuration, network modules, and deployment scripts against every requirement below, and output a **PASS / FAIL** audit report with exact file names and line numbers.
>
>
> **1. Gradle & Native Binary Audit (`app/build.gradle.kts`)**
> * Verify `targetSdk` (`target_sdk_version=37`), `versionCode`, and `versionName` are properly configured for release.
>
>
> * Confirm the `release` buildType enables R8 shrinking (`isMinifyEnabled = true`) with `proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")`.
>
>
> * Confirm the `release` buildType contains `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` so the Android Gradle Plugin automatically embeds native `.so` debug symbols (for LiteRT, Room/SQLite, `libdatastore_shared_counter.so`, and `libzstd-jni-1.5.7-12.so`) into `BUNDLE-METADATA` inside the `.aab`.
>
>
> * Check for 16 KB page-size and ELF alignment compatibility across prebuilt native libraries.
>
>
>
>
> **2. Production Security, App Check & Logcat Leak Audit**
> * Inspect `AppCheckInitializer` and Firebase setup (`project: buildwithai-453716` / `20197827893`, app `1:20197827893:android:e821983b8267c31015987c`) to verify `DebugAppCheckProviderFactory` is strictly gated behind debug builds and **Play Integrity** is enforced in release builds.
>
>
> * Audit `AtmosphericRepository` and all HTTP/OkHttp clients to ensure HTTP request logging is set to `NONE` in release builds so the plaintext OpenWeatherMap API key (`appid=ea705...`) is never leaked in production Logcat traces.
>
>
> * Verify no Firebase AI or Gemini API keys are printed to Logcat during cold start or manifest verification.
>
>
>
>
> **3. CDN Endpoint, Zstd & Cryptographic Gate (`StarterPackRepo`)**
> * Inspect `StarterPackRepo` and `KOCOLOR_CDN_BASE_URL` to confirm the client points to the active Cloudflare CDN endpoint (`[https://kocolor-cdn.pages.dev/v1/manifest.json](https://kocolor-cdn.pages.dev/v1/manifest.json)` or `[https://cdn.kocolor.com/inventory/dist/manifest.json](https://cdn.kocolor.com/inventory/dist/manifest.json)`) and cannot fall back to a stale `2026-08-20` manifest.
>
>
> * Confirm `fetchVerifiedPackage` requests the current bundle IDs (`com.kocolor.pack.cosmetics.complete` and `com.kocolor.pack.fashion.complete`) and verifies both the `sha256` digest and `ed25519` signature against `key_id: "atelier-dev-01"` before decompressing via `libzstd-jni`.
>
>
> * Inspect `deploy_cdn.sh` and the catalog compiler configuration to verify:
> * `.kpkg` bundles are compressed with `zstd -19` **without** `--ultra` so Android `libzstd-jni` decompression RAM stays $\le 64\text{ KB}$.
>
>
> * The generated Cloudflare `_headers` file sets `Cache-Control: public, max-age=0, must-revalidate` on both `manifest.json` and `*.kpkg` (never `immutable` on static `.kpkg` filenames) so OkHttp revalidates via ETag.
>
>
> * `CLOUDFLARE_ACCOUNT_ID` (`8e54bb926df3927e951a97c58ae19a04`) is exported for non-interactive subshells.
>
>
>
>
>
>
> **4. Deterministic Catalog Engine & AI Grounding Guardrails**
> * Verify `CatalogRecord.kt` uses camelCase keys (`commonMetadata`, `wardrobeMetadata`, `cosmeticsMetadata`) with `ignoreUnknownKeys = true` and `coerceInputValues = true` for backward-compatible deserialization.
>
>
> * Verify `ProvenanceType.isTrustedForHardFilter` allows only `SOURCE_VERIFIED`, `RULE_DERIVED`, and `HUMAN_REVIEWED` tags in hard environmental/occasion filters, while `LLM_SUGGESTED` tags are restricted exclusively to `scoreWardrobeSoftPreferences()`.
>
>
> * Verify `DeterministicCatalogEngine` enforces `EnginePreflightResult.AbortedMissingSlots` so every required `GarmentSlot` (`TOP`, `BOTTOM`, `FOOTWEAR`) contains at least 1 eligible item before calling Firebase AI (`gemini-3.8-flash`), preventing the Zero-Item Manifest bug.
>
>
> * Verify cosmetics candidate filtering strictly isolates `EYE`, `CHEEK`, `LIP`, and `NAIL` with `chromaticBehavior` in `["OPAQUE", "SHEER"]`, excluding `TRANSLUCENT` and `NON_CHROMATIC` items (such as primers, cleansers, or beard oils) from CIELAB color harmony scoring.
>
>
> * Verify `BlueprintValidator.validateAiSelection()` validates Gemini's output before `[5] AESTHETIC CALIBRATION (FASHIONISTA)` runs.
>
>
>
>
> **5. Git Hygiene & Sensitive Permissions (`AndroidManifest.xml` & `.gitignore`)**
> * Check `.gitignore` to confirm raw catalog inputs in `package/input/KoColor/` (232 raw files) remain tracked in Git while compiled outputs in `package/dist/` (`*.kpkg`, `manifest.json`, `quality_report.json`), `kocolor-v1-deploy.zip`, and intermediate `*.meta.json` files are ignored.
>
>
> * Scan `AndroidManifest.xml` for Health Connect permissions (`READ_SLEEP`, `WRITE_STEPS`, `WRITE_HYDRATION`, or other `WRITE_*` permissions) and list every declared permission that requires a green checkmark under **Google Play Console > Policy > App content > Health Connect**.
>
>
>
>
> **6. Required Output Format**
> Present your findings as a Markdown table with columns: `Check Category`, `Status (PASS / FAIL / MANUAL)`, `File:Line Evidence`, and `Action Required`, followed by a reminder checklist for the developer covering:
> 1. **Play App Signing SHA-256:** Confirm the **App signing key certificate (SHA-256)** (`40:1E:62:8E:27:8E:A9:78:7C:B0:BB:A6:2D:1C:BD:...`) from **Google Play Console > Test and release > Setup > App integrity > App signing** matches **Firebase Console > Project settings > General > Your apps**.
>
>
> 2. **Google Cloud API Key Restrictions:** Confirm **Google Play Integrity API**, **Firebase App Check API**, and **Firebase Installations API** are enabled and checked in the 26 permitted APIs list for project `BuildwithAi` (`20197827893`), and that the Cloud project is linked in **Google Play Console > Release > App integrity**.
>
>
> 3. **App Check Rollout Mode:** Keep Firebase App Check in **Monitored** mode until requests from outdated registered apps drop to 0%, then switch to **Enforced**.
>
>
> 4. **Play Console Upload & Policy Checks:** Complete the Health Connect declaration (with unlisted YouTube / Google Drive demo video URL, Data Safety form, and Privacy Policy link) while on the closed track, and if Play Console throws `Translation failed for these languages: de` during `.aab` upload, re-upload `mobile-release.aab` (or provide `values-de/strings.xml`) to clear the transient translation timeout.
>
>
>
>