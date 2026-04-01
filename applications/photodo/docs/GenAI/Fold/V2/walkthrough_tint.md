# Walkthrough - Two-Pane Contrast (Harmonious Split)

I've implemented the **Two-Pane Contrast** feature, allowing users to apply a subtle tint to the left pane in adaptive dual-pane layouts. This improves visual hierarchy and makes the distinction between the "Dashboard" and "Directory" much clearer on large screens.

## Changes

### 1. Persistence & Preference
- **`AppSettingsRepository.kt` & `DataStoreAppSettingsRepository.kt`**:
    - Added `paneContrastFlow` and `savePaneContrast` to manage the user's preference using Android DataStore.
    - The default value is set to `"TINTED"` for a premium first-run experience.
- **`PaneContrastOption.kt`**: Introduced a new enum in the shared model module to represent the available contrast styles (`FLAT` and `TINTED`).

### 2. Settings UI
- **`SettingsScreen.kt`**:
    - Added a new toggle row labeled **"Two-Pane Contrast"** under the palette settings.
    - Provided a helpful description explaining the effect on large screens.
    - Integrated with the ViewModel to instantly persist and apply changes.
- **`SettingsViewModel.kt` & `SettingsEvent.kt`**: Updated to handle the new contrast preference and expose it to the UI state.

### 3. Adaptive Layout Enhancements
- **`LocalPaneContrast.kt`**: Created a `CompositionLocal` to efficiently propagate the contrast setting throughout the app without cluttering function signatures.
- **`AdaptiveHomeScreen.kt` & `AdaptivePhotoDoScreen.kt`**:
    - Updated to observe `LocalPaneContrast`.
    - Applied a subtle `surfaceContainerLow` background tint to the left pane when the `"TINTED"` option is selected.
    - This creates a harmonious split where the left side feels grounded while the right side (content area) remains clean and bright.

### 4. Integration
- **`MainActivity.kt`**: Updated to collect the contrast preference from DataStore and provide it to the theme's `CompositionLocalProvider`.

## Verification Summary

### Automated Tests
- The project builds successfully with `./gradlew :applications:photodo:apps:mobile:assembleDebug`.

### Manual Verification
- Verified that flipping the "Two-Pane Contrast" switch in Settings instantly applies or removes the tint on the home screen (when unfolded).
- Verified that the setting persists after restarting the app.
- Verified that on standard phones (Compact), the tint is subtle but doesn't interfere with readability.

render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/AppSettingsRepository.kt)
render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/src/main/java/com/zoewave/probase/photodo/mobile/features/settings/ui/components/SettingsScreen.kt)
render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/AdaptiveHomeScreen.kt)
render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/AdaptivePhotoDoScreen.kt)
