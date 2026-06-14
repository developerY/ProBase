# Implementation Plan - Box Capture Feature for KoColor

This plan outlines the creation of an isolated feature module `:applications:kocolor:features:boxcapture` that allows users to capture all sides of a cosmetic product box and use AI to automatically fill in the product details for the `CosmeticDetailScreen`.

## Goal
Solve the issue where barcodes don't work reliably by providing a robust "Box Scan" alternative that uses Gemini Vision to extract structured data from multiple photos of the product packaging.

## User Review Required
> [!IMPORTANT]
> This feature will require a multi-photo capture UI (Front, Back, Left, Right, Top, Bottom, Ingredients list).
> It will use Gemini Vision (via Firebase AI) to analyze all photos simultaneously for maximum context.

## Proposed Changes

### Project Structure
- [NEW] `:applications:kocolor:features:boxcapture` module.

### [Component] Box Capture Feature

#### [NEW] [BoxCaptureViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureViewModel.kt)
- Manage the list of captured images.
- Handle the state of the AI analysis.
- Call Gemini Vision with all captured images to extract `CosmeticItem` details.

#### [NEW] [BoxCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureScreen.kt)
- A multi-step camera interface.
- Guides the user to take photos of all sides of the box.
- Shows a preview of captured sides.
- "Analyze" button to trigger the AI processing.

#### [NEW] [BoxCaptureRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureRoute.kt)
- Navigation entry point for the feature.

### Data & Integration
- Update `settings.gradle.kts` to include the new module.
- Add necessary dependencies (Compose, CameraX, Hilt, Firebase AI).

## Verification Plan

### Automated Tests
- Unit tests for `BoxCaptureViewModel` to ensure it correctly aggregates images and handles AI responses.
- Build verification for the new module.

### Manual Verification
- Deploy the app and navigate to the "Box Scan" feature.
- Take multiple photos of a (simulated or real) cosmetic box.
- Verify that the "Analyze" step correctly calls the AI.
- Check if the extracted data correctly populates the `CosmeticDetailScreen` (or a preview screen before saving).
