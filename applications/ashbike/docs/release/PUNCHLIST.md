# AshBike Phone — Release Punch List

**Scope: phone app only.** XR/glass, Wear OS, and everything outside `applications/ashbike/apps/mobile`
(plus its shared modules) is out of scope for this release.
Distilled Nov 2026 from a file-by-file review of HEAD; each item states the evidence and the
acceptance check. Work top to bottom: **P0 → P1 → P2**.

Recorded decisions (do not redo): Android Studio builds/signs releases; Play Store handles
translations, hosts the videos, and the listing itself is done. Items below are only what remains.

---

## P0 — ship blockers (nothing releases until these close)

- [ ] **1. Gate Firebase collection behind consent.** Crashlytics + Analytics SDKs ship via the
  shared Firebase convention plugin and collect from first launch, with no consent UI anywhere.
  - Add `firebase_crashlytics_collection_enabled=false` and `firebase_analytics_collection_enabled=false`
    manifest meta-data in `apps/mobile/src/main/AndroidManifest.xml`.
  - Add a first-run consent screen (Crashlytics + Analytics separately toggleable) that enables
    collection, persists the choice, and exposes opt-out in Settings.
  - Accept: fresh install sends zero Firebase traffic before consent (logcat/proxy); a forced test
    crash on the release build appears deobfuscated in the console (confirms mapping upload works).

- [ ] **2. Make the privacy policy true and live.** `docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md`
  asserts "no external crash reporting engines" (false while Crashlytics ships), claims an
  "encrypted" Room database (unverified), is dated March 2026, and is hosted nowhere —
  `MainActivity` opens `ylabz.com` with an "update to your actual privacy policy URL" TODO.
  - Disclose Crashlytics/Analytics (what, opt-in/out, retention); verify or drop "encrypted";
    refresh the date; host the policy; point `MainActivity` at the real URL; confirm the Play
    Console URL and Data-safety answers match the code.
  - Accept: policy URL live, MainActivity TODO gone, console answers == actual behavior.

- [ ] **3. Fix the FGS type for BLE — likely crash on every device.** The mobile manifest declares
  FGS type `location` only, but `MobileTrackingModule` injects the BLE engine into
  `BikeForegroundService`, and minSdk 34 means Android 14 type enforcement is always on.
  Bluetooth from a location-type service risks `SecurityException`.
  - Restore `connectedDevice` to the service's `foregroundServiceType` + the
    `FOREGROUND_SERVICE_CONNECTED_DEVICE` permission, and update the Play FGS declaration.
    (Alternative: prove BLE never runs under the service — unlikely given the binding.)
  - Accept: ride with a BLE chest strap on API 34+, no exception, HR flows.

- [ ] **4. Kill the re-added lint baseline.** `apps/mobile/lint-baseline.xml` is back with the same
  `Instantiatable: BikeForegroundService must extend android.app.Service` suppression.
  - Resolve the real finding (or prove it stale), delete the baseline, `lintRelease` clean.
  - Accept: no baseline file, zero fatal/error findings on the release variant.

- [ ] **5. Smoke-test the shrunk release artifact.** No release build has ever been installed and
  exercised; the ProGuard rules are new and the single kotlinx.serialization keep may not cover
  the Nav3 keys under R8 full mode (classic release-only crash).
  - Build the shrunk release in Android Studio, install, and run the full loop: start → pause →
    resume → stop → background → ride detail → Health Connect sync; watch logcat for
    serialization/R8 failures.
  - Accept: full loop green on the release artifact, not just debug.

- [ ] **6. Lock phone release versioning.** Mobile is at `versionCode = 4` / `0.0.4`.
  - Adopt the release scheme at branch time (`1.0.0`, code increments). Note: Wear shares the
    package ID, so coordinate codes at upload even though Wear is out of scope for this list.
  - Accept: versioning rule written down, values set for the release candidate.

---

## P1 — pre-release hardening

- [ ] **7. Guard the manifest ↔ policy match with a test.** The sync outage class (policy requests
  what the manifest strips, or vice versa) currently has no automated guard — `AshBikeHealthPermissionPolicyTest`
  covers policy↔code only.
  - Add an `androidTest` asserting every permission in the resolved policy is declared in the
    merged manifest (via `PackageManager`), so the next manifest trim fails loudly.
  - Accept: new test green; verified it fails if a policy permission is stripped.

- [ ] **8. Resolve the speed/elevation three-way mismatch.** Manifest declares `READ_SPEED` /
  `READ_ELEVATION_GAINED`, the privacy policy claims they power speed/elevation profiles, but no
  code reads speed records and the runtime policy never requests them. Also `WRITE_SPEED`,
  `WRITE_ELEVATION_GAINED`, and `WRITE_ACTIVE_CALORIES_BURNED` still merge in from the library,
  unused.
  - Decide: implement the reads (request + use) or strip the declarations and fix the policy doc
    to match. Either way all three layers must agree.
  - Accept: manifest, runtime policy, and policy doc list exactly the same HC types.

- [ ] **9. Settle the Firebase project config.** `google-services-ylabz.json` sits next to
  `google-services.json` (undecided), both pointing at `android-zoewave`.
  - Confirm the production project, delete or document the variant, restrict API keys to the app
    + upload/Play-signing certs.
  - Accept: one prod config, restrictions in place, no stray variant.

- [ ] **10. Confirm the Health Connect declaration is approved.** Filing is separate from app review
  and takes days; the strip + per-app work changed the declared/requested types since any earlier
  filing.
  - Confirm in Play Console that the current type list is declared and approved.
  - Accept: approval confirmed for the exact v1 type list.

- [ ] **11. Run the phone field tests.** Emulators can't validate: 1-hour GPS ride (accuracy +
  battery), 2-hour screen-off ride (no OS kill — also proves the background-location removal is
  safe), BLE connect + out-of-range reconnect, uninstall/reinstall wipe check.
  - Accept: signed results (device, build, outcome) attached to the release.

---

## P2 — polish and hygiene

- [ ] **12. Write explicit backup rules.** Still `fullBackupContent="true"` with extraction rules
  commented out. Decide Room cache in/out, exclude tokens/keys, wire both XML files.
- [ ] **13. Confirm notification permission timing.** `POST_NOTIFICATIONS` is requested around
  `AshBikeMainScreen.kt:178` — confirm it fires in the context of starting tracking, not cold start.
- [ ] **14. Verify or drop `NFC` / `VIBRATE` / `BLUETOOTH_ADVERTISE`.** Confirm a v1 call path for
  each; remove leftovers that slow review.
- [ ] **15. Justify or remove the ARCore manifest metadata.** A bike app declaring AR bits invites
  review questions; drop it if nothing uses it.
- [ ] **16. Clean the tree.** Delete the 32 `Example*` placeholder tests; remove the committed build
  intermediate (`intermediates/asset_pack_manifest/`) and gitignore it; drop the unused imports +
  stale KDoc in `AshBikeHealthPermissionModule.kt` (left by the trim commit).
- [ ] **17. Retire stale trackers.** `Steps.md` and the readiness snapshot contradict current code
  (baseline, signing, analytics state). Refresh or delete them so nobody works from them. This
  punch list supersedes both for the phone release.

---

## Explicitly out of scope (recorded, not forgotten)

Wear OS (minSdk 35 decision, Tile, complication label, data-sync removal check), XR/glass voice
path, release signing (Android Studio), translations, videos, and listing content (Play Store).

*Generated from the Nov 2026 file-by-file review. If code changed since, the release artifact wins.*
