# Fix Adaptive Camera Navigation

The goal is to fix the issue where the "Add Camera" button in `TaskDetailScreen` does not work when the device is unfolded. This is caused by `AdaptivePhotoDoScreen` not forwarding non-back navigation events to the main navigation controller.

## Proposed Changes

### UI Components

---

#### [AdaptivePhotoDoScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/AdaptivePhotoDoScreen.kt)

- Update the signature to accept `navTo: (PhotoTodoRoute) -> Unit`.
- Update the internal `navTo` lambdas for `HomeOverviewScreen`, `TasksListScreen`, and `TaskDetailScreen` to:
    - Handle back navigation (null) internally by shifting state.
    - Forward other routes (like `PhotoTodoRoute.Camera`) to the top-level `navTo`.

### Navigation

---

#### [photoTodoNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)

- Update calls to `AdaptivePhotoDoScreen` to pass the `navigateTo` callback.

#### [AdaptiveHomeScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/AdaptiveHomeScreen.kt)

- Ensure it also correctly forwards any unexpected navigation events if necessary (though it already seems to handle `navTo`).

## Verification Plan

### Automated Tests
- Build verification: `./gradlew :applications:photodo:apps:mobile:assembleDebug`

### Manual Verification
1. Open the app on a foldable emulator or tablet.
2. Unfold the device.
3. Navigate to a Category and then a Project.
4. Open the FAB menu and tap the Camera button.
5. Verify that the camera opens correctly.
6. Take a photo and verify it saves to the project.
7. Repeat the same steps when folded to ensure no regressions.
