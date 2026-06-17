# Implementation Plan - XR Glasses Translation & 3-Tab App Navigation

This plan outlines the steps to implement a live translation feature in the XR glasses module and update the main application to a 3-tab navigation structure with a Gemini "Bring Your Own Key" (BYOK) settings page.

## User Review Required

> [!IMPORTANT]
> The translation feature will require **Record Audio** permissions. The user will be prompted for this permission when they first open the translation screen.

> [!NOTE]
> We will be using the Google Generative AI (Gemini) SDK for translation. This requires a valid API key, which will be managed in the new Settings tab.

## Proposed Changes

### [features/xr/xrglasses]

We will implement the core translation logic and UI in this module. This module is **completely isolated** from the main app logic and only depends on shared `:core` modules.

#### [MODIFY] [FullXRApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/FullXRApp.kt)
- Add `Translation` to `XRSample`.
- Update `FullXRApp` to handle the `Translation` sample.

#### [NEW] [TranslationScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/TranslationScreen.kt)
- Create a Glimmer-optimized screen for displaying translated text (inspired by `LiveTranslationSamples.kt`).

#### [NEW] [TranslationViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/xrglasses/src/main/java/com/zoewave/probase/features/xr/xrglasses/ui/samples/TranslationViewModel.kt)
- Implement microphone recording logic.
- **Isolation**: Injects `AiConfigurationSettings` from `:core:data` to retrieve the Gemini API key, ensuring it doesn't depend on the `:app` or `:features:ai:configuration` UI modules.
- Integrate with Gemini SDK for translating captured audio (speech-to-text + translation).

---

### [app]

We will update the main app's navigation structure.

#### [MODIFY] [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/java/com/zoewave/probase/MainActivity.kt)
- Replace `FeatureInventoryNavHost` with a new `MainScaffold` that includes a `NavigationBar`.

#### [NEW] [MainScaffold.kt](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/java/com/zoewave/probase/ui/components/MainScaffold.kt)
- Implement a screen with `Scaffold` and `NavigationBar`.
- Define three tabs: `Main` (Inventory), `Examples` (XR Glasses), and `Settings` (AI Config).

#### [MODIFY] [FeatureInventoryEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/java/com/zoewave/probase/ui/components/FeatureInventoryEntryProvider.kt)
- Ensure all screens are reachable through the new navigation structure.

---

### [features/ai/configuration]

#### [MODIFY] [AiConfigurationViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/src/main/java/com/zoewave/probase/features/ai/configuration/ui/AiConfigurationViewModel.kt)
- Ensure the Gemini API key is persisted (e.g., using DataStore or SharedPreferences).

## Verification Plan

### Automated Tests
- Unit tests for `TranslationViewModel` to verify state transitions.
- Build the project to ensure all modules are correctly linked.

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate through the 3 tabs.
- Set a Gemini API key in the Settings tab.
- Open the XR Glasses translation sample.
- Grant microphone permissions.
- Speak into the mic and verify that translated text appears on the screen.
