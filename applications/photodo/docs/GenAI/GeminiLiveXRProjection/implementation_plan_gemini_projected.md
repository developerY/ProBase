# Implementation Plan - Photodo Gemini Live & XR Projection

Add real-time voice-based task management and XR projection to the Photodo app, inspired by the `gemini-live-todo` sample.

## Proposed Changes

### [Component] Photodo Features

#### [MODIFY] [TaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailScreen.kt)
- Add a "Project to Glasses" button in the `TopAppBar`.
- Implement logic to check for XR glasses connection and launch the projected activity.

#### [NEW] [ProjectedTaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/ProjectedTaskDetailScreen.kt)
- A Glimmer-optimized UI for the AI glasses.
- Displays the current project's task list in a high-contrast, readable format for see-through displays.
- Includes a `VoiceInputIndicator` to show Gemini Live session status.

#### [NEW] [PhotoDoLiveSessionManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/data/PhotoDoLiveSessionManager.kt)
- Manages the Gemini Live session using the Firebase AI SDK.
- **Tool Calling Integration**: Implements tools like `addTask`, `toggleTaskStatus`, and `getTaskList` so users can manage their projects hands-free via voice.
- Handles audio streaming between the glasses and Gemini.

#### [NEW] [PhotoDoGlassesActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/PhotoDoGlassesActivity.kt)
- Standalone activity registered with `requiredDisplayCategory="xr_projected"`.
- Bridges the mobile project state to the glasses.
- Observes the Gemini Live session and updates the Glimmer UI.

### [Component] Build & Configuration

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/build.gradle.kts)
- Add dependencies for:
    - `androidx.xr.projected`
    - `androidx.xr.glimmer`
    - `com.google.firebase:firebase-ai` (if not already inherited)
    - `androidx.camera` (for future vision tasks)

#### [MODIFY] [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/AndroidManifest.xml)
- Add `<uses-permission android:name="android.permission.RECORD_AUDIO" />`.
- Register `PhotoDoGlassesActivity`.

## Verification Plan

### Automated Tests
- Build the `:applications:photodo:apps:mobile:features:tasks` module to verify dependency alignment.

### Manual Verification
- Deploy Photodo to a phone and an XR glasses emulator.
- Open a project, tap the "Project" icon.
- Verify the task list appears on the glasses.
- Start a Gemini Live session and try saying "Add a task to buy paint" to see if it updates the list in real-time.
