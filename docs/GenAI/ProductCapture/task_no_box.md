# Tasks - Front & Back Product Capture Implementation

- [ ] Update Models and UI State
    - [ ] Add `CaptureMode` to `BoxCaptureUiState.kt`
    - [ ] Update `CaptureStep` with mode-specific logic
- [ ] Refactor ViewModel logic
    - [ ] Support `CaptureMode` in `BoxCaptureViewModel.kt`
    - [ ] Update step sequencing logic
    - [ ] Tailor AI prompt for product-only mode
- [ ] Update UI Components
    - [ ] Support mode-based indicators in `BoxCaptureScreen.kt`
    - [ ] Add "Scan Product" entry point in `StitchProductBuilder.kt`
- [ ] Update Navigation
    - [ ] Add `mode` parameter to `KoColorRoute.BoxCapture`
    - [ ] Update `KoColorNavEntryProvider.kt` to handle the parameter
- [ ] Verification
    - [ ] Build and verify both capture sequences
