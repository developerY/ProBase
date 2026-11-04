---
name: google-play-deployment
description: Build, sign, troubleshoot, and deploy the KoColor Android App Bundle (.aab) to Google Play Store tracks, verify Play Integrity / Firebase App Check certificates, and deploy .kpkg catalog bundles to the CDN.
---

# KoColor Google Play Store & CDN Deployment Skill

1. **Native Debug Symbols (`app/build.gradle.kts`)**:
   Ensure `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` is inside `buildTypes.getByName("release")` to package native symbols for C++/Rust/LiteRT/SQLite `.so` dependencies.
2. **Signed App Bundle Generation**:
   Increment `versionCode` and `versionName`, then run `./gradlew :app:bundleRelease` (or **Build > Generate Signed Bundle / APK...**). If Google Play Console throws `Translation failed for these languages: de`, re-upload the `.aab` to clear the transient timeout.
3. **Firebase App Check & Play Integrity (Project `20197827893` / `buildwithai-453716`)**:
   - Play Store builds require the **App signing key certificate (SHA-256)** from **Google Play Console > Test and release > Setup > App integrity > App signing** to be added in **Firebase Console > Project settings > General > Your apps (`com.zoewave.probase.kocolor`)**.
   - Ensure **Google Play Integrity API** is enabled and included in API key restrictions in Google Cloud Console.
   - For local debug builds, register the Logcat `Firebase App Check debug token` in Firebase Console > App Check > Apps > Manage debug tokens.
   - Clear rate-limit lockouts via `adb shell pm clear com.zoewave.probase.kocolor`.
4. **CDN Catalog Sync (`server/Inventory/package/`)**:
   Export `KOCOLOR_SIGNING_SEED_HEX` (from the secret manager — both pipelines exit without signing if it is missing) and `KOCOLOR_KEY_ID` (production id, e.g. `atelier-prod-01`; never the burned `atelier-dev-01` seed), then compile `.kpkg` packs (`zstd` level 19 without `--ultra`, `sha256`, Ed25519 signature) and upload `.kpkg` files before `manifest.json`. See `server/Inventory/docs/Guides/Deployment_Pipeline_Manual.md` for the rotation procedure.