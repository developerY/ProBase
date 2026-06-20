# Implementation Plan - Fix Vision Permission Gate

The user reported that they can access the Vision Diagnostic Hub without granting the necessary XR Glasses permissions. The root cause is a hardcoded `true` value for glasses permission status in the `VisionViewModel`, which allows the `VisionRequirementGate` to be bypassed.

## User Review Required

> [!IMPORTANT]
> I will replace the hardcoded "true" with a real permission check. This means users will be blocked by the "Setup Required" screen until they explicitly grant camera access on the glasses.

## Proposed Changes

### Vision Feature (`features/xr/glass/vision`)

#### [MODIFY] [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionViewModel.kt)
- Fix `checkGlassesPermission()` to perform a real check using `ProjectedContext`.
- Ensure the result updates `_isGlassesPermissionGranted`.
- Add a dependency on `Activity` or just `Context` for this check if possible, or delegate to `cameraManager`.

#### [MODIFY] [GlassesCameraManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/manager/GlassesCameraManager.kt)
- Add a `checkGlassesPermission(activity: Activity): Boolean` method to perform the actual `ContextCompat.checkSelfPermission` on the projected context.
- Update the manager to expose this status more cleanly if needed.

#### [MODIFY] [UnifiedVisionScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/UnifiedVisionScreen.kt)
- Ensure that the initial `LaunchedEffect` and the `LifecycleEventObserver` call the updated `checkGlassesPermission` with the current activity.

## Verification Plan

### Automated Tests
- Build module: `./gradlew :features:xr:glass:vision:assembleDebug`

### Manual Verification
1.  **Revoke Permissions**: Ensure glasses camera permission is revoked.
2.  **Launch Vision AI**: Verify that you are met with the **Setup Required** screen.
3.  **Check Status**: Verify the "Glasses Camera Access" card shows a grey/FAIL state.
4.  **Grant Access**: Click "GRANT GLASSES ACCESS", allow the permission, and verify you are automatically moved to the Diagnostic Hub.
