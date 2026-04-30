# Implementation Plan - MemBlox UI Overhaul

Transform MemBlox into a 5-star professional app with gorgeous visuals, polished animations, and refined layouts.

## Proposed Changes

### [feature] [MemBloxScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/memblox/src/main/java/com/zoewave/probase/gotmind/features/memblox/ui/MemBloxScreen.kt)

- **Header Redesign**:
    - Use a sleek, card-based header with subtle elevation.
    - Improve typography with varying font weights and sizes for score and progress.
    - Add a "Progress Bar" to visualize pairs matched towards the target.
- **Power-Up Bar**:
    - Standardize button sizes and use `ElevatedAssistChip`-style buttons.
    - Add micro-animations (pulsing) to available power-ups.
- **Game Board & Blocks**:
    - **Gradients**: Add linear gradients to blocks to give them a 3D "gem-like" feel.
    - **Gloss Overlay**: Add a subtle light-sweep overlay on blocks.
    - **Board Background**: Switch to a very dark, slightly tinted gradient background for better contrast.
    - **Animations**: Use `Modifier.animateContentSize` or similar for smoother board updates.
- **Difficulty Selection**:
    - Replace the list of buttons with high-quality "Challenge Cards" featuring icons and detailed level descriptions.

### [feature] [MemBloxBlock.kt] (Internal Composable)
- Create a dedicated `MemBloxBlockRender` composable to encapsulate the complex gradient and border logic for individual blocks.

## Verification Plan

### Automated Tests
- Run `./gradlew :applications:gotmind:apps:mobile:assembleDebug` to verify the build.

### Manual Verification
- Visual inspection of the new UI on different screen sizes.
- Verify block gradients look professional and consistent.
- Ensure all animations (nuke, matches, transitions) are fluid and high-performance.
- Test the new difficulty cards.
