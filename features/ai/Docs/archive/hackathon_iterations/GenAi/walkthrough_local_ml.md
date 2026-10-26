# Gemini Nano Sandbox Walkthrough

We have successfully isolated the local AI pipeline into the `/features/ai/local` sandbox. This configuration bypasses the ML Kit "GenAI Prompt" wrapper (which was hitting Beta-wall errors on the Pixel 9a) in favor of the raw **AI Edge SDK**.

## Changes Made

### 1. Build & Dependency Layer
- **[libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)**: Added `googleAiEdgeAicore` (0.0.1-exp02) and fixed the ML Kit Text Recognition version to 19.0.0.
- **[build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/local/build.gradle.kts)**: Configured the local feature module with raw AI Edge, ML Kit, and Compose dependencies.

### 2. Core Engines (The Pipeline)
- **[SandboxOcrEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/SandboxOcrEngine.kt)**: Uses GMS Play Services ML Kit to "scoop" raw text from a Bitmap.
- **[SandboxNanoEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/SandboxNanoEngine.kt)**: The "Brain." It interfaces directly with `com.google.ai.edge.aicore.GenerativeModel`.
    - **Hardware Bypass:** Uses `model.prepareInferenceEngine()` as a readiness check to see if the Pixel 9a's NPU is accessible.
    - **Low-Temp Inference:** Set to 0.1f to ensure stable data extraction from OCR noise.

### 3. Orchestration & UI
- **[AiSandboxViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/AiSandboxViewModel.kt)**: Manages the state machine (Ready -> OCR -> Nano -> Result).
- **[AiSandboxScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/AiSandboxScreen.kt)**: A terminal-style UI for triggering the pipeline and viewing raw debug output.

## Verification Plan

### Manual Verification on Pixel 9a
1. **Launch the Sandbox:** Hook `AiSandboxScreen` into your main navigation graph (e.g., via a "Secret Debug" button).
2. **Trigger Pipeline:** Use a static `Bitmap` (or hook up a quick image picker).
3. **Observe Console:**
    - If you see `NANO_UNSUPPORTED`, the Pixel 9a system service is still blocking direct access.
    - If you see `3. Final Result:`, the bypass was successful!

### Build Verification
- Run `./gradlew :features:ai:local:assembleDebug` to ensure all symbol resolutions (especially the experimental aicore ones) are correct.

> [!IMPORTANT]
> Ensure "AICore" is updated via the Play Store on the target device, or the `prepareInferenceEngine()` call will fail with a "Service not found" error.

---

Accomplishments:
1.
Dependency Alignment: Updated   libs.versions.toml with the raw aicore and bundled ML Kit OCR dependencies.
2.
Raw AI Edge Engine: Implemented   SandboxNanoEngine.kt using the direct com.google.ai.edge.aicore API.
3.
OCR "Scoop" Engine: Created   SandboxOcrEngine.kt for local text extraction.
4.
Sandbox UI & Logic: Built   AiSandboxViewModel.kt and   AiSandboxScreen.kt for an isolated testing environment.
5.
Manifest Readiness: Verified the   AndroidManifest.xml query blocks for AICore.
Next Steps:
•
Final Sync: Run a Gradle Sync to clear any remaining IDE analysis ghosts.
•
Integration: Hook AiSandboxScreen into your app's navigation to begin testing.
•
Model Check: Ensure the Pixel 9a has "AICore" and "Gemini Nano" enabled in system settings.
You can now feed a static Bitmap to the AiSandboxScreen to verify if the 9a's silicon allows direct generative execution!