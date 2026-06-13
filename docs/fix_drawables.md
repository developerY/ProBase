# Walkthrough - Restoring Missing Drawables & Fixing DI Bindings

I have fixed the "missing drawables" issue in the KoColor app and resolved the duplicate Hilt bindings that were breaking the main application build.

## 1. Restored Missing Drawables (KoColor)

Several localized backgrounds in the KoColor Home feature were referenced in code but missing from the project. I have recreated these as XML placeholders to restore build stability.

### Files Added:
- `home_sunny_bg.xml`: Placeholder for sunny weather background.
- `home_cloudy_bg.xml`: Placeholder for cloudy weather background.
- `home_rainy_bg.xml`: Placeholder for rainy weather background.
- `home_storm_bg.xml`: Placeholder for stormy weather background.
- `boutique_bg.xml`: Placeholder for the "KoColor Boutique" card background.

> [!TIP]
> You can now replace these `.xml` placeholders with the high-resolution `.jpg` or `.webp` assets from Unsplash as detailed in the [localization walkthrough](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/nutrition/localization_walkthrough.artifact.md).

## 2. Fixed Duplicate DI Bindings (Photodo / App)

A recent refactor in the Photodo module removed Hilt qualifiers, causing duplicate binding errors when the main app tried to bridge these settings.

### Changes in `DataStoreModule.kt` (Photodo):
- Restored `@Named("PhotoDo")` to `bindAiConfigurationSettings`.
- Restored `@Named("PhotoDo")` to `bindSmartCaptureSettings`.

This ensures that the main app's `GlobalSettingsBridgeModule` can correctly identify and provide the Photodo-specific implementations without conflict.

## Verification Results

### Build Success
- **KoColor Home Feature**: Built successfully (`:applications:kocolor:apps:mobile:features:home:assembleDebug`).
- **Main Application**: Built successfully (`:app:assembleDebug`).

### Visuals
- The Home screen now correctly compiles with the weather-dynamic background logic and the Boutique card.
