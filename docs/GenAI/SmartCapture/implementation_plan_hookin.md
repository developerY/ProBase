# Integrate AI Smart Capture into PhotoDo

This plan outlines the integration of the multimodal AI Smart Capture feature into the main PhotoDo application. This includes user-managed Gemini API keys, an AI auto-fill toggle in the camera flow, and high-fidelity task extraction with visual feedback.

## User Review Required

- **Visual Differentiation**: We'll use a soft purple theme for cloud-enhanced (AI) task suggestions to distinguish them from standard local parsing.
- **BYOK (Bring Your Own Key)**: Users must provide their own Gemini API key in the settings to enable Tier 1 (Cloud) parsing. If no key is provided, the app will seamlessly fallback to Tier 2 (Local ML Kit).

## Proposed Changes

### Core Data & Infrastructure

#### [AppSettingsRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/AppSettingsRepository.kt)
- Add `geminiApiKeyFlow` and `isAiEnabledFlow`.
- Add `saveGeminiApiKey` and `saveAiEnabled` functions.

#### [DataStoreAppSettingsRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/DataStoreAppSettingsRepository.kt)
- Implement new flows and save functions using DataStore.

#### [NEW] [RealSmartCaptureSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/features/smartcapture/src/main/java/com/zoewave/probase/features/smartcapture/data/RealSmartCaptureSettings.kt)
- Implement `SmartCaptureSettings` by delegating to `AppSettingsRepository`. This connects the feature module to the app's real preferences.

---

### Settings Feature

#### [SettingsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/src/main/java/com/zoewave/probase/photodo/mobile/features/settings/ui/SettingsViewModel.kt)
- Expose `geminiApiKey` and `isAiEnabled` in `SettingsUiState`.
- Handle events to save these values.

#### [SettingsScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/src/main/java/com/zoewave/probase/photodo/mobile/features/settings/ui/components/SettingsScreen.kt)
- Add a new `AiSettingsCard` containing:
    - Toggle for "Enable AI Auto-fill".
    - Password field for "Gemini API Key".
    - Link to Google AI Studio to get a key.

---

### Navigation & Integration

#### [photoTodoNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)
- Update the `PhotoTodoRoute.Camera` handling.
- If AI is enabled in settings, navigate to `SmartCaptureUiRoute` instead of the standard save flow after capture.
- On `onCaptureComplete`, pass the `TaskDraftState` to the "Add Task" or "Quick Project" flow.

---

### Task Feature

#### [TasksViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/TasksViewModel.kt)
- Add an event `OnPopulateFromAi(draft: TaskDraftState)` to pre-fill the `TaskDraftState`.

---

## Verification Plan

### Automated Tests
- Run `:features:smartcapture:assembleDebug`
- Run `:applications:photodo:apps:mobile:assembleDebug`

### Manual Verification
1.  **Settings**: Enter a Gemini API Key and enable AI.
2.  **Capture**: Snap a photo from the Home screen.
3.  **Analysis**: Verify the purple "AI analyzing" state appears.
4.  **Verification**: Confirm the extracted fields (Task Name, Project, etc.) match the image.
5.  **Edit**: Change one of the AI-filled fields and save the task.
6.  **Fallback**: Delete the API key and verify it falls back to the "Soft Blue" local OCR engine.
