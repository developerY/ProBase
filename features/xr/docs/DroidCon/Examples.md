# 1. Full XR App (Core SDK Samples)

Located in `features/xr/xrglasses`, this suite implements **14 modular samples** covering the foundational Jetpack XR SDK features:

- **Compose for XR:** Spatial Panels, Orbiters, Subspaces, Spatial Dialogs, Popups, Elevation, and Layouts.
- **SceneCore 3D:** GLTF Model Loading, Surface Anchoring, and the Transform System.
- **ARCore Perception:** Plane Detection, User Tracking, and Face & Eye Tracking.

# 2. Glimmer UI Suite (Projected Experiences)

Located in `features/xr/glass`, this suite implements **19 specialized samples** for the new **Jetpack Compose Glimmer** library mentioned in the DroidCon notes:

- Includes high-fidelity components like Glass-optimized Buttons, Cards, Depth Effects, Pagers, and Voice Input Indicators.
- Features a dedicated **"Morning Ritual"** spatial layout demo.

# Summary Comparison

| Feature | android/xr-samples | Our `features/xr` Implementation |
|----------|-------------------|----------------------------------|
| Hello XR Basics | ✅ Included | ✅ Included (as `SpatialPanel` & `Orbiter` samples) |
| SceneCore / ARCore | ⚠️ Partial | ✅ Full (Modular demos for every major API) |
| Glimmer UI | ❌ Missing | ✅ Full (19 component-level samples) |
| Projected Context | ❌ Missing | ✅ Full (Integrated mobile-to-glasses bridging) |

## Conclusion

Yes, we have all the fundamental concepts from the official samples, but they have been broken down into professional, standalone demos to make them easier to reference, learn from, and extend.

