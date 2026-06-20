# Android XR: Projected Camera Architecture & Debugging Guide

This document outlines the technical requirements, implementation standards, and verification logs for accessing camera hardware on AI Glasses via a projected context.

## 1. Conceptual Model

AI Glasses operate as a **Projected Device** connected to an **Android Host** (Phone).
- **Hardware Space**: The camera is physically on the Glasses.
- **Context Bridge**: Standard `Activity` contexts only see the phone. You **must** use a `ProjectedContext` to "see" and "bind" the glasses' camera.

---

## 2. Technical Requirements

### A. Manifest Constraints
The activity that initializes the camera MUST be declared with the `xr_projected` category.
```xml
<activity
    android:name=".LiveVisionActivity"
    android:requiredDisplayCategory="xr_projected" />
```

### B. Coordinated Permissions
Access requires dual-consent:
1. **Phone Permission**: Standard `Manifest.permission.CAMERA` granted on the phone.
2. **Glasses Permission**: Explicit grant for the projected device ID using `ProjectedPermissionsResultContract`.

---

## 3. Implementation Pipeline (The "Happy Path")

The following code demonstrates the operationally correct way to bind the glasses camera using CameraX and Camera2 Interop.

```kotlin
/**
 * Verifies and binds the AI Glasses camera.
 * @param activity The projected Glasses Activity (MUST NOT be the Phone's MainActivity)
 */
suspend fun initializeGlassesCamera(activity: ComponentActivity) {
    // 1. Create the Projected Context
    val projectedContext = try {
        ProjectedContext.createProjectedDeviceContext(activity)
    } catch (e: Exception) {
        Log.e("VisionDebug", "PHASE 1 FAIL: Projected device not found.")
        return
    }
    Log.d("VisionDebug", "PHASE 1 SUCCESS: Created ProjectedContext.")

    // 2. Obtain context-matched Camera Provider
    val cameraProvider = ProcessCameraProvider.awaitInstance(projectedContext)
    
    // 3. Select the outward-pointing Glasses camera
    // Within a projected context, DEFAULT_BACK_CAMERA is automatically mapped to the glasses.
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    // 4. Pre-binding Hardware Verification
    if (!cameraProvider.hasCamera(cameraSelector)) {
        Log.e("VisionDebug", "PHASE 4 FAIL: Hardware not reported by provider.")
        return
    }
    Log.d("VisionDebug", "PHASE 4 SUCCESS: Hardware verified.")

    // 5. Configure Use Case with Camera2 Interop (e.g., for FPS/Thermals)
    val imageCaptureBuilder = ImageCapture.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER))
                .build()
        )

    // CORRECT: Use Extender to apply Camera2-level options (e.g. FPS range)
    val fpsRange = Range(10, 10) // Optimized for Computer Vision thermals
    Camera2Interop.Extender(imageCaptureBuilder)
        .setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)

    val imageCapture = imageCaptureBuilder.build()

    // 6. Bind to Lifecycle
    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            activity, // MUST be the Projected Activity
            cameraSelector,
            imageCapture
        )
        Log.d("VisionDebug", "PHASE 6 SUCCESS: Shutter linked to Glasses.")
    } catch (e: Exception) {
        Log.e("VisionDebug", "PHASE 6 FAIL: ${e.message}")
    }
}
```

---

## 4. Verification Logs (End-to-End Trace)

Use these log patterns to verify your setup is working correctly.

### Context & Hardware Check
| Stage | Log Message | Meaning |
| :--- | :--- | :--- |
| **Start** | `VisionDebug: PHASE 1 SUCCESS: Created ProjectedContext.` | The OS has recognized the glasses link. |
| **Discovery** | `CameraManagerGlobal: Connecting to camera service` | The system is reaching out to the virtual hardware bridge. |
| **Validation** | `CameraValidator: Virtual device with ID: 1 has X cameras.` | **MUST be > 0.** If 0, the OS hasn't finished linking the hardware. |
| **Verification**| `VisionDebug: PHASE 4 SUCCESS: Hardware verified.` | CameraX confirms `DEFAULT_BACK_CAMERA` exists in this context. |

### Binding & Runtime
| Stage | Log Message | Meaning |
| :--- | :--- | :--- |
| **Binding** | `VisionDebug: PHASE 6 SUCCESS: Shutter linked to Glasses.` | The camera is now active and controlled by the glasses lifecycle. |
| **Capture** | `VisionVM: Capture Success! Processing bitmap...` | The image has been successfully pulled from the glasses hardware memory. |

---

## 5. Common Failure Signatures

### "Cams: 0" (Hardware Link Missing)
- **Log**: `IllegalArgumentException: No available camera can be found. Cams:0`
- **Cause**: The `ProjectedContext` was used before the virtual hardware was ready.
- **Fix**: Add a 1.5s delay or retry loop after `createProjectedDeviceContext`.

### "Filters: 1" (Selector Conflict)
- **Log**: `Bind failed: No available camera can be found. Filters:1`
- **Cause**: The `CameraSelector` has contradictory filters (e.g. asking for `EXTERNAL` when the OS reports the lens as `BACK`).
- **Fix**: Use `DEFAULT_BACK_CAMERA` inside the `ProjectedContext`; do not add manual lens-facing requirements.

### "Channel Broken" (ANR/Crash)
- **Log**: `InputDispatcher: Channel is unrecoverably broken`
- **Cause**: Running the initialization loop on the Main thread.
- **Fix**: Offload `setupCamera` to `Dispatchers.IO`.
