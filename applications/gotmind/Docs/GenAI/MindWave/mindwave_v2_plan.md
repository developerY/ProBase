# Implementation Plan - MindWave Symphony Mode

Enhancing MindWave with a "Symphony" mode that adds pastel colors and musical notes to the grid nodes, providing a multi-sensory memory experience.

## User Review Required
- **Mode Switching**: The new "Symphony" version will be toggleable in the Settings screen.
- **Audio Feedback**: The "Music Note" label is visual for now; actual synthesized tones could be added in a follow-up.

## Proposed Changes

### Core Models & Settings
#### [MemBloxSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/model/src/main/java/com/zoewave/probase/gotmind/model/MemBloxSettings.kt)
- Add `MindWaveMode` enum: `CLASSIC`, `SYMPHONY`.
- Add `mindWaveMode` to `MemBloxSettings`.

#### [MindWaveState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/MindWaveState.kt)
- Update `Node` to include `color: Long?` and `note: String?`.
- Add `mode: MindWaveMode` to `MindWaveState`.

---

### Logic & Persistence
#### [AppSettingsRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/data/src/main/java/com/zoewave/probase/gotmind/data/repository/AppSettingsRepository.kt)
- Add DataStore key for `mindwave_mode`.
- Implement `saveMindWaveMode`.

#### [MindWaveViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/MindWaveViewModel.kt)
- Initialize the 4x4 grid with pastel colors and musical notes when in `SYMPHONY` mode.
- Map node indices to specific frequencies/notes.

---

### UI & Settings
#### [MindWaveScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/ui/MindWaveScreen.kt)
- Update `MindWaveNode` to display the pastel color background.
- Render the music note text (e.g., "C#") inside the node when in Symphony mode.

#### [SettingsScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/settings/src/main/java/com/zoewave/probase/gotmind/features/settings/ui/SettingsScreen.kt)
- Add a new "MindWave Settings" section.
- Add a dropdown for "Game Version" (Classic vs. Symphony).

## Verification Plan

### Manual Verification
- Deploy to device.
- Open Settings and switch MindWave to "Symphony" mode.
- Launch MindWave and verify:
    - Nodes are pastel-colored.
    - Music notes are displayed inside nodes.
    - Gameplay sequence follows the new multi-sensory cues.
- Switch back to "Classic" and verify it reverts to the original look.
