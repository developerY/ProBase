# Refactor: Remove Modifier from UiState

This plan aims to align the KoColor codebase with Jetpack Compose best practices by removing `Modifier` properties from `UiState` data classes and instead passing them as separate parameters to Composable functions.

## User Review Required

> [!IMPORTANT]
> This change updates many Composable signatures from 3-argument to 4-argument:
> `(uiState: T, modifier: Modifier = Modifier, onEvent: (E) -> Unit, navTo: (KoColorRoute) -> Unit)`
> or for internal components:
> `(uiState: T, modifier: Modifier = Modifier, ...)`

## Proposed Changes

### Core UI Components
- [MODIFY] [WellnessTrackerHeroCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/core/src/main/java/com/zoewave/probase/kocolor/mobile/core/ui/components/WellnessTrackerHeroCard.kt)
- [MODIFY] [KoColorMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorMainScreen.kt)
- [MODIFY] [HealthUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/core/src/main/java/com/zoewave/probase/kocolor/mobile/core/ui/health/HealthUiRoute.kt)
- [MODIFY] [StyleHealthDashboard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/core/src/main/java/com/zoewave/probase/kocolor/mobile/core/ui/health/StyleHealthDashboard.kt)

### Feature: Home
- [MODIFY] [HomeScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/features/home/src/main/java/com/zoewave/probase/kocolor/mobile/features/home/ui/HomeScreen.kt)
- [MODIFY] [CollectionHubScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/features/home/src/main/java/com/zoewave/probase/kocolor/mobile/features/home/ui/CollectionHubScreen.kt)

### Feature: Analyzer
- [MODIFY] [LocationInput.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/components/LocationInput.kt)
- [MODIFY] [OccasionFilter.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/components/OccasionFilter.kt)
- [MODIFY] [StyleCaptureSlot.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/components/StyleCaptureSlot.kt)

### Feature: Cosmetics
- [MODIFY] [GroupSectionCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/components/GroupSectionCard.kt)
- [MODIFY] [ProfessionalTaxonomyDialog.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/components/ProfessionalTaxonomyDialog.kt)
- [MODIFY] [SubCategoryCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/components/SubCategoryCard.kt)
- [MODIFY] [SummaryStatCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/components/SummaryStatCard.kt)
- [MODIFY] [VanityCategoryCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/components/VanityCategoryCard.kt)

### Feature: Inventory
- [MODIFY] [WardrobeComponents.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/ui/components/WardrobeComponents.kt)

### Feature: Routines
- [MODIFY] [EditStepForm.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/routines/src/main/java/com/zoewave/probase/kocolor/features/routines/ui/components/EditStepForm.kt)
- [MODIFY] [SplitRitualStep.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/routines/src/main/java/com/zoewave/probase/kocolor/features/routines/ui/components/SplitRitualStep.kt)

### Feature: Stitch
- [MODIFY] [StitchComponents.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/stitch/src/main/java/com/zoewave/probase/kocolor/features/stitch/ui/components/StitchComponents.kt)

### Feature: Health Hydration
- [MODIFY] [HydrationWaterDropCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/hydration/src/main/java/com/zoewave/probase/features/health/hydration/ui/components/HydrationWaterDropCard.kt)

## Verification Plan

### Automated Tests
- Run `:applications:kocolor:apps:mobile:assembleDebug` to ensure all signatures and call sites are updated correctly.

### Manual Verification
- Verify that UI layouts (padding, weights) are maintained correctly after the refactor.
