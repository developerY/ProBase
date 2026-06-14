# Tasks - Local AI Fallback for Product Capture

- [x] Setup Dependencies
    - [x] Add ML Kit Text Recognition to `boxcapture/build.gradle.kts`
- [x] Implement Heuristic Engine
    - [x] Create `LocalProductAnalyzer.kt`
    - [x] Implement OCR extraction logic
    - [x] Implement brand/name/volume/ingredients heuristics
- [x] Integrate Fallback Logic
    - [x] Update `BoxCaptureViewModel.kt` to use `LocalProductAnalyzer` when Gemini key is missing
    - [x] Update `BoxCaptureUiState.kt` to reflect local analysis status
- [x] UI Refinement
    - [x] Update `BoxCaptureScreen.kt` for "Local AI" feedback
- [x] Verification
    - [x] Build and verify offline capture flow
