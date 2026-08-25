# Implementation Plan: Decouple Vision AI from KoColor Suite

This plan details the steps to remove "Vision AI" visibility and its standalone launcher from the KoColor application while keeping the feature code intact in the repository.

## User Review Required

> [!IMPORTANT]
> - This change **removes the "Vision AI" app icon** from the device app drawer.
> - This change **removes "Vision AI" and "Object Recognition"** from the XR Samples menu reachable via KoColor.
> - The `:features:xr:glass:vision` module will **NOT** be deleted; it will simply be disconnected from the KoColor dependency graph.

## Proposed Changes

### 1. Remove Standalone Launcher
The "Vision AI" icon appears because the Vision module's manifest declares a `LAUNCHER` activity.

#### [MODIFY] [vision/AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/AndroidManifest.xml)
- Remove the `<intent-filter>` containing `<category android:name="android.intent.category.LAUNCHER" />` from `LiveVisionActivity`.

### 2. Build Decoupling
Break the physical link between the core XR feature and the Vision logic.

#### [MODIFY] [features/xr/glass/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/build.gradle.kts)
- Remove `implementation(project(":features:xr:glass:vision"))`.

### 3. UI Cleanup (Samples & Routing)
Remove the Vision entries from the internal XR test environment.

#### [MODIFY] [GlimmerSample.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlimmerSample.kt)
- Remove `Vision("Vision AI")` and `ObjectRecognition("Object Recognition")` from the `GlimmerSample` enum.

#### [MODIFY] [GlassApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassApp.kt)
- Remove import and usages of `VisionRoute`.
- Remove `GlimmerSample.Vision` and `GlimmerSample.ObjectRecognition` branches from the UI switch.

#### [MODIFY] [GlassXRDemosPhoneScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassXRDemosPhoneScreen.kt)
- Remove `VisionViewModel` injection and all usages of `GlimmerSample.Vision` in the UI logic (headers, footers, and content blocks).

## Verification Plan

### Automated Tests
- Run `:applications:kocolor:apps:mobile:assembleDebug` to ensure the app builds successfully without the vision dependency.

### Manual Verification
- **App Drawer**: Confirm only the "KoColor" icon is present.
- **XR Test Menu**: Confirm "Vision AI" and "Object Recognition" are missing from the Glimmer Samples list.
