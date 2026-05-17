# Health Data Seeding Feature

This plan adds a developer tool to generate a comprehensive set of health data (Steps, Exercise, Sleep, Nutrition, Hydration, Weight, etc.) to seed applications during development.

## Proposed Changes

### [Health Core Feature](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core)

#### [HealthEvent.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/java/com/zoewave/probase/features/health/core/ui/HealthEvent.kt)

- Add `object SeedData` to `HealthEvent` sealed interface.

#### [HealthViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/java/com/zoewave/probase/features/health/core/ui/HealthViewModel.kt)

- Implement `seedHealthData()` method that uses `HealthSessionManager` to generate and write multiple types of data:
    - Multiple exercise sessions (Run, Bike, Walk).
    - Daily steps, calories, and distance for the last 7 days.
    - Sleep data for the last 7 days.
    - Sample nutrition and hydration logs.
    - Weight records.
- Add `SeedData` event handling to `onEvent`.

#### [SettingsTab.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/java/com/zoewave/probase/features/health/core/ui/settings/SettingsTab.kt)

- Add a "Seed Comprehensive Data" button in the Debug Tools section.

#### [strings.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/res/values/strings.xml)

- Add string resource for the new button: `features_health_action_seed_data`.

---

## Verification Plan

### Automated Tests
- Run `./gradlew :features:health:core:assembleDebug` to verify compilation.

### Manual Verification
- Verify the "Seed Comprehensive Data" button appears in the Settings tab under Debug Tools.
- Verify that clicking the button triggers the seeding logic (can be observed via logcat as `HealthSessionManager` has extensive logging).
- Verify that the Overview tab updates with the new data after seeding.
