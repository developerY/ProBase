# Walkthrough - Switching Mic to AI Glasses

I have updated the translation feature to capture audio directly from the AI Glasses' microphone instead of the phone.

## Key Changes

### 1. Glasses Hardware Targeting
- **[MODIFY] [TranslationViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/glass/translation/ui/TranslationViewModel.kt)**: Updated the Speech Engine to use the **Projected Device Context** (`createProjectedDeviceContext`).
    - **Why**: This explicitly directs the Android Speech Recognizer to use the hardware (microphone) of the connected AI Glasses rather than the host phone.
- **Auto-Fallback**: If the glasses context cannot be created (e.g., glasses disconnected), the engine automatically falls back to the phone microphone to ensure the session doesn't crash.

### 2. Microphone Source Diagnostic
- **[MODIFY] [UnifiedTranslationScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/glass/translation/ui/UnifiedTranslationScreen.kt)**: Added a new **"Microphone Source"** diagnostic row.
    - **Result**: You can now see in real-time whether the audio is coming from the **"Glasses"** or the **"Phone"**.

### 3. UI Status Enhancements
- Updated the `DiagnosticRow` to support custom status text (like "Glasses" or "Phone") instead of just "OK/FAIL".

## Verification Path

1. **Launch**: Open **Translation Hub**.
2. **Check Source**: Look at the "Microphone Source" row in diagnostics.
    - If your emulator/glasses are active, it should say **"Glasses"** with a green icon.
3. **Start Translating**: Tap the button and speak.
4. **Hardware Test**: Speak into your glasses (or the host microphone mapped to the emulator). Verify that transcription appears on the screen.

> [!TIP]
> If the source says "Phone", make sure your glasses are successfully connected and showing the "Glasses Connection: OK" status first.
