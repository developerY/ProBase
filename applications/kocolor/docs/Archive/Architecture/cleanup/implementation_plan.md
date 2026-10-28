# Implementation Plan - Decouple Vision AI from KoColor

This plan details the steps to remove "Vision AI" visibility and its standalone launcher from the KoColor application while keeping the feature code intact in the repository.

## Proposed Changes

### Vision Module Cleanup
#### [MODIFY] [vision/AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/AndroidManifest.xml)
- Remove the launcher intent filter from `LiveVisionActivity` to eliminate the duplicate app icon.

### XR Feature Decoupling
#### [MODIFY] [features/xr/glass/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/build.gradle.kts)
- Remove the dependency on `:features:xr:glass:vision`.

### UI Integration Removal
#### [MODIFY] [GlimmerSample.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlimmerSample.kt)
- Remove Vision-related samples from the enum.

#### [MODIFY] [GlassApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassApp.kt)
- Remove UI routing for Vision features.

#### [MODIFY] [GlassXRDemosPhoneScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassXRDemosPhoneScreen.kt)
- Remove `VisionViewModel` and conditional logic for Vision samples.

## Verification Plan

### Automated Tests
- Build KoColor: `:applications:kocolor:apps:mobile:assembleDebug`

### Manual Verification
- Check for a single KoColor icon in the app drawer.
- Verify the "Google XR Test" menu no longer shows Vision samples.
