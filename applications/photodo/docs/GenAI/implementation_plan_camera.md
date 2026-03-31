# Add Camera FAB with Instant Camera and Post-Photo Selection

The goal is to add a "Global FAB" to the `photodo` app that instantly opens the camera. Once a photo is taken, the user should be prompted via a Bottom Sheet to select a category and project to save the photo to.

## User Review Required

- **Global FAB Placement**: Currently, some screens like `HomeOverviewScreen` and `TasksListScreen` have their own FABs. Adding a "Global FAB" in `PhotoDoMainScreen`'s `Scaffold` will cause overlap. I plan to move existing FAB actions into a single Global FAB menu or similar. However, for "Instant Camera", I might need to make the camera the primary action.
- **Bottom Sheet vs. Route**: After taking a photo, I'll navigate to a `SavePhoto(uri)` route which will display the selection UI. This is cleaner than trying to manage a bottom sheet across multiple screens.

## Proposed Changes

### Navigation and Routing

---

#### [PhotoTodoRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/model/src/main/java/com/zoewave/probase/photodo/model/navigation/PhotoTodoRoute.kt)

- Update `Camera` route to have an optional `projectId`.
- Add `SavePhoto(uri: String)` route.

```diff
-    data class Camera(val projectId: Long) : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_camera, icon = Icons.Default.CameraAlt)
+    data class Camera(val projectId: Long? = null) : PhotoTodoRoute(titleRes = R.string.applications_photodo_model_route_camera, icon = Icons.Default.CameraAlt)
+    data class SavePhoto(val photoUri: String) : PhotoTodoRoute(icon = Icons.Default.Save)
```

#### [photoTodoNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)

- Handle `Camera` route's optional `projectId`.
- If `projectId` is null, navigate to `SavePhoto(uri)` after the photo is taken.
- Implement the `SavePhoto` route handling with a `SavePhotoBottomSheet` (or a screen displaying it).

### UI Components

---

#### [NEW] [SavePhotoBottomSheet.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/components/SavePhotoBottomSheet.kt)

- A new Bottom Sheet component that displays categories and allows filtering projects within them.
- Once a project is selected, it saves the photo using `AddPhotoToTaskUseCase`.

#### [PhotoDoMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/PhotoDoMainScreen.kt)

- Add the Global FAB to the main `Scaffold`.
- The FAB will navigate to `PhotoTodoRoute.Camera(projectId = null)`.

### Domain and Data

---

#### [SavePhotoViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/SavePhotoViewModel.kt)

- Handle fetching categories and projects.
- Manage selection state and saving.

## Verification Plan

### Automated Tests
- I'll add a unit test for `SavePhotoViewModel` to ensure it correctly fetches and filters projects.
- Command: `./gradlew :applications:photodo:apps:mobile:testDebugUnitTest`

### Manual Verification
1. Run the app on an emulator.
2. Tap the Global FAB on the home screen.
3. Verify that the camera opens instantly.
4. Take a photo.
5. Verify that a Bottom Sheet appears asking to save to a category/project.
6. Select a category and a project.
7. Verify that the photo is saved and visible in the selected project's details.
