# Add Text-Only AI Task Extraction to Smart Capture

The goal is to allow users to generate structured tasks by simply typing a command (e.g., "clean the car") into the Smart Capture screen, even without an image. This feature will be available when Cloud AI is enabled.

## User Review Required

- **Text-Only Processing**: If the user provides a comment but no image, the app will call the Cloud AI engine to extract a task based solely on the text prompt.
- **UI Logic**: I will update the "Analyze" button to be enabled when *either* an image is captured *or* a non-blank comment is provided (provided Cloud AI is on).
- **Engine Support**: The `SmartCaptureEngine` and `CloudCaptureEngineImpl` will be updated to handle a null bitmap and process text-only prompts.

## Proposed Changes

### AI Capture Feature (`features/ai/capture`)

---

#### [SmartCaptureEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/domain/SmartCaptureEngine.kt)

- Update `processImage` to make the `bitmap` parameter optional (`Bitmap?`).

#### [CloudCaptureEngineImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/CloudCaptureEngineImpl.kt)

- Implement text-only processing in `processImage` when the `bitmap` is null but `userContext` is present.
- Adjust the prompt to tell Gemini to generate a task based only on the provided text.

#### [LocalCaptureEngineImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/LocalCaptureEngineImpl.kt)

- Update `processImage` to handle a null bitmap (returning an error or a blank draft, as Local AI currently requires an image for OCR).

#### [SmartCaptureOrchestrator.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/SmartCaptureOrchestrator.kt)

- Propagate the optional bitmap to the engines.
- If the bitmap is null and no Cloud key is present, return an error (Local AI fallback not possible for text-only).

#### [SmartCaptureViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureViewModel.kt)

- Add an `analyzeTextOnly(userContext: String)` method.
- Update `analyzePhoto` to handle the case where the URI is null but user context exists.

#### [SmartCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureScreen.kt)

- Update `SmartCaptureUiRoute` to allow analysis when `capturedUri` is null if a comment exists.
- Modify `EmptyState` to show a text input field if Cloud AI is enabled, allowing users to start without a photo.
- Alternatively, update `ContextInputState` to be accessible even without an image.

## Verification Plan

### Automated Tests
- I will run `gradle_build("app:assembleDebug")` to ensure the project compiles.

### Manual Verification
1.  **Text-Only Extraction**:
    - Ensure Cloud AI is enabled in settings.
    - Go to Smart Capture.
    - Without taking a photo, type "Clean the car this weekend" into the comment field.
    - Click "Analyze".
    - Verify that a task draft is generated with "Clean the car" as the name and appropriate details.
2.  **Image + Text Extraction**:
    - Take a photo and add a comment.
    - Click "Analyze".
    - Verify that both are used for extraction (as currently implemented).
3.  **Local AI Fallback (Negative Test)**:
    - Disable Cloud AI.
    - Try to analyze with text only.
    - Verify that it either prevents analysis or shows an error (as Local AI requires an image).
