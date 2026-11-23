◆ Verification complete. I checked every claim in the implementation summary against code, re-ran your build table, and extended the test run to all AshBike modules. Bottom line: the important work is real and
  mostly correct — with 6 follow-ups, one of them a red suite.

  Punch-list disposition
  
  ┌────────────────┬──────────────────────────────────┬──────────┬──────────────────────────────────────────────────────────────────────────┐
  │ #              │ Item                             │ Status   │ Note                                                                     │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P0-1           │ Firebase consent                 │ Done*    │ Plumbing + toggles + policy real; shipped as opt-out, not opt-in (see 2) │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P0-2           │ Policy truth + URL               │ Done     │ §2 rewritten, hosted on GitHub, URL pinned by test                       │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P0-3           │ FGS/BLE crash                    │ Resolved │ BLE stack is fully inert — no crash possible (see 4)                     │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P0-4           │ Lint baseline                    │ Done     │ Deleted; lintRelease green, confirmed                                    │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P0-5           │ Release smoke                    │ Partial  │ Bundle + R8 + mapping upload green; on-device loop still yours           │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P0-6           │ Versioning                       │ Done     │ 0.0.4 + versioning doc; 1.0.0 at branch                                  │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P1-7           │ Manifest guard test              │ Done     │ JVM + instrumented; proven running against the real merged manifest      │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P1-8           │ Speed/elevation                  │ Done     │ Stripped from manifest, policy, and policy doc — all agree               │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P1-9           │ Firebase config                  │ Done     │ ylabz variant deleted; key restrictions are console-side                 │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P1-10          │ HC declaration                   │ Yours    │ Guide written; console approval unverifiable from here                   │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P1-11          │ Field tests                      │ Yours    │ Protocols written; execution needs hardware                              │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P2-12          │ Backup rules                     │ Done     │ Wired; DB + prefs excluded (see 6)                                       │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P2-13          │ Notif timing                     │ Partial  │ Disclosure dialog exists; the test proves nothing (see 3)                │
  ├────────────────┼──────────────────────────────────┼──────────┼──────────────────────────────────────────────────────────────────────────┤
  │ P2-14/15/16/17 │ Perms, ARCore, hygiene, trackers │ Done     │ All verified, including NFC-unused and intermediate removal              │
  └────────────────┴──────────────────────────────────┴──────────┴──────────────────────────────────────────────────────────────────────────┘

  Also confirmed: merged release manifest carries exactly the 8 HC permissions matching the runtime policy, and uploadCrashlyticsMappingFileRelease runs in the build.

  New findings, ranked

  1. Glass suite is red (2 failures) — and it fails the module-wide gate. Cause: the Nov 19 tests were written against an API the May-era GlassViewModel doesn't implement (setGear delegation, GEAR_LIST
     mapping) — stale on arrival, never green. XR is out of scope, but glass ships inside the phone APK. Either align the two expectations (small, mechanical) or explicitly exclude glass from the release gate.
  2. Consent shipped as opt-out. Defaults are true, no first-run prompt, no manifest flags — collection runs from install until the user finds Settings. The policy doc now honestly says "Enabled by Default,"
     so this is coherent, just not what the punch list specified. If you keep it: add the manifest flags to close the pre-onCreate window and call resetAnalyticsData() on opt-out. If not: flip defaults + add
     the prompt.
  3. Two vacuous tests. NotificationPermissionTimingTest asserts a string constant equals itself; TelemetryConsentTest's first test asserts the mock it just scripted. Both pass while proving nothing — delete
     or make them real.
  4. BLE is entirely stubbed (simulator with simulate = false, real GATT repo unreachable — no scan/connect call exists on any AshBike path). So strip BLUETOOTH_SCAN/BLUETOOTH_CONNECT per minimum scope, and
     reconcile policy §C plus the "sync including heart rate data" claim: phone-only rides currently have no HR source at all.
  5. JVM guard test can silently skip if the merged manifest is absent (early return = pass). It ran for real today, but make it fail-closed with an existence assert so CI can't go green-vacuous.
  6. Device-transfer backup excludes everything — rides and settings won't migrate to a new phone. Reasonable with Health Connect as the continuity path, but confirm it's intentional.

  Test tally (independently run)

  59 green, 0 failures across phone scope: app 16, database 16, data 6, features/main 8, home 2, rides 3, settings 5, health:core 3 — plus lintRelease, manifest processing, and bundleRelease all green. Only
  glass is red (item 1 above).

  Remaining work that only you can do: install the release AAB and run the ride loop, execute the field-test protocols, confirm the HC console approval, and restrict the API keys. Everything else on the punch
  list is closed or has a concrete follow-up above.

◆ Worked for 10m 58s · 1:40 PM · 23/Nov