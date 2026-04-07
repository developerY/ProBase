# Consolidate Shots and Hydration into Input Module (Reused)

This plan outlines the steps to create a new `input` feature module that hosts both caffeine shot and hydration tracking by reusing the existing `shots` and `hydration` modules. This will simplify the bottom navigation to a clean 3-tab layout (Home, Log, Settings).

## User Review Required

- **UI Structure**: The "Log" tab (Input module) will contain a tabbed or paged interface to switch between Caffeine and Water logging.
- **Module Maintenance**: The original `shots` and `hydration` feature modules will be KEPT and REUSED as dependencies of the new `input` module.

## Proposed Changes

### GoSwift Feature Input Module (`applications/goswift/apps/mobile/features/input`)

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/input/build.gradle.kts)
- Include dependencies on `:applications:goswift:apps:mobile:features:shots` and `:applications:goswift:apps:mobile:features:hydration`.

#### [NEW] [InputUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/input/src/main/java/com/zoewave/probase/goswift/mobile/input/ui/InputUiRoute.kt)
- Main container for the "Log" tab.
- Implements a TabRow to switch between Caffeine and Water by calling `ShotsScreen` and `HydrationScreen` from their respective modules.

---

### Navigation and App Shell

#### [GoSwiftDestination.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/features/main/src/main/java/com/zoewave/probase/goswift/features/main/navigation/GoSwiftDestination.kt)
- Add `Log` destination.
- Keep `Shots`, `AddShot`, and `Hydration` for internal routing or direct access if needed (but primarily `Log` will be in the bottom bar).

#### [GoSwiftMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/ui/components/GoSwiftMainScreen.kt)
- Update `GoSwiftBottomBar` to show 3 items: Home, Log (Input), Settings.
- Use `Icons.Default.Add` or `Icons.Default.EditNote` for the Log tab.

#### [goSwiftNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/ui/navigation/goSwiftNavEntryProvider.kt)
- Update to handle `GoSwiftDestination.Log` by showing `InputUiRoute`.

---

### Project Configuration

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)
- Include `:applications:goswift:apps:mobile:features:input`.
- Keep existing `:shots` and `:hydration` includes.

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/build.gradle.kts) (App Module)
- Update dependencies to include `:applications:goswift:apps:mobile:features:input`.
- Keep `:shots` and `:hydration` if needed, but they can be transitive through `:input`.

---

## Verification Plan

### Automated Tests
- Run existing tests for Shots and Hydration.
- Command: `./gradlew :applications:goswift:apps:mobile:features:input:assembleDebug`

### Manual Verification
- Deploy the app and verify the 3-tab bottom navigation.
- Navigate to the "Log" tab and verify both Caffeine and Water logging screens are functional and correctly reusing the underlying modules.
