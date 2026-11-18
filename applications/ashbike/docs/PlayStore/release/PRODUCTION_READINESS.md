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
| SDK floor | `android-minSdk = "34"`, wear overrides `minSdk = 35`; `targetSdk = 37`, `compileSdk = 37` (`gradle/libs.versions.toml`) | 🛑 minSdk far too high for production |
| Release signing | `release` signing config reads `RELEASE_*` Gradle props; prints warning and builds **unsigned** if missing (both app modules) | 🛑 No enforced signed-release gate |
| R8 / minify | `isMinifyEnabled` gated on `isMinifyForRelease` Gradle property (convention plugin); module `proguard-rules.pro` files are near-empty templates | ⚠️ Verify release is actually shrunk + tested |
| Lint | `apps/mobile/lint-baseline.xml` baselines 1 issue: `Instantiatable` on `BikeForegroundService` in the manifest | 🛑 Resolve, don't ship a baselined service error |
| Mobile permissions | Location (fine/coarse/background), FGS `location\|connectedDevice`, Health background-read, BLE scan/connect/advertise, NFC, RECORD_AUDIO, VIBRATE, POST_NOTIFICATIONS; AD_ID explicitly removed ✅ | 🛑 Sensitive-permission review needed (background location, mic, health) |
| Wear permissions | Location (fine/coarse/background), FGS `location\|health`, BODY_SENSORS, ACTIVITY_RECOGNITION, POST_NOTIFICATIONS; `standalone=true` ✅ | ⚠️ Needs Wear quality + health declarations |
| Health Connect | Writes `ExerciseSessionRecord` (`SyncRideUserCase.kt`); manifest declares background-read + rationale intent-filters; **no granular `health.WRITE_*` / `health.READ_*` decls found** | 🛑 Incomplete permission declaration |
| Analytics / trackers | Firebase Analytics active in both `MainActivity`s (`setUserProperty("device_platform", …)`); convention plugin applies Firebase to both apps | 🛑 Contradicts "no hidden trackers" store copy — decide and disclose |
| Privacy policy | No policy file or URL anywhere in repo | 🛑 Hard Play requirement (Health + background location + Data safety) |
| Backup | Mobile uses `android:fullBackupContent="true"` (default rules); extraction rules commented out; only the glass feature ships `backup_rules.xml` | ⚠️ Write explicit rules for Room cache |
| Tests | Only `ExampleUnitTest` / `ExampleInstrumentedTest` placeholders; TestPlan targets 80% unit coverage | 🛑 No real coverage on tracking/sync |
| Store assets | Listing copy drafted (`Info.md`); `PlayStore/Graphics/` **empty**; Wear has one `ic_launcher-playstore.png` | ⚠️ Screenshots/feature graphic/phone+Wear assets missing |
| `google-services.json` | Committed for mobile (+ `google-services-ylabz.json` variant) and wear, same Firebase project `android-zoewave` | ⚠️ Confirm production project + restrict API keys |
| Asset packs | Mobile references `assetPacks += setOf(":features:ai:model_pack")` | ⚠️ Verify the pack exists and is wanted in the release AAB |

---

## 2. P0 — Release Blockers (must be done before any track upload)

### P0-1. Set the production SDK floor and versioning
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
- [ ] Declare the granular permissions you actually use (`android.permission.health.WRITE_EXERCISE`,
  plus every `READ_*` type you read: exercise, heart rate, distance, calories, speed, elevation — match to `SyncRideUserCase.kt`).
  A lone `READ_HEALTH_DATA_IN_BACKGROUND` will not pass review.
- [ ] Verify the `ACTION_SHOW_PERMISSIONS_RATIONALE` and `VIEW_PERMISSION_USAGE` intent-filters in the mobile manifest
  resolve to real UI explaining *why* each health type is needed.
- [ ] Align behavior with [Data_Residency&Privacy.md](../../Data_Residency&Privacy.md): "delete removes Room copy, Health Connect
  record persists" must be exactly what the app does **and** what the store copy + privacy policy say
  (note: this nuances the "no trace behind" claim in `Info.md` — reconcile the wording, see P0-6).
- [ ] File the Play Console **Health Connect declaration** (Health apps declaration + each data type justified);
  approval is separate from app review and can take days — submit early on the Internal track.
- **Accept:** on a Health-Connect-capable device, fresh install → rationale → grant → ride syncs `ExerciseSessionRecord` →
  visible in Health Connect toolbox; denial path keeps the app fully usable on Room alone.

### P0-6. Resolve the privacy contradiction, then publish the policy
- [ ] Decide v1's analytics stance — today both apps initialize **Firebase Analytics**, while `Info.md` promises
  "no hidden trackers, no ads, no cloud dependencies". Pick one:
  - **Option A (privacy-first, recommended for this listing):** remove Analytics/Crashlytics from v1, delete the
    `Firebase.analytics` calls in both `MainActivity`s, and keep the zero-footprint claim; *or*
  - **Option B (telemetry):** keep Firebase, add opt-in consent before first collection, and rewrite the listing +
    Data-safety form to disclose device IDs, app activity, and diagnostics.
- [ ] Write and host a public **Privacy Policy** covering: location (foreground + background), health data via Health Connect
  (what's written/read, retention, deletion semantics), Bluetooth/NFC, microphone (if kept), Firebase (if kept),
  data residency ("on-device Room cache + on-device Health Connect; no first-party server"), contact email, and account-deletion
  statement ("no accounts; uninstall wipes local data; Health Connect data managed in the Health Connect UI").
- [ ] Reconcile `Info.md` + `video.md` claims ("offline", "no cloud", "no trace") with reality:
  Firebase (if kept), Google Maps Compose dependency, and one-shot weather calls all need either removal or disclosure.
  "Offline ride maps" is only claimable if the release AAB never requires map tiles/network for the map path.
- **Accept:** policy URL live; listing copy, Data-safety answers, and actual network behavior all agree.

### P0-7. Lock the production Firebase / Maps configuration
- [ ] Confirm `android-zoewave` is the production Firebase project (both `google-services.json` files point there);
  decide the fate of `apps/mobile/google-services-ylabz.json` (flavor file? delete if unused).
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
  Bluetooth, diagnostics). "No data shared with third parties" is only true under Option A + no Firebase.
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
