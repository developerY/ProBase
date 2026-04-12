# Isolate PhotoDo Camera and Save Photo Feature

The goal is to move the camera capture and "Save Photo" logic into its own isolated feature module within the PhotoDo project. This will facilitate the future integration of AI-powered auto-fill features.

## Proposed Changes

### 1. Create New Module `:applications:photodo:features:camera`

- **Update `settings.gradle.kts`**: Include the new module.
- **Create `build.gradle.kts`**: Configure dependencies including Room, Hilt, Compose, and the shared `:features:camera` module.

### 2. Move Logic from `:applications:photodo:apps:mobile:features:tasks` to `:applications:photodo:features:camera`

- **Move UI Components**:
    - [SavePhotoBottomSheet.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/components/SavePhotoBottomSheet.kt) -> `:applications:photodo:features:camera`
- **Move ViewModels and State**:
    - [SavePhotoViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/SavePhotoViewModel.kt) -> `:applications:photodo:features:camera`
    - [SavePhotoUiState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/SavePhotoUiState.kt) -> `:applications:photodo:features:camera`
- **Move Domain Logic**:
    - [AddPhotoToTaskUseCase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/domain/AddPhotoToTaskUseCase.kt) -> `:applications:photodo:features:camera`

### 3. Refactor Navigation

- **Update `photoTodoNavEntryProvider.kt`**:
    - Update imports to use the new module location for `SavePhotoViewModel` and `SavePhotoBottomSheet`.
- **Update `PhotoDoMainScreen.kt`**:
    - Ensure dependencies are updated if necessary.

### 4. Update Application Dependency Graph

- **`mobile/build.gradle.kts`**: Add implementation dependency on `:applications:photodo:features:camera`.

---

## Technical Details

### Module Namespace
`com.zoewave.probase.photodo.features.camera`

### Internal Visibility
Where appropriate, use `internal` to keep the module's implementation details hidden.

---

## Verification Plan

### Automated Tests
- Run `:applications:photodo:features:camera:assembleDebug`
- Run `:applications:photodo:apps:mobile:assembleDebug`

### Manual Verification
- Launch the app, navigate to the camera, capture a photo, and verify that the "Save Photo" sheet still works correctly and persists data to the database.
- Verify that the navigation backstack remains stable after saving.
