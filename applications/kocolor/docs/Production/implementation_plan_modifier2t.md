# KoColor Production Readiness & MAD Standard Refactor

This plan aims to make the KoColor application production-ready by eliminating hard-coded strings, standardizing composable signatures, and adhering to Modern Android Development (MAD) gold standards.

## User Review Required

> [!IMPORTANT]
> To comply with the "exactly 3 arguments" rule for composables, I will be wrapping all stateful parameters (including `Modifier` and specialized flows) into a consolidated `UiState` object for each composable. This will significantly change internal component signatures.

## Proposed Changes

### Core Architecture & MAD Standards
- **`collectAsStateWithLifecycle()`**: Ensure all screen-level state collection uses this instead of `collectAsState()` for better lifecycle awareness.
- **Unidirectional Data Flow (UDF)**: Audit all ViewModels and Screens to ensure events flow up and state flows down.

### Resource Management
- **Audit & Extraction**: Scan all `.kt` files in `applications/kocolor` for hard-coded UI strings.
- **Localization Support**: Move all extracted strings to the appropriate `strings.xml` files within each module.

### Composable Signature Refactoring
Every composable in `applications/kocolor` will be refactored to follow the signature:
`fun ComposableName(uiState: T, onEvent: (E) -> Unit, navTo: (KoColorRoute) -> Unit)`

#### [MODIFY] [HomeScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/features/home/src/main/java/com/zoewave/probase/kocolor/mobile/features/home/ui/HomeScreen.kt)
- Remove `Modifier` from `HomeScreen` signature (it's a top-level screen).
- Refactor internal components like `HomeHeader`, `WellnessInsightsSection`, and `QuickActionCard` to follow the 3-arg rule.

#### [MODIFY] [HealthUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/core/src/main/java/com/zoewave/probase/kocolor/mobile/core/ui/health/HealthUiRoute.kt)
- Refactor `HealthUiRoute` from 5 arguments to 3.
- Wrap `Modifier` and `Flow<HealthSideEffect>` into `HealthUiState`.

#### [MODIFY] [AnalyzerScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/AnalyzerScreen.kt)
- Ensure all internal capture slots and filters follow the 3-arg pattern.

#### [MODIFY] [StitchScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/stitch/src/main/java/com/zoewave/probase/kocolor/features/stitch/ui/StitchScreen.kt)
- Refactor to meet the signature standard.

### Project-wide Cleanup
- Remove any unused imports introduced by these changes.
- Ensure all `Preview` functions are updated to match the new signatures.

## Verification Plan

### Automated Tests
- Run `gradle :applications:kocolor:apps:mobile:assembleDebug` to ensure compilation.
- Verify that all Hilt dependencies are still satisfied.

### Manual Verification
- Deployment to an emulator to verify that all strings are correctly loaded from resources.
- Smoke test navigation and main UI flows to ensure state is correctly preserved during the refactor.
