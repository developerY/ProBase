# Implementation Plan - MindWave Music Education

Transforming MindWave Symphony into an educational tool for music theory, ear training, and melody mastery.

## Educational Goal
Enable users to learn musical intervals, scales, and melodies by associating **Grid Position**, **Pastel Color**, and **Synthesized Frequency**.

## Proposed Features

### 1. Synthesized Audio Engine (Phase 1)
- **Real-Time Tone Generation**: Use `AudioTrack` to synthesize pure sine/square waves for each note frequency (C4 to D#5) with low latency.
- **Auditory-Visual Sync**: Trigger the sound exactly when a node flashes or is clicked.
- **Ear Training Mode**: A settings toggle to hide colors/labels, forcing users to rely on sound and location only.

### 2. Guided Curriculum (Phase 2)
- **Melody Sequences**: Instead of random nodes, use actual musical phrases (e.g., "Ode to Joy," "Twinkle Twinkle") for level progression.
- **Scale Explorer**: New "Lessons" that teach Major, Minor, and Pentatonic scales by highlighting them on the grid.
- **Interval Training**: Challenges that focus on the relationship between two specific notes.

### 3. Visual Music Notation (Phase 3)
- **Staff Visualization**: Add a minimalist musical staff overlay at the top that displays the note (quarter/half note) as it's played.
- **Chord Indicators**: Highlight nodes that form harmonic chords (Major, Minor, diminished).

## Proposed Changes

### Core Logic
#### [IMindWaveEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/IMindWaveEngine.kt)
- Add `playSound(nodeId: Int)`.

#### [BaseMindWaveEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/BaseMindWaveEngine.kt)
- Implement `SoundSynthesizer` integration.
- Update `playSequence` to trigger audio.

### New Module
#### [NEW] [WaveSynthesizer.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/util/src/main/java/com/zoewave/probase/core/util/audio/WaveSynthesizer.kt)
- A low-level utility to generate pure tones for specific frequencies.

## Verification Plan
- **Latency Check**: Verify that sound triggers immediately upon click (< 100ms).
- **Pitch Accuracy**: Test frequency output against a tuner.
- **Curriculum Flow**: Manual walkthrough of a "Scale Lesson."
