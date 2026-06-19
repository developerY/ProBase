# How to Run Live Translation on AI Glasses (Display Glasses)

The Live Translation feature is designed to run as a **Projected Experience**. This means the heavy lifting (translation logic, connectivity) happens on your Android phone, while the UI is projected onto the AI Glasses.

## 1. Setup Your Environment

### Hardware
- **Android Phone**: Must be running Android 14 (API 34) or higher.
- **AI Glasses**: Connected to the phone via USB-C or Bluetooth (if supported).

### Emulator (Recommended for Development)
If you don't have physical glasses, use the **Android XR Emulator**:
1. Open **Device Manager** in Android Studio.
2. Create a new device using the **Android XR (Display Glasses)** system image.
3. Start the emulator. It will automatically appear as a "Projected Device" to your phone emulator or physical device.

---

## 2. Configure Gemini API Key

The translation feature requires a Gemini API key.
1. Launch the **ProBase** app on your phone.
2. Navigate to the **Settings** tab (the third tab).
3. Expand the **AI Configuration** card.
4. Enter your **Gemini API Key**.
5. Tap **Save Key**.
6. (Optional) Tap **Test Connection** to ensure the key is valid and the model is reachable.

---

## 3. Launch the Translation Feature

1. On the phone app, go to the **Examples** tab (the second tab).
2. You will see a list of **Glass XR Demos**.
3. Locate and tap **Live Translation**.
    - If the glasses are connected, the app will automatically request a **Projected Context**.
    - The phone screen will show a "Phone Companion" UI, while the glasses will show the **Translation Screen**.

---

## 4. Using the Feature

1. **Permissions**: The first time you use it, the phone will prompt for **Record Audio** permission. Grant it.
2. **Start Listening**: Tap the **Microphone icon** on the phone UI.
3. **Speak**: Speak into the phone or glasses microphone.
4. **View Translation**:
    - You will see the **transcription** (what you said) appear as small text at the bottom.
    - The **translation** (into Spanish) will appear as large, high-contrast text directly in your field of view on the glasses.

---

## Technical Details

- **Module**: `:features:xr:glass:translation`
- **Activity**: `LiveTranslationActivity` (configured with `xr_projected` display category).
- **UI Toolkit**: Built with **Jetpack Compose Glimmer** for additive display transparency.
- **ASR**: Uses native Android `SpeechRecognizer` for on-device processing.
