# Isolated Firebase AI Logic for AI Glasses

Integrate Gemini into AshBike AI Glasses using the Firebase AI Logic SDK while keeping existing features on the Google AI SDK. A new isolated module `features:ai:firebase` will be created to host the Firebase-specific implementation.

## User Review Required

> [!IMPORTANT]
> - **Isolated SDK**: This implementation will **NOT** modify `features:ai:capture` or `features:ai:vision`. It will use the Firebase SDK in parallel for the glasses.
> - **Firebase BoM Update**: Update `firebase-bom` to `34.10.0` to support `firebase-ai`.
> - **Gemini Live API**: The glasses will use the real-time audio capabilities of the Firebase AI Logic SDK.

## Proposed Changes

### [Core]

#### [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)
- Update `firebaseBom` to `34.10.0`.
- Add `firebase-ai` library entry if not already present (using BoM).

### [AI Feature Module] (features/ai)

#### [NEW] [features:ai:firebase](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/firebase)
- Create a new module dedicated to Firebase AI Logic.
- **`GeminiFirebaseManager.kt`**: Provides access to `GenerativeModel` and `LiveModel` using the `com.google.firebase:firebase-ai` SDK.
- **`FirebaseLiveSession.kt`**: A lifecycle-aware wrapper for Gemini Live audio streaming.

### [AshBike Glass Feature] (applications/ashbike/apps/mobile/features/glass)

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/build.gradle.kts)
- Add dependency on `:features:ai:firebase`.

#### [GlassesMainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/src/main/java/com/zoewave/ashbike/mobile/glass/GlassesMainActivity.kt)
- Integrate the new Firebase AI component.
- Implement a voice-triggered AI assistant (e.g., listening for "Hey Ash") using Gemini Live.

## Verification Plan

### Automated Tests
- Build and run `:features:ai:firebase` unit tests.
- Ensure no regressions in `:features:ai:capture` or `:features:ai:vision` (Google AI SDK).

### Manual Verification
- Verify that `GlassesMainActivity` correctly initializes the Firebase AI Logic session.
- Ensure the app correctly handles two different AI SDKs in the same project without conflict.
