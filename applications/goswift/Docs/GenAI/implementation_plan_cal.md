# Add Nutrition Module for Calories Tracking

This plan outlines the steps to add a new `nutrition` feature module to GoSwift for tracking calorie intake. This module will be reused within the "Log" tab of the `input` module.

## User Review Required

- **Data Privacy**: The app will request `READ_NUTRITION` and `WRITE_NUTRITION` permissions from Health Connect.
- **UI Interaction**: The user will manually input the food name and the number of calories.

## Proposed Changes

### Core Data Module (`core:data`)

#### [HealthConnectRepositroy.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/health/HealthConnectRepositroy.kt)
- Add `readNutritionRecords(startTime: Instant, endTime: Instant)` to the interface.
- Add `insertNutritionRecord(foodName: String, calories: Double, timestamp: Instant)` to the interface.

#### [HealthConnectRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/health/HealthConnectRepositoryImpl.kt)
- Implement `readNutritionRecords` and `insertNutritionRecord` using `NutritionRecord`.

---

### GoSwift Data Module (`applications/goswift/data`)

#### [NEW] [NutritionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/data/src/main/java/com/zoewave/probase/goswift/data/NutritionRepository.kt)
- High-level repository for nutrition operations.

#### [NEW] [NutritionRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/data/src/main/java/com/zoewave/probase/goswift/data/NutritionRepositoryImpl.kt)
- Implementation using `HealthConnectRepository`.

#### [DataModule.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/data/src/main/java/com/zoewave/probase/goswift/data/di/DataModule.kt)
- Bind `NutritionRepository`.

---

### GoSwift App Module (`applications/goswift/apps/mobile`)

#### [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/AndroidManifest.xml)
- Add `android.permission.health.READ_NUTRITION` and `android.permission.health.WRITE_NUTRITION`.

#### [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/MainActivity.kt)
- Include nutrition permissions in the check and request flow.

---

### GoSwift Feature Nutrition Module (`applications/goswift/apps/mobile/features/nutrition`)

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/nutrition/build.gradle.kts)
- Initialize with required dependencies.

#### [NEW] [NutritionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/nutrition/src/main/java/com/zoewave/probase/goswift/mobile/nutrition/ui/NutritionViewModel.kt)
- Manage nutrition state and logging.

#### [NEW] [NutritionUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/nutrition/src/main/java/com/zoewave/probase/goswift/mobile/nutrition/ui/NutritionUiRoute.kt)
- UI for logging food/calories and viewing daily intake.

---

### GoSwift Feature Input Module (`applications/goswift/apps/mobile/features/input`)

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/input/build.gradle.kts)
- Add dependency on `:applications:goswift:apps:mobile:features:nutrition`.

#### [InputUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/input/src/main/java/com/zoewave/probase/goswift/mobile/input/ui/InputUiRoute.kt)
- Add "Calories" tab to the `TabRow`.
- Integrate `NutritionScreen` into the tab content.

---

### GoSwift Feature Home Module (`applications/goswift/apps/mobile/features/home`)

#### [HomeViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/home/src/main/java/com/zoewave/probase/goswift/mobile/home/ui/HomeViewModel.kt)
- Fetch and display daily calorie intake on the dashboard.

---

## Verification Plan

### Automated Tests
- `NutritionViewModelTest` to verify logging and calculations.
- Command: `./gradlew :applications:goswift:apps:mobile:features:nutrition:testDebugUnitTest`

### Manual Verification
- Log a meal and verify it reflects in the Home screen dashboard.
- Verify that permissions are requested correctly.
