# Android AI Glasses Development: A Masterclass in Google XR

The computing landscape is undergoing a tectonic shift, moving from the confines of rectangular screens held in the palm to immersive, persistent interfaces displayed directly in the user’s line of sight. Android XR, and specifically the support for AI Glasses within the Jetpack XR SDK, represents Google’s unified architectural response to this transition. Unlike the fragmented proprietary SDKs of the past, Android XR introduces a standardized framework that treats Extended Reality (XR) devices—ranging from fully immersive headsets to lightweight smart glasses—as first-class citizens within the Android ecosystem.

However, navigating this unified architecture requires understanding the hardware first. The specific software libraries you use are dictated entirely by the physical constraints of the device: how many displays the user is looking at, and where the CPU is located.

---

## The Hardware Platform

The Samsung and Google XR hardware platform—powered by the new Android XR operating system and built in close partnership with Qualcomm—isn’t anchored to just one flagship headset. Instead, it spans fully immersive mixed reality, lightweight AI assistance, and tethered spatial computing.

Here is a breakdown of the core device categories defining the Android XR platform.

### The XR Headset: Immersion First

Designed for heavy spatial computing, gaming, and high-fidelity media, these devices compete directly with the Apple Vision Pro and Meta Quest Pro.

* **Example:** Samsung Galaxy XR.
* **Display:** Dual Micro-OLED panels delivering millions of pixels per eye with a 100°+ field of view (FOV).
* **Processor:** Qualcomm Snapdragon XR2+ Gen 2.
* **Sensors:** Extensive arrays including world-tracking, high-resolution passthrough, depth sensors, and eye-tracking cameras.

### Wired Display Glasses: Productivity First

These devices split the difference between a full XR headset and lightweight frames by tethering advanced AR optics to a pocketable compute unit.

* **Example:** XREAL Project Aura.
* **Display:** Sony Micro-OLED optics offering massive FOVs and electrochromic dimming to block out the real world.
* **Processor:** A split-compute architecture. A tethered puck runs the Android XR OS, while a spatial coprocessor lives inside the glasses for low-latency sensor rendering.

### AI Glasses: Ambient Intelligence First

These devices prioritize form factor and social acceptability, resembling standard designer eyewear (e.g., partnerships with Gentle Monster and Warby Parker). They are designed for ambient, all-day wear.

* **No-Screen Glasses (Audio & AI):** Ditches the display entirely. Features an autofocusing 12-megapixel camera, directional speakers, and microphones. You use them to take hands-free photos or ask Gemini to identify objects in your line of sight.
* **Mono-Screen Glasses (Display):** Projects a bright, vibrant floating rectangle into the corner of *one* eye. Designed for “micro-moments” (like glancing at a floating caller ID or checking your AshBike speedometer) rather than deep immersion.

> **The "Why" Behind the One-Eye Display:** Adding a second waveguide and micro-projector immediately adds weight, halves the battery life, and generates noticeable heat on the user’s face. Furthermore, because Jetpack Compose Glimmer is designed for 2D, glanceable “cards” and text, the user doesn’t need stereoscopic depth. A single eye is perfectly capable of reading a floating 2D notification.

---

## The Unified Software Ecosystem

At its core, Google XR is not a new operating system; it is a spatial extension of Android. This is its greatest strength. The spatial computing revolution is starting with millions of developers who already know the language: you write in Kotlin, build UI with Jetpack Compose, and manage the lifecycle with standard Android Activities.

The technical vehicle for this platform is the **Jetpack XR SDK**. It is a unified umbrella of tools that explicitly branches into two distinct paths:

1. **The Immersive Toolkit (`androidx.xr.compose`, `androidx.xr.scenecore`):** For VR Headsets and Wired Binocular Glasses. This provides APIs for real-time 3D perception and stereoscopic spatial window clustering.
2. **The Ambient Toolkit (`androidx.xr.glimmer`, `androidx.xr.projected`):** For Monocular Display AI Glasses. This path abandons heavy 3D rendering. It provides APIs for tethered split-computing and a strictly optimized, flat UI toolkit to manage additive light and thermal budgets.

### Environment Setup & Configuration

To begin, your development environment must be on the bleeding edge.

* **IDE:** Android Studio Canary.
* **Android SDK:** Android 15 (API Level 35) or Android 16 Developer Preview.
* **Emulator:** The Android XR Emulator, configured with an “AI Glasses” hardware profile.

For a tethered AI Glasses application, the `projected` (Context Bridge) and `glimmer` (UI toolkit) libraries are the essential building blocks. Explicitly avoid including immersive libraries like `scenecore` or `arcore`, as AI Glasses lack the hardware to run heavy 3D spatial mapping.

```kotlin
dependencies {
    // The foundational XR system capabilities.
    implementation("androidx.xr.runtime:runtime:<current_version>")

    // ESSENTIAL: The Context Bridge library for connecting Phone <-> Glasses.
    implementation("androidx.xr.projected:projected:<current_version>")

    // ESSENTIAL: The UI toolkit optimized for transparent, additive displays.
    implementation("androidx.xr.glimmer:glimmer:<current_version>")
    implementation("androidx.xr.glimmer:glimmer-google-fonts:<current_version>")
}

```

---

## The Architecture: Split-Computing

The defining characteristic of Android XR for AI Glasses is the Split-Computing architecture. To achieve a sub-50-gram form factor, it is impossible to onboard heavy computation and large batteries directly onto the user's face.

By tethering to a smartphone, the glasses avoid carrying GPS modules, 5G radios, and massive batteries. The host phone runs the application logic, maintains network connectivity, and executes AI inference. The glasses function as a thin client, focused exclusively on lightweight projection and sensor capture.

### How Jetpack Projected Works

This relationship is formalized through the `androidx.xr.projected` library, which abstracts the complex Inter-Process Communication (IPC) required to project an Android Activity from the phone to the wearable display.

To tell Android that an Activity should be projected to an external XR display, declare the category in your `AndroidManifest.xml` within your feature module, alongside the required sensor permissions:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application>
        <activity
            android:name=".XRMainActivity"
            android:requiredDisplayCategory="xr_projected">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
            </intent-filter>
        </activity>
    </application>
</manifest>

```

### The Capability Handshake

Because AI Glasses can be Visual (display) or Audio-Only (no display), your application must inspect device capabilities at startup.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val controller = ProjectedDeviceController.create(this)
    val hasDisplay = controller.capabilities.contains(ProjectedCapabilities.CAPABILITY_VISUAL_UI)
    
    if (hasDisplay) {
        // Safe to initialize Jetpack Compose Glimmer
        setContent { GlassesGlimmerApp() }
    } else {
        // Fallback: Device is audio-only. Start TTS/Voice Agent.
        startConversationalAudioAgent()
    }
}

```

---

## Designing for Additive Light

Why can't XR Headsets and AI Glasses share the same UI code? Because declarative programming can only solve for structural abstraction; it cannot solve for the laws of physics.

In an opaque VR headset, your application owns the canvas. The system renders a dark passthrough dimming layer behind your UI to guarantee legibility. On AI Glasses using Optical See-Through (OST) waveguides, the background is the chaotic real world.

### Jetpack Compose Glimmer (`androidx.xr.glimmer`)

If you declare a standard Compose `Surface(color = Color.DarkGray)` on AI glasses, it becomes completely transparent due to waveguide physics, leaving your text floating illegibly against a potentially white wall. Because a declarative engine doesn’t know *why* you chose a color, it cannot safely auto-invert it.

To solve this, the Jetpack XR SDK provides `GlimmerTheme`. It completely replaces Material 3, offering a tailored `ColorScheme`, `Typography`, and `Shapes` system engineered for maximum legibility and thermal safety on additive displays.

```kotlin
import androidx.xr.glimmer.theme.GlimmerTheme
import androidx.xr.glimmer.components.Surface

@Composable
fun GlassesAppEntry() {
    GlimmerTheme {
        // Glimmer surfaces handle optical safe zones and additive blending
        Surface {
             AssistantLayout()
        }
    }
}

```

### Color: Designing with Light

In Glimmer, color is literal light:

* **Pure Black (`#000000`) is completely transparent.** It emits zero photons. It is your best friend for saving battery and preventing overheating.
* **Pure White (`#FFFFFF`) is a flashlight.** It blasts maximum light into the pupil. Never use large blocks of white backgrounds.
* **The Dark Mode Mandate:** There is no “Light Theme” on AI Glasses. Always design dark, transparent interfaces with bright, sharp foreground elements.

### Shape & Focus Management

On optical waveguides, sharp 90-degree corners can cause harsh light artifacts ("light bleed"). `GlimmerTheme.shapes` defaults to heavily rounded geometry (like `CircleShape` pills) to soften light projection.

Because there is no touchscreen, users navigate your UI by swiping the temple touchpad. In `GlimmerTheme`, focused items do not use a standard Material "ripple". Instead, Glimmer uses a **Glowing Outline**—a highly visible, rounded stroke that provides instant visual feedback while consuming a fraction of the power of a solid background fill.

---

## Sensors and Context Routing

Developing for AI Glasses requires a strict thermal and battery budget. If you leave a camera or microphone open idly on glasses lacking active cooling, you will physically burn the user's face.

> **⚠️ Emulator Limitation:** The Android XR Canary Emulator currently fails to provide a bindable virtual camera or microphone stream through the `ProjectedContext`. The production code below will safely abort during the lifecycle binding phase on an emulator. To test AI pipelines locally, you must temporarily bypass the `ProjectedContext` and bind CameraX to the host `ComponentActivity` instead.

### Using the Camera (ImageCapture)

Do not use `ImageAnalysis` (continuous video) over the Context Bridge. Use `ImageCapture` to take a single high-resolution photo exactly where the user is looking, and immediately let the hardware cool down.

```kotlin
private fun startCameraOnGlasses(activity: ComponentActivity) {
    val projectedContext = try {
        ProjectedContext.createProjectedDeviceContext(activity)
    } catch (e: IllegalStateException) { return }

    val cameraProviderFuture = ProcessCameraProvider.getInstance(projectedContext)
    
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        if (!cameraProvider.hasCamera(cameraSelector)) return@addListener

        // Enforce Thermal and Latency Limits (Mandatory)
        val targetResolution = Size(640, 480)
        val resolutionStrategy = ResolutionStrategy(targetResolution, ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER)
        val resolutionSelector = ResolutionSelector.Builder().setResolutionStrategy(resolutionStrategy).build()

        val imageCapture = ImageCapture.Builder()
            .setResolutionSelector(resolutionSelector)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.unbindAll()
            // CRITICAL: Binds to lifecycle to auto-shutdown when glasses are removed
            cameraProvider.bindToLifecycle(activity, cameraSelector, imageCapture)
        } catch (exc: Exception) {
            Log.e("XR", "Binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(activity))
}

```

### Using the Mic (ASR)

Android XR supports routing Automatic Speech Recognition (ASR) directly through the bridge. Passing the `ProjectedContext` into `SpeechRecognizer` automatically activates the beamforming array, illuminates the privacy LED, and provides a clean text string.

> **Crucial Lifecycle Note:** Unlike CameraX, `SpeechRecognizer` does not automatically observe lifecycles. You must manually call `speechRecognizer.destroy()` in `onDestroy` to turn off the mic and privacy LED.

---

## Securing the AI: The BYOK Architecture

To maintain a zero-footprint application and avoid massive cloud intermediary costs, XR apps should connect directly to Google’s inference engines. However, **you cannot hardcode an API key in an APK.** ProGuard/R8 only shrinks identifiers; it does not encrypt strings. JNI/C++ native libraries are trivially extracted using the `strings` command. Package Name/SHA-1 restrictions are easily bypassed by dedicated attackers using MitM proxies.

**The Solution: Bring Your Own Key (BYOK)**
The application provides a secure onboarding screen where the user provides their own generated Google AI Studio key, which is saved locally to `EncryptedSharedPreferences`.

When the user taps the glasses, the app captures a frame and uses the GenAI SDK directly on the host phone:

```kotlin
// Retrieve the BYOK key from local encrypted storage
val userApiKey = secureStorage.getApiKey()

// Initialize the client on the Host Phone
val client = genai.Client(apiKey = userApiKey)

// Execute the multimodal request using the captured Projected Image
val response = client.generativeModel("gemini-1.5-flash").generateContent(
    content {
        image(glassesCapturedBitmap)
        text("Briefly describe what I am looking at.")
    }
)

```

---

## Conclusion: Why AI Glasses are the Bridge

It is easy to view heavy VR headsets and lightweight AI Glasses as competing products, but historically, they are two paths converging toward a single destination.

Currently, physics forces a trade-off: you can have immersion (Wired XR) or you can have comfort (AI Glasses).

* **Wired XR** is a technological stopgap: it delivers the experience of the future but lacks the wireless, invisible form factor required for mass adoption.
* **AI Glasses** are a cultural stopgap: they deliver the form factor of the future but lack the visual immersion required for deep spatial work.

As waveguide optics become more efficient, the capabilities of Wired XR will shrink into the frames of AI Glasses. For developers, this means the AI Glasses you build for today are the training ground for the spatial era. By mastering the Context Bridge, the physics of additive light via Glimmer, and zero-footprint architecture today, you are effectively building the operating system for the inevitable future.

**The hardware is ready. The SDK is live. It’s time to start building for the eyes, not just the hands.**