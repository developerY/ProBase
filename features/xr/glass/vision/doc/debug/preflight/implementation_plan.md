# Implementation Plan - Vision Pre-flight Permission Gate

The goal is to prevent users from accessing the Vision Diagnostic Hub until all mandatory requirements (Camera Permission and Gemini API Key) are met. This improves the user experience by providing clear setup instructions instead of showing a "FAIL" state in the functional UI.

## User Review Required

> [!IMPORTANT]
> The gate will block access to the Diagnostic Hub until:
> 1. Phone Camera Permission is granted.
> 2. Gemini API Key is configured in settings.
>
> [!NOTE]
> I will also add a check for the Glasses Connection as a "Soft Requirement" (warning instead of block).

## Proposed Changes

### Vision Feature (`features/xr/glass/vision`)

#### [NEW] [VisionRequirementGate.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/components/VisionRequirementGate.kt)
- Create a reusable composable that checks `uiState` for permissions and API key.
- If missing: Displays a fullscreen "Setup Required" UI with:
    - **Camera Card**: Button to request permission.
    - **API Key Card**: Button to navigate to Settings.
- If met: Displays the provided content (the Diagnostic Hub).

#### [MODIFY] [UnifiedVisionScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/UnifiedVisionScreen.kt)
- Wrap the main `Scaffold` content in `VisionRequirementGate`.
- Move the `TopAppBar` outside the gate so the user can still navigate back or to settings if they are stuck.

#### [MODIFY] [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionViewModel.kt)
- Add a more robust API key check to the `uiState`.

## Verification Plan

### Automated Tests
- Build the module: `./gradlew :features:xr:glass:vision:assembleDebug`

### Manual Verification
1.  **No Permissions / No API Key**: Tap "Vision AI". Verify you see the "Setup Required" screen with two FAIL cards.
2.  **Grant Permission**: Tap "Grant Permission". Verify the Camera card turns green (OK).
3.  **Configure API Key**: Tap "Go to Settings", add a key, and return. Verify the API Key card turns green.
4.  **Enter Hub**: Once both are green, verify the screen automatically transitions to the Diagnostic Hub.
5.  **Revoke Permission**: Revoke camera permission in system settings. Verify the app kicks the user back to the Gate screen.
