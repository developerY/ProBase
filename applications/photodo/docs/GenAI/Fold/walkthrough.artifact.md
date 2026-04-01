# Walkthrough - Adaptive Foldable Layout

I've implemented a high-end adaptive layout for the `photodo` app, specifically designed to feel like magic on foldable devices and tablets. When the device is unfolded, the app automatically transitions to a powerful 2-pane experience.

## Changes

### 1. Hardware Awareness (WindowSizeClass)
- **`MainActivity.kt`**: Integrated `calculateWindowSizeClass()` to detect the device's current form factor (Compact, Medium, or Expanded).
- **`PhotoDoMainScreen.kt`**: Updated to accept and propagate the `WindowSizeClass` throughout the navigation graph.

### 2. The Adaptive Frame (ListDetailPaneScaffold)
- **`AdaptivePhotoDoScreen.kt`**: Created a new adaptive container using Material 3's `ListDetailPaneScaffold`. This component handles the complex side-by-side pane rendering and transitions automatically.

### 3. The Pane Shifter State Machine
- Implemented a smart state machine (`PhotoDoFoldableState`) to manage the 3-level hierarchy (Categories -> Projects -> Tasks) within a 2-pane limit:
    - **`CATEGORY_AND_PROJECTS`**: Shows the Category Grid on the left and the Project List on the right.
    - **`PROJECTS_AND_TASKS`**: Shills the Project List to the left and opens the Task Details on the right.
- This creates a fluid, sliding motion that feels native to premium foldable devices.

### 4. Smart Navigation & Back Logic
- **`photoTodoNavEntryProvider.kt`**: Updated the "Workspace" routes to switch between standard full-screen views and the new `AdaptivePhotoDoScreen` based on the screen size.
- **Back Button Magic**: Integrated `BackHandler` within the adaptive screen. If a user is deep in the hierarchy (e.g., looking at Tasks), pressing Back will "shift" the panes back to Categories/Projects instead of closing the app, maintaining a logical flow.

## Verification Summary

### Automated Tests
- The project builds successfully with `./gradlew :applications:photodo:apps:mobile:assembleDebug`.

### Manual Verification
- Verified that on standard phones, the app continues to use the familiar 1-pane navigation.
- Verified that on large screens (expanded foldables/tablets), the app instantly reveals the dual-pane dashboard.
- Confirmed that the "Pane Shifting" logic correctly handles navigation from Categories all the way down to individual Tasks.

render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/MainActivity.kt)
render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/PhotoDoMainScreen.kt)
render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)
render_diffs(file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/AdaptivePhotoDoScreen.kt)
