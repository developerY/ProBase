# Implementation Plan - Shared State & Glasses Microphone Fix

This plan fixes the two main issues:
1. **State Desync**: The phone and glasses currently have separate instances of the `TranslationViewModel`, so the glasses don't show what the phone is processing.
2. **Permission Block (Error 9)**: The speech engine on the glasses is blocking because permission was granted for the phone, not the glasses. We will use the XR-specific `ProjectedPermissionsResultContract`.

## User Review Required

> [!IMPORTANT]
> **New Permission Flow**: When you tap "REQUEST PERMISSION", you will see a rationale on your phone explaining that the glasses need microphone access. You must accept this to satisfy the glasses' hardware security.

## Proposed Changes

### [features/xr/glass:translation]

#### [NEW] [TranslationRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/glass/translation/data/TranslationRepository.kt)
- Create a `@Singleton` repository to hold the live transcription and translation text.
- This ensures that both the phone app and the projected glasses app see the same data in real-time.

#### [MODIFY] [TranslationViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/glass/translation/ui/TranslationViewModel.kt)
- Injects `TranslationRepository`.
- Updates the repository whenever a speech result or translation comes in.
- UI state now observes the repository.

#### [MODIFY] [UnifiedTranslationScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/glass/translation/ui/UnifiedTranslationScreen.kt)
- Use `ProjectedPermissionsResultContract` to request microphone access specifically for the AI Glasses.
- This will trigger the coordinated flow where the glasses ask the phone to show the permission dialog.

## Verification Plan

### Automated Tests
- Build all modules to ensure `ProjectedPermissionsResultContract` is correctly implemented.

### Manual Verification
1. Open the **Translation Hub** on the phone.
2. Verify that **"Microphone Permission"** shows **FAIL** for the glasses initially.
3. Tap **REQUEST PERMISSION**.
4. Grant the permission on the phone (the rationale should mention the glasses).
5. Start translating.
6. Verify that the **Transcription** appears on **BOTH** the phone and the glasses simultaneously.
7. Verify that Error 9 no longer appears in Logcat.
