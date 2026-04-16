# Integrate Play Age Signals for Compliance (Refined)

Add a new feature module `:features:compliance` to integrate the Play Age Signals API. This module performs a secure handshake with the Play Store to retrieve authoritative age signals, fulfilling the requirements of the 2026 App Store Accountability Acts.

## User Review Required

> [!NOTE]
> The `getAgeSignal()` API will now return a `kotlin.Result<AgeSignal>` to ensure apps can gracefully handle specific errors like `SDK_VERSION_OUTDATED` or `NETWORK_ERROR`.

## Proposed Changes

### [features:compliance]

New library module to wrap Play Age Signals API.

#### [AgeSignal.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/model/AgeSignal.kt)

- Refined data class to include the three mandatory data points:
    - `ageRange`: Banded age bucket (e.g., 0-12, 13-15, 16-17, 18+).
    - `verificationStatus`: Trust level (Verified, Declared, Supervised, etc.).
    - `mostRecentApprovalDate`: Timestamp of last parental approval.

#### [ComplianceError.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/model/ComplianceError.kt) [NEW]

- Custom exception types for `SdkVersionOutdated`, `NetworkError`, and `GenericError` to facilitate graceful error handling.

#### [AgeSignalsManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/AgeSignalsManager.kt)

- Interface for retrieving age signals using `Result`.
```kotlin
interface AgeSignalsManager {
    /**
     * Retrieves the current user's age signal from the Play Store.
     * @return Result containing [AgeSignal] or a [ComplianceError].
     */
    suspend fun getAgeSignal(): Result<AgeSignal>
}
```

#### [AgeSignalsManagerImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/compliance/src/main/java/com/zoewave/probase/features/compliance/AgeSignalsManagerImpl.kt)

- Implements the secure handshake using `checkAgeSignals`.
- Maps Play SDK error codes (e.g., `SDK_VERSION_OUTDATED`) to internal `ComplianceError` types.
- Maps `AgeSignalsResult` to the decoupled `AgeSignal` model.

---

### [App Integration]

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/build.gradle.kts)

- Ensure `:features:compliance` is included.

## Verification Plan

### Automated Tests
- `AgeSignalsManagerTest`:
    - Verify successful mapping of valid Play Store responses.
    - Verify that `SDK_VERSION_OUTDATED` from the SDK results in a `Result.failure` with `ComplianceError.SdkVersionOutdated`.
    - Verify network failure handling.
- Command: `./gradlew :features:compliance:testDebugUnitTest`

### Manual Verification
- Build and assemble the module to ensure all dependencies and mappings are correct.
- Command: `./gradlew :features:compliance:assembleDebug`
