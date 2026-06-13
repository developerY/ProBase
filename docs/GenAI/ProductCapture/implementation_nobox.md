# Implementation Plan - Front & Back Product Capture (No Box)

This plan adds a streamlined 2-photo capture sequence (Front and Back) for cosmetic products when the original packaging/box is unavailable.

## Goal
Provide a faster alternative to the 7-step box capture for users who only have the product container.

## User Review Required
> [!IMPORTANT]
> A new navigation route `KoColorRoute.ProductCapture` (or similar) will be added.
> The existing `BoxCaptureViewModel` will be refactored to support different capture "modes": `BOX` (7 steps) and `PRODUCT` (2 steps).

## Proposed Changes

### Data & Models
- [MODIFY] `BoxCaptureUiState.kt`: Add a `CaptureMode` enum (`BOX`, `PRODUCT`) to `Idle` state.
- [MODIFY] `CaptureStep`: Ensure it can be filtered or handled based on mode.

### Logic Layer
- [MODIFY] `BoxCaptureViewModel.kt`:
    - Support initializing with a `CaptureMode`.
    - Update `getNextStep()` to skip non-relevant steps in `PRODUCT` mode.
    - Update the AI prompt in `analyzePhotos()` to reflect that it's analyzing a product container, not a full box, which might have less text context but still useful visual cues.

### UI Layer
- [MODIFY] `BoxCaptureScreen.kt`: Adjust step indicators (e.g., "STEP 1/2" vs "STEP 1/7") based on mode.
- [MODIFY] `StitchProductBuilder.kt`:
    - Add a "Scan Product" button (maybe a different icon or variant) near the "Scan Box" button.
    - Update navigation to pass the mode.

### Navigation
- [MODIFY] `KoColorRoute.kt`: Add `ProductCapture` or add a `mode` parameter to `BoxCapture`.
- [MODIFY] `KoColorNavEntryProvider.kt`: Handle the new route/parameter.

## Verification Plan

### Automated Tests
- Unit tests for `BoxCaptureViewModel` to verify correct step sequencing for both modes.

### Manual Verification
- Navigate to "Add to Collection".
- Test the "Scan Box" flow (7 steps).
- Test the "Scan Product" flow (2 steps).
- Verify AI extraction works for both.
