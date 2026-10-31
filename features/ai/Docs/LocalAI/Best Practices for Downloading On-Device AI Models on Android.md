# **Best Practices for Downloading On-Device AI Models on Android**

# **1\. Executive Summary**

Delivering large on-device AI and Large Language Models (LLMs) to Android applications requires a dedicated delivery, storage, verification, and lifecycle architecture. Because quantized on-device models typically range from 500 MB to over 2 GB, bundling model weights directly inside the APK's `assets/` directory is a critical antipattern that bloats initial install size, causes uncompressed memory-mapping issues, and forces users to re-download gigabytes of weights on every minor code update.

This document outlines the production best practices for downloading, verifying, initializing, and sharing on-device AI models across modern Android applications using [Play for On-device AI](https://developer.android.com/google/play/on-device-ai), custom background download pipelines, and the [LiteRT-LM Kotlin API](https://developer.android.com/agents/skills/device-ai/litert/litert-lm/skill).

# **2\. Choosing the Right Model Delivery Architecture**

Android supports three primary architectural patterns for delivering AI models to a device. Choose the pattern that matches your distribution channel and multi-app strategy:

## **Option A: Play for On-Device AI (AI Packs)**

If your app is distributed through Google Play, [Play for On-device AI](https://developer.android.com/google/play/on-device-ai) is the recommended managed solution. You package custom models into dedicated **AI Packs** inside your Android App Bundle (AAB).

* **Size Limits:** Individual AI packs support up to **1.5 GB** (compressed download size), with a maximum cumulative app size of **4 GB** per version (requires `minSdk` 21 or higher for apps over 1 GB).  
* **Delivery Modes:**  
  * `install-time`: Downloaded as part of the app installation so the model is guaranteed to be present on first launch.  
  * `fast-follow`: Automatically triggered in the background immediately after the app is installed; the user can open the app before the download finishes.  
  * `on-demand`: Requested explicitly at runtime when the user enables a local AI feature (such as toggling "Download AI (Local Execution)").  
* **Device Targeting:** Allows you to define device groups in XML based on RAM, system properties, or SoC model so flagship devices receive higher-precision models while lower-tier devices receive smaller quantized variants or a default empty pack.  
* **Automatic Delta Patching:** AI packs are updated alongside your app binary, but Google Play automatically patches unchanged files so users do not re-download the model when you ship a regular app update.  
* **Key Restrictions:** AI packs can only contain model files (no Java/Kotlin code or native `.so` libraries). Native inference libraries must live in the base module or a feature module configured with matching targeting rules. Never cache AI pack paths across app launches—always query the pack status at startup.

## **Option B: Custom In-App HTTP Download Pipeline (WorkManager \+ Coroutines)**

When hosting models on your own CDN (such as Cloudflare R2, AWS S3, or Hugging Face) or supporting sideloaded builds and dynamic model catalogs, implement a resilient background download pipeline.

* **Use `WorkManager` with a Foreground Service:** Multi-gigabyte downloads take minutes. Wrap your download inside a `CoroutineWorker` promoted to foreground execution (`setForeground(ForegroundInfo(...))`) with `ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC` so the OS does not kill the transfer when the user leaves the settings screen.  
* **Storage Destination (`filesDir` vs. `DownloadManager`):**  
  * As noted in the [LiteRT-LM Android Guide](https://developer.android.com/agents/skills/device-ai/litert/litert-lm/skill), Android's system `DownloadManager` **cannot write to internal storage (`context.filesDir`)**—it can only write to `context.getExternalFilesDir(null)` or public storage.  
  * If security and sandbox isolation are priorities, use your app's HTTP client (OkHttp or Ktor) inside a `CoroutineWorker` to stream directly into `context.filesDir`.  
* **Pause/Resume with HTTP Range Requests:** Mobile connections drop frequently. Always download to a temporary `.part` file (for example, `gemma3_int8.litertlm.part`) and send `Range: bytes=<existing_length>-` headers (`HTTP 206 Partial Content`) so interrupted downloads resume without discarding gigabytes of progress.

## **Option C: System Shared Model via AICore (Gemini Nano)**

For general-purpose text summarization, rewriting, or smart replies on supported flagship hardware, evaluate Google's system-managed AICore service. Because the OS manages the single Gemini Nano installation across the entire device, your app consumes zero storage overhead for weights. However, custom fine-tuned models and specialized architectures still require Option A or Option B.

# **3\. Pre-Flight Checks, Network Constraints, and File Integrity**

Before initiating a large model download—and before passing a downloaded file to the native C++ inference runtime—enforce the following safeguards:

* **Pre-Flight Disk Space Verification:** Query `StorageManager.getAllocatableBytes(uuid)` before starting the download. Require at least **1.2x the model's uncompressed size** to ensure the device does not run out of storage mid-stream or during KV-cache allocation.  
* **Wi-Fi / Unmetered Network Policy:** Default `WorkManager` constraints to `NetworkType.UNMETERED` and `setRequiresStorageNotLow(true)`. If a user selects "Download AI" while on cellular data, present an explicit confirmation dialog displaying the exact download size (for example, "Download 1.1 GB over mobile data?") before overriding the unmetered constraint.  
* **Two-Stage Integrity Verification (Size \+ SHA-256):** Corrupted or truncated weight files cause immediate native JNI crashes (`SIGSEGV` or `IllegalStateException`) during memory mapping. Verify that the downloaded `.part` file's byte count matches the exact expected size from your model manifest. Compute the file's **SHA-256 checksum** on `Dispatchers.IO` and compare it against a signed server manifest before atomically renaming `model.litertlm.part` to `model.litertlm`.

# **4\. State Machine Design for the "Download AI" Configuration UI**

A simple boolean flag (`useDownloadAi: Boolean`) in `DataStore` is necessary to record the user's preference, but the UI and ViewModel should decouple user intent from model readiness using a dedicated sealed state machine:sealed interface LocalModelState {

    data object NotDownloaded : LocalModelState

    data class Downloading(

        val bytesDownloaded: Long,

        val totalBytes: Long,

        val progressPercent: Int

    ) : LocalModelState

    data object VerifyingChecksum : LocalModelState

    data class Ready(

        val absoluteModelPath: String,

        val modelVersion: String

    ) : LocalModelState

    data class Error(

        val reason: String,

        val isRetryable: Boolean

    ) : LocalModelState

}

## **UI & ViewModel Wiring Best Practices**

* **Do Not Route Prompts Until `Ready`:** If `useDownloadAi` is `true` in `DataStore` (for instance, after an app reinstall or cache clear where user settings were restored via Auto Backup, or while a download is still at 45%), your AI router must check that `LocalModelState is Ready` and that `File(absoluteModelPath).exists()` returns `true` before routing inference requests to the local engine.  
* **Expose Live Progress and Cancellation:** Bind `WorkManager.getWorkInfosForUniqueWorkFlow("ai_model_download")` into your `AiConfigurationViewModel` so `AiConfigurationCard.kt` can display real-time MB/GB progress, a cancel button, and a "Delete Downloaded Model" action to reclaim disk space.

# **5\. Runtime Initialization & Lifecycle with LiteRT-LM**

As documented in Google's [LLM Inference guide for Android](https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference/android), the older MediaPipe LLM Inference API is now in maintenance-only mode, and Android projects should target the [**LiteRT-LM Kotlin API**](https://developer.android.com/agents/skills/device-ai/litert/litert-lm/skill) (`com.google.ai.edge.litertlm`).

Follow these strict engineering mandates when loading downloaded models:

* **Optimize Quantization for Target Hardware:** As detailed in [Deploying LLM models on Mobile device using Google LiteRT](https://kednaik.medium.com/deploying-llm-models-on-mobile-device-using-google-litert-c0ec5be4bab9), use **INT8 Dynamic or Full Integer Quantization** (reducing a 1B parameter model to \~500–800 MB) for NPU and CPU execution, or **FP16 Quantization** when targeting the GPU delegate (`Accelerator.GPU`).  
* **Never Initialize on the Main Thread:** `Engine(config).initialize()` loads and memory-maps multi-hundred-megabyte weight files and blocks for several seconds. Always execute initialization on a background thread.  
* **Single-Thread Executor Ownership:** Dedicate a single-thread executor (`Executors.newSingleThreadExecutor().asCoroutineDispatcher()`) to own the `Engine` and `Conversation` instances. Creation, token streaming, cancellation, and closing must all run on this single owner thread.  
* **Safe `AutoCloseable` Handling:** Both `Engine` and `Conversation` implement `AutoCloseable`. Calling `close()` twice—or calling `close()` on an `Engine` whose `initialize()` threw an exception—throws an `IllegalStateException`. Store the engine reference only *after* `initialize()` returns cleanly. Never close the engine while a reply is actively streaming; call `cancelProcess()` and wait for the token flow to complete first. Never launch engine cleanup inside `viewModelScope` from `ViewModel.onCleared()`, because `viewModelScope` is already cancelled when `onCleared()` executes. Trigger cleanup directly on your dedicated single-thread engine executor.  
* **Memory Pressure & Thermal Callbacks:** Register a `ComponentCallbacks2` listener to observe `onTrimMemory(TRIM_MEMORY_RUNNING_CRITICAL)` or `TRIM_MEMORY_UI_HIDDEN`. Unload the engine gracefully when the app moves to the background on RAM-constrained devices so the Android LMK (Low Memory Killer) does not terminate your process.

# **6\. Multi-App Model Sharing Across an Application Suite**

Because Android sandboxes each application by package name (`context.filesDir` is private to each APK), having six separate apps in a suite (such as **KoColor, Seaweed, PhotoDo, AshBike, and GoSwift**) each download the same 1.2 GB .litertlm file wastes over 7 GB of user storage and bandwidth.

To make a single download work across all your apps on the same device, use one of these three multi-app architectures:

## **Pattern 1: Signature-Protected ContentProvider (Zero Duplicate Disk Usage)**

Designate one app (or whichever app downloads the model first) to expose the verified model file via a `ContentProvider` secured with a custom signature permission:

* Declare `<permission android:name="com.yoursuite.permission.READ_AI_MODEL" android:protectionLevel="signature" />` in the manifest of all apps in your suite and sign all six apps with the same release signing key.  
* Override `openFile(Uri, String)` in the host `ContentProvider` to return a read-only `ParcelFileDescriptor` (`ParcelFileDescriptor.open(modelFile, ParcelFileDescriptor.MODE_READ_ONLY)`).  
* Sibling apps query the provider URI at launch; if a valid descriptor is returned, they memory-map or copy/reference the model without triggering an external network download.

## **Pattern 2: Centralized Bound AI Service via AIDL (Zero Duplicate Disk & Zero Duplicate RAM)**

Instead of every app loading its own 1 GB engine into RAM simultaneously, create a dedicated lightweight "Core AI Host" service (or embed the service in your primary app):

* The host app manages the single model download, storage lifecycle, and `LiteRT-LM` `Engine` instance.  
* Client apps (KoColor, Seaweed, PhotoDo, AshBike, GoSwift) bind to the service via an `AIDL` interface protected by `android:protectionLevel="signature"` and stream tokens back over IPC.  
* This guarantees that downloading the model once makes local AI immediately available across every app in your ecosystem while keeping total device RAM overhead to a single model instance.

# **7\. Production Readiness Checklist**

- [ ] **No Bundled Weights in `assets/`:** Verified that `.litertlm` / `.task` / `.tflite` files over 50 MB are excluded from the base APK and delivered via Play AI Packs or background download.  
- [ ] **Pre-Download Storage Check:** Verified `StorageManager.getAllocatableBytes()` checks for \>= 1.2x model size before starting download.  
- [ ] **Resumable Background Transfer:** Configured `WorkManager` `CoroutineWorker` with Foreground Service notification and HTTP `Range` header support.  
- [ ] **Atomic File Verification:** Implemented `.part` temporary file streaming, byte-length validation, and SHA-256 checksum verification before marking `LocalModelState.Ready`.  
- [ ] **Decoupled UI State:** Connected `useDownloadAi` toggle to a full `LocalModelState` flow (`NotDownloaded`, `Downloading`, `VerifyingChecksum`, `Ready`, `Error`) rather than assuming a boolean toggle means the file is on disk.  
- [ ] **Off-Main-Thread Engine Ownership:** Restricted `LiteRT-LM` `Engine.initialize()`, streaming, `cancelProcess()`, and `close()` to a dedicated single-thread executor.  
- [ ] **Startup Existence Check:** Verified that the app checks `File(modelPath).exists()` (or AI Pack installation state) on every cold start to handle OS cache clears gracefully.

