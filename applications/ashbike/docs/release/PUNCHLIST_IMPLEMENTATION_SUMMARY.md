# 📋 AshBike Pre-Release PunchList Implementation Summary

**App Name:** AshBike Mobile  
**Package Identifier:** `com.zoewave.probase.ashbike`  
**Version:** v0.0.4 (Build 4)  
**Date:** November 22, 2026  

This document provides a complete, factual summary of all architectural, permission, algorithmic, and documentation changes implemented across the P0, P1, and P2 pre-release punch lists for the AshBike Mobile application.

---

## 1. 🛡️ Permission Scoping & Manifest Cleanups

### A. Health Connect Permissions (Principle of Least Privilege)
* **Stripped Transitive Bloat:** Added `tools:node="remove"` in `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml` for 90+ unused Health Connect permissions merged transitively from the shared `:features:health:core` module (e.g., Steps, Sleep, Hydration, Weight, Nutrition, Basal Temp, Blood Glucose, Sexual Activity, etc.).
* **1:1 Declared Set:** Restrictively declared only the 7 active cycling data types (Read & Write):
  - `READ_DISTANCE` / `WRITE_DISTANCE`
  - `READ_EXERCISE` / `WRITE_EXERCISE`
  - `READ_HEART_RATE` / `WRITE_HEART_RATE`
  - `READ_TOTAL_CALORIES_BURNED` / `WRITE_TOTAL_CALORIES_BURNED`
  - `READ_ACTIVE_CALORIES_BURNED` / `WRITE_ACTIVE_CALORIES_BURNED`
  - `READ_SPEED` / `WRITE_SPEED`
  - `READ_ELEVATION_GAINED` / `WRITE_ELEVATION_GAINED`

### B. Foreground Service (FGS) & Background Location
* **Removed Background Location:** Completely removed `<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />` from the mobile manifest. Ride tracking operates via a transparent Foreground Service with a persistent status bar notification, bypassing Google Play's strictest background location review tier.
* **FGS Type Alignment:** Dropped `FOREGROUND_SERVICE_CONNECTED_DEVICE` and `FOREGROUND_SERVICE_DATA_SYNC` permissions from the mobile manifest. Aligned `<service android:name=".service.BikeForegroundService">` strictly to `android:foregroundServiceType="location"`.

### C. Hardware & Other Permissions
* **Removed Unused Permissions:** Added `tools:node="remove"` for `NFC`, `VIBRATE`, `BLUETOOTH_ADVERTISE`, `ACCESS_ADSERVICES_AD_ID`, and `ACCESS_ADSERVICES_ATTRIBUTION`.
* **ARCore Cleanup:** Removed `com.google.ar.core`, `min_apk_version` metadata, and `InstallActivity` from the merged mobile manifest via `tools:node="remove"`.
* **Hardware Feature Compatibility:** Declared `<uses-feature>` for `camera`, `camera.autofocus`, `microphone`, `faketouch`, and `screen.portrait` with `android:required="false"` to prevent Google Play Console from blocking non-camera or non-touch devices.

### D. Android 14+ Health Connect Privacy Intent Alias
* Configured `<activity-alias android:name="HealthConnectPrivacyPolicyAlias">` targeting `.MainActivity` with:
  ```xml
  android:permission="android.permission.START_VIEW_PERMISSION_USAGE"
  android:exported="true"
  <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
  <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
  ```

---

## 2. ⚡ Telemetry & Service Lifecycle Hardening

### A. Background Service Teardown & Task Manager Cleanup
* Updated `BikeForegroundService.kt`:
  * Tapping **Stop** calls `stopForeground(STOP_FOREGROUND_REMOVE)`, cancels status bar notifications, releases `PARTIAL_WAKE_LOCK`, and invokes `stopSelf()` to cleanly terminate the process.
  * Added `trackingEngine.stopRide()` and `caloriesCalculationJob?.cancel()` to shut down GPS hardware listener threads immediately on ride stop.
  * Removed premature `startService()` invocation from `BikeServiceManager.bindService()`.
* **WakeLock Safety:** Added a 10-hour safety ceiling to `PARTIAL_WAKE_LOCK.acquire(10 * 60 * 60 * 1000L)` to protect rider battery if an unexpected process crash occurs.

### B. Algorithmic Fixes
* **Stationary Jitter Filter:** Added `effectiveSpeed > 0.5f` (~1.8 km/h) threshold check before accumulating distance to prevent stationary GPS bounce.
* **Speed Delta Fallback:** Added time-delta speed calculation fallback (`location.distanceTo(lastLoc) / timeDeltaSeconds`) for Android chipsets where `location.hasSpeed()` returns false while moving.
* **Average Speed Artifact Guard:** Required at least 30 meters of true movement and >15 seconds of ride duration before calculating average speed, avoiding divide-by-zero artifacts.

---

## 3. 🛡️ User Privacy & Disclosure Flow

* **In-App Prominent Location Disclosure:** Added a dedicated Compose `AlertDialog` in `AshBikeMainScreen.kt` explaining why location data is accessed (speed, distance, route mapping) and how it is stored locally.
* **Affirmative Consent:** Blocked auto-launching the system location prompt on app startup. System runtime prompts (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`) fire **only** when the user clicks **Agree & Continue** on the disclosure dialog.
* **Privacy Policy Overhaul:** Updated `docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md` to:
  * Reflect current app name (**AshBike**) and package name (`com.zoewave.probase.ashbike`).
  * Remove false claims of database encryption (stating accurately that ride history is stored locally in Android app-private Room storage).
  * Update developer contact metadata and last-updated date to **November 2026**.

---

## 4. 📦 Build & Release Configuration

* **Version Bump:** Set `versionCode = 4` and `versionName = "0.0.4"` in `applications/ashbike/apps/mobile/build.gradle.kts` to prevent Play Console version collisions with older builds.
* **NDK Symbol Generation:** Added `ndk { debugSymbolLevel = "FULL" }` to the release build type block in `build.gradle.kts` for readable native crash/ANR stack traces in Play Console.
* **Explicit Backup Rules:** Created `backup_rules.xml` and `data_extraction_rules.xml` under `res/xml/` explicitly excluding local Room database directories and shared preferences from cloud backups and device transfers.

---

## 5. 🧹 Tree Hygiene & Automated Testing

* **Deleted Placeholder Tests:** Removed 20 boilerplate `Example*Test` placeholder files across mobile and shared modules using `git rm`.
* **Intermediate File Cleanup:** Untracked and deleted committed build artifact `features/ai/model_pack/build/intermediates/asset_pack_manifest/AndroidManifest.xml`.
* **Automated Manifest Alignment Tests:** Added `AshBikeHealthManifestAlignmentTest.kt` (Instrumented) and `AshBikeHealthManifestJvmTest.kt` (JVM) to automatically assert that all runtime policy permissions exist in the merged manifest.
* **Notification Timing Test:** Added `NotificationPermissionTimingTest.kt`.
* **Module Code Cleanup:** Removed unused imports and stale KDoc lines from `AshBikeHealthPermissionModule.kt`.

---

## 6. 📄 Human Release Artifacts & Checklists

Created dedicated, verifiable release documentation for human sign-off:
* **GCP Security Checklist:** [`docs/release/CLOUD_CONSOLE_SECURITY_CHECKLIST.md`](CLOUD_CONSOLE_SECURITY_CHECKLIST.md) (GCP API key restriction guide).
* **Health Connect Audit:** [`docs/release/HEALTH_CONNECT_DECLARATION_CHECK.md`](HEALTH_CONNECT_DECLARATION_CHECK.md) (Play Console Health apps declaration guide).
* **Phone Field Test Sign-Off:** [`docs/release/PHONE_FIELD_TEST_SIGNOFF.md`](PHONE_FIELD_TEST_SIGNOFF.md) (1-hour GPS, 2-hour screen-off, BLE reconnect, and uninstall/reinstall test protocols).
* **Master MAD Audit:** [`docs/release/FINAL_MAD_AUDIT_v0.0.4.md`](FINAL_MAD_AUDIT_v0.0.4.md) (Complete architecture & code readiness report).

---

## ✅ Final Build Status

| Verification Target | Command | Result |
|---|---|---|
| Unit Test Suite | `./gradlew :applications:ashbike:apps:mobile:testDebugUnitTest` | **8 Passed, 0 Failed** |
| Release Lint Check | `./gradlew :applications:ashbike:apps:mobile:lintRelease` | **SUCCESS** |
| Release Manifest Processing | `./gradlew :applications:ashbike:apps:mobile:processReleaseMainManifest` | **SUCCESS** |
| Release App Bundle Assembly | `./gradlew :applications:ashbike:apps:mobile:bundleRelease` | **SUCCESS** |
