# RxLogic - Medical Reminder App Implementation Plan

Build a comprehensive medical reminder app using Modern Android Development (MAD) best practices, including Jetpack Compose, Room, Hilt, and a multi-module architecture.

## Proposed Changes

### Project Structure & Configuration

Create a new "Product Line" under `applications/rxlogic`.

- **`:applications:rxlogic:model`**: Pure Kotlin module for domain models.
- **`:applications:rxlogic:db`**: Room database, entities, and DAOs.
- **`:applications:rxlogic:data`**: Repository layer.
- **`:applications:rxlogic:features:reminders`**: Feature module for medication management and daily schedule.
- **`:applications:rxlogic:apps:mobile`**: Main application module.

### [Module] :applications:rxlogic:model

#### `RxLogicRoute.kt`
- Navigation routes for RxLogic (Main, Medications, Settings).

---

### [Module] :applications:rxlogic:features:reminders

#### `RemindersScreen.kt`
- Updated to focus on "Today/Upcoming" view.

#### [NEW] `MedicationsScreen.kt`
- List and management of all medications.

#### [NEW] `SettingsScreen.kt`
- App settings (Theme, Notifications).

#### [NEW] `RxLogicMainScreen.kt`
- Container with BottomBar for the three tabs.

#### `RemindersViewModel.kt`
- Renamed or updated to `RxLogicViewModel` to handle main navigation and global state.

---

### [Module] :applications:rxlogic:apps:mobile

#### `MainActivity.kt`
- Updated to use `RxLogicMainScreen`.

---

## Verification Plan

### Automated Tests
- Unit tests for `MedicationRepository`.
- Room database migration tests (if applicable, but initial version).
- Unit tests for `RemindersViewModel`.

### Manual Verification
1. Open RxLogic app.
2. Add a new medication with a specific time.
3. Verify it appears in the daily schedule.
4. Mark the medication as taken and verify it updates in the UI.
5. Wait for the scheduled time and verify notification (if possible in emulator).
