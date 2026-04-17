# Integrate Compliance Handshake into PhotoDo

This plan outlines the integration of the `:features:compliance` module into the PhotoDo mobile app. It implements a "Verifiable Handshake" before Cloud AI operations and adds a compliance status indicator in the Settings screen.

## User Review Required

> [!IMPORTANT]
> Cloud AI operations will now perform a real-time age verification check. If the check fails (e.g., user is under 13 or SDK is outdated), Cloud AI will be blocked or degraded to Local AI.

## Proposed Changes

### [features:ai:capture]

#### [SmartCaptureOrchestrator.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/data/SmartCaptureOrchestrator.kt)

- Inject `AgeSignalsManager`.
- In `processImage`, perform a `getAgeSignal()` handshake before calling `cloudEngine`.
- **Logic**:
    - If `getAgeSignal()` fails with `SdkVersionOutdated`, block Cloud AI and log the error.
    - If `AgeRange` is `AGE_0_12`, block Cloud AI (13+ Policy).
    - If successful, proceed with Cloud AI.

---

### [applications:photodo:apps:mobile:features:settings]

#### [SettingsUiState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/ui/SettingsUiState.kt)

- Add `ageVerificationStatus: String` and `isAgeVerified: Boolean`.

#### [SettingsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/mobile/features/settings/ui/SettingsViewModel.kt)

- Inject `AgeSignalsManager`.
- Fetch `AgeSignal` in `init` (or via a specific event) and update `uiState`.
- **Zero-Footprint**: The signal is kept in memory only for the duration of the ViewModel's lifecycle.

#### [AboutSettingsCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/ui/components/AboutSettingsCard.kt)

- Update the UI to display the "Regulatory Compliance" status under the "About" or a new "Legal & Compliance" section.
- Show `ageVerificationStatus` and a "Verified" badge if applicable.

---

## Verification Plan

### Automated Tests
- `SmartCaptureOrchestratorTest`: Mock `AgeSignalsManager` to return different `Result` types (Success with various age ranges, Failure with `SdkVersionOutdated`) and verify that `cloudEngine` is called or skipped correctly.
- Command: `./gradlew :features:ai:capture:testDebugUnitTest`

### Manual Verification
- Open Settings in the PhotoDo app and verify that the "Regulatory Compliance" status is displayed.
- Trigger a Smart Capture and verify in the logs that the "Compliance Handshake" is performed before Cloud AI initialization.
