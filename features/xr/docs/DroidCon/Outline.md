Here is the comprehensive, restructured outline. I have woven your raw text into the logical progression we discussed—front-loading the hardware constraints (the "Why") before diving into the Split-Computing architecture and the Jetpack Compose Glimmer implementation (the "How").

I've also integrated your specific philosophy on the Bring Your Own Key (BYOK) architecture to wrap up the intelligence layer.

---

# Android AI Glasses Dev: Building with Jetpack Compose Glimmer

## 1. Introduction: The Platform Divide

The computing landscape is undergoing a tectonic shift, moving from the confines of rectangular screens held in the palm to immersive, persistent interfaces displayed directly in the user’s line of sight. Android XR introduces a standardized framework that treats Extended Reality (XR) devices as first-class citizens within the Android ecosystem.

However, the hardware platform spans different categories that require fundamentally different development approaches:

* **XR Headsets (Samsung Galaxy XR):** Prioritize *immersion*. They use camera passthrough and dual Micro-OLED internal displays to create fully virtual or mixed-reality environments.
* **Wired XR Glasses (XREAL AURA):** Prioritize *productivity*. Users view the real world directly while digital content is projected onto dual transparent lenses, powered by a tethered compute puck.
* **AI Glasses:** Prioritize *ambient reality*. Lightweight, socially acceptable, and designed for glanceable intelligence. This category is further split into **No-Screen (Audio-First)** and **Mono-Screen (Monocular Display)**.

**The Target of this Guide:** This documentation focuses exclusively on developing for **Mono-Screen AI Glasses** using Jetpack Compose Glimmer.

## 2. A Tale of Two Form Factors: Why Declarative Code Fails Across Hardware

You cannot simply write one declarative Compose UI and expect it to automatically scale from an immersive XR Headset down to a pair of Mono-Screen AI Glasses. The system cannot safely “auto-adjust” the visual layer because of three foundational constraints:

### Constraint 1: The Physics of Light vs. Developer Intent

AI Glasses use additive optical see-through displays where black is transparent and darker colors emit less light.

* If you declare `Surface(color = Color.DarkGray)` on a headset, it renders perfectly. On transparent glasses, that dark gray becomes invisible against a white wall.
* The runtime cannot autonomously override your RGB rendering values (e.g., auto-inverting to neon green) without breaking your explicit layout contracts or blinding the user.

### Constraint 2: Spatial Alignment and the Hardware Horizon

* On a Headset (110° FOV), an alert anchored via `Modifier.align(Alignment.TopEnd)` sits comfortably in the user’s upper peripheral vision.
* On Mono-Screen Glasses (e.g., 50° FOV), the physical edges of the display prisms cut off mid-viewport. That top-right alignment means the alert is physically sliced in half or completely invisible outside the glass lens.

### Constraint 3: The 50-Gram Limit & Thermal Budgets

To hit the form factor of normal frames (e.g., Gentle Monster), AI glasses drop the second waveguide and projector. Powering two micro-displays halves battery life and generates noticeable heat on the user’s face. Furthermore, projecting a massive white UI box will drain the tiny ~155–245 mAh battery in minutes.

## 3. The Architecture: The Shift to Split-Computing

To solve the 50-gram thermal problem, Android XR relies on a **Split-Computing (Projection-Based)** model. AI Glasses are treated as a “Thin Client.”

The heavy lifting—app logic, AI inference, and network connectivity—happens entirely on the host smartphone in the user’s pocket. The glasses simply act as an external sensor hub and a transparent projection surface.

### How Jetpack Projected Works

The `androidx.xr.projected` library abstracts the complex inter-device communication required to “throw” an Android Activity from the host phone to the wearable display.

* **The Bridge (IPC):** Manages the bi-directional stream of video (rendering the UI out) and sensor data (routing the 12MP world-facing camera back in).
* **The `xr_projected` Declaration:** You must declare `android.hardware.display.category.XR_PROJECTED` in your Manifest to signal that the Activity is designed to be projected.
* **The Capability Handshake:** Because the ecosystem includes *No-Screen* audio glasses, your `onCreate` method must perform a capability check (`CAPABILITY_VISUAL_UI`). If you attempt to render a screen on audio-only frames, the app will fail.

### The Input Interaction Model (The "No-Touch" Paradigm)

There is no touchscreen floating in the air. The SDK abstracts raw sensor data from the side touchpad into `ProjectedInputEvent` flows.

* **Gestures:** Tap (Select), Swipe (Scroll/Focus), Double Tap (Dismiss).
* **The "Earcon" Requirement:** Audio feedback is mandatory. Because there is a tiny but perceptible latency when tapping a plastic frame, you must play a sound (an “earcon”) immediately so the user knows the input registered.

## 4. Jetpack Compose Glimmer: The Software Solution

Because standard Material Design fails against the physics of additive light (Constraint 1) and thermal limits (Constraint 3), Google created Jetpack Compose Glimmer (`androidx.xr.glimmer`).

### The Glimmer Design System

* **GlimmerTheme:** Replaces standard Material theming to enforce high-contrast, semi-transparent backgrounds and strips away gradients to prevent halation.
* **Text & Typography:** Tuned for legibility on small projections, defaulting to bolder weights to prevent “shimmering” against real-world backgrounds.

### Core Components

Do not use `androidx.compose.material3` equivalents.

* **Cards & TitleChips:** Designed around "glanceability." A TitleChip provides non-interactive context, while the Card organizes the AI's response into specific, minimal-footprint slots.
* **VerticalList:** Do not use `LazyColumn`. Standard lists don’t handle “temple swipes” correctly. `VerticalList` is optimized to show limited items and automatically handles the “focus-to-scroll” mechanics required when you don't have a touchscreen.

## 5. The Intelligence Layer: Zero-Footprint AI

To create a multimodal "Look and Ask" agent, the phone takes the silent image frame from the glasses, bundles it with the user's audio command, and streams both directly to the Gemini multimodal engine.

### Thermal Throttling the Camera

Streaming 60fps video down a tether will physically overheat the glasses. You must implement a Frame Dropping (Backpressure Strategy), grabbing a single `ImageProxy` exactly when the AI engine is ready, and silently discarding intermediate frames.

### Securing the Gemini Live API Key (BYOK)

You must **never** hardcode an API key in the APK binary, as decompilers (`jadx`) or simple `strings` commands can extract it instantly from the compiled Dalvik Executable or native C++ libraries. ProGuard/R8 obfuscation does not encrypt string literals.

**The Solution:** Implement a Bring Your Own Key (BYOK) architecture.

1. The user generates an Auth Key in Google AI Studio.
2. The app provides a secure onboarding screen where the user inputs the key, which is then saved exclusively to the device’s local `EncryptedSharedPreferences`.
3. The phone initializes the Gemini Live stateful WebSocket connection locally. The image and audio are kept strictly in volatile memory. Once the AI returns an answer, the physical frame data is instantly destroyed, ensuring a zero-footprint, privacy-safe loop.

## 6. Conclusion (The Convergence Thesis)

It is easy to view these form factors as competing, but they are converging. Currently, physics forces a trade-off: immersion (Wired XR) or comfort (AI Glasses).

Wired XR Glasses are a technological stopgap; AI Glasses are a cultural stopgap. As waveguide optics become more efficient, the bulky "sunglasses" will inevitably slim down into transparent, wireless frames. By mastering multimodal "Look and Ask" interactions with Glimmer today, developers are building the operating system for the inevitable future where all glasses are smart, immersive, and indistinguishable from the pair you wear today.

---

# Presentation Outline: Building for Reality with Jetpack Compose Glimmer

## 1. Introduction: The Hardware Divide

* **The Android XR Spectrum:** Define the three core hardware categories.
* *XR Headsets & Wired Glasses:* Built for **Immersion** (Dual-display, heavy compute, stereoscopic 3D).
* *AI Glasses:* Built for **Ambient Reality** (Lightweight, wireless or low-power thin client).


* **The Target Device:** This talk focuses on **Mono-Screen (Display) AI Glasses**.
* **The Core Thesis:** You cannot build software for AI Glasses without understanding the physical laws governing the hardware.

## 2. The Three Hard Limits of AI Glasses (The "Why")

* **Constraint 1: The 50-Gram Limit & Thermal Budgets:** Tiny batteries (~155–245 mAh) require offloading compute to prevent the frames from overheating on the user's face.
* **Constraint 2: The Physics of Additive Light:** Optical see-through displays mean black = transparent, and white = a blinding flashlight.
* **Constraint 3: The Interaction Void:** No touchscreens or 6DoF controllers—only temple touchpads and head tracking.

## 3. The Software Divide: How the Jetpack XR SDK is Programmatically Split

*Google split the Jetpack XR SDK into two distinct, strict library ecosystems based on your hardware target.*

### Path A: Libraries for Fully-Immersive Experiences (Headsets & Wired Glasses)


Targeting dedicated, high-fidelity devices where you can spatialize UI and load 3D models.

* **`Jetpack Compose for XR` & `Material Design for XR`:** For declaratively building spatial layouts that adapt to 3D environments.


* **`Jetpack SceneCore`:** The native 3D rendering engine to build and manipulate the scene graph.


* **`ARCore for Jetpack XR`:** Provides real-world semantic perception capabilities.


* The Architectural Note (Why SceneCore and ARCore are separated):


* Google deliberately separated them so developers aren't locked into Google’s rendering tools.


* *Flexibility:* A developer can use ARCore for hand-tracking math but pipe it into a custom Vulkan/OpenGL or OpenXR engine instead of SceneCore.


* Alternatively, a VR app (like a virtual cinema) can use SceneCore for 3D rendering while completely ignoring ARCore perception.





### Path B: Libraries for Augmented and Helpful Experiences (AI Glasses)



Targeting lightweight audio and display glasses via projected app experiences from a phone.

* **`Jetpack Projected`:** The core API that establishes the connection bridge and Context from the phone to the glasses.


* **`Jetpack Compose Glimmer`:** The dedicated UI toolkit optimized specifically for transparent display glasses.



## 4. The Architectural Solution: Split-Computing & The Lifecycle

* **The Phone as the Server:** The glasses act as a thin client sensor hub.
* **The Lifecycle Loop (Referencing image_047a8a.png):**
* *`onCreate()` to `onResume()`:* Initializing the `ProjectedDeviceController` and starting input consumption when donned.
* *The Activity Running State:* Understanding from `image_047a8a.png` that physical display state changes affect visual rendering but do not alter the Android activity lifecycle.
* *`onPause()` to `onDestroy()`:* Instantly unbinding camera and microphone streams when the glasses are doffed to prevent thermal throttling.


* **The Capability Handshake:** Using `CAPABILITY_VISUAL_UI` at launch to branch code cleanly between a Glimmer visual layout (Display glasses) and a voice agent (Audio-only glasses).

## 5. Deep Dive: Jetpack Compose Glimmer (The Software Solution)

* **Designing for Additive Light:** How `GlimmerTheme` handles safe zones, semi-transparent contrast, and eliminates solid backgrounds.
* **Glanceable Components:** Utilizing `TitleChip` for status-barless context and `Card` containers to minimize pixel footprint and conserve battery.
* **Focus-Based Navigation:** Replacing `LazyColumn` with `VerticalList` to map linear temple touchpad swipes (`ProjectedInputEvent`) to glowing outline focus states instead of touch coordinates.
* **The Latency Mask:** Implementing immediate audio cues ("Earcons") on touchpad taps to mask optical projection delay.

## 6. The Intelligence Payload: Zero-Footprint AI

* **Camera Backpressure:** Implementing frame-dropping strategies on the world-facing camera to avoid thermal choking.
* **Bring Your Own Key (BYOK) Security:** Why hardcoding keys in the APK natively exposes them to decompilers, and how to store keys securely in local encrypted storage.
* **The Stateful Stream:** Opening a bidirectional WebSocket using the Google GenAI SDK to stream image frames and microphone audio into a conversational Gemini Live loop.

## 7. Conclusion: The Convergence Thesis

* Wired XR glasses are a technological stopgap; AI glasses are a cultural stopgap.
* Mastering the lightweight, multimodal interaction model of Glimmer today prepares developers for the inevitable future where all smart eyewear seamlessly merges.