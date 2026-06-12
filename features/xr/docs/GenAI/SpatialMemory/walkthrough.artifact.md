# Walkthrough - Advanced XR Samples for DroidCon 2026

I have implemented a set of advanced XR samples that demonstrate the cutting-edge features of the Jetpack XR SDK. These samples are structured to be easily presented during a live-coding session or as part of a technical deep-dive on spatial computing.

## 1. Spatial Memory (ARCore Persistence)

The **[SpatialMemorySample](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/arcore/SpatialMemorySample.kt)** demonstrates how to use the Anchor Persistence API to leave virtual content in a physical location that survives app restarts.

> [!IMPORTANT]
> This feature relies on identifying physical edges and corners in the room to re-localize anchors upon app launch.

## 2. Native glTF Assets (SceneCore)

The **[SpatialAssetSample](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/scenecore/SpatialAssetSample.kt)** showcases the ease of loading industry-standard `.glb` models natively in Kotlin. SceneCore handles PBR materials and environment lighting automatically, ensuring assets look realistic in any setting.

## 3. Procedural Geometry (Experimental CustomMesh API)

The **[CustomMeshSample](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/scenecore/CustomMeshSample.kt)** introduces the `@ExperimentalCustomMeshApi`. This allows for the dynamic generation of 3D geometry from vertex data at runtime—perfect for live data visualizations or procedurally generated environments.

## 4. Glimmer UI: Spatial Note Overlay

To support the Spatial Memory demo, I've created the **[SpatialNoteSamples](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/samples/SpatialNoteSamples.kt)**. This Glimmer-optimized UI ensures that virtual sticky notes remain legible across high-contrast environments.

> [!TIP]
> Use the **Spatial Note - Bright Wall** and **Spatial Note - Dark Hallway** Compose previews to verify the high-contrast rendering of the Glimmer theme.

## 5. Integration and Build

All new samples have been integrated into the **[FullXRApp](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/FullXRApp.kt)** and the **[GlassApp](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/src/main/java/com/zoewave/probase/features/xr/glass/ui/GlassApp.kt)**.

- [x] Successfully compiled `:features:xr:glass`
- [x] Successfully compiled `:features:xr:xrglasses`
