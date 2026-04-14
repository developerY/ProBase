# Move AI Connection Configuration to Shared Module

Move all Machine Learning (ML) connection configuration from the PhotoDo Settings feature into its own reusable feature module `:features:ai:configuration`.

## Proposed Changes

### Shared Feature: `:features:ai:configuration` [NEW]

Created this module to hold universal AI configuration UI and logic.

#### [AiConfigurationSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/src/main/java/com/zoewave/probase/features/ai/configuration/domain/AiConfigurationSettings.kt)
- Defined the interface for persistent AI settings.

#### [AiConfigurationViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/src/main/java/com/zoewave/probase/features/ai/configuration/ui/AiConfigurationViewModel.kt)
- Ported testing and model discovery logic from PhotoDo `SettingsViewModel`.

#### [AiConfigurationCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/src/main/java/com/zoewave/probase/features/ai/configuration/ui/AiConfigurationCard.kt)
- Ported the UI from `AiSettingsCard.kt`.

---

### PhotoDo Integration

#### [AppSettingsRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/AppSettingsRepository.kt)
- Make it extend `AiConfigurationSettings`.

#### [AiConfigurationBridgeModule.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/di/AiConfigurationBridgeModule.kt) [NEW]
- Bind `AppSettingsRepository` to `AiConfigurationSettings`.

#### [SettingsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/src/main/java/com/zoewave/probase/photodo/mobile/features/settings/ui/SettingsViewModel.kt)
- Clean up the testing/model logic that moved to the shared module.

#### [SettingsScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/src/main/java/com/zoewave/probase/photodo/mobile/features/settings/ui/components/SettingsScreen.kt)
- Replace `AiSettingsCard` with `AiConfigurationCard`.

#### [DELETE] [AiSettingsCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/settings/src/main/java/com/zoewave/probase/photodo/mobile/features/settings/ui/components/AiSettingsCard.kt)

---

## Verification Plan

### Automated Tests
- Run `:features:ai:configuration:assembleDebug`
- Run `:applications:photodo:apps:mobile:assembleDebug`

### Manual Verification
1. Open PhotoDo Settings.
2. Expand the AI configuration card.
3. Verify "Test Connection" and "Ping Selected Model" still work exactly as before.
4. Verify the UI is consistent with the rest of the app.
