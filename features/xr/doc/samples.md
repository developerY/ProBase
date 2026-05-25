# XR Samples

Here is an architectural breakdown of the `androidx/xr` modules shown in your screenshot and how they map to the different hardware profiles we discussed.

### AndroidX XR Module Compatibility

| `androidx/xr` Module | XR Headsets (Mixed Reality/VR) | XR Glasses (Tethered/Optical) | AI Glasses (Display/Projected) | Primary Function |
| --- | --- | --- | --- | --- |
| **`compose`** | ✅ Yes | ✅ Yes | ❌ No | Spatial UI, panels, orbiters, and volumetric layouts using Jetpack Compose. |
| **`scenecore`** | ✅ Yes | ✅ Yes | ❌ No | 3D scene graph manipulation, entity management, and `.gltf` rendering. |
| **`arcore`** | ✅ Yes | ✅ Yes | ❌ No | Environment perception, spatial tracking, planes, and anchoring. |
| **`runtime`** | ✅ Yes | ✅ Yes | ❌ No | Core immersive subsystem backing SceneCore and ARCore. |
| **`assets`** | ✅ Yes | ✅ Yes | ❌ No | 3D asset loading and material definitions for immersive scenes. |
| **`glimmer`** | ❌ No | ❌ No | ✅ Yes | Bespoke UI toolkit for low-distraction, 2D heads-up displays. |
| **`projected`** | ❌ No | ❌ No | ✅ Yes | The underlying projection framework that manages Glimmer experiences. |
| **`testutils`** | ✅ Yes | ✅ Yes | ✅ Yes | Shared instrumentation and unit testing utilities across all XR environments. |

---

### Architectural Summary

* **The Immersive Stack (`compose`, `scenecore`, `arcore`, `runtime`, `assets`):** These modules form the heavy-duty spatial architecture. If you are building high-performance, 6DoF applications that need to understand the physical room or render objects in true 3D space, you will rely on this stack. It runs identically across both fully immersive headsets and optical see-through XR glasses.
* **The Projected Stack (`glimmer`, `projected`):** This is the lightweight, 2D-constrained architecture. It bypasses the spatial and 3D rendering engines entirely to deliver low-power, localized UI explicitly for AI Glasses.

[Code in this feature directory](https://github.com/androidx/androidx/tree/androidx-main/xr/glimmer/glimmer/samples)

This directory contains sample applications demonstrating how to use the XR libraries:

* **[Glimmer Sample](https://github.com/androidx/androidx/tree/androidx-main/xr/glimmer/glimmer/samples)**: A comprehensive example of spatial UI and environment integration.
* **[Input Sample](https://github.com/androidx/androidx/tree/androidx-main/xr/scenecore/scenecore/samples)**: Demonstrates handling of hand tracking and controller input.
* **[Session Sample](https://github.com/androidx/androidx/tree/androidx-main/xr/scenecore/scenecore/samples)**: Shows how to manage the lifecycle of an XR session.

**1. [Compose XR Samples](https://github.com/androidx/androidx/tree/androidx-main/xr/compose/compose/samples)**
This module demonstrates how to adapt declarative UI architectures for spatial computing. The samples here showcase how to build out spatial panels, orbiters, and volumetric layouts using standard Jetpack Compose patterns, extending the canvas seamlessly into 3D space.

**2. [SceneCore Samples](https://github.com/androidx/androidx/tree/androidx-main/xr/scenecore/scenecore/samples)**
To dive into the lower-level performance and manipulation of the 3D scene graph, the SceneCore samples are the best reference. These examples demonstrate how to directly manage entities (like `GltfModelEntity` and `PanelEntity`), load 3D assets, and handle the rendering lifecycle outside of the Compose layer.

**3. [ARCore for XR Samples](https://github.com/androidx/androidx/tree/androidx-main/xr/arcore/arcore/samples)**
These samples focus on environment perception. You can run these on the emulator to test spatial tracking, plane detection (such as finding virtual tables or walls), and anchoring 3D objects to specific coordinates in the simulated physical room.

**4. [Hello Android XR](https://github.com/android/xr-samples)**
Outside of the immediate AndroidX framework tree, the most robust sample to run on the Canary emulator is the standalone "Hello Android XR" project. This is the comprehensive reference application that pulls all the libraries shown in your screenshot (Compose, SceneCore, and ARCore) into a single unified architecture, complete with interactive 3D rendering and simulated environment overrides.


To run these spatial and AR samples, you need the **Android XR Emulator**, which is currently part of the Android XR Developer Preview.

Because the Jetpack XR SDK and the required tools are still in early development, standard versions of Android Studio won't work. Here is exactly what you need to set up:

### 1. Android Studio Canary

You must download and install the **latest Canary build of Android Studio**. Lower versions (like Stable or Beta) do not include the necessary XR tools, Layout Inspector updates, or SDK changes.

### 2. Update SDK Tools

Once you have the Canary build open, go to the **SDK Manager** > **SDK Tools** tab and ensure you have the latest versions of:

* Android Emulator
* Android SDK Platform-Tools
* Android SDK Build-Tools
* Layout Inspector for API 31 - 36

For the **XR Glasses** emulator profile (which simulates tethered, wired devices like XREAL's Project Aura), you will be running the samples designed for **immersive** spatial experiences.

This form factor uses the full 3D spatial SDK rather than the lightweight 2D HUD components. Here are the specific samples from the AndroidX tree and official repositories that target this emulator:

### 1. The Immersive AndroidX Samples

Since XR Glasses support true spatial depth, you can run the following sample directories from the source tree you expanded:

* **`xr/compose/.../samples`**: These showcase **Jetpack Compose for XR**. When you run these on the XR Glasses emulator, you will see traditional 2D Android UI broken out into 3D space using Spatial Panels, Orbiters (elements that float around a main panel), and Subspaces.
* **`xr/scenecore/.../samples`**: These demonstrate lower-level 3D manipulation. You can run these to see how to load `.gltf` models, anchor objects in the physical room, and manipulate the 3D scene graph directly.
* **`xr/arcore/.../samples`**: These highlight advanced perception features. On the XR Glasses emulator, these samples will test spatial tracking, plane detection, and even the new face and eye-tracking perception capabilities.

### 2. Hello Android XR

The standalone **Hello Android XR** project (found on GitHub) is the primary reference app for the XR Glasses form factor. It brings the Compose, SceneCore, and ARCore libraries together to demonstrate a complete, production-ready immersive application.

---

### A Quick Clarification on Glimmer

The **`xr/glimmer`** and **`xr/projected`** samples are actually designed for the **AI Glasses** emulator profile (specifically "Display Glasses"). Glimmer is a specialized UI toolkit built for lightweight, non-immersive augmented reality—think discreet 2D text, icons, and voice indicators on a small heads-up display, rather than the full 3D spatial environments supported by the heavy-duty **XR Glasses** profile.

### 3. Create the Android XR Virtual Device (AVD)

In Android Studio Canary, open the **Device Manager** and click **Add a new device**. You will see a new category specifically for this setup:

* Under the **Form Factor** section, select **XR**.
* From there, you can choose the specific hardware profile you want to simulate:
* **XR Headset:** Fully immersive or mixed reality headsets.
* **XR Glasses:** Tethered/wired glasses with optical see-through displays.
* **Display/Audio Glasses:** *Note: If you are testing lightweight display glasses, the emulator setup actually requires you to run a standard Phone AVD as a host alongside the Glasses AVD to simulate the tethered processing.*

### 4. Select a System Image

After selecting the hardware profile, you must download a system image that supports the XR features. Look for images labeled **"Android XR"** (typically based on API 34 or higher). Ensure you select the **Google APIs** variant to have access to the necessary XR services during runtime.

You should select the **`Google Play XR API v3 ARM 64 v8a System Image (Developer Preview)`**, which is the one you currently have highlighted.

Here is why that is the correct choice:

* **It is the Recommended Image:** The star icon next to the system image indicates that Android Studio officially recommends it for your selected form factor and host machine architecture (ARM64, meaning you are running Apple Silicon).
* **Latest API Support:** The "v3" indicates that this image contains the latest Developer Preview 3 iteration of the Android XR APIs. To successfully compile and run the most recent Jetpack Compose for XR, SceneCore, and ARCore samples from the AndroidX tree, you need the most up-to-date framework components on the emulator.

Once you ensure that 1.5 GB system image is downloaded (using the download arrow icon next to it), you can hit **Finish** to create the AVD and start running your immersive samples.

### 5. Run the Samples

1. In the **Device Manager**, click the **Play** button next to your new XR AVD to launch the emulator.
2. Once the emulator has booted, select the desired sample module (e.g., `glimmer-sample`) from the **Run/Debug Configuration** dropdown in the Android Studio toolbar.
3. Click **Run** (Shift+F10).
4. Put on your headset (if using physical hardware) or use the **Emulator Extended Controls** (three dots > Virtual Sensors) to simulate head movement and hand tracking within the environment.

### Troubleshooting

* **Emulator fails to start:** Ensure that "Windows Hypervisor Platform" is enabled in Windows Features or that you have granted necessary permissions on macOS.
* **Black screen in XR Headset:** This often happens if the GPU drivers are outdated. Ensure you are using the latest Game Ready or Studio drivers for your NVIDIA/AMD card.
* **Missing XR features in Layout Inspector:** Verify you are using Android Studio Canary and that the app is running on an API 34+ XR system image.
* **Input not responding:** Check the "Virtual Sensors" tab in the emulator settings to ensure hand tracking data is being sent to the guest OS.

### Summary: Choosing Your SDK

| Feature | Glimmer (AI Glasses) | Compose for XR (Headsets/Wired Glasses) |
| :--- | :--- | :--- |
| **Primary Library** | `androidx.xr.glimmer` | `androidx.xr.compose` |
| **Display Type** | 2D Heads-up Display (HUD) | 3D Spatial / Volumetric |
| **Tracking** | 3DoF (Orientation only) | 6DoF (Position + Orientation) |
| **Environment** | Screen-locked / Projected | World-anchored / Spatial |
| **Input** | Touchpad, Voice, Gestures | Hand Tracking, Controllers, Gaze |
| **3D Assets** | Not Supported | Supported (.gltf via SceneCore) |

For most developers starting with the **Android XR Emulator**, the **Compose for XR** track is the recommended path as it represents the full spatial computing vision of the platform.

**Hardware Requirements Note:** Because spatial rendering is intensive, the XR Emulator has strict hardware requirements. If you are on a Mac, you need Apple Silicon (M1 or newer) running macOS 13.3+. If you are on Windows, you need Windows 11, at least an Intel 9th Gen or Ryzen 1000-series CPU with VMX enabled, 16GB of system RAM, and a dedicated GPU (NVIDIA 10-series/AMD RX 5000-series or newer) with at least 8GB of VRAM.

#### Notes about input
Here is exactly why the XR emulator is routing your inputs:

* **The Scroll Wheel (Direct Event Routing):** When you use the scroll wheel, the emulator fires off a raw `AXIS_VSCROLL` hardware event directly to the window currently in focus. Jetpack Compose recognizes this standard generic motion event and seamlessly scrolls your `LazyColumn`, just like it would if you had a physical Bluetooth mouse paired to a standard Android tablet.
* **Click and Drag (System Interception):** In a spatial OS, clicking and dragging on a 2D panel is fundamentally ambiguous. The system compositor typically reserves the "click, hold, and drag" action as the gesture to **grab and reposition the entire window** in 3D space (simulating a hand pinch or a laser pointer grab). Because the XR window manager intercepts that drag gesture at the system level to check if you are trying to move the physical panel, the swipe event never actually gets passed down to your Compose UI.

It is definitely a paradigm shift! In traditional mobile Android, your app owns the glass and every touch belongs to your UI. In XR, the operating system has to play referee with your mouse clicks to decide if you are interacting *with* the app's contents or interacting with the *spatial container* holding the app.

**How to test scrolling in the Emulator:**
Since you cannot "swipe" to scroll with a mouse:
1. **Use the Scroll Wheel:** As noted above, this is the most reliable way to trigger `Modifier.verticalScroll` or `LazyColumn` behavior.
2. **Primary Click (Tap):** A quick click (without dragging) is still passed through as a `PointerEventType.Press` and `Release`, allowing you to interact with buttons and clickable items.
3. **Virtual Sensors:** Open the **Extended Controls** (three dots) > **Virtual Sensors** > **Device Pose**. You can use the "Low-level" input simulation to mimic hand tracking "pinch and drag" gestures, which the system can distinguish from window-move gestures based on the depth and intent of the hand model.

**Developer Tip:**
When designing for XR, avoid relying on edge-swipes or complex multi-touch gestures that might conflict with system-level window management. Stick to clear, clickable targets and standard scrollable containers that respond to generic motion events.

### Handling Spatial Input in Code

When moving from touch to spatial input, you should use the `androidx.xr.compose` spatial input modifiers. These allow your UI to respond to gaze (where the user is looking) and hand gestures (pinching).

```kotlin
// Example: Making a component respond to spatial interaction
Box(
    modifier = Modifier
        .size(100.dp)
        .spatialClickable {
            // Handles both hand-pinch and controller triggers
            doSomething()
        }
) {
    Text("Interact with me")
}
```

* **Gaze-and-Pinch:** The system automatically handles the "hover" state when a user looks at a `spatialClickable` element.
* **Entity Input:** For 3D objects (Entities) created via SceneCore, use `setEntityInputListener` to capture 6DoF interactions that occur outside of the Compose layout bounds.

---
