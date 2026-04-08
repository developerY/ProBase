# Create GoSwift Wear OS Application

This plan outlines the steps to build a gold-standard Wear OS application for GoSwift, mirroring the features of the mobile app while following Wear OS best practices (Horologist, Material3 Wear, Swipe-to-dismiss).

## User Review Required

- **Wear UI Structure**: The Wear app will use a vertical-scrolling home screen for a quick health overview and a dedicated input screen for logging caffeine, water, and calories.
- **Transitive Module Reuse**: The Wear app will reuse the core logic from `:goswift:data` and `:core:data`, but will have its own optimized Wear UI components.

## Proposed Changes

### Wear App Module (`applications/goswift/apps/wear`)

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/build.gradle.kts)
- Configure Wear OS app with Material3 Wear and Horologist dependencies.
- Depend on `:core:data`, `:goswift:data`, and `:goswift:model`.

#### [NEW] [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/src/main/AndroidManifest.xml)
- Standard Wear OS manifest with Health Connect permissions and `WAKE_LOCK`.

#### [NEW] [GoSwiftWearTheme.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/src/main/java/com/zoewave/probase/goswift/wear/ui/theme/GoSwiftWearTheme.kt)
- Wear OS specific Material3 theme using `androidx.wear.compose.material3`.

#### [NEW] [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/src/main/java/com/zoewave/probase/goswift/wear/MainActivity.kt)
- Standard Wear entry point with Splash Screen support.

#### [NEW] [GoSwiftWearMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/src/main/java/com/zoewave/probase/goswift/wear/ui/GoSwiftWearMainScreen.kt)
- Root Composable with `AppScaffold`, `SwipeToDismissBox`, and `NavDisplay`.

---

### Feature Modules (Wear Optimized)

#### [NEW] [HomeRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/features/home/src/main/java/com/zoewave/probase/goswift/wear/home/ui/HomeRoute.kt)
- Home dashboard for Wear OS using `ScalingLazyColumn` (via Horologist).
- Displays current caffeine, sleep, exercise, and hydration levels.

#### [NEW] [LogRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/wear/features/input/src/main/java/com/zoewave/probase/goswift/wear/input/ui/LogRoute.kt)
- Input screen for Wear OS.
- Quick buttons for logging caffeine shots, water (250ml/500ml), and a simplified calorie entry.

---

### Project Integration

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)
- Include `:applications:goswift:apps:wear`.
- Include `:applications:goswift:apps:wear:features:home`.
- Include `:applications:goswift:apps:wear:features:input`.

---

## Verification Plan

### Manual Verification
- Deploy to Wear OS emulator.
- Verify Health Connect permission request flow on the watch.
- Verify dashboard data matches mobile data (via Health Connect sync).
- Verify logging from the watch correctly updates Health Connect.
