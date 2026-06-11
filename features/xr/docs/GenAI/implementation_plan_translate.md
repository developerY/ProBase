# Implementation Plan - Live Translation XR Example

Add a new "Live Translation" example to the `features/xr/glass` module. This example will demonstrate how a mobile app can bridge to AI glasses, capture audio, and render translated text using Jetpack Compose Glimmer.

## Proposed Changes

### [Component] XR Glass Features

#### [MODIFY] [SamplesMenu.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/SamplesMenu.kt)
- Add `Translation` to the `GlimmerSample` enum to include it in the main showcase menu.

#### [NEW] [LiveTranslationSamples.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/samples/LiveTranslationSamples.kt)
- Implement `GlassesTranslationScreen` using `GlimmerTheme` and `Text` optimized for optical see-through displays.

#### [MODIFY] [GlassApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassApp.kt)
- Add `GlimmerSample.Translation` to the sample rendering logic.

#### [NEW] [LiveTranslationActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/LiveTranslationActivity.kt)
- Implement a standalone activity that demonstrates the "Host Activity" pattern:
    - Checks for glasses connection using `ProjectedDeviceController`.
    - Bridges the audio stream (conceptual implementation for the demo).
    - Renders the Glimmer UI to the glasses display.

#### [MODIFY] [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/AndroidManifest.xml)
- Register `LiveTranslationActivity` with the `xr_projected` display category.

## Verification Plan

### Automated Tests
- Build the `features:xr:glass` module to ensure no compilation errors with the new activity and samples.

### Manual Verification
- Deploy the app and navigate to the "Live Translation" sample in the Glimmer Samples menu.
- (For the standalone activity) The user can trigger `LiveTranslationActivity` via intent to see the standalone "talk-ready" example.
