# Implementation Plan - Scan Power-Up (Reveal All Matches)

Add a new "Scan" power-up that helps players by sequentially revealing all currently matchable pairs on the board.

## Proposed Changes

### [feature] [MemBloxEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/memblox/src/main/java/com/zoewave/probase/gotmind/features/memblox/MemBloxEngine.kt)

- **Power-Up Update**:
    - Add `SCAN` ("Scan", "🔍") to `PowerUpType`.
- **Logic Implementation**:
    - Implement `usePowerUp(SCAN)`:
        1. Find all emojis that have 2 or more instances on the grid.
        2. Group these blocks by their emoji.
        3. Launch a coroutine to iterate through each group.
        4. For each pair:
            - Add their IDs to `initiallyRevealedBlockIds`.
            - Delay for 600ms.
            - Remove their IDs.
            - Small gap (100ms) before the next pair.

## Verification Plan

### Automated Tests
- Run `./gradlew :applications:gotmind:apps:mobile:assembleDebug` to verify the build.

### Manual Verification
- Launch MemBlox and populate the board with several blocks.
- Trigger the "Scan" power-up.
- Verify that every matching pair on the board flashes (reveals and hides) one after another.
- Verify the power-up count decrements correctly.
