# Implementation Plan - Local AI Fallback for Product Capture

This plan adds a local, zero-network fallback mechanism for product information extraction using ML Kit OCR and heuristic-based parsing. This ensures the "Scan Box" and "Scan Product" features remain functional even without a Gemini API key.

## Goal
Provide a reliable "best guess" extraction flow that works offline and without external API dependencies.

## User Review Required
> [!IMPORTANT]
> The local extraction will be less comprehensive than Gemini. It will focus on:
> 1.  **Product Name & Brand** (via prominent text detection)
> 2.  **Volume** (via Regex for ml/g/oz)
> 3.  **Ingredients** (via keyword matching)
> Users will be encouraged to review and fix the "Extracted Product" details in the subsequent edit screen.

## Proposed Changes

### Dependencies
- [MODIFY] `applications/kocolor/features/boxcapture/build.gradle.kts`: Add `libs.mlkit.text.recognition`.

### Logic Layer

#### [NEW] [LocalProductAnalyzer.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/data/LocalProductAnalyzer.kt)
- Wrapper for ML Kit `TextRecognizer`.
- Orchestrates multi-image text extraction.
- Implements a heuristic engine to find:
    - **Brand**: Cross-reference with a localized list of common brands.
    - **Volume**: Regex parsing for standard sizes.
    - **Ingredients**: Identifying text blocks with high density of chemical names or following the "Ingredients" header.

#### [MODIFY] [BoxCaptureViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureViewModel.kt)
- Logic to detect if Gemini is available (API key present).
- If unavailable, switch the analysis flow to `LocalProductAnalyzer`.
- Update `BoxCaptureUiState.Analyzing` to show "Running Local AI..." status.

### UI Layer
- [MODIFY] `BoxCaptureScreen.kt`: Update the `Analyzing` view to show when local processing is active.

## Verification Plan

### Automated Tests
- Unit tests for `LocalProductAnalyzer` heuristics using sample OCR text strings.

### Manual Verification
- Remove the Gemini API key from settings.
- Run a "Scan Product" flow.
- Verify that text is extracted and correctly mapped to the `CosmeticItem` draft.
- Confirm the user can fix any incorrect guesses in the final edit screen.
