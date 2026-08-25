# Implementation Plan: Blueprint Refinement & Dynamic Rationale

This plan details the steps to fix the "Details" placeholder bug in the visual blueprints and implement dynamic rationale generation for local (non-AI) styling requests.

## Proposed Changes

### 1. Presentation Layer: Blueprint Callout Binding
The `BlueprintCallout` component currently displays the hardcoded string "Details" when in its collapsed state. We will update it to prioritize the actual product name.

#### [MODIFY] [BlueprintCallout.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/graphics/BlueprintCallout.kt)
- Update the `else` branch of the `isExpanded` check to display the `productName` (capped to 1 line) instead of "Details".
- This ensures that users see their specific garments (e.g., "Silk Blazer") immediately upon landing on the detail screen.

### 2. Domain Layer: Dynamic Local Rationale
Currently, the engine outputs a static debug string for local heuristic selections. We will update this to build a descriptive sentence based on the items picked.

#### [MODIFY] [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- Update `architectLocalBlueprint` to construct a rationale string using the names of the `selectedItems`.
- Example output: *"Optimized for rotation. Features your Midnight Navy Blazer and Slim Fit Chinos."*

### 3. ViewModel Layer: Reliable Mapping
We will ensure that the `StylePlaylistViewModel` correctly orchestrates the data mapping between the flat ID lists and the hierarchical visual blueprint.

#### [MODIFY] [StylePlaylistViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/playlist/ui/StylePlaylistViewModel.kt)
- Verify the `ResolvedDailyPlan` mapping logic correctly extracts the "w_" and "c_" prefixes.
- Ensure the `BlueprintDetailContent` receives the fully resolved item lists.

### 4. Persistence: AI Provenance
#### [MODIFY] [PlaylistEmbeddedModels.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/PlaylistEmbeddedModels.kt)
- Finalize the `SelectionEvidence` structure to include `scoringVersion` for historical auditability.

## Verification Plan

### Automated Tests
- **UseCase Test**: Update `GeneratePlaylistUseCaseTest` to verify that the generated rationales contain the names of the selected garments.
- **Mapper Test**: Verify `mapToVisualBlueprintData` correctly places items in their respective UI slots (Top, Bottom, Shoes).

### Manual Verification
1. Open an existing Style Playlist.
2. Click on a daily card to view the **DAILY BLUEPRINT**.
3. **Verify**: All nodes (Eyes, Lips, Top, Bottom) display actual item names instead of "Details".
4. **Verify**: The rationale text at the top describes the specific outfit instead of the "Local Architect" placeholder.
