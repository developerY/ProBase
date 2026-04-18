# Gemini Integration for AI Glasses

Integrate Gemini into the AshBike AI Glasses using the recommended Firebase AI Logic SDK. This task also includes migrating existing AI features in `features/ai` to use the Firebase SDK for better consistency and security, especially for XR platforms.

## User Review Required

> [!IMPORTANT]
> - **Firebase SDK Migration**: I will migrate `features:ai:capture` and `features:ai:vision` from the Google AI SDK to the Firebase AI Logic SDK. This is recommended for production use and specifically for Android XR.
> - **Firebase BoM Update**: Update `firebase-bom` to `34.10.0`.
> - **Real-time Audio**: The integration will use the Gemini Live API for real-time voice interactions on the glasses.

## Proposed Changes

### [Core]

#### [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)
- Update `firebaseBom` to `34.10.0`.

### [AI Feature Module] (features/ai)

#### [NEW] [features/ai/live](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/live)
- Create a new module for real-time conversational AI.
- **`GeminiLiveManager.kt`**: Wraps `Firebase.ai.liveModel` to manage real-time audio sessions.

#### [features/ai/capture](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture)
- Update `CloudCaptureEngineImpl.kt` to use `Firebase.ai.generativeModel`.
- Remove `libs.google.generative.ai` dependency in favor of `libs.firebase.ai`.

#### [features/ai/vision](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/vision)
- Update `CloudReceiptEngine.kt` to use the Firebase SDK.

### [AshBike Glass Feature] (applications/ashbike/apps/mobile/features/glass)

#### [GlassesMainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/src/main/java/com/zoewave/ashbike/mobile/glass/GlassesMainActivity.kt)
- Integrate `GeminiLiveManager`.
- Add "Hey Ash" or a trigger for the Gemini conversational mode.

## Verification Plan

### Automated Tests
- Verify that all AI modules build correctly with the new SDK.
- Check that existing tests for task extraction and receipt processing still pass (or adapt them to the new SDK).

### Manual Verification
- Code review of the SDK migration to ensure `GenerativeModel` calls are correctly translated to the Firebase version.
- Verify that AshBike AI Glasses can now handle conversational queries via Gemini Live.
