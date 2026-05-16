# Fix Health Permission UI and Add Data Button

This plan addresses two issues in the Health feature:
1. The UI not updating to "granted" after permissions are provided. This is caused by a mismatch between the permissions requested in `HealthViewModel` and those declared in the `AndroidManifest.xml` of the various applications using the feature.
2. The need for a button to add health data to Google Health Connect within the app.

## Proposed Changes

### [Health Core Feature](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core)

#### [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/AndroidManifest.xml)

- Declare all Health Connect permissions required by `HealthViewModel`. This ensures that all applications consuming this library will have the necessary permissions merged into their manifests.

```xml
    <uses-permission android:name="android.permission.health.READ_STEPS" />
    <uses-permission android:name="android.permission.health.WRITE_STEPS" />
    <uses-permission android:name="android.permission.health.READ_EXERCISE" />
    <uses-permission android:name="android.permission.health.WRITE_EXERCISE" />
    <uses-permission android:name="android.permission.health.READ_HEART_RATE" />
    <uses-permission android:name="android.permission.health.WRITE_HEART_RATE" />
    <uses-permission android:name="android.permission.health.READ_DISTANCE" />
    <uses-permission android:name="android.permission.health.WRITE_DISTANCE" />
    <uses-permission android:name="android.permission.health.READ_TOTAL_CALORIES_BURNED" />
    <uses-permission android:name="android.permission.health.WRITE_TOTAL_CALORIES_BURNED" />
    <uses-permission android:name="android.permission.health.READ_WEIGHT" />
    <uses-permission android:name="android.permission.health.WRITE_WEIGHT" />
    <uses-permission android:name="android.permission.health.READ_SLEEP" />
```

#### [OverviewTab.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/java/com/zoewave/probase/features/health/core/ui/overview/OverviewTab.kt)

- Update `OverviewTab` to accept an `onEvent` callback.
- Add a "Quick Add" button at the bottom of the overview to trigger a test data insertion.

#### [HealthDashboard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/core/src/main/java/com/zoewave/probase/features/health/core/ui/components/HealthDashboard.kt)

- Pass the `onEvent` callback from `HealthDashboard` to `OverviewTab`.

---

## Verification Plan

### Automated Tests
- Run `./gradlew :features:health:core:assembleDebug` to verify compilation.

### Manual Verification
- Verify that the `AndroidManifest.xml` in `features/health/core` contains all 13 permissions.
- Verify that `OverviewTab` now includes a button to add health data.
- Verify that `HealthDashboard` correctly passes the event handler to `OverviewTab`.
