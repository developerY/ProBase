# GoSwift Unified Input & 3-Tab Layout Walkthrough

I have restructured the GoSwift mobile application to use a clean 3-tab bottom navigation layout by creating a new `input` feature module that reuses the existing `shots` and `hydration` modules.

## Key Changes

### Module Reuse & Consolidation
- **New Input Module**: Created `:applications:goswift:apps:mobile:features:input` as a container for all user data entry.
- **Transitive Reuse**: The `input` module depends on and reuses the UI and logic from the existing `:shots` and `:hydration` modules. These original modules remain intact and fully reusable for other parts of the project (e.g., a possible Wear OS app).
- **Unified Log Tab**: Implemented `InputUiRoute`, which provides a `TabRow` (Caffeine and Water) within the second main tab of the app.

### 3-Tab Navigation
- **Simplified Bottom Bar**: Updated the navigation to show three primary destinations:
    1. **Home**: Central health dashboard.
    2. **Log** (Input): Unified entry for caffeine and hydration.
    3. **Settings**: App configurations.
- **Intuitive Icons**: Switched to `Icons.Default.Add` for the Log tab to clearly indicate its role as the primary action for recording data.

### Architectural Integrity
- **Navigation 3 Integration**: Updated `GoSwiftDestination` and `goSwiftNavEntryProvider` to support the new `Log` route while maintaining internal routes like `AddShot`.
- **Dependency Management**: Optimized `build.gradle.kts` and `settings.gradle.kts` to support the new modular structure without breaking existing features.

## Verification Summary

### Build Verification
- Successfully performed a full build of the mobile application.
- Command: `./gradlew :applications:goswift:apps:mobile:assembleDebug`
- Result: **Success**

### Functional Check
- Verified that the new `input` module correctly imports and displays the `ShotsScreen` and `HydrationScreen` from their respective modules.
- Verified that the bottom bar correctly navigates between the three top-level screens.
