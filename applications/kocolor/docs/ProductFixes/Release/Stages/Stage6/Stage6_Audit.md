# Stage 6 Execution Audit

## 6.1 Add Fail-Fast `signingConfigs`
- Configured `signingConfigs.create("release")` reading `RELEASE_STORE_FILE` and other credentials.
- Added a `GradleException` task graph check for `bundleRelease` if credentials are missing.
- Verified that `env -u RELEASE_STORE_FILE ./gradlew :applications:kocolor:apps:mobile:bundleRelease` fails immediately with the required error message.

## 6.2 Archive Oct 18 Artifacts & Build Fresh
- Moved stale `.aab` and `mapping.txt` into `release/archive/0.5-code7/`. (Recorded SHA-256 hashes).
- Ran `./gradlew :applications:kocolor:apps:mobile:bundleRelease`.
- Copied outputs to `release/production/`.
- Verified signature: `jarsigner -verify -strict -certs release/production/mobile-release.aab` output included `jar verified.`
- Extracted Upload Key `SHA256:` fingerprint with `keytool`.
- Confirmed `BUNDLE-METADATA/com.android.tools.build.debugsymbols` contains `.so.sym` files.

## 6.3 & 6.4 Play Integrity App Check Registration
- Checked `AppCheckInitializer` sets `PlayIntegrityAppCheckProviderFactory` for release builds.
- Registered the Play App-Signing SHA-256 certificate in Firebase Console.
- Verified Internal Track Play Store installation and App Check enforcement successfully.
