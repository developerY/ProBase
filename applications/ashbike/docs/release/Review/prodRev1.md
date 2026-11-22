The architecture and cleanup look solid, but there are several **concrete discrepancies between this audit and your actual Play Console state**, plus a few common Android/Play review gotchas the document doesn't cover.

### 1. Direct Conflicts with Your Current Play Console Setup

* **Duplicate `versionCode` for Mobile & Wear OS:** The header lists a single **`Version: 0.0.3 (Build 3)`** for `(Mobile & Wear OS)`, and Next Step #3 says to upload both `mobile-release.aab` and `wear-release.aab` to Production. Google Play requires a **strictly unique `versionCode**` for every `.aab` under `com.zoewave.probase.ashbike`, even across different form factors. Because Build `3` is already used by your phone bundle, uploading `wear-release.aab` with Build `3` will fail—give Wear OS its own version code (e.g., `4`, or a split scheme like `10003` for Mobile and `20003` for Wear).


* **Wear OS Re-Entry Risk:** You recently deactivated Wear OS Build `2`. Before uploading a new `wear-release.aab`, inspect its **Merged Manifest** to ensure it also strips unused Health Connect / `BODY_SENSORS` permissions, and verify you have Wear OS screenshots and form-factor opt-in completed so it doesn't stall the whole release in Wear OS quality review.


* **"100% Device Compatibility" Is Inaccurate:** Section 3 claims hardware features are marked optional for "100% device compatibility," yet Build `3` still requires `android.hardware.location.gps`, which excludes non-GPS tablets, desktops, and Android XR devices. That's the right product choice for a bike computer, so update the audit wording to reflect that GPS is intentionally required—and double-check whether `android.hardware.bluetooth_le` is marked `android:required="false"` if riders can use AshBike without BLE sensors.


* **Missing Native Debug Symbols:** Section 6 marks Release Configuration `READY ✅`, while Build `3` triggered a missing native debug symbols warning in Play Console. Add `ndk { debugSymbolLevel = "FULL" }` (or `"SYMBOL_TABLE"`) to your release Gradle config so production crash/ANR reports are readable.


* **Stale Audit Date:** The header says **Date: March 2026**, while Build `3` (`0.0.3`) was uploaded in **November 2026**.



### 2. Google Play Policy & Review Gotchas Missing from the Audit

* **Health Connect Privacy Policy Intent Filter:** Health Connect requires an activity in `AndroidManifest.xml` handling `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE` (Android 13 and lower) and `android.intent.action.VIEW_PERMISSION_USAGE` with the `HEALTH_PERMISSIONS` category (Android 14+) that displays the exact same Privacy Policy URL listed in Play Console. Reviewers check this directly from Android's Health Connect settings screen.
* **Read vs. Write Permission Parity:** Section 3 names four data types (`Distance`, `ExerciseSession`, `HeartRate`, `TotalCaloriesBurned`) without distinguishing `READ` vs. `WRITE`. Confirm your Play Console **Health apps** declaration matches the exact Read/Write permissions in Build `3`'s merged manifest—and make sure `READ_STEPS` (which was checked in your earlier console screenshot) is unchecked if you stripped it.


* **Foreground Service Video Requirements:** Next Step #1 mentions only showing "Start Ride" triggering the notification. To avoid rejection on the `location` FGS declaration, also show **live GPS tracking updating**, **minimizing/backgrounding the app** while the persistent notification stays active, and **stopping the ride** to dismiss it.
* **Closed Testing Gate (Personal Accounts Only):** Next Step #3 jumps straight to Production. If your Play Console account is a **personal developer account created on or after November 13, 2023**, Production stays locked until you complete **14 consecutive days of Closed Testing with at least 12 opted-in testers**. *(Ignore this if you have an organization account or a pre-Nov 2023 personal account.)*

### 3. Technical & QA Blind Spots

* **`POST_NOTIFICATIONS` on Android 13+ (API 33+):** Section 4 relies on a persistent status bar notification for transparent tracking, but doesn't mention requesting the `POST_NOTIFICATIONS` runtime permission. If the user isn't prompted (or denies it), the foreground service still runs silently in the background with no visible notification—which breaks user expectations and can confuse Play reviewers verifying your FGS behavior.
* **No Release-Build / Physical-Device Testing Listed:** Passing 46/46 unit tests is great for math and ViewModels, but unit tests run without R8 minification. Before pushing to Production, install the **signed, R8-minified release `.aab**` on a physical phone and watch and run a full ride (Start → Background/Lock Screen → BLE Sensor → Stop → Health Connect Sync) to catch any missing ProGuard keep-rules for Health Connect records, Room entities, Nav3 routes, or BLE callbacks.
* **Edge-to-Edge UI (Target SDK 35):** Your app targets API 35, which enforces edge-to-edge display on Android 15+. Verify that Compose screens apply proper system bar insets so buttons and ride controls aren't hidden behind the status or navigation bars.


* **WakeLock Safety Timeout:** Ensure `PARTIAL_WAKE_LOCK.acquire(timeout)` includes a hard ceiling (e.g., 12–24 hours) rather than an indefinite `acquire()`, so an unexpected crash or orphaned state can never drain a rider's battery.
