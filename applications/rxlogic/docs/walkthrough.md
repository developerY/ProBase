# RxLogic - Medical Reminder App Walkthrough

RxLogic is a medical reminder app built using Modern Android Development (MAD) best practices. It features a multi-module architecture, offline-first data management with Room, dependency injection with Hilt, and background reminders using WorkManager.

## Accomplishments

### 1. Multi-Module Architecture
The app is organized into several modules to ensure separation of concerns and scalability:
- `:applications:rxlogic:model`: Domain models and navigation routes.
- `:applications:rxlogic:db`: Room database, entities, and DAOs.
- `:applications:rxlogic:data`: Repository layer for data access.
- **Feature Modules**:
    - `:applications:rxlogic:features:daily`: Today's schedule and upcoming reminders.
    - `:applications:rxlogic:features:medications`: Medication management (Add/Edit/View).
    - `:applications:rxlogic:features:settings`: User settings and profile.
- `:applications:rxlogic:apps:mobile`: Android application module.

### 2. Three-Tab Navigation
- Implemented a bottom navigation bar with three tabs: **Main**, **Medications**, and **Settings**.
- Used **Navigation 3** for managing the backstack and screen transitions.
- Each tab is isolated in its own feature module, following MAD best practices for modularization.

### 3. Data Layer
- **Room Database**: Implemented `RxLogicDatabase` with entities for `Medication` and `MedicationLog`.
- **Repository Pattern**: `MedicationRepository` provides a clean API for the UI to interact with data.
- **Unit Tests**: Verified mapping and repository logic with `MedicationRepositoryTest`.

### 4. Background Reminders
- **WorkManager**: Integrated `ReminderWorker` to schedule notifications when medications are due.
- **Scheduling**: Medications are automatically scheduled for reminders when added.

## Verification Summary

### Automated Tests
- Ran unit tests for the data layer:
  ```bash
  ./gradlew :applications:rxlogic:data:testDebugUnitTest
  ```
- **Result**: 2 passed, 0 failed.

### Build
- Successfully built the main application:
  ```bash
  ./gradlew :applications:rxlogic:apps:mobile:assembleDebug
  ```
- **Result**: Build Successful.

## How to Run
1. Select the `applications.rxlogic.apps.mobile` run configuration in Android Studio.
2. Click Run.
3. Navigate between the three tabs (**Main**, **Medications**, **Settings**).
4. In the **Medications** tab, use the "+" button to add a medication.
5. Mark medications as taken by clicking the checkmark.
