# Adaptive Layout for Home and Settings

The goal is to fix the "Stretched UI" on large screens by splitting the Home screen into two panes and constraining the width of the Settings screen.

## Proposed Changes

### UI Components

---

#### [HomeScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeScreen.kt)

- I will keep the current `HomeScreen` as the compact (mobile) view.
- I'll rename the current `HomeScreen` to `CompactHomeScreen` internally if needed, or just keep it as is and use it inside `AdaptiveHomeScreen`.

#### [NEW] [AdaptiveHomeScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/AdaptiveHomeScreen.kt)

- Implement `AdaptiveHomeScreen` which uses `ListDetailPaneScaffold` when the device is unfolded.
- **Left Pane (Dashboard)**: Contains the `OverviewSummaryCard` and "Jump Back In" project list.
- **Right Pane (Directory)**: Contains the `HomeOverviewScreen` (Category Grid).
- Fallback to `HomeScreen` when the screen is Compact.

#### [SettingsScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/src/main/java/com/zoewave/probase/photodo/mobile/features/settings/ui/components/SettingsScreen.kt)

- Wrap the main `Column` in a `Box` with `contentAlignment = Alignment.TopCenter`.
- Apply `.widthIn(max = 600.dp)` to the main `Column` to prevent it from stretching on large screens.

### Navigation

---

#### [photoTodoNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)

- Update the `PhotoTodoRoute.Home` entry to use `AdaptiveHomeScreen` instead of `HomeScreen`.
- Pass the `windowSizeClass` down to `AdaptiveHomeScreen`.

## Verification Plan

### Automated Tests
- Build verification: `./gradlew :applications:photodo:apps:mobile:assembleDebug`

### Manual Verification
1. Open the app on a foldable emulator or tablet.
2. **Home Screen**:
    - Verify that when unfolded, the screen splits into a Dashboard (Left) and Category Grid (Right).
    - Verify that when folded, it shows the standard vertical list.
3. **Settings Screen**:
    - Verify that on a large screen, the settings content is centered and capped at 600dp width instead of stretching across the whole screen.
