# Walkthrough - Isolated Firebase AI Logic for AI Glasses

I have integrated the Firebase AI Logic SDK into the project as an isolated module, specifically for the Samsung/Google AI Glasses (Android XR), while maintaining the existing Google AI SDK for other features.

## Changes

### 1. Isolated AI Module
- Created a new module [features:ai:firebase](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/firebase/build.gradle.kts).
- **GeminiFirebaseManager**: Provides a singleton service to create `LiveModel` (for Gemini Live) and `GenerativeModel` instances using the `com.google.firebase:firebase-ai` SDK.
- **FirebaseLiveSessionManager**: A lifecycle-aware component that manages the connection to a Gemini Live session, enabling real-time audio conversations.

### 2. Core Dependencies
- Updated `firebase-bom` to `34.10.0` in [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml) to support the new Firebase AI Logic SDK.

### 3. AI Glasses Integration
- Updated [GlassesMainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/src/main/java/com/zoewave/ashbike/mobile/glass/GlassesMainActivity.kt) to include `FirebaseLiveSessionManager`.
- Updated [VoiceGearController.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/glass/src/main/java/com/zoewave/ashbike/mobile/glass/audio/VoiceGearController.kt) to recognize "Hey Ash" or "Talk to Ash" voice commands.
- When the "Hey Ash" command is detected, the `FirebaseLiveSessionManager` starts a real-time conversational session with Gemini.

## Verification Results

### Automated Tests
- Verified that the project structure is correct and that the new module is recognized via Gradle sync.
- Confirmed that `:features:ai:capture` and `:features:ai:vision` still depend on the original Google AI SDK, ensuring no breaking changes to existing AI features.

### Manual Verification
- Code review confirms that the `GlassesMainActivity` correctly manages the lifecycle of both the `VoiceGearController` and the `FirebaseLiveSessionManager`.
- The permission flow previously implemented for `RECORD_AUDIO` is leveraged for the new Gemini Live capabilities.
