# Implementation Plan - MindWave Memory Game

Building "MindWave," the second memory game in the GotMind arcade suite. MindWave is a sequence-based memory challenge (Simon Says style) set in a 4x4 futuristic node grid.

## Proposed Changes

### Core Models & Database
#### [NEW] [MindWaveScoreEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/database/src/main/java/com/zoewave/probase/gotmind/database/MindWaveScoreEntity.kt)
- Create a Room entity to store MindWave scores (score, level, timestamp).

#### [NEW] [MindWaveScoreDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/database/src/main/java/com/zoewave/probase/gotmind/database/dao/MindWaveScoreDao.kt)
- Create a DAO with methods to insert and retrieve top 7 MindWave scores.

#### [GotMindDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/database/src/main/java/com/zoewave/probase/gotmind/database/GotMindDatabase.kt)
- Add `MindWaveScoreEntity` to the database.

---

### MindWave Feature Module
#### [MindWaveState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/MindWaveState.kt)
- Refine state to include haptic/sound toggles and multi-dimensional feedback.

#### [MindWaveViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/MindWaveViewModel.kt)
- Implement sequence generation logic.
- Integrate `AppSettingsRepository` for persistent haptics/sound.
- Integrate `MindWaveScoreDao` for persistence.

#### [MindWaveScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/ui/MindWaveScreen.kt)
- Add "Nebula" particle background.
- Implement advanced node VFX (glow, scale, and haptic pulses).
- Add "Quit" and "Pause" tactical controls consistent with MemBlox.

---

### App Integration
#### [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/apps/mobile/src/main/java/com/zoewave/probase/gotmind/mobile/MainActivity.kt)
- Finalize the `MindWave` route in `NavDisplay`.

#### [GamesScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/games/src/main/java/com/zoewave/probase/gotmind/features/games/GamesScreen.kt)
- Make the "MindWave" button active and clickable.

## Verification Plan

### Automated Tests
- Run `:applications:gotmind:apps:mobile:assembleDebug` to verify compilation.
- Run lint to ensure code quality.

### Manual Verification
- Deploy to device and verify:
    - Navigation from Games Hub to MindWave.
    - Sequence playback and user input matching.
    - Score persistence after game over.
    - Tactical controls (Quit/Restart) functionality.
