# Implementation Plan - Sunset Portrait Detection

This plan details the steps to replace the raw "Portrait Detection" (Visual Identity) feature with the newly implemented "Color & Contrast Calibration" profile throughout the Style Simulator to improve biometric privacy.

## Proposed Changes

### AI Engine Layer
#### [MODIFY] [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- Remove `userPortrait` parameter from `architectStyleBlueprint` and `architectCloudBlueprint`.
- Remove `image(it)` from the `content` block in `architectCloudBlueprint`.
- Update `buildArchitectPrompt` to remove "IMAGE DATA" section and rely solely on `SKIN PROFILE`.

### Presentation Layer
#### [MODIFY] [StyleSimulatorViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
- Remove `userPortraitUri` from `StyleSimulatorUiState`.
- Delete `CapturePortrait`, `PickPortrait`, and `OnPortraitSelected` events from `SimulatorEvent`.
- Delete `OpenGalleryPicker` from `SimulatorEffect`.
- Remove URI to Bitmap loading logic.
- Update `combine` block to remove dependency on `sessionRepository.faceUri`.

#### [MODIFY] [MessagingStep.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/MessagingStep.kt)
- Remove the "Visual Identity Active" portrait card.
- Add a "Color Profile" status component that displays the user's established season (e.g., "True Winter") if available, or prompts for Calibration.

### Navigation & Cleanup
#### [MODIFY] [KoColorNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorNavEntryProvider.kt)
- Remove `"face_simulator"` from the camera target mapping.

## Verification Plan

### Automated Tests
- Run `StyleSimulatorIntegrationTest` to ensure AI generation still works with the updated signature.

### Manual Verification
- Verify in Logcat that no image data is logged in the "DATA_OUT" prompt.
- Confirm the Style Simulator UI no longer shows the face photo.
