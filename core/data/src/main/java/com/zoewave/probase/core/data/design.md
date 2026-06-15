# Walkthrough - Refactored AiConfigurationSettings to Core

I have refactored the `AiConfigurationSettings` architecture to move the common contract to `core:data` and removed all app-specific `@Named` qualifiers and bridge modules. Each application now provides its own unnamed implementation of the interface, which is natively injected into shared feature viewmodels.

## Changes Made

### Core Contract
- Moved `AiConfigurationSettings` interface to [AiConfigurationSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/AiConfigurationSettings.kt).
- Removed it from the [features:ai:configuration](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/src/main/java/com/zoewave/probase/features/ai/configuration/domain/AiConfigurationSettings.kt) module.

### Shared Features
- Updated [AiConfigurationViewModel](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/src/main/java/com/zoewave/probase/features/ai/configuration/ui/AiConfigurationViewModel.kt) to use the un-annotated `AiConfigurationSettings` from `core:data`.
- Updated [AiConfigurationCard](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/src/main/java/com/zoewave/probase/features/ai/configuration/ui/AiConfigurationCard.kt) to fix imports.
- Modified [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/configuration/build.gradle.kts) to expose `:core:data` via `api` for transitive dependencies.

### Application Modules (Simplified DI)
- **Seaweed**: Removed `AiConfigurationBridgeModule` and `SmartCaptureBridgeModule`. The [DataModule](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/DataModule.kt) now provides the unnamed bindings directly.
- **PhotoDo**: Removed `@Named` qualifiers from [DataStoreModule](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/di/DataStoreModule.kt).
- **KoColor**: Removed `@Named("KoColor")` from [DatabaseModule](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/di/DatabaseModule.kt) and deleted the `SettingsBridgeModule`.
- **AshBike**: Removed `@Named("AshBike")` from [DataStoreModule](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/database/src/main/java/com/zoewave/probase/ashbike/database/di/DataStoreModule.kt) and deleted the `SettingsBridgeModule`.

### Project-wide Cleanup
- Updated all imports of `AiConfigurationSettings` across all apps (Seaweed, KoColor, AshBike, PhotoDo).
- Removed `@Named` injection points from various ViewModels (e.g., `AnalyzerViewModel`, `StyleSimulatorViewModel`, `CosmeticsViewModel`, `SuggestionsViewModel`).

## Verification Results

### Automated Tests
- Successfully executed build for the following targets:
    - `:features:ai:configuration`
    - `:applications:seaweed:apps:mobile`
    - `:applications:photodo:apps:mobile`
    - `:applications:kocolor:apps:mobile`
- Verified that Dagger/Hilt successfully generated the graph without cycles or duplicate bindings.

> [!NOTE]
> Each app now operates in its own isolated Dagger graph at runtime, fulfilling the `AiConfigurationSettings` dependency with its local implementation without the need for qualifiers or bridges.
