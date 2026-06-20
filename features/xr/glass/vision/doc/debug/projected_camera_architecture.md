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

    // CORRECT: Use Extender to apply Camera2-level options
    val fpsRange = Range(10, 10) 
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

## 4. Cross-Device Debugging Strategy

Because AI Glasses execute logic on the phone but manage hardware on the headset, logs are split across two distinct streams.

### A. The Phone Logcat (Application Space)
- **Target**: The connected Phone or Emulator.
- **Filter**: `package:mine` or `tag:VisionVM`.
- **Content**: Jetpack Compose state, Gemini API calls, and the `ProjectedContext` lifecycle.

### B. The Display Glasses Logcat (Hardware Space)
- **Target**: `Display Glasses (emulator-5556)`.
- **Filter**: `tag:XR_Compositor`, `tag:CameraDevice`, `tag:SurfaceFlinger`.
- **Content**: Low-level hardware faults, frame drops, and projection bridge handshake errors.

### C. Recommended Workflow
1. **Split Logcat**: Right-click the Logcat tab in Android Studio and select **Split Right**.
2. **Left Panel**: Target **Phone**. Monitor `VisionVM` for "Phase" success signals.
3. **Right Panel**: Target **Glasses**. Monitor for hardware initialization errors or permission denials.

---

## 5. Verification Logs (End-to-End Trace)

### Phone Logcat (Logic Trace)
| Stage | Log Message | Meaning |
| :--- | :--- | :--- |
| **Start** | `VisionDebug: PHASE 1 SUCCESS: Created ProjectedContext.` | The OS has recognized the glasses link. |
| **Verification**| `VisionDebug: PHASE 4 SUCCESS: Hardware verified.` | CameraX confirms `DEFAULT_BACK_CAMERA` exists. |
| **Binding** | `VisionDebug: PHASE 6 SUCCESS: Shutter linked to Glasses.` | The camera is now active. |

### Glasses Logcat (Hardware Trace)
| Stage | Log Message | Meaning |
| :--- | :--- | :--- |
| **Bridge** | `CameraManagerGlobal: Connecting to camera service` | Glasses OS is linking to the Phone's request. |
| **Validation** | `CameraValidator: Virtual device with ID: 1 has X cameras.` | **MUST be > 0.** If 0, hardware mount failed. |
| **Bridge Crash** | `retryOpenSession: failed... microxr.Audio` | **FATAL.** Bridge is severed. Reset emulators. |
| **Handshake** | `XR_Compositor: New projected buffer allocated` | Visual data is streaming from glasses to phone memory. |

---

## 6. Common Failure Signatures

| Observation | Root Cause | Fix |
| :--- | :--- | :--- |
| **`Cams: 0`** | Virtual hardware not yet registered. | Add retry loop with 1.5s delay in `setupCamera`. |
| **`Filters: 1`** | Lens facing conflict (BACK vs EXTERNAL). | Remove explicit facing requirements when using raw IDs. |
| **`Filters: 2`** | Over-constrained selector during fallback. | Use standard `DEFAULT_BACK_CAMERA` for Host/Phone contexts. |
| **`Error 3`** | Hardware link timeout on the headset. | Restart the glasses emulator or check physical cable. |
| **`microxr RPC Hub`** | Foundational communication bridge crashed. | **Wipe Data** and **Cold Boot** both emulators. |
