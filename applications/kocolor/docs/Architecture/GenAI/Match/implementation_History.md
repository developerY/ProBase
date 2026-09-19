# Implementation Plan - KoColor Dual-Image Fashion Analysis

Implement a feature to capture both a face photo and a clothing photo, and use Gemini AI to generate a coordinated makeup color palette.

## Proposed Changes

### Feature: Analyzer

#### [AnalyzerEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/data/AnalyzerEngine.kt)
- Update `analyzeSelfie` to `analyzeFaceAndClothes` (or add a new method).
- Method signature: `suspend fun analyzeFaceAndClothes(faceBitmap: Bitmap, clothesBitmap: Bitmap, apiKey: String, modelName: String): FashionAdvice`.
- Update prompt to explicitly ask for makeup coordination between the face (seasonal type/undertone) and the clothing colors.

#### [AnalyzerViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/AnalyzerViewModel.kt)
- Update `AnalyzerScreenUiState` to hold both `faceUri` and `clothesUri`.
- Update `AnalyzerEvent` with `OnFaceCaptured(uri)` and `OnClothesCaptured(uri)`.
- Update `onEvent` logic to handle both images and trigger analysis only when both are present (or provide clear feedback).
- Implement `loadBitmapFromUri` to handle multiple images.

#### [AnalyzerScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/AnalyzerScreen.kt)
- Redesign the UI to show two image capture slots: "Face" and "Clothes".
- Add buttons/placeholders for each slot that navigate to the camera with specific targets.
- Update `ReadyToAnalyzeState` to display both images.
- Ensure `@Preview` updates reflect the dual-image layout.

---

### App Level

#### [KoColorNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorNavEntryProvider.kt)
- Update `KoColorRoute.Camera` handling to dispatch `OnFaceCaptured` or `OnClothesCaptured` based on the `target` parameter (e.g., `KoColorRoute.Camera("face")`).

---

## Verification Plan

### Automated Tests
- Run `:applications:kocolor:apps:mobile:assembleDebug` to verify build.

### Manual Verification
1. Launch KoColor app.
2. Go to Analyzer screen.
3. Capture a face photo.
4. Capture a clothing photo.
5. Tap "Analyze" and verify that Gemini returns a summary coordinating both images and a specific makeup palette.
6. Verify that the result can be saved to the profile.
