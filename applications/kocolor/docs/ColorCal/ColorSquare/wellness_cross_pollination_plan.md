# Implementation Plan: Wellness & Cosmetic Cross-Pollination

This plan details the technical steps to integrate seasonal wellness insights and K-Beauty concepts into the KoColor Hub, bridging the gap between color theory and holistic skin health.

## User Review Required

> [!IMPORTANT]
> We are introducing a new "Wellness Layer" that connects `:features:colors` with `:features:routines`. This requires a cross-module data flow where color profile data influences recommended beauty rituals.

## Proposed Changes

### 1. Domain & Data Models
Establish the bridge between color and wellness.

#### [MODIFY] [ColorIntelligence.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/colors/src/main/java/com/zoewave/probase/kocolor/features/colors/domain/model/ColorIntelligence.kt)
- Add `WellnessInsight` data class:
  ```kotlin
  data class WellnessInsight(
      val seasonalTheme: String, // e.g. "Glass Skin & Winter Hydration"
      val biologicalFocus: String, // e.g. "Barrier Protection"
      val kBeautyConcept: String, // e.g. "The 7-Skin Method"
      val description: String,
      val linkedRoutineId: Long? = null
  )
  ```
- Expand `ColorHubUiState` to include `wellnessInsight: WellnessInsight?`.

### 2. Logic & Analytics
Evolve the analysis engine to handle biological wellness.

#### [NEW] [WellnessAdvisor.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/colors/src/main/java/com/zoewave/probase/kocolor/features/colors/domain/engine/WellnessAdvisor.kt)
- Implement `generateSeasonalWellness(season, undertone)`:
  - **Winter/Cool**: Focus on "Moisture Sandwiching" to enhance radiance against deep jewel tones.
  - **Summer/Cool**: Focus on "Calming & Cooling" to maintain clarity for pastel palettes.
  - **Autumn/Warm**: Focus on "Nourishing Oils" to complement earthy, muted tones.
  - **Spring/Warm**: Focus on "Vitamin C & Brightening" to match high-vibrancy warm colors.

#### [MODIFY] [ColorIntelligenceRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/colors/src/main/java/com/zoewave/probase/kocolor/features/colors/domain/repository/ColorIntelligenceRepository.kt)
- Add `getWellnessInsight(userSeason: SeasonalType): Flow<WellnessInsight>`.

### 3. UI Implementation
Transform the visual anchor into a functional gateway.

#### [MODIFY] [ColorHubScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/features/color/src/main/java/com/zoewave/probase/kocolor/mobile/features/color/ui/hub/ColorHubScreen.kt)
- Replace the static **"Seasonal Inspiration"** card with a dynamic **"Wellness & Glow"** Hero Card.
- Display the `seasonalTheme` and `biologicalFocus` prominently.
- **Interactivity**: Tapping the card navigates to the user's specific routine (e.g., Morning Protection) with a "Seasonal Tip" overlay.

#### [NEW] [SeasonalWellnessOverlay.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/routines/src/main/java/com/zoewave/probase/kocolor/features/routines/ui/components/SeasonalWellnessOverlay.kt)
- A specialized UI component in the Routines module that displays the context-aware advice when navigated from the Color Hub.

## Verification Plan

### Automated Tests
- **Unit Test**: Verify `WellnessAdvisor` returns the correct K-Beauty concept for all 4 seasons.
- **Navigation Test**: Ensure the deep-link from Color Hub to a specific routine ID works correctly.

### Manual Verification
1. **Context Shift**: Change profile from Winter to Spring; verify the bottom card shifts from "Hydration" to "Brightening."
2. **End-to-End**: Tap the "Wellness" card and confirm it opens the Morning Routine with the correct seasonal guidance displayed.
