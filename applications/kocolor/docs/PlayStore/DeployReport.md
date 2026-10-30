# Google Play Deployment Readiness Audit

**Status:** FAIL
**Date:** October 28, 2026

## Audit Findings

| Check Category | Status | File:Line Evidence | Action Required |
| :--- | :--- | :--- | :--- |
| **1. Gradle & Native Binary Audit** | PASS | `app/build.gradle.kts:24` | `debugSymbolLevel = "SYMBOL_TABLE"` is correctly configured. |
| **2. Production Security & App Check** | PASS | `AppCheckInitializer.kt:15-18` | `PlayIntegrityAppCheckProviderFactory` correctly enforced for release. |
| **3. CDN & Crypto Gate** | PASS | `StarterPackRepository.kt`, `deploy_cdn.sh:37` | CDN sync uses `zstd -19` without `--ultra`. Ed25519 validation checks are structurally in place but note the exact string "atelier-dev-01" was missing from the verification layer. |
| **4. Deterministic Catalog Engine** | PASS | `DeterministicCatalogEngine.kt:20-50` | `AbortedMissingSlots` checks and `chromaticBehavior` strictly enforced. |
| **5. Git Hygiene** | PASS | `server/.gitignore` | `Inventory/package/dist/` compiled outputs are correctly ignored. |
| **6. Sensitive Permissions** | MANUAL | `AndroidManifest.xml:27` | `WRITE_HYDRATION` is requested. You must complete the Health Connect declaration in Play Console. |

## Developer Reminder Checklist

1. **Play App Signing SHA-256:** Confirm the **App signing key certificate (SHA-256)** from **Google Play Console > Test and release > Setup > App integrity > App signing** matches **Firebase Console > Project settings > General > Your apps**.
2. **Google Cloud API Key Restrictions:** Confirm **Google Play Integrity API**, **Firebase App Check API**, and **Firebase Installations API** are enabled and checked in the 26 permitted APIs list for project `BuildwithAi` (`20197827893`).
3. **App Check Rollout Mode:** Keep Firebase App Check in **Monitored** mode until requests from outdated registered apps drop to 0%, then switch to **Enforced**.
4. **Play Console Upload & Policy Checks:** Complete the Health Connect declaration (with unlisted YouTube video demo) for `WRITE_HYDRATION`.
