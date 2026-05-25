# Full XR Glasses Samples

This module contains samples for the Full XR Glasses experience, utilizing the `androidx.xr` libraries. Unlike the "Glass" feature which uses projected UI (Glimmer), these samples run directly on XR hardware (or the XR emulator) and leverage spatial features.

## Sample Categories

### 1. Jetpack Compose for XR (`xr/compose`)
These samples showcase how to adapt traditional 2D Android UI into a 3D spatial environment.
- **Spatial Panels:** Learn how to layout UI in 3D space using `SpatialPanel`.
- **Orbiters:** Implement UI elements that "float" or orbit around a main content panel using `Orbiter`.
- **Subspaces:** Define custom 3D areas within your Compose hierarchy using `Subspace`.

### 2. 3D Manipulation with SceneCore (`xr/scenecore`)
Demonstrates lower-level 3D manipulation and scene graph management.
- **GLTF Loading:** How to load and render `.gltf` and `.glb` 3D models.
- **Anchoring:** Placing and persisting 3D objects relative to physical surfaces (floor, walls, tables).
- **Scene Graph:** Direct manipulation of 3D entities, transforms (rotation, scale, position), and hierarchies.

### 3. Perception with ARCore XR (`xr/arcore`)
Highlights advanced perception and world-sensing capabilities.
- **Spatial Tracking:** Real-time tracking of the user's position and orientation in the room.
- **Plane Detection:** Identifying horizontal and vertical surfaces in the physical environment.
- **Eye & Face Tracking:** Utilizing advanced sensors to track user gaze and facial expressions for immersive interactions.

## How to Run
These samples are designed for the **XR Glasses Emulator**.
1. Open the XR Glasses Emulator.
2. Select the "Full XR Showcase" from the System Features Inventory.
3. Choose a category and sample to explore.
