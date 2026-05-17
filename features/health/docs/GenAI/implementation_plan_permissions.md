# Comprehensive Health Permission Consolidation

This plan expands the consolidated Health Connect permissions to include additional data types like Nutrition and Hydration, ensuring all apps (AshBike, KoColor, GoSwift, RxLogic) have the permissions they need now and for future growth.

## Proposed Changes

### [Health Core Feature](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core)

#### [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/AndroidManifest.xml)

- Add Nutrition and Hydration permissions to the core manifest. This makes them available to all apps using the health feature.

```xml
    <uses-permission android:name="android.permission.health.READ_NUTRITION" />
    <uses-permission android:name="android.permission.health.WRITE_NUTRITION" />
    <uses-permission android:name="android.permission.health.READ_HYDRATION" />
    <uses-permission android:name="android.permission.health.WRITE_HYDRATION" />
    <uses-permission android:name="android.permission.health.WRITE_SLEEP" />
```

---

## Verification Plan

### Automated Tests
- Run `./gradlew :features:health:core:assembleDebug` to verify compilation.

### Manual Verification
- Verify that the `AndroidManifest.xml` in `features/health:core` now contains 18 permissions (original 13 + 5 new ones).
- Verify that `RxLogic` and other apps correctly inherit these permissions through manifest merging (can be checked via `merged_manifests` build output if needed, but assemble success is a good indicator).
