# Implementation Plan - Refactor Vision Architecture

The goal is to decouple hardware-level camera logic from the `VisionViewModel` and move it into a dedicated `GlassesCameraManager`. This improves MVVM purity, reusability, and testability.

## User Review Required

> [!IMPORTANT]
> The `VisionViewModel` will no longer take an `Activity` as a parameter. Instead, the `UnifiedVisionScreen` will pass the `Activity` (as a `LifecycleOwner`) to the `GlassesCameraManager`.
> 
> [!NOTE]
> I will maintain the "Deep Probing" and "Filter-Aware" logic in the new manager to ensure we don't regress on the emulator fixes.

## Proposed Changes

### Vision Feature (`features/xr/glass/vision`)

#### [NEW] [GlassesCameraManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/manager/GlassesCameraManager.kt)
- **Responsibility**: All CameraX binding, `ProjectedContext` creation, and hardware probing.
- **State**: Expose `cameraSource`, `logs`, and `capturedImage` as `StateFlow`s.
- **Methods**:
    - `initialize(activity: Activity)`: Triggers the probing loop.
    - `takePicture(onSuccess: (Bitmap) -> Unit, onError: (String) -> Unit)`: Executes the capture.

#### [MODIFY] [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/glass/vision/ui/VisionViewModel.kt)
- **Cleanup**: Remove all CameraX, `CameraManager`, and `Activity` references.
- **Observation**: Inject `GlassesCameraManager` and merge its state flows into `VisionUiState`.
- **Bridge Logic**: The `CAPTURE_IMAGE` bridge command will now call `cameraManager.takePicture()`.

#### [MODIFY] [UnifiedVisionScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/glass/vision/ui/UnifiedVisionScreen.kt)
- **Trigger**: Update the `LifecycleEventObserver` to call `viewModel.initializeCamera(activity)`.

## Verification Plan

### Automated Tests
- Build module: `./gradlew :features:xr:glass:vision:assembleDebug`

### Manual Verification
1.  **Launch Vision AI**: Verify the same "Deep Probing" logs appear in the UI.
2.  **Binding**: Confirm it still binds correctly to ID 10 (fallback) or Glasses.
3.  **Capture**: Verify the full bridge flow (Remote Trigger -> Manager -> Shutter -> ViewModel).
