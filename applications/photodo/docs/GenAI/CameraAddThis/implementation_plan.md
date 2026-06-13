# Implementation Plan - Photodo Gemini Multimodal "Add this item"

Add multimodal capabilities to the Photodo XR experience, allowing users to say "Add this item to my project images" to capture a photo from the glasses and save it to their project.

## Proposed Changes

### [Component] Photodo Features

#### [MODIFY] [PhotoDoLiveSessionManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/data/PhotoDoLiveSessionManager.kt)
- Add a new tool: `captureProjectImage`.
- Implement a `SharedFlow` to notify the Activity when Gemini requests an image capture.
- Update `handleToolCall` to emit to this flow.

#### [MODIFY] [PhotoDoGlassesActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/PhotoDoGlassesActivity.kt)
- Integrate **CameraX `ImageCapture`**.
- Observe the capture requests from `PhotoDoLiveSessionManager`.
- Implement `takePicture` logic to save the photo and update the project via `viewModel`.

#### [MODIFY] [ProjectedTaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/ProjectedTaskDetailScreen.kt)
- Add a visual feedback for "Capturing..." state.

### [Component] Build & Infrastructure

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/build.gradle.kts)
- Add CameraX dependencies (`core`, `camera2`, `lifecycle`, `view`).

#### [MODIFY] [TaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailScreen.kt)
- Request `CAMERA` permission if not granted before launching projection.

## Verification Plan

### Automated Tests
- Build the task feature module to verify CameraX integration.

### Manual Verification
- Deploy to phone and XR glasses.
- Start a Gemini Live session on the glasses.
- Say "Add this item to my project images".
- Verify that a photo is taken and appears in the project's photo gallery.
