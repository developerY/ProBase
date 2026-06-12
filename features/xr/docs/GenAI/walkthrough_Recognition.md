# Walkthrough - Object Recognition XR Example

I have implemented the **Object Recognition** feature for AI/XR glasses. This example demonstrates how to leverage the glasses' camera for on-device AI analysis and project real-time overlays into the user's field of view.

## Changes Made

### Glimmer UI Overlay
- **[ObjectRecognitionSamples.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/samples/ObjectRecognitionSamples.kt)**:
    - Created `ObjectRecognitionScreen` which renders a high-contrast label pill at the top of the FOV.
    - Used the `.surface()` modifier for AR-optimized transparency.
    - Added `@Preview` with both **Light** and **Dark** environment simulations to verify legibility in different real-world conditions.

### Host Activity Pattern with CameraX
- **[ObjectRecognitionActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ObjectRecognitionActivity.kt)**:
    - Implemented a standalone activity that runs on the phone and bridges to the glasses.
    - Integrated **CameraX `ImageAnalysis`** to stream frames from the glasses' back camera.
    - Added a mock AI processing pipeline (`analyzeFrameWithAI`) that updates the Glimmer UI on the glasses.

### Build and Integration
- **[build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/build.gradle.kts)**: Added CameraX dependencies (`core`, `camera2`, `lifecycle`, `view`) to support vision features.
- **[AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/AndroidManifest.xml)**:
    - Added `android.permission.CAMERA`.
    - Registered `ObjectRecognitionActivity` with the `xr_projected` category.
- **[SamplesMenu.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/SamplesMenu.kt)** & **[GlassApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassApp.kt)**: Integrated the new sample into the main showcase app.

## Verification Results

### Build Status
- [x] Successfully compiled the `:features:xr:glass` module with the new CameraX integration.

### Implementation Highlights
> [!IMPORTANT]
> The CameraX binding uses `CameraSelector.DEFAULT_BACK_CAMERA`, which correctly selects the glasses' outward-facing camera when running in a projected context.

> [!TIP]
> Use the **Object Recognition - Light Environment** preview to verify that the Glimmer UI remains readable even against bright, sunlit backgrounds.
