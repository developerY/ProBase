# Refactor GotMind to MAD Gold Standard and Update API Level

This plan refactors the `gotmind` application to adhere to the "MAD Gold Standard" patterns used in other high-quality modules in the project (like `kocolor`), and updates the project to the latest API level.

## User Review Required

> [!IMPORTANT]
> - The navigation pattern will change from passing `String` keys to using the `@Serializable` `GotMindRoute` types. This ensures type safety but requires updating `MainActivity` and all feature screens.
> - The `targetSdk` will be updated to `37` globally in the project's version catalog. This ensures `gotmind` and other apps are targeting the latest stable Android SDK.

## Proposed Changes

Standardize all top-level composables to the signature:
`(uiState, modifier, onEvent, navTo)`.

### [global-config]

#### [MODIFY] [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)
- Update `android-targetSdk` to `"37"`.

### [gotmind-features]

#### [MODIFY] [MindWaveScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/mindwave/src/main/java/com/zoewave/probase/gotmind/features/mindwave/ui/MindWaveScreen.kt)
- Update signature to include `modifier` and `navTo: (GotMindRoute) -> Unit`.
- Add `@Preview` with mock `MindWaveState`.
- Pass `modifier` to the root `Box`.

#### [MODIFY] [MemBloxScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/memblox/src/main/java/com/zoewave/probase/gotmind/features/memblox/ui/MemBloxScreen.kt)
- Update signature to `(uiState, modifier, onEvent, navTo)`.
- Add `@Preview`.

#### [MODIFY] [GamesScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/games/src/main/java/com/zoewave/probase/gotmind/features/games/GamesScreen.kt)
- Update signature.
- Create a `GamesUiState` and `GamesEvent` if they don't exist to maintain the pattern.
- Add `@Preview`.

#### [MODIFY] [LeaderboardScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/leaderboard/src/main/java/com/zoewave/probase/gotmind/features/leaderboard/ui/LeaderboardScreen.kt)
- Update signature.
- Consolidate parameters into a `LeaderboardUiState`.
- Add `@Preview`.

#### [MODIFY] [SettingsScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/features/settings/src/main/java/com/zoewave/probase/gotmind/features/settings/ui/SettingsScreen.kt)
- Update signature.
- Add `@Preview`.

### [gotmind-app]

#### [MODIFY] [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/gotmind/apps/mobile/src/main/java/com/zoewave/probase/gotmind/mobile/MainActivity.kt)
- Update call sites for all refactored screens.
- Implement the `navTo` logic using the `backStack` and `GotMindRoute`.

## Verification Plan

### Automated Tests
- `gradlew :applications:gotmind:features:mindwave:assembleDebug`
- `gradlew :applications:gotmind:features:memblox:assembleDebug`
- `gradlew :applications:gotmind:apps:mobile:assembleDebug`

### Manual Verification
- Render Compose Previews for each screen to ensure they look correct.
- Verify navigation still works as expected in the app.
