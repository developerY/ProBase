# Visual Identity Refinement Implementation Plan

This plan outlines the architectural refinement of the Visual Identity UI, explicitly decoupling raw biometric measurements from deterministic classification to enhance transparency and explainability.

## Objective
Display the user's Color Profile (e.g., "NEUTRAL SPRING") alongside a "Dimensional Explanation" (e.g., "Warm • Light • Balanced") and an accurate plot on the Seasonal Map.

## Proposed Changes

### 1. Logic Refinement: `ProfileTranslation.kt`
Add a helper function to aggregate individual biometric measurements into a single descriptive string.

- **New Function**: `getDimensionalExplanation(telemetry: FaceTelemetryData): String`
    - **Temperature**: Extract via `getTemperatureProfile(undertoneScore)`.
    - **Depth**: Extract via `getDepthProfile(hairLuminance, eyeLuminance)`.
    - **Contrast**: Extract via `getContrastProfile(contrastDelta)`.
    - **Format**: Concatenate using the " • " separator.

### 2. UI Refinement: `VisualIdentityHeader.kt` (formerly `UserPortraitSlot.kt`)
Refactor and rename the component to align with the new architectural vision.

- **Rename**: `UserPortraitSlot` -> `VisualIdentityHeader`.
- **Typography**:
    - Use a premium Serif font for the primary "Visual Identity" title.
    - Display the 12-Season Classification prominently.
    - Add the **Dimensional Explanation** directly beneath the classification using muted, smaller typography.
- **Layout**:
    - User portrait (circular) with established halo/glow effects.
    - Action icons (Camera/Gallery) aligned to the right.

### 3. Orchestration: `MessagingStep.kt`
Update the main screen component to integrate the refined header.

- Pass `uiState.faceTelemetry` and `uiState.fashionProfileLabel` to the header.
- Ensure proper spacing between the header and the `SeasonalQuadrantMap`.

## Architectural Benefits
- **Transparency**: Users understand *why* they received a specific classification.
- **Consistency**: The UI respects the engine's authority while showcasing the supporting raw data.
- **Explainability**: Sets the stage for future styling recommendations (e.g., explaining color pairings).

## Verification Plan
- **Preview**: Update `VisualIdentityHeaderPreview` with sample data to verify layout and typographic hierarchy.
- **Build**: Run local Gradle build for the `:applications:kocolor:features:analyzer` module.
- **Integration**: Verify in the Style Simulator screen that the dimensional label updates correctly based on the analysis result.
