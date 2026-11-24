  Verdict: not ready — 4 hard blockers, 6 must-fix-before-track items

  The team made real progress (per-app permissions, manifest trim, 20 real tests, privacy draft, icons), but three P0 fixes have regressed since they were marked done, and the privacy story now contradicts the
  shipped code.

  P0 gate status
  
  ┌──────────────────────────────────┬────────────────┬────────────────────────────────────────────────────────────────────┐
  │ Gate                             │ Verdict        │ Evidence                                                           │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ Crashlytics consent + disclosure │ BLOCKED        │ SDK auto-collects, no consent UI, policy doc denies trackers exist │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ Signed release builds            │ BLOCKED        │ Missing keys now silently fall back to debug signing               │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ Lint baseline                    │ BLOCKED        │ Same Instantiatable suppression re-added by f375d1a7b              │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ FGS connectedDevice              │ BLOCKED        │ Type removed while BLE engine runs inside the service              │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ HC manifest ↔ policy match       │ PASS (fragile) │ Coherent today, nothing enforces it                                │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ Versioning                       │ OPEN           │ Mobile v4 vs Wear v2, same package                                 │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ Listing + policy truth           │ OPEN           │ False claims in Info.md, policy unhosted                           │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ Release-build smoke test         │ OPEN           │ Never run on a shrunk artifact                                     │
  ├──────────────────────────────────┼────────────────┼────────────────────────────────────────────────────────────────────┤
  │ Glass voice path                 │ OPEN           │ Requests a permission no longer declared                           │
  └──────────────────────────────────┴────────────────┴────────────────────────────────────────────────────────────────────┘

  The 4 blockers, in fix order

  1. Privacy contradiction is now a three-way lie. Crashlytics ships via the Firebase convention plugin and collects from first launch with no consent screen. Meanwhile the new
  docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md asserts "no external crash reporting engines," and docs/release/Info.md still promises "no hidden trackers, no cloud dependencies." You already decided to keep
  Crashlytics — so implement P0-6 as written (manifest flags off, consent screen, Settings opt-out), rewrite policy section 2, and fix the listing. Related:
  apps/mobile/src/main/java/com/zoewave/probase/ashbike/mobile/MainActivity.kt:80 opens ylabz.com as the "privacy policy" with a TODO, the policy is dated March 2026 and hosted nowhere, and it claims an
  "encrypted" Room database — verify that or drop the word.

  2. Release signing silently degrades to debug keys. edd36f8a8 replaced the fail-loud gate with a debug-key fallback in apps/mobile/build.gradle.kts:26. A keyless CI run now produces a debug-signed "release"
  AAB that could reach Play with the wrong cert. Make missing keys fail by default; allow the fallback only behind an explicit local-dev flag.

  3. BLE will likely crash the tracking service on every device. The manifest trim dropped FGS type location|connectedDevice to location only, but
  apps/mobile/src/main/java/com/zoewave/probase/ashbike/mobile/di/MobileTrackingModule.kt:20 still injects the BLE engine into the service — and minSdk 34 means every user is on Android 14 FGS-type
  enforcement. Restore the type + permission and declare it, or prove BLE never runs under the service. Either way this needs a field test with a real BLE sensor.

  4. Lint baseline is back. f375d1a7b re-added the exact Instantiatable suppression P0-2 was supposed to kill. Resolve the underlying finding instead of re-baselining it.

  Must-fix before any track upload

  • Version skew. Mobile is v4/0.0.4, Wear v2/0.0.1 on the same applicationId — this already violates the "Wear never lower than phone" rule. Align them and adopt the 1.0.0 scheme at release. (Wear minSdk = 35
    = Wear OS 6-only is also still an unconfirmed decision.)
  • Listing falsehoods. No values-* dirs exist, so the EN/ES/DE/FR claim is false; "master-detail" was removed from nav; "no trace behind" contradicts Health Connect persistence. Screenshots and the promo
    video are also still missing (icons and feature graphic are done).
  • Dead voice path. Glass still requests RECORD_AUDIO, now undeclared — requests auto-deny. Disable or remove voice from v1.
  • Serialization vs R8. The new ProGuard rules are a start, but the single kotlinx.serialization keep is unlikely sufficient for Nav3 keys under R8 full mode — and no shrunk release build has ever been
    smoke-tested. Install the release AAB and run a full ride loop before Internal.
  • Manifest/policy coherence is one commit away from breaking again. f785cf881 correctly re-trimmed my AshBike policy to match the manifest strip (I verified: all 9 requested permissions are grantable, both
    policy tests still pass statically). But nothing enforces this. Add an androidTest asserting every policy permission appears in the merged manifest via PackageManager, so the next trim fails loudly instead
    of silently killing sync.
  • Hygiene. A build intermediate got committed (intermediates/asset_pack_manifest), 32 Example* placeholder tests remain, the google-services-ylabz.json variant is undecided, Tile XML is dead code, and the
    complication still ships labeled "Example complication." Also justify or drop the ARCore metadata — reviewers will ask why a bike app needs it.

  What's genuinely in good shape

  Per-app HC permissions with passing tests, the manifest trim itself (unused speed/elevation + WRITE_ACTIVE_CALORIES excepted), 20 real unit tests, HC rationale handling + the Android 14 permission-usage
  alias, AD_ID removal, background-location removal (correct if the FGS covers all tracking — the screen-off field test proves it), clean secrets, and the release runbook. The background-location declaration
  work in P0-4/P1-3 can be retired if the field test confirms no background access outside the service.

  Suggested order: signing gate → Crashlytics consent + policy/listing truth → connectedDevice → baseline → release smoke test → manifest/policy guard test → hygiene. Want me to start with the signing gate and
  the consent implementation?