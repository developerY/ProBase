# Implementation Plan - Vision Debug and Diagnostic Screen

The goal is to provide a comprehensive diagnostic and debug interface for the Vision AI feature on AI Glasses. This will help identify why "camera pic to description" is failing by showing real-time logs, permission status, the captured image, and Gemini's response.

## User Review Required

> [!IMPORTANT]
> The implementation uses `ProjectedContext` to access the glasses' camera hardware. Ensure the glasses are connected during testing.
>
> [!NOTE]
> The Gemini API key (BYOK) is required in the app settings for the vision analysis to work.

## Proposed Changes

### Vision Feature (`features/xr/glass/vision`)

#### [MODIFY] [VisionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/data/VisionRepository.kt)
- No changes needed, already contains `capturedImage` and other necessary flows.

#### [MODIFY] [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionViewModel.kt)
- Add `logs: List<String>` to `VisionUiState`.
- Add `capturedImage: Bitmap?` to `VisionUiState`.
- Implement `addLog(message: String)` to track steps and display them in the UI.
- Add detailed logging (Logcat + UI logs) for:
    - Camera setup (Projected vs Phone).
    - Permission status.
    - Capture triggers and success/failure.
    - Gemini processing status, prompt used, and response received.
    - API Key validation.

#### [NEW] [UnifiedVisionScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/UnifiedVisionScreen.kt)
- Create a phone-side debug screen mirroring the "Translation Hub" style.
- Sections:
    - **System Diagnostics**: Connection, Permission, Camera Source, Gemini status.
    - **Camera Preview**: Display the last captured `Bitmap`.
    - **Analysis Result**: Gemini's description.
    - **Live Event Log**: A scrolling list of detailed step-by-step actions.
    - **Controls**: Capture trigger.

#### [MODIFY] [LiveVisionActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/LiveVisionActivity.kt)
- Update to use `UnifiedVisionScreen` as the primary phone UI.
- Ensure permission handling is robust.

#### [MODIFY] [VisionScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionScreen.kt)
- Refine the Glimmer UI to ensure the image description is displayed prominently on the glasses.

## Verification Plan

### Automated Tests
- Build the module: `./gradlew :features:xr:glass:vision:assembleDebug`

### Manual Verification
1.  Launch the Vision feature on the phone.
2.  Check the **System Diagnostics** section for any red flags (Permissions, Connection, API Key).
3.  Trigger a capture from the phone or glasses.
4.  Verify that:
    - "Capture Triggered" appears in logs.
    - The image appears in the **Camera Preview** section on the phone.
    - "Sent to Gemini" appears in logs.
    - Gemini's description appears on both the phone and glasses.
5.  Check Logcat for detailed trace of `VisionVM` messages.
