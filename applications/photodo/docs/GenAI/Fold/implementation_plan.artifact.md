# Adaptive Layout for Foldables

The goal is to implement an adaptive layout for foldable devices using `ListDetailPaneScaffold`. This will provide a 2-pane experience when the device is unfolded (Expanded/Medium width) and a standard 1-pane experience on normal phones (Compact).

## User Review Required

- **Adaptive Entry Point**: I'll implement the adaptive logic for the "Workspace" flow (Categories -> Projects -> Tasks). When the app is on a large screen and the user is in this flow, they will see two panes side-by-side.
- **State Machine**: I'll use a `FoldableState` enum to manage which level of the hierarchy is visible in each pane.

## Proposed Changes

### Configuration and Dependencies

---

#### [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/MainActivity.kt)

- Add `WindowSizeClass` detection using `calculateWindowSizeClass()`.
- Pass the `WindowSizeClass` to `PhotoDoMainScreen`.

#### [PhotoDoMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/PhotoDoMainScreen.kt)

- Accept `WindowSizeClass` as a parameter.
- Pass it down to the `entryProvider`.

### Adaptive UI Components

---

#### [NEW] [AdaptivePhotoDoScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/AdaptivePhotoDoScreen.kt)

- Implement the 2-pane logic using `ListDetailPaneScaffold`.
- Use a state machine (`PhotoDoFoldableState`) to shift panes:
    - `CATEGORY_AND_PROJECTS`: Left = Categories, Right = Projects.
    - `PROJECTS_AND_TASKS`: Left = Projects, Right = Tasks.
- Handle the back button using `BackHandler` to shift states back.

### Navigation

---

#### [photoTodoNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)

- Update `TasksList` and `TaskDetail` (and potentially `CategoryGrid`) routes.
- If `windowSizeClass` is not Compact, use `AdaptivePhotoDoScreen` instead of the standard full-screen composables.

## Verification Plan

### Automated Tests
- Build verification: `./gradlew :applications:photodo:apps:mobile:assembleDebug`

### Manual Verification (requires foldable emulator or tablet)
1. Open the app on a foldable emulator.
2. Unfold the device (Expanded width).
3. Navigate to the "Tasks" tab.
4. Verify that you see Categories on the left and Projects on the right.
5. Tap a Project on the right.
6. Verify that Projects shift to the left and Tasks appear on the right.
7. Press the Back button.
8. Verify that the panes shift back to Categories/Projects.
9. Fold the device.
10. Verify that the app reverts to the standard 1-pane navigation.
