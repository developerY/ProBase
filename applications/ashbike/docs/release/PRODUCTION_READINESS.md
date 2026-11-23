# AshBike — Production Readiness Checklist

**Location:** `applications/ashbike/docs/PlayStore/release/PRODUCTION_READINESS.md`
**Scope:** Mobile (`:applications:ashbike:apps:mobile`) + Wear OS (`:applications:ashbike:apps:wear`)
**Goal:** a shippable, policy-compliant Play Store release. Work the gates in order: **P0 → P1 → P2**.
Nothing ships to Production until every P0 box is checked.

Related docs: [DEPLOYMENT_PLAY_STORE.md](../DEPLOYMENT_PLAY_STORE.md) (generic flow),
[Info.md](../Info.md) (listing copy), [video.md](../video.md) (promo script),
[TestPlan.md](../../TestPlan.md), [Data_Residency&Privacy.md](../../Data_Residency&Privacy.md).

---

## 1. Current-State Snapshot (audit date: Nov 2026)

Findings below come from a static audit of this repo. Re-verify each line before release.

| Area | Current state | Verdict |
|---|---|---|
| Mobile `applicationId` | `com.zoewave.probase.ashbike`, `versionCode = 1`, `versionName = "0.0.1"` (`apps/mobile/build.gradle.kts`) | ⚠️ Not release versioning |
| Wear `applicationId` | `com.zoewave.probase.ashbike`, `versionCode = 2`, `versionName = "0.0.1"` (`apps/wear/build.gradle.kts`) | ⚠️ Same package as mobile — confirm paired-delivery strategy |
| SDK floor | `android-minSdk = "34"`, wear overrides `minSdk = 35`; `targetSdk = 37`, `compileSdk = 37` (`gradle/libs.versions.toml`) | ✅ Latest-only is a deliberate product decision — confirm Wear 35 (Wear OS 6+) vs 34 (Wear OS 5+) |
| Release signing | `release` signing config reads `RELEASE_*` Gradle props; prints warning and builds **unsigned** if missing (both app modules) | 🛑 No enforced signed-release gate |
| R8 / minify | `isMinifyEnabled` gated on `isMinifyForRelease` Gradle property (convention plugin); module `proguard-rules.pro` files are near-empty templates | ⚠️ Verify release is actually shrunk + tested |
| Lint | `apps/mobile/lint-baseline.xml` baselines 1 issue: `Instantiatable` on `BikeForegroundService` in the manifest | 🛑 Resolve, don't ship a baselined service error |
| Mobile permissions | Location (fine/coarse/background), FGS `location\|connectedDevice`, Health background-read, BLE scan/connect/advertise, NFC, RECORD_AUDIO, VIBRATE, POST_NOTIFICATIONS; AD_ID explicitly removed ✅ | 🛑 Sensitive-permission review needed (background location, mic, health) |
| Wear permissions | Location (fine/coarse/background), FGS `location\|health`, BODY_SENSORS, ACTIVITY_RECOGNITION, POST_NOTIFICATIONS; `standalone=true` ✅ | ⚠️ Needs Wear quality + health declarations |
| Health Connect | Writes `ExerciseSessionRecord` (`SyncRideUserCase.kt`); manifest declares background-read + rationale intent-filters; **no granular `health.WRITE_*` / `health.READ_*` decls found** | 🛑 Incomplete permission declaration |
| Analytics / trackers | Firebase Analytics + Crashlytics applied to both apps via convention plugin (`AndroidApplicationFirebaseConventionPlugin`); Analytics active in both `MainActivity`s | ✅ DECIDED: telemetry stays — implement consent + disclosure (P0-6) |
| Privacy policy | No policy file or URL anywhere in repo | 🛑 Hard Play requirement (Health + background location + Data safety) |
| Backup | Mobile uses `android:fullBackupContent="true"` (default rules); extraction rules commented out; only the glass feature ships `backup_rules.xml` | ⚠️ Write explicit rules for Room cache |
| Tests | Only `ExampleUnitTest` / `ExampleInstrumentedTest` placeholders; TestPlan targets 80% unit coverage | 🛑 No real coverage on tracking/sync |
| Store assets | Listing copy drafted (`Info.md`); `PlayStore/Graphics/` **empty**; Wear has one `ic_launcher-playstore.png` | ⚠️ Screenshots/feature graphic/phone+Wear assets missing |
| `google-services.json` | Committed for mobile and wear, same Firebase project `android-zoewave` | ⚠️ Confirm production project + restrict API keys |
| Asset packs | Mobile references `assetPacks += setOf(":features:ai:model_pack")` | ⚠️ Verify the pack exists and is wanted in the release AAB |

---

## 2. P0 — Release Blockers (must be done before any track upload)

### P0-1. Confirm the latest-only SDK floor and versioning
> **Product decision (recorded):** AshBike intentionally supports only the latest phones and uses cutting-edge libs.
> minSdk 34+ stays. The tasks below just make that decision explicit and verified.

- [ ] Mobile `minSdk = 34` (Android 14+): keep. Check Play Console → Device catalog reach is acceptable for a v1
  and state "requires Android 14 or newer" in the store listing so users aren't surprised.
- [ ] Wear `minSdk = 35`: **confirm deliberately.** API mapping — Wear OS 4 = API 33, Wear OS 5/5.1 = API 34,
  Wear OS 6 = API 35. `minSdk = 35` means **Wear OS 6 watches only**; `34` covers all "latest watches" (5+).
  If 35 is intended, keep the override in `apps/wear/build.gradle.kts` and note it in the listing; otherwise lower to 34.
- [ ] Confirm the dual-app strategy: both apps share `applicationId = com.zoewave.probase.ashbike`.
  That's the correct shape for one Play listing with a Wear companion — keep it, but align `versionCode` so the Wear AAB
  never ships a lower code than the phone AAB in the same release, and document the rule here.
- [ ] Set release `versionCode = 1` (first Production upload) / next integer per release, `versionName = "1.0.0"`.
- [ ] Keep `targetSdk = 37` (meets Play's target-API requirement); re-check the [Play target API policy](https://support.google.com/googleplay/android-developer/answer/11926878) at ship time.
- **Accept:** `./gradlew :applications:ashbike:apps:mobile:assembleRelease :applications:ashbike:apps:wear:assembleRelease`
  succeed with the new floors; `aapt dump badging` shows intended `sdkVersion`/`targetSdkVersion`.

### P0-2. Fix the baselined `BikeForegroundService` lint error
- [ ] `apps/mobile/lint-baseline.xml` suppresses `Instantiatable: BikeForegroundService must extend android.app.Service`.
  The class extends `LifecycleService` (a `Service`), so this is likely a stale lint resolution — prove it:
  delete the baseline entry (or the file), run `./gradlew :applications:ashbike:apps:mobile:lintRelease`, and fix any real finding
  (wrong manifest class path, missing Hilt entry point, stripped superclass under R8).
- [ ] Do the same for Wear (`BikeForegroundService` declared with `foregroundServiceType="location|health"` — verify the Wear Hilt binding actually provides a health-capable engine).
- **Accept:** `lintRelease` passes on both apps with zero fatal/error findings and no manifest-related baseline entries.

### P0-3. Enforce signed, shrunk release builds
- [ ] Create the upload keystore, enroll in **Play App Signing**, and store credentials outside the repo
  (CI secrets → `RELEASE_STORE_FILE` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD`).
- [ ] Make a missing signing config **fail** the release build instead of printing `⚠️ … will not be signed`
  (both `apps/mobile/build.gradle.kts` and `apps/wear/build.gradle.kts`).
- [ ] Set `isMinifyForRelease=true` for release CI and confirm R8 + resource shrinking actually run
  (convention plugin: `KotlinAndroid.kt`); add keep rules for Room, Hilt, kotlinx.serialization Nav3 keys,
  Health Connect records, and Maps models — the current per-module `proguard-rules.pro` files are empty templates.
- [ ] Upload the deobfuscation mapping file with every AAB (Play Console → release → mapping file) so Crashlytics stack traces resolve.
- [ ] Smoke-test the **shrunk release artifact**, not just debug: install `*-release.apk/AAB`, start/pause/stop a ride,
  background the app, sync to Health Connect. R8 misconfiguration is the #1 release-only crash source.
- **Accept:** unsigned release build fails loudly; signed shrunk AAB installs and completes a full ride loop.

### P0-4. Permission audit — justify or remove every sensitive permission
- [ ] `RECORD_AUDIO` (mobile manifest; used by `apps/mobile/features/glass/…/GlassesMainActivity.kt` + `VoiceGearController.kt`):
  decide — is voice control part of v1? If yes, prepare the Play sensitive-permission justification + in-app rationale + Data-safety entry.
  If no, remove the permission **and** gate/disable the glass voice path so it can't request a permission you don't declare.
- [ ] `ACCESS_BACKGROUND_LOCATION` (both apps) + FGS `location`: required for background ride tracking, but triggers
  Play's strictest location review. Implement the compliant flow: foreground location first with in-app rationale →
  background location only when the user enables background tracking → graceful degraded mode on denial.
  Record the mandatory **disclosure video** (see P1-3).
- [ ] `BLUETOOTH_ADVERTISE`, `NFC`, `VIBRATE`: confirm each is exercised in v1 (NFC→BLE pairing exists in
  `data/…/repository/bike/`; verify advertise/vibrate call sites). Remove any permission with no v1 call path —
  undeclared-looking permissions slow review.
- [ ] Wear `BODY_SENSORS` + `ACTIVITY_RECOGNITION`: keep (Health Services HR), ensure runtime rationale exists on-watch.
- [ ] Re-check `POST_NOTIFICATIONS` request timing (ask in context of starting tracking, not on cold start).
- **Accept:** merged manifest (`app/build/.../merged_manifest/release/...`) contains exactly the permissions v1 justifies;
  each sensitive one has an in-app rationale string and a Play-declaration owner.

### P0-5. Complete the Health Connect integration for review
- [ ] Declare the granular permissions you actually use. Notes from the Nov-19 sync-button fix:
  `features/health/core`'s library manifest declares ~all Health Connect types and merges them into the app —
  functionally complete but a Play-review liability (every declared type needs justification; trim to what v1 uses:
  exercise/distance/calories/heart-rate/steps/sleep/hydration, and drop unused speed/elevation).
  The actual runtime request set is `HealthViewModel.permissions` — it must 1:1 cover every type inserted
  (`SyncRideUseCase`: ExerciseSession, Distance, TotalCaloriesBurned, HeartRate) and read (`initialLoad`).
  A type that is never requested can never be granted, so sync fails with `SecurityException` (this was the bug;
  caused Oct-2 by KoColor's minimum-scope commit `b45624ea0` trimming the *shared* request set).
  Per-app system now in place (Nov-19): each app binds its own `AppHealthPermissionPolicy` in its app module
  (`AshBikeHealthPermissionModule`, `KoColorHealthPermissionModule`); `HealthViewModel`/`GetHealthSummaryUseCase`
  consume the resolved set via Hilt, with `DefaultHealthPermissionPolicy` as fallback. Future Play-scope changes
  go in the affected app's own module — never the shared default. AshBike's coverage is guarded by
  `AshBikeHealthPermissionPolicyTest`.
- [ ] Verify the `ACTION_SHOW_PERMISSIONS_RATIONALE` and `VIEW_PERMISSION_USAGE` intent-filters in the mobile manifest
  resolve to real UI explaining *why* each health type is needed.
- [ ] Align behavior with [Data_Residency&Privacy.md](../../Data_Residency&Privacy.md): "delete removes Room copy, Health Connect
  record persists" must be exactly what the app does **and** what the store copy + privacy policy say
  (note: this nuances the "no trace behind" claim in `Info.md` — reconcile the wording, see P0-6).
- [ ] File the Play Console **Health Connect declaration** (Health apps declaration + each data type justified);
  approval is separate from app review and can take days — submit early on the Internal track.
- **Accept:** on a Health-Connect-capable device, fresh install → rationale → grant → ride syncs `ExerciseSessionRecord` →
  visible in Health Connect toolbox; denial path keeps the app fully usable on Room alone.

### P0-6. Ship Firebase properly: consent, Crashlytics verification, disclosure
> **Product decision (recorded):** Firebase stays — Crashlytics + Analytics are needed to build good software.
> Already wired: `AndroidApplicationFirebaseConventionPlugin` applies the Crashlytics + google-services Gradle plugins
> and adds `firebase-analytics`, `firebase-crashlytics`, `firebase-installations` to both apps. What remains is
> consent gating, crash-pipeline verification, and honest disclosure.

- [ ] **Gate collection behind user consent (both apps).** Firebase collects from first launch by default — for a
  Health & Fitness app with background location, Play review + user trust require opt-in:
  - Set `firebase_crashlytics_collection_enabled=false` and `firebase_analytics_collection_enabled=false`
    (manifest meta-data) so nothing leaves the device before consent.
  - Add a first-run consent screen (Crashlytics + Analytics, separately toggleable) that calls
    `Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)` / `Firebase.analytics.setAnalyticsCollectionEnabled(true)`.
  - Persist the choice (DataStore) and expose it in Settings with an opt-out that disables collection + deletes the
    Analytics app instance (`resetAnalyticsData()`).
  - Keep the existing `setUserProperty("device_platform", …)` calls — but move them after consent is granted.
- [ ] **Verify the Crashlytics pipeline on a release build:** force a test crash in `bundleRelease` output,
  confirm it appears deobfuscated in the Firebase console (mapping upload from P0-3 working), then remove the test trigger.
- [ ] **Make crashes actionable for ride debugging:** add custom keys (`ride_state`, `tracking_engine`, `sensor_source`)
  and `log()` breadcrumbs at ride start/pause/stop/sync points; record non-fatals for sync failures
  (Health Connect write errors, Data Layer handoff failures) instead of swallowing them.
- [ ] **Rewrite the privacy claims to match reality.** `Info.md` / `video.md` currently promise "no hidden trackers,
  no cloud dependencies" — replace with accurate copy, e.g.: "Free forever, ad-free. Optional, opt-in crash &
  usage reporting helps us improve AshBike; ride data stays on your device (Room + Health Connect)."
  "Offline ride maps" stays claimable only if the map path never requires map tiles/network.
- [ ] Write and host a public **Privacy Policy** covering: location (foreground + background), health data via Health Connect
  (what's written/read, retention, deletion semantics), Bluetooth/NFC, microphone (if kept), Firebase Crashlytics +
  Analytics (what's collected, opt-in/out, retention), data residency ("ride data on-device in Room + Health Connect;
  diagnostics go to Firebase only with consent"), contact email, and account-deletion statement
  ("no accounts; uninstall wipes local data; Health Connect data managed in the Health Connect UI").
- **Accept:** fresh install sends zero Firebase traffic before consent (verify with proxy/logcat);
  test crash + non-fatal appear in console with custom keys; policy URL live; listing copy, Data-safety answers,
  and actual network behavior all agree.

### P0-7. Lock the production Firebase / Maps configuration
- [ ] Confirm `android-zoewave` is the production Firebase project.
- [ ] Restrict API keys (Android-app + SHA-1/SHA-256 of upload **and** Play-signing certs) and enable App Check if any callable backend exists.
- [ ] Audit Maps: `google.maps.compose` is a dependency but the secrets-plugin block is commented out in `apps/mobile/build.gradle.kts`.
  Either wire the key properly (never committed) or drop the dependency if Smart Trip Maps are fully offline.
- [ ] Verify no key/secret is committed: `git grep -i "AIza\|api[_-]\?key" -- ':!*.md'` outside known-safe files.
- **Accept:** fresh-clone release build works with keys from CI/secrets only; Play pre-launch report shows no exposed-key warnings.

### P0-8. Establish the minimum release test gate (no placeholders)
- [ ] Replace/augment `ExampleUnitTest` placeholders with real unit tests for: distance/duration/calorie accumulation,
  pause/resume state machine, GPX generation, Health Connect record mapping (`SyncRideUseCase*`), and Room DAO round-trips.
- [ ] Add the TestPlan's L2/L3 minimum: in-memory Room + repository test, Compose smoke test (launch → grant → start ride),
  and the TC-001…TC-007 smoke matrix executed on **release** builds.
- [ ] Run the mandatory **field tests** on physical devices (emulators can't validate these): 1-hour GPS ride (accuracy + battery),
  screen-off 2-hour background ride (no OS kill), BLE sensor connect + out-of-range reconnect, Wear standalone ride (no phone) →
  Data Layer handoff → Health Connect write, and uninstall/reinstall data-wipe verification.
- **Accept:** `./gradlew testDebugUnitTest` + `lintDebug` green in CI; signed smoke doc (device, build, result) attached to the release.

---

## 3. P1 — Play Console & Store Listing (needed before Production, start early)

### P1-1. Console declarations (several need lead time)
- [ ] App access, target audience + content rating (**Everyone** per `Info.md`), News/COVID declarations as applicable.
- [ ] **Data safety form** — map every type from the P0-4/P0-5 audit (location incl. background, health/fitness, microphone if kept,
  Bluetooth), plus Firebase Crashlytics + Analytics (device IDs, app activity, diagnostics — collected only after opt-in consent
  per P0-6, Google Firebase declared as a service provider).
- [ ] Background-location declaration + video; Health Connect / health-data declaration; foreground-service declaration
  (location + connectedDevice + health types must match the manifests exactly).
- [ ] Confirm **Contains ads: No** (AD_ID already removed ✅ — keep it that way; adding any ads SDK later reopens this).
- [ ] Wear OS: opt into the Watch track from the same listing; complete the Wear-specific quality review.

### P1-2. Listing assets (copy exists, art doesn't)
- [ ] Fill `docs/PlayStore/Graphics/` (currently **empty**): 512px icon, 1024×500 feature graphic, phone screenshots (min 2),
  7" + 10" tablet screenshots if you keep tablet support, Wear round + square screenshots, promo video per `video.md`.
- [ ] Verify every screenshot/claim matches the release build (reviewers reject mismatches): offline grid map, one-shot weather,
  Health Connect sync animation, dark mode. Localize EN/ES/DE/FR if claimed in `Info.md` — or drop the claim.
- [ ] "New in this version" text + full/short descriptions finalized from `Info.md` after the P0-6 wording reconciliation.

### P1-3. Declaration video + rationale evidence
- [ ] Record the background-location disclosure video on the release build (in-app prompt → system dialog → feature use).
- [ ] Screenshot every runtime rationale (location, background location, health, BLE, mic-if-kept, notifications) for the review packet.

---

## 4. P2 — Quality Bar (release-quality, not just review-passing)

- [ ] **Backup rules:** replace `fullBackupContent="true"` with explicit `backup_rules.xml` + `data_extraction_rules.xml`
  wired in the mobile manifest (Room ride cache: include or exclude deliberately; exclude tokens/keys; document the Health Connect story).
  Wear `allowBackup="true"` — same decision.
- [ ] **16 KB page size:** verify native libs alignment for the 16 KB requirement (`android:pageSizeCompat` only as a stopgap).
- [ ] **Edge-to-edge + predictive back:** `enableOnBackInvokedCallback` is set ✅ — verify gesture nav on API 35+ devices, no clipped Compose layouts.
- [ ] **Wear quality checklist:** standalone install with no phone, round/square layouts, complication renders live data
  (rename `complication_label` "Example complication"), Tile either ships working or stays removed (currently commented out — decide),
  ambient/burn-in behavior, and Data Layer sync + ack (`RideAckListenerService`) under Bluetooth drop/reconnect.
- [ ] **Performance:** re-enable Baseline Profiles (currently commented out in mobile gradle) or drop the claim from docs;
  measure cold start, 1-hour ride battery delta, and APK/AAB size (audit `:features:ai:model_pack` weight — ML assets can balloon installs).
- [ ] **Accessibility:** content descriptions (weather strings exist ✅ — extend to dashboard controls), TalkBack pass on phone + watch,
  font-scale 200% and small-screen Wear layouts.
- [ ] **Crash/ANR budget:** define the rollout guardrails now — block rollout on bad-behavior thresholds
  (reference: ≥1.09% crash / ≥0.47% ANR daily). Pre-launch report must be clean on the release AAB.
- [ ] **Release hygiene:** `versionCode` bump procedure, mapping-file upload, staged-rollout + halt criteria, release-notes template,
  Play Console Vitals + review monitoring rota (see §5).

---

## 5. Release Runbook (execute once P0 is green)

```bash
# 1. Version + verify
#    Bump versionCode/versionName in apps/mobile/build.gradle.kts AND apps/wear/build.gradle.kts

# 2. Full local gate (from repo root)
./gradlew lintRelease testDebugUnitTest \
  :applications:ashbike:apps:mobile:bundleRelease \
  :applications:ashbike:apps:wear:bundleRelease

# 3. Inspect artifacts
ls applications/ashbike/apps/mobile/build/outputs/bundle/release/*.aab
ls applications/ashbike/apps/wear/build/outputs/bundle/release/*.aab
# aapt dump badging <apk/aab-extracted-apk> | grep -E "sdkVersion|targetSdkVersion|uses-permission"

# 4. Install the shrunk release bits on hardware and run §P0-8 smoke + field tests
```

Track progression: **Internal** (team + declarations submitted) → **Closed** (QA + stakeholders, ≥1 week soak)
→ **Open** (optional beta) → **Production staged rollout** (start 5–10%, watch Vitals 48h, then ramp).
Never skip Closed for a v1 with background location + health permissions.

Post-launch (first 14 days): daily Vitals check (crashes/ANRs, background-location and health-permission denial rates),
triage every 1–2★ review mentioning tracking accuracy/battery/sync, and keep a hotfix branch ready for R8/permission regressions.

---

## 6. Suggested Ownership Order

1. Privacy + permissions (P0-4, P0-5, P0-6) — longest review lead times, start first.
2. Build integrity (P0-1, P0-2, P0-3, P0-7) — blocks all test artifacts.
3. Test gate (P0-8) — runs on the fixed build.
4. Console + assets (P1) — parallelizable once copy is reconciled.
5. Polish (P2) — bake into the Closed-track soak.

---

*Generated from a static audit of `applications/ashbike` (Nov 2026). Treat every finding as "verify on the release build" —
if code changed since the audit, the release artifact wins over this doc.*
