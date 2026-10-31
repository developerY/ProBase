Copy and paste the following verification and patch prompt into your coding agent to audit and fix those four multi-app and Android 14+ runtime edge cases:

```markdown
Please audit and patch our `features/ai/local` implementation across `AndroidManifest.xml`, `AiModelProvider.kt`, `AiModelDownloadWorker.kt`, `LocalLiteRtLmEngineManager.kt`, `AiConfigurationViewModel.kt`, and `SmartCaptureOrchestrator.kt` for the following four runtime issues:

### 1. Prevent ContentProvider Authority Collisions & Enable Android 11+ Visibility
* **Dynamic Authority:** In `features/ai/local/src/main/AndroidManifest.xml`, check the `<provider>` declaration for `AiModelProvider`. Ensure it uses `android:authorities="${applicationId}.ai.modelprovider"` rather than a hardcoded static string so installing multiple apps from our suite (KoColor, Seaweed, PhotoDo, AshBike, GoSwift) on the same device does not fail with `INSTALL_FAILED_CONFLICTING_PROVIDER`.
* **Package Visibility (`<queries>`):** Add a `<queries>` block in `features/ai/local/src/main/AndroidManifest.xml` listing the package names of all 6 apps (or a shared `<intent>` / `<provider>` query) so Android 11+ (API 30+) package visibility rules allow each app's `ContentResolver` to discover sibling `AiModelProvider` authorities at runtime.

### 2. Wire Cross-App Model Resolution for Sibling Apps
* **The Issue:** Currently, checking `File(context.filesDir, "model.litertlm").exists()` only checks the current app's private sandbox (`context.filesDir`). If KoColor downloads the model, AshBike or GoSwift will see `File.exists() == false` in their own `filesDir` and won't know the shared model is already on the device.
* **The Fix:** Create a `SharedModelResolver` utility in `features/ai/local` and wire it into `AiConfigurationViewModel` and `SmartCaptureOrchestrator`:
  1. First check if the local `File(context.filesDir, "model.litertlm")` exists and passes size verification.
  2. If missing locally, iterate through the sibling app provider URIs (`content://<sibling_package>.ai.modelprovider/model`) and call `context.contentResolver.openFileDescriptor(uri, "r")`.
  3. If a sibling provider returns a valid read-only `ParcelFileDescriptor`, either:
     - Pass `/proc/self/fd/${pfd.fd}` as the model path to `EngineConfig` while keeping the `ParcelFileDescriptor` open inside `LocalLiteRtLmEngineManager` until `Engine.close()` is called on the single-thread executor, OR
     - If `LiteRT-LM` requires a regular filesystem path on this device, copy the already-verified stream from the sibling `ParcelFileDescriptor` on `Dispatchers.IO` without making any network requests.
  4. Update `AiConfigurationViewModel` startup checks and `SmartCaptureOrchestrator` so sibling apps immediately transition to `LocalModelState.Ready` when a valid model is available via `AiModelProvider`.

### 3. Enforce Android 14+ (API 34+) Foreground Service Type Declarations
* **Manifest Declaration:** In `features/ai/local/src/main/AndroidManifest.xml`, ensure WorkManager's foreground service is explicitly merged with the `dataSync` type so Android 14+ does not throw `MissingForegroundServiceTypeException`:
  ```xml
  <service
      android:name="androidx.work.impl.foreground.SystemForegroundService"
      android:foregroundServiceType="dataSync"
      tools:node="merge" />

```

* **Worker `ForegroundInfo`:** In `AiModelDownloadWorker.kt`, verify that `setForeground(...)` passes `ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC` into the 3-argument `ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)` constructor on API 29+.

### 4. Finalize Signature Permission Namespace & `<uses-permission>` Pairing

* **Namespace Update:** Replace the placeholder `com.yoursuite.permission.READ_AI_MODEL` across `AndroidManifest.xml` and `AiModelProvider.kt` with our actual shared package prefix.
* **Declare AND Request:** Verify that `features/ai/local/src/main/AndroidManifest.xml` includes all three of the following:
1. `<permission android:name="...READ_AI_MODEL" android:protectionLevel="signature" />` (defines the signature permission).
2. `<uses-permission android:name="...READ_AI_MODEL" />` (grants each sibling app permission to read from the provider).
3. `android:exported="true"` and `android:readPermission="...READ_AI_MODEL"` on the `<provider>` tag for `AiModelProvider`.



```

Here is why each of these four checks is important before testing on a physical device:

* **Provider Authority Uniqueness:** Android requires every `ContentProvider` authority on a device to be globally unique; using `${applicationId}` ensures **KoColor, Seaweed, PhotoDo, AshBike, and GoSwift**[cite: 1] can all be installed side-by-side.
* **Cross-App `filesDir` Isolation:** Because `context.filesDir` is private to each APK[cite: 1], sibling apps must query the signature-protected `ContentProvider` (`ParcelFileDescriptor.MODE_READ_ONLY`)[cite: 1] before falling back to `LocalModelState.NotDownloaded`[cite: 1].
* **Android 14+ `dataSync` Enforcement:** Promoting a `CoroutineWorker` with `ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC`[cite: 1] requires both the manifest `<service>` merge tag and the 3-argument `ForegroundInfo` constructor on modern Android versions.
* **Signature Permission Handshake:** Defining `<permission ... android:protectionLevel="signature" />`[cite: 1] creates the lock, while `<uses-permission>` gives your other five apps the key when they are signed with the same keystore[cite: 1].

```