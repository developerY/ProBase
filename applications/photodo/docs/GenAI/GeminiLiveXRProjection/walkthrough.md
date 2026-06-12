# Walkthrough - Photodo Gemini Live & XR Projection

I have successfully integrated **Gemini Live** voice interactions and **XR Projection** into the Photodo app. This implementation allows users to project their task list onto AI glasses and manage their projects hands-free using real-time voice commands.

## Key Components

### 1. Gemini Live with Tool Calling
- **[PhotoDoLiveSessionManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/data/PhotoDoLiveSessionManager.kt)**: Manages the bidirectional audio stream and defines tools that Gemini can invoke:
    - `addTask(text: String)`: Adds a new item to the checklist.
    - `setTaskStatus(taskId: Long, isChecked: Boolean)`: Toggles task completion.
- When the user speaks, Gemini can understand the intent and directly call these local functions to update the Room database.

### 2. Projected Glimmer UI
- **[ProjectedTaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/ProjectedTaskDetailScreen.kt)**: A high-contrast, spatial-optimized UI for display glasses.
    - Uses `GlimmerTheme` to ensure readability on additive displays.
    - Includes a `VoiceInputIndicator` that visualizes Gemini's listening state.
    - Features a simplified checklist for ergonomic viewing in AR.

### 3. Cross-Device Projection
- **[TaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailScreen.kt)**: Added a "Project" button that leverages the `androidx.xr.projected` SDK.
- **[PhotoDoGlassesActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/PhotoDoGlassesActivity.kt)**: The entry point for the glasses, bridging the phone's project state and starting the AI session.

## Implementation Details

### Permissions
> [!IMPORTANT]
> Added `android.permission.RECORD_AUDIO` to the manifest. The phone app is responsible for requesting this permission before initiating the XR projection to ensure a seamless experience on the glasses.

### Build Configuration
- Added `androidx.xr.glimmer`, `androidx.xr.projected`, and `com.google.firebase:firebase-ai` dependencies to the tasks feature module.
- Resolved Dagger/Hilt binding conflicts by aligning the `AiConfigurationSettings` provision across the project.

## Verification Results

### Build Status
- [x] Successfully compiled `:applications:photodo:apps:mobile` with all new AI and XR components.

### Layout Verification
- [x] Verified `ProjectedTaskDetailScreen` via Compose Preview with a black background simulation for additive displays.
