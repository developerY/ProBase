# Implementation Plan - Advanced XR Samples for DroidCon 2026

Add a set of advanced samples demonstrating Spatial Memory (ARCore Persistence), glTF Model Loading, and Custom Mesh generation to the XR feature set.

## Proposed Changes

### [Component] Full XR Samples (`features/xr/xrglasses`)

#### [NEW] [SpatialMemorySample.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/arcore/SpatialMemorySample.kt)
- Implement `SpatialMemorySample` demonstrating `Anchor.create`, `anchor.persist()`, and `Anchor.load(session, uuid)`.
- Include a UI for "dropping" and "restoring" persistent spatial notes.

#### [NEW] [SpatialAssetSample.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/scenecore/SpatialAssetSample.kt)
- Implement `SpatialAssetSample` demonstrating native glTF loading with `GltfModel` and `GltfModelEntity`.

#### [NEW] [CustomMeshSample.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/scenecore/CustomMeshSample.kt)
- Implement `CustomMeshSample` demonstrating the `@ExperimentalCustomMeshApi` for procedural geometry generation.

#### [MODIFY] [FullXRApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/FullXRApp.kt)
- Add `SpatialMemory`, `SpatialAsset`, and `CustomMesh` to the `XRSample` enum.
- Update the UI to include these new samples in the menu and rendering logic.

### [Component] Glimmer UI Samples (`features/xr/glass`)

#### [NEW] [SpatialNoteSamples.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/samples/SpatialNoteSamples.kt)
- Implement `SpatialNoteOverlay` using `GlimmerTheme` and high-contrast `Surface`.
- Include `@Preview` with bright/dark background simulations.

#### [MODIFY] [SamplesMenu.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/SamplesMenu.kt)
- Add `SpatialNote` to the `GlimmerSample` enum.

#### [MODIFY] [GlassApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassApp.kt)
- Register `GlimmerSample.SpatialNote` in the sample rendering logic.

## Verification Plan

### Automated Tests
- Run `:features:xr:glass:assembleDebug` and `:features:xr:xrglasses:assembleDebug` to ensure all new code compiles correctly.

### Manual Verification
- Verify the new samples appear in the respective menus.
- Inspect Compose Previews for `SpatialNoteOverlay` and the new Full XR samples.
