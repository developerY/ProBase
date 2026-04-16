# Integrate Play Age Signals for Compliance

Add a new feature module `:features:compliance` to integrate the Play Age Signals API. This will allow all apps in the ProBase project to comply with the 2026 App Store Accountability Acts by retrieving user age signals from Google Play.

## User Review Required

> [!IMPORTANT]
> The `com.google.android.play:age-signals` library is currently at version `0.0.3` (as of Feb 2026). This version includes the `DECLARED` status and `SDK_VERSION_OUTDATED` error handling.

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)

- Add `playAgeSignals` version and `google-play-age-signals` library definition.
```toml
[versions]
playAgeSignals = "0.0.3"

[libraries]
google-play-age-signals = { group = "com.google.android.play", name = "age-signals", version.ref = "playAgeSignals" }
```

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)

- Include the new `:features:compliance` module.

---

### [NEW] [features/compliance]

New library module to wrap Play Age Signals API.

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/build.gradle.kts)

- Use convention plugins: `composetemplate.android.library` and `composetemplate.android.hilt`.
- Dependencies: `google-play-age-signals`, `kotlinx-coroutines-play-services`, `hilt-android`.

#### [NEW] [AgeSignal.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/model/AgeSignal.kt)

- Data classes and enums to represent age range and verification status, decoupling the app from the Play library's internal models.

#### [NEW] [AgeSignalsManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/AgeSignalsManager.kt)

- Interface for retrieving age signals.
```kotlin
interface AgeSignalsManager {
    suspend fun getAgeSignal(): AgeSignal
}
```

#### [NEW] [AgeSignalsManagerImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/AgeSignalsManagerImpl.kt)

- Implementation using `AgeSignalsManagerFactory` from the Play library.
- Uses `await()` from `kotlinx-coroutines-play-services` for a clean suspend API.

#### [NEW] [ComplianceModule.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/di/ComplianceModule.kt)

- Hilt module to provide `AgeSignalsManager`.

---

### [PhotoTodo App]

Demonstrate integration of the new module.

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/build.gradle.kts)

- Add `implementation(project(":features:compliance"))`.

## Verification Plan

### Automated Tests
- Create a unit test `AgeSignalsManagerTest` in the new module.
- Mock the Play API (or use `FakeAgeSignalsManager` if possible) to verify mapping logic.
- Command: `./gradlew :features:compliance:testDebugUnitTest`

### Manual Verification
- Verify that the project compiles with the new module and dependency.
- Use `gradlew :features:compliance:assembleDebug` to ensure the module builds correctly.
- Check for any dependency conflicts in the `photodo` app after adding the new module.
