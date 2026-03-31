# Implementation Plan - Firebase Crashlytics and Analytics Integration

Add Google Firebase Crashlytics and Google Analytics for Firebase to all application modules (PhotoDo, Seaweed, AshBike, and GoSwift) to improve error tracking and user insight.

## User Review Required

> [!IMPORTANT]
> This plan assumes that `google-services.json` files for each project are either already present in their respective app modules or will be provided/configured by the user later. The plan focus on the build configuration.

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)
- [x] Verified that Firebase BOM, Crashlytics, and Analytics are already defined.
- [x] Verified that Google Services and Firebase Crashlytics plugins are already defined.

---

### PhotoDo Project

#### [build.gradle.kts (PhotoDo Mobile)](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/build.gradle.kts)
- Add missing `google-services` plugin.
- Verified dependencies are already present.

---

### Seaweed Project

#### [build.gradle.kts (Seaweed Mobile)](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/build.gradle.kts)
- Add `google-services` and `firebase-crashlytics` plugins.
- Add Firebase BOM, Crashlytics, and Analytics dependencies.

---

### GoSwift Project

#### [build.gradle.kts (GoSwift Mobile)](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/build.gradle.kts)
- Add `google-services` and `firebase-crashlytics` plugins.
- Add Firebase BOM, Crashlytics, and Analytics dependencies.

---

### AshBike Project

#### [build.gradle.kts (AshBike Mobile)](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/build.gradle.kts)
- [x] Verified that both plugins and dependencies are already present.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure that the projects build successfully with the new plugins and dependencies.
- Specifically:
    - `./gradlew :applications:photodo:apps:mobile:assembleDebug`
    - `./gradlew :applications:seaweed:apps:mobile:assembleDebug`
    - `./gradlew :applications:goswift:apps:mobile:assembleDebug`
    - `./gradlew :applications:ashbike:apps:mobile:assembleDebug`

### Manual Verification
- Check the generated build artifacts to ensure the Google Services task is triggered during the build process.
