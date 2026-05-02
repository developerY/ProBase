# Implementation Plan - MemBlox 7-Star "Legendary" Upgrade

Transform MemBlox into a legendary experience with atmospheric visuals, high-impact feedback, and an addictive ranking system.

## Proposed Changes

### [feature] [MemBloxEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/memblox/src/main/java/com/zoewave/probase/gotmind/features/memblox/MemBloxEngine.kt)

- **Mechanics**:
    - **FRENZY Mode**: 10% chance to trigger after a 5x combo. Matches give 2x points and blocks spawn at 1.5x speed.
    - **Ranking System**: Implement `calculateRank()` based on score, accuracy, and best streak (S, A, B, C, D).
- **VFX State**:
    - `activeShockwaves: List<Shockwave>` (for Nukes and Equalizers).
    - `isFrenzy: Boolean`
    - `floatingScores: List<ScorePopup>`

### [feature] [MemBloxScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/memblox/src/main/java/com/zoewave/probase/gotmind/features/memblox/ui/MemBloxScreen.kt)

- **Atmospheric Visuals**:
    - **Dynamic Particle Background**: A constant, subtle field of floating "dust" that speeds up as the board fills.
    - **Frenzy FX**: The board border glows neon purple and sparks during Frenzy mode.
- **Juice & Feedback**:
    - **Floating Score Popups**: Satisfying "+10" (or "+50" with combo) text that flies up from match sites.
    - **Power-Up Shockwaves**: A high-impact "white ring" expansion when a Nuke or Equalizer triggers.
    - **Rank Badges**: Show a large, stylized S/A/B rank on the end-game screen.

## Verification Plan

### Automated Tests
- Run `./gradlew :applications:gotmind:apps:mobile:assembleDebug` to verify the build.

### Manual Verification
- Verify the particle background reacts to game speed.
- Verify score popups appear and follow a natural upward arc.
- Test Nuke and Equalizer to see the shockwave expansion.
- Reach high combos to trigger and verify Frenzy mode.
- Verify the final Rank appears on the Victory/Game Over screens.
