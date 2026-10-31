Copy and paste the following prompt directly into your coding agent to read the documentation and generate the complete implementation across your `features/ai` module and six applications:

---

Please read the two reference documents (**Best Practices for Downloading On-Device AI Models on Android** and **Best Practices for Downloading**) and generate the production Kotlin code to wire up the actual model downloader, verification pipeline, LiteRT-LM runtime, and multi-app model sharing for the `"Download AI (Local Execution)"` (`useDownloadAi`) option we just added to `features/ai`.

We have already wired the `useDownloadAi` boolean across `AiConfigurationCard.kt`, `AiConfigurationUiState.kt`, `AiConfigurationEvent.kt`, `AiConfigurationViewModel.kt`, `AiConfigurationSettings`, and the 6 app repositories (`KoColorSettings`, `Seaweed` `UserSettingsRepositoryImpl`, `PhotoDo` `DataStoreAppSettingsRepository`, `AshBike` `AppSettingsRepository`, `GoSwift` `GoSwiftAiSettings`, and `FakeSmartCaptureSettings`). Now implement the following components according to the specifications in those documents:

1. **Gradle Dependencies & Manifest Configuration**
* Add `implementation("com.google.ai.edge.litertlm:litertlm-android:0.18.0")` (requiring `minSdk = 24`) to the `features/ai` module.
* Configure the required `INTERNET` and foreground service data sync (`FOREGROUND_SERVICE_TYPE_DATA_SYNC`) permissions in `AndroidManifest.xml`, plus the `<uses-native-library>` entries for GPU acceleration.


* Declare a custom signature-level permission (`<permission android:name="com.yoursuite.permission.READ_AI_MODEL" android:protectionLevel="signature" />`) so all 6 apps signed with the same key can share a single downloaded model without duplicating disk space.




2. **Decoupled `LocalModelState` State Machine**
* Create the `LocalModelState` sealed interface in `features/ai` with states: `NotDownloaded`, `Downloading(bytesDownloaded: Long, totalBytes: Long, progressPercent: Int)`, `VerifyingChecksum`, `Ready(absoluteModelPath: String, modelVersion: String)`, and `Error(reason: String, isRetryable: Boolean)`.


* Update `AiConfigurationUiState.kt` and `AiConfigurationEvent.kt` to expose `localModelState: LocalModelState` alongside `useDownloadAi: Boolean`, and add events for starting, canceling, retrying, and deleting the model download, as well as confirming cellular data downloads.




3. **Pre-Flight Checks & Resumable `WorkManager` Download Pipeline**
* Before starting a download, check `StorageManager.getAllocatableBytes(uuid)` to verify at least 1.2x the model's uncompressed size is available.


* Default `WorkManager` constraints to `NetworkType.UNMETERED` and `setRequiresStorageNotLow(true)`, and trigger an explicit cellular confirmation dialog in the UI if the user is on a metered connection.


* Implement `AiModelDownloadWorker : CoroutineWorker` promoted to a foreground service (`setForeground(ForegroundInfo(...))` with `ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC`).


* Stream weights using our HTTP client directly into `context.filesDir` (not `DownloadManager`, which cannot write to `filesDir`) using a temporary `.litertlm.part` file and `Range: bytes=<existing_length>-` headers (`HTTP 206 Partial Content`) for automatic pause and resume.


* Implement two-stage verification on `Dispatchers.IO`: validate exact expected byte length and verify the SHA-256 checksum against the model manifest before atomically renaming `model.litertlm.part` to `model.litertlm`.




4. **UI & ViewModel Wiring (`AiConfigurationViewModel.kt` & `AiConfigurationCard.kt`)**
* Perform a startup existence check (`File(modelPath).exists()`) on cold start so OS cache clears or Auto Backup restores of `useDownloadAi = true` reset `LocalModelState` to `NotDownloaded` gracefully.


* Bind `WorkManager.getWorkInfosForUniqueWorkFlow("ai_model_download")` into `AiConfigurationViewModel.kt` to emit live `LocalModelState` updates.


* Update `AiConfigurationCard.kt` so that when `"Download AI (Local Execution)"` is selected, it renders real-time MB/GB progress, a progress bar, Cancel/Retry buttons, and a "Delete Downloaded Model" button when `Ready`.


* Update the AI inference router so prompts only route to the local engine when `useDownloadAi == true`, `localModelState is LocalModelState.Ready`, and `File(absoluteModelPath).exists()` returns `true`.




5. **LiteRT-LM Runtime Engine & Thread-Safe Lifecycle Manager**
* Implement `LocalLiteRtLmEngineManager` targeting `com.google.ai.edge.litertlm` (`Engine`, `EngineConfig`, `Conversation`, `ConversationConfig`, `SamplerConfig(topK = 10, topP = 0.95, temperature = 0.8)`) optimized for ~1B INT8/FP16 `.litertlm` models on mid-range hardware like the Pixel 9a.


* Dedicate a single-thread executor (`Executors.newSingleThreadExecutor().asCoroutineDispatcher()`) that exclusively owns `Engine` and `Conversation` creation, `Engine(config).initialize()`, token streaming, `cancelProcess()`, and `close()`—never initializing on the main thread.


* Enforce strict `AutoCloseable` safety: store the `Engine` reference only after `initialize()` succeeds, never call `close()` twice, always call `cancelProcess()` and await flow completion before closing an active stream, and run cleanup on the dedicated single-thread executor rather than inside `viewModelScope` in `onCleared()`.


* Register `ComponentCallbacks2` to listen for `onTrimMemory(TRIM_MEMORY_RUNNING_CRITICAL)` and `TRIM_MEMORY_UI_HIDDEN` to unload the engine when backgrounded on RAM-constrained devices.




6. **Cross-App Model Sharing Across All 6 Apps**
* Implement a signature-protected `ContentProvider` (`openFile` returning `ParcelFileDescriptor.open(modelFile, ParcelFileDescriptor.MODE_READ_ONLY)`) in the shared AI module so that once any of the 6 apps (KoColor, Seaweed, PhotoDo, AshBike, or GoSwift) downloads and verifies the `.litertlm` model, the remaining apps detect and load the verified model descriptor without re-downloading.