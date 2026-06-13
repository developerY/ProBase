# Walkthrough - Gemini Multimodal "Add this item" for Photodo

I have implemented the multimodal "Add this item" feature for Photodo on AI glasses. This allows users to capture photos from their wearable hardware and automatically attach them to their projects using voice commands.

## Key Implementation Details

### 1. Multimodal Tooling
- **[PhotoDoLiveSessionManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/data/PhotoDoLiveSessionManager.kt)**:
    - Added a new tool definition: `captureProjectImage`.
    - Implemented a `SharedFlow` (`captureRequests`) that emits whenever Gemini invokes this tool in response to the user's voice command.
    - Updated the `functionCallHandler` to catch and handle this specific multimodal intent.

### 2. CameraX on Glasses
- **[PhotoDoGlassesActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/PhotoDoGlassesActivity.kt)**:
    - Integrated **CameraX `ImageCapture`** directly into the projected activity.
    - Bound the camera to the activity lifecycle using `DEFAULT_BACK_CAMERA` (the glasses' outward-facing camera).
    - Added an observer for `captureRequests` that triggers the `takePicture` flow.

### 3. Spatial Feedback UI
- **[ProjectedTaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/ProjectedTaskDetailScreen.kt)**:
    - Added an `isCapturing` parameter and visual state.
    - When a photo is being taken, a "Capturing Photo..." status appears on the glasses display to provide immediate feedback to the user.

### 4. Integration and Permissions
- **[TaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailScreen.kt)**: Updated the "Project" button logic to check for both `CAMERA` and `RECORD_AUDIO` permissions before launching the XR session.
- **[build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/build.gradle.kts)**: Added the necessary CameraX and XR dependencies.

## Verification Results

### Build Status
- [x] Successfully compiled `:applications:photodo:apps:mobile` with multimodal support.

### Feature Workflow
1. User wears glasses and projects the Photodo task list.
2. User says "Add this item to my project images".
3. Gemini Live identifies the intent and calls the `captureProjectImage` tool.
4. The glasses activity receives the request, shows "Capturing...", and snaps a photo using CameraX.
5. The image is processed and saved to the project (simulated in the current version).
