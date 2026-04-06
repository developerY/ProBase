# Integrate Google Health Connect into GoSwift

This plan outlines the steps to integrate Google Health Connect into the GoSwift app to track caffeine, exercise, and sleep. This data will be used to provide smarter caffeine recommendations.

## User Review Required

- **Data Privacy**: The app will request permissions for reading Sleep and Exercise data from Health Connect.
- **Correlation Logic**: The initial logic for how exercise and sleep affect caffeine recommendations will be based on simple heuristics. For example, high caffeine levels late in the day will flag a sleep impact, and intense exercise might prompt a higher caffeine recommendation.

## Proposed Changes

### Core Data Module (`core:data`)

The core data module already has some Health Connect implementation. I will enhance it to support sleep data.

#### [HealthConnectRepositroy.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/health/HealthConnectRepositroy.kt)

- Add `readSleepSessions(startTime: Instant, endTime: Instant)` to the interface.
- Add `readExerciseSessions(startTime: Instant, endTime: Instant)` (already exists, but verify it returns what we need).

#### [HealthConnectRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/health/HealthConnectRepositoryImpl.kt)

- Implement `readSleepSessions` using `SleepSessionRecord`.
- Implement `readExerciseSessions` using `ExerciseSessionRecord`.

---

### GoSwift Data Module (`applications/goswift/data`)

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/data/build.gradle.kts)

- Add `implementation(project(":core:data"))` dependency.

#### [NEW] [HealthRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/data/src/main/java/com/zoewave/probase/goswift/data/HealthRepository.kt)

- Interface to fetch combined health data (sleep, exercise) for GoSwift.

#### [NEW] [HealthRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/data/src/main/java/com/zoewave/probase/goswift/data/HealthRepositoryImpl.kt)

- Implementation using `HealthConnectRepository` from `core:data`.

---

### GoSwift Feature Home Module (`applications/goswift/apps/mobile/features/home`)

#### [HomeViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/home/src/main/java/com/zoewave/probase/goswift/mobile/home/ui/HomeViewModel.kt)

- Inject `HealthRepository`.
- Fetch sleep data (e.g., last night's sleep) and exercise data (e.g., today's activity).
- Update `calculateCurrentCaffeine` to account for exercise if relevant (though metabolism changes are complex, we might start with simple advice).
- Update `getRecommendation` and `getSleepImpact` with real health data.

#### [HomeUiState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/home/src/main/java/com/zoewave/probase/goswift/mobile/home/ui/HomeUiState.kt)

- Update `Success` state to include sleep duration and exercise intensity/type.

#### [HomeUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/home/src/main/java/com/zoewave/probase/goswift/mobile/home/ui/HomeUiRoute.kt)

- Update UI to show sleep and exercise info.

---

## Verification Plan

### Automated Tests
- Create unit tests for `HomeViewModel` to verify that caffeine recommendations change based on mocked sleep/exercise data.
- Command: `./gradlew :applications:goswift:apps:mobile:features:home:test`

### Manual Verification
- Deploy the app and check if health data placeholders (or real data if available on device) appear on the home screen.
- Verify that adding a caffeine shot updates the "Sleep Impact" section.
