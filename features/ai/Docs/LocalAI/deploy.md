For mid-range phones like the Pixel 9a where system-managed **AICore (Gemini Nano)** is unavailable or restricted—since **Option C** only works on supported flagship hardware—your new **"Download AI (Local Execution)"** mode is the exact right approach.

Here is the best setup for those devices based on your 6-app architecture and the best practices guide:

### 1. Best Model & Quantization for Mid-Range Hardware

* **Use the LiteRT-LM Kotlin API:** Target `com.google.ai.edge.litertlm`, since the older MediaPipe LLM Inference API is in maintenance-only mode.


* **Stick to ~1B Parameter Models with INT8 Quantization:** Use **INT8 Dynamic or Full Integer Quantization**, which shrinks a 1B parameter model down to **~500–800 MB** for NPU and CPU execution.


* **Alternative GPU Execution:** If NPU support is limited on the device's SoC, use **FP16 Quantization** targeting the GPU delegate (`Accelerator.GPU`).



### 2. Best Delivery & Multi-App Strategy

Because mid-range phones often have tighter storage and RAM budgets, how you deliver and share the downloaded weights across **KoColor, Seaweed, PhotoDo, AshBike, and GoSwift** matters:

* **For Your 6-App Suite (Best Overall): Custom Download Pipeline + Shared Host**
* Having all six apps download the same model into their private `context.filesDir` sandboxes would waste over 7 GB of storage and bandwidth.


* Download the model once using a `WorkManager` `CoroutineWorker` promoted to a Foreground Service (`ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC`) with `.part` files and HTTP `Range` header resume support.


* **To save both Disk and RAM (Pattern 2 — Centralized Bound Service via AIDL):** Have one core host app/service manage the single download and the `LiteRT-LM` `Engine` instance, while your other five apps bind to it via an `AIDL` interface protected with `android:protectionLevel="signature"` to stream tokens over IPC. This keeps total device RAM usage to a single loaded model instance.


* **To save Disk only (Pattern 1 — Signature-Protected `ContentProvider`):** Have whichever app downloads the model first expose a read-only `ParcelFileDescriptor` (`ParcelFileDescriptor.MODE_READ_ONLY`) via a `ContentProvider` secured with a custom `signature` permission so sibling apps can use the weights without re-downloading.




* **For Single-App Play Store Delivery: Play for On-Device AI (Option A)**
* If distributing via Google Play, you can package models into an `on-demand` **AI Pack** that triggers when the user selects `"Download AI (Local Execution)"`.


* Use **Device Targeting** in XML (filtering by RAM, system properties, or SoC model) so lower-tier or mid-range devices automatically receive a smaller quantized model variant while flagships receive a larger one.





### 3. Critical Memory & Stability Safeguards for Non-Flagship Phones

Mid-range devices are much more aggressive about killing background apps when RAM fills up. Make sure your `"Download AI"` implementation includes:

* **Aggressive Memory Unloading (`ComponentCallbacks2`):** Register a `ComponentCallbacks2` listener for `onTrimMemory(TRIM_MEMORY_RUNNING_CRITICAL)` and `TRIM_MEMORY_UI_HIDDEN` to close and unload the engine as soon as the app goes to the background, preventing the Android Low Memory Killer (LMK) from terminating your app.


* **Single-Thread Background Ownership:** Never call `Engine(config).initialize()` on the main thread. Dedicate a single-thread executor (`Executors.newSingleThreadExecutor().asCoroutineDispatcher()`) to own engine initialization, token streaming, `cancelProcess()`, and `close()`.


* **Pre-Flight Disk & Checksum Checks:** Before downloading, verify free space is at least **1.2x the uncompressed model size** via `StorageManager.getAllocatableBytes(uuid)`. Before loading into `LiteRT-LM`, verify the exact byte count and **SHA-256 checksum** on `Dispatchers.IO` so a truncated download never triggers a native JNI crash.


* **Guard the AI Router with `LocalModelState.Ready`:** Even when `useDownloadAi` is `true` in `DataStore`, verify that `LocalModelState is Ready` and `File(absoluteModelPath).exists()` returns `true` before routing prompts to the local engine—otherwise keep the user on your `Firebase` or `BYOK` cloud options until the download finishes verifying.