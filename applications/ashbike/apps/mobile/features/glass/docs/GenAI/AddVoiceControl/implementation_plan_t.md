# Voice Command Gear Control for AI Glasses

Implement voice command support to change bike gears using the microphone on Samsung/Google AI Glasses (Android XR). This will allow hands-free operation while riding.

## User Review Required

> [!IMPORTANT]
> - **Always-On Listening**: The implementation will keep the microphone active while the Glass mode is active to listen for commands. This might impact battery life.
> - **Permissions**: The user will be prompted on their phone to grant `RECORD_AUDIO` permission for the glasses.

## Proposed Changes

### [Glass Module] (applications/ashbike/apps/mobile/features/glass)

#### [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/src/main/AndroidManifest.xml)
- Add `android.permission.RECORD_AUDIO` permission.

#### [NEW] [VoiceGearController.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/src/main/java/com/zoewave/ashbike/mobile/glass/audio/VoiceGearController.kt)
- Create a lifecycle-aware component that manages `SpeechRecognizer`.
- Implement `RecognitionListener` to detect gear commands ("gear up", "gear down", etc.).
- Call `BikeRepository` methods to change gears.
- Handle automatic restart of listening for "always-on" behavior.

#### [GlassesMainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/src/main/java/com/zoewave/ashbike/mobile/glass/GlassesMainActivity.kt)
- Integrate `VoiceGearController`.
- Implement `ProjectedPermissionsResultContract` to request microphone access on the glasses.
- Start the voice controller once permission is granted.

#### [AudioInterface.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/src/main/java/com/zoewave/ashbike/mobile/glass/audioInterface.kt)
- Add a method to speak specifically for permission rationales if needed (as recommended by XR docs).

## Verification Plan

### Automated Tests
- I will verify the logic by ensuring the code compiles and that the `SpeechRecognizer` is correctly initialized and started in the activity.
- Since I cannot run an actual XR device or emulator with a microphone, I will rely on unit-testing the command matching logic if possible, or verifying the structural correctness of the implementation.

### Manual Verification
- Verify that `RECORD_AUDIO` is added to the manifest.
- Verify that `GlassesMainActivity` correctly handles the permission flow using `ProjectedPermissionsResultContract`.
- Verify that `VoiceGearController` is correctly lifecycle-bound to the activity.
