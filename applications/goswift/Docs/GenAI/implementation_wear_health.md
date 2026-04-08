# GoSwift Standalone Wear OS Integration (Health Connect Direct)

This plan redesigns the GoSwift Wear OS app to use **Health Connect as the single source of truth**. Both the phone and watch apps will read/write directly to Health Connect, eliminating the need for a custom Wearable Data Layer sync mechanism. This approach targets Wear OS 5+ (Android 14/15) where Health Connect is a system module.

## User Review Required

- **Target Version**: This direct integration requires Wear OS 5 or higher. For older watch versions, the app will show a "Health Connect Unavailable" message.
- **Permission Flow**: Users will need to grant health permissions on the watch. Depending on the system, this may involve a notification or prompt to complete the setup on the paired phone.

## Proposed Changes

### Core Data Module (`core:data`)

#### [HealthConnectRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/health/HealthConnectRepositoryImpl.kt)
- **Safe Initialization**: Ensure the `HealthConnectClient` is only initialized if the system supports it.
- **Standard Implementation**: Use the same `insert` and `read` methods for both platforms.

### GoSwift Data Layer (`goswift:data`)

- **Unified Logic**: Both mobile and wear ViewModels will use the same `HealthRepository` and `HydrationRepository` interfaces, which now point to the shared `HealthConnectRepository`.

---

### GoSwift Wear OS App (`applications/goswift/apps/wear`)

#### [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/src/main/java/com/zoewave/probase/goswift/wear/MainActivity.kt)
- Implement Wear-specific permission handling.
- Use `PermissionController.createRequestPermissionResultContract()` to trigger the setup flow.

#### [HomeViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/features/home/src/main/java/com/zoewave/probase/goswift/wear/home/ui/HomeViewModel.kt)
- Fetch sleep, exercise, and hydration data directly from the repository.
- No changes to the logic needed, as it now matches the mobile implementation.

#### [LogRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/features/input/src/main/java/com/zoewave/probase/goswift/wear/input/ui/LogRoute.kt)
- Direct writes to Health Connect when buttons are pressed.

---

## Verification Plan

### Manual Verification
- Deploy to a **Wear OS 5 emulator** (API 34/35).
- Grant permissions via the watch UI (or phone if prompted).
- Log water/caffeine on the watch and verify they appear in the phone app's dashboard.
- Log activity on the phone and verify it appears on the watch's dashboard.
