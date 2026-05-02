# Implementation Plan - MemBlox "Immersive Vision" Update

Maximize the playable game area by refactoring the UI into a minimalist "HUD" (Heads-Up Display) layout.

## Proposed Changes

### [feature] [MemBloxScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/memblox/src/main/java/com/zoewave/probase/gotmind/features/memblox/ui/MemBloxScreen.kt)

- **Layout Overhaul**:
    - Change the main container from a `Column` to a `Box` to allow the game board to fill the entire background.
    - **Ultra-Slim Header**: Replace the large `ElevatedCard` with a floating, translucent row at the top.
    - **Floating Power-Up Bar**: Relocate the power-up chips to a floating horizontal scroll row at the bottom of the board area, just above the navigation bar.
    - **Thin Progress Bar**: Replace the large `LinearProgressIndicator` with a 2dp high line at the very top of the board boundary.
- **Visual Juice**:
    - Apply a subtle glassmorphism blur (or translucency) to all HUD elements so the game particles are visible behind them.
    - Condense "Score" and "Pairs" into side-by-side minimalist labels.

## Verification Plan

### Automated Tests
- Run `./gradlew :applications:gotmind:apps:mobile:assembleDebug` to verify the build.

### Manual Verification
- Launch MemBlox and verify that the blocks now use significantly more vertical space.
- Check that the power-up bar is still easily accessible and scrollable at the bottom.
- Ensure the "Back" and "Analytics" buttons are visible and functional in the new floating header.
- Verify the 3D flip animations and shockwaves still look correct in the larger area.
