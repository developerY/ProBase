# Implementation Plan: Sunset Portrait Detection in favor of Color Calibration

This plan details the steps to replace the raw "Portrait Detection" (Visual Identity) feature with the newly implemented "Color & Contrast Calibration" profile throughout the Style Simulator.

## Rationale: Privacy & Redundancy
- **Portrait Detection**: Captures a raw high-res face photo and sends it to Cloud AI (Gemini). High privacy cost.
- **Color Calibration**: Extracts mathematical aesthetic data (Season, Undertone, Contrast) locally on the device. Zero-Cloud privacy.
- **Goal**: Use the established mathematical profile to "ground" the AI instead of sending raw biometric images.

## Proposed Changes

### 1. Update AI Orchestration
Modify the engine to rely solely on the calibrated profile rather than the raw portrait image.

#### [MODIFY] [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- Update `architectStyleBlueprint` to remove the `userPortrait` parameter.
- Update `architectCloudBlueprint` to remove `userPortrait` from the input content.
- Refine `buildArchitectPrompt` to emphasize the `SKIN PROFILE` derived from calibration and remove references to "IMAGE DATA".

### 2. Update Style Simulator State
Remove the portrait capture/selection UI and state.

#### [MODIFY] [StyleSimulatorViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
- Remove `userPortraitUri` from `StyleSimulatorUiState`.
- Remove `CapturePortrait`, `PickPortrait`, and `OnPortraitSelected` events.
- Remove logic that loads the Bitmap from URI.
- Ensure `skinContext` is always derived from `fashionRepository.getProfile()`.

#### [MODIFY] [MessagingStep.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/MessagingStep.kt)
- Remove the "Visual Identity Active" card and the camera/gallery icons.
- Replace it with a "Color Profile Active" indicator that shows the user's Established Season (e.g., "True Winter").

### 3. Cleanup Resources
#### [MODIFY] [KoColorNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorNavEntryProvider.kt)
- Remove the `"face_simulator"` case from the camera navigation logic.

## Verification Plan

### Automated Tests
- Run `StyleSimulatorIntegrationTest` to ensure the AI still generates blueprints correctly using only the `fashionProfile` string.

### Manual Verification
- **Privacy Check**: Run the simulator and verify via logs that **no image data** is being sent in the Gemini request.
- **UI Check**: Confirm the "Visual Identity" card is gone and replaced by the "Color Profile" status.
- **UX Flow**: Ensure the user is prompted to run Calibration if they haven't established a profile yet.
