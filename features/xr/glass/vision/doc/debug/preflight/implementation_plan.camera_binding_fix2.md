# Implementation Plan - Fix Camera Binding for AI Glasses

The goal is to resolve the "No available camera can be found" error when using AI glasses. This error occurs because the `ProcessCameraProvider` obtained via the `ProjectedContext` sometimes reports zero cameras, either due to initialization lag, missing glasses-side permissions, or incorrect camera mapping.

## User Review Required

> [!IMPORTANT]
> I will be implementing a "Context Probing" loop similar to the one used in the Seaweed app. This will try to bind to the Glasses camera first, then fallback to the Phone camera if the glasses hardware is truly unavailable.
> 
> [!WARNING]
> Accessing the glasses camera requires **glasses-specific permissions**. The current check only verifies the phone's permission. I will update the code to check both.

## Proposed Changes

### Vision Feature (`features/xr/glass/vision`)

#### [MODIFY] [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionViewModel.kt)
- Update `setupCamera` to be a coroutine-based setup.
- Implement a loop that probes:
    1. **Glasses Context**: `ProjectedContext.createProjectedDeviceContext(activity)`
    2. **Host Context**: `ProjectedContext.createHostDeviceContext(activity)`
    3. **Application Context** (Fallback)
- For each context:
    - Obtain `ProcessCameraProvider`.
    - Log the number of available cameras.
    - Check for `DEFAULT_BACK_CAMERA` and then `DEFAULT_FRONT_CAMERA`.
    - If found, bind the lifecycle and stop the loop.
- Add `isGlassesPermissionGranted` to `VisionUiState`.
- Add a method to check permission on the projected context.

#### [MODIFY] [VisionRequirementGate.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/components/VisionRequirementGate.kt)
- Update to check both phone-side and glasses-side permissions using the ViewModel's state.
- Ensure the "Grant Permission" button triggers the correct `ProjectedPermissionsResultContract` flow if the glasses permission is missing.

#### [MODIFY] [UnifiedVisionScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/UnifiedVisionScreen.kt)
- Pass the `ProjectedPermissionsLauncher` (from `LiveVisionActivity`) to the gate or handle it via a shared callback.

#### [MODIFY] [LiveVisionActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/LiveVisionActivity.kt)
- Expose the permission launcher or result callback to the UI components.

## Verification Plan

### Automated Tests
- Build the module: `./gradlew :features:xr:glass:vision:assembleDebug`

### Manual Verification
1.  **Launch Vision AI**: Verify the Event Log shows the probing sequence.
2.  **Glasses Permission**: If glasses permission is missing, verify the Gate screen shows FAIL for glasses camera and offers a button to grant it.
3.  **Binding Success**: Verify the log shows `SUCCESS: Bound to Glasses camera`.
4.  **Capture**: Take a picture and verify it uses the glasses camera (outward facing).
5.  **Reconnection**: Disconnect and reconnect glasses; verify the camera re-initializes.
