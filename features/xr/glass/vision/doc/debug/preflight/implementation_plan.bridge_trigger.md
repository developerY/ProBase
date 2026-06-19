# Implementation Plan - Shared Vision Capture via Message Bridge

The goal is to fix the "No available camera found" error by letting the **Glasses Activity** handle the camera hardware, while the **Phone Hub** acts as a remote control. This avoids binding issues in the phone activity and ensures we use the correct hardware.

## User Review Required

> [!IMPORTANT]
> I am moving from a "Local Phone Binding" model to a "Remote Command" model.
> 1. The Phone Hub will send a `CAPTURE_IMAGE` command via a shared repository.
> 2. The Glasses Activity (which is already running) will receive the command and take the picture.
> 3. The result will be shared back to the phone via a Singleton Repository.

## Proposed Changes

### Core Data (`core:data`)
- No changes needed, `GlassBridgeRepository` already exists.

### Vision Feature (`features/xr/glass/vision`)

#### [MODIFY] [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionViewModel.kt)
- Inject `GlassBridgeRepository`.
- **Command Listener**: In `init`, listen to `glassBridgeRepository.glassCommands`. If `CAPTURE_IMAGE` is received and an `imageCapture` is bound, execute `takePicture()`.
- **Remote Trigger**: Implement `triggerGlassesCapture()` which emits the command.
- **Deep Debugging**: Use `CameraManager` inside `setupCamera` to log the exact cameras available to the OS for the current context.

#### [MODIFY] [UnifiedVisionScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/UnifiedVisionScreen.kt)
- Update the **TRIGGER GLASSES CAMERA** button to use the new `triggerGlassesCapture()` method.

#### [MODIFY] [app/src/main/AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/AndroidManifest.xml)
- Add `android:requiredDisplayCategory="xr_projected"` to `MainActivity`. This helps the OS expose projected hardware to the phone's main process.

## Verification Plan

### Automated Tests
- Build the module: `./gradlew :features:xr:glass:vision:assembleDebug`

### Manual Verification
1.  **Launch Vision AI**: Verify both phone and glasses activities are running.
2.  **Verify Binding**: Check Event Log for `SUCCESS: Camera successfully bound to Glasses` (this should now happen on the glasses side).
3.  **Command Flow**: Tap the button on the phone. Verify the phone log shows `Command Sent: CAPTURE_IMAGE`.
4.  **Capture Action**: Verify the glasses-side log shows it received the command and triggered the shutter.
5.  **Result Sync**: Confirm the image appears on the phone preview immediately.
