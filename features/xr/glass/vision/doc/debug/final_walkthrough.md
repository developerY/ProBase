# Walkthrough - Production-Grade Vision AI Architecture

I have finalized the architectural evolution of the Vision feature. The system has transitioned from a basic prototype to a decoupled, resilient, and 100% MVVM-compliant engine designed specifically for the complexities of Android XR hardware projection.

## 1. Architectural Shift: Hardware Decoupling

I extracted all hardware-specific code from the logic layer and moved it into a dedicated manager.

*   **[NEW] [GlassesCameraManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/manager/GlassesCameraManager.kt)**: 
    *   **Responsibility**: Manages the entire `ProjectedContext` lifecycle and CameraX hardware binding.
    *   **Singleton Pattern**: Acts as a single source of truth for hardware state (logs, active source) across the module.
    *   **Lifecycle Awareness**: Uses CameraX's `bindToLifecycle` to automatically unbind and clean up hardware memory when the UI is dismissed, preventing memory leaks without storing `Activity` references.

*   **[REFACTORED] [VisionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/ui/VisionViewModel.kt)**:
    *   **100% MVVM Purity**: Removed all `android.app.Activity` and `androidx.camera` imports.
    *   **Pure Logic**: Now purely coordinates the handoff between the Camera Manager's raw image data and the Gemini 1.5 Flash analysis engine.

---

## 2. Hardware Resilience & Emulator Fixes

The probing sequence was refined to handle virtualized hardware environments (Emulators) and real AI glasses simultaneously.

*   **Deep Probing Logic**: The system now iterates through multiple contexts (**Glasses** -> **Host Phone** -> **Application**) to find available cameras.
*   **Filter-Aware Selectors**: 
    *   **Glasses**: Uses precision ID-binding via Camera2 Interop to target the outward-facing projected lens.
    *   **Fallback**: Automatically clears strict "External" requirements when falling back to the phone, allowing binding to standard emulator webcams (resolving the previous `Filters: 1/2` conflicts).
*   **Threading Safety**: The entire probing and retry loop is offloaded to `Dispatchers.IO` to ensure the UI thread remains responsive, preventing the "Channel broken" process crashes seen in previous logs.

---

## 3. Instrumented Debugging Strategy

The **Vision Diagnostic Hub** on the phone now provides a real-time window into the hardware's "soul."

*   **Phased Logging**: Logs are categorized into **PHASE 1 (Context)**, **PHASE 4 (Verification)**, and **PHASE 6 (Binding)** success signals.
*   **Cross-Device Trace**: The [Debugging Guide](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/doc/debug/projected_camera_architecture.md) defines a split-logcat workflow (Phone for Logic, Glasses for Hardware) to isolate projection bridge failures.

---

## 4. End-to-End Functional Flow

1.  **Requirement Gate**: User grants dual-permission (Phone + Glasses) and configures the Gemini API Key.
2.  **Initialization**: The UI triggers `cameraManager.initialize(activity)`. 
3.  **Binding**: The manager finds the best available camera and binds the shutter to the current lifecycle.
4.  **Capture**: Tapping the button on the phone (or card on glasses) sends a remote command through the repository bridge.
5.  **Analysis**: The manager triggers the hardware shutter, sends the image to the ViewModel, and Gemini returns a concise description.
6.  **Display**: The result is rendered in the Glimmer HUD on the glasses.

## Verification Results

### Build Status
> [!TIP]
> The `:features:xr:glass:vision` module and its dependencies compile successfully with zero experimental API warnings.

### Final Debug Signal
When successful, you will see the following sequence in your Phone Logcat:
- `Successfully created ProjectedContext for Glasses.`
- `PHASE 4: Hardware Verification for ID X...`
- `PHASE 6 SUCCESS: Shutter linked to Glasses.`
