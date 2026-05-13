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

#### `Medication.kt`
- Domain model for medication (name, dosage, frequency, times).

#### `Frequency.kt`
- Enum/Sealed class for frequency options (Daily, Weekly, Interval).

#### `MedicationLog.kt`
- Record of medication being taken or skipped.

---

### [Module] :applications:rxlogic:db

#### `MedicationEntity.kt`
- Room entity for medications.

#### `MedicationLogEntity.kt`
- Room entity for logs.

#### `RxLogicDatabase.kt`
- Room database definition.

#### `MedicationDao.kt`
- Data access object for medications and logs.

---

### [Module] :applications:rxlogic:data

#### `MedicationRepository.kt`
- Interface and implementation for managing medication data.

---

### [Module] :applications:rxlogic:features:reminders

#### `MedicationListScreen.kt`
- Dashboard showing today's medications and their status.

#### `AddMedicationScreen.kt`
- Form to add or edit medications.

#### `RemindersViewModel.kt`
- ViewModel handling UI state for reminders.

#### `ReminderWorker.kt` (or similar)
- WorkManager integration for scheduling notifications.

---

### [Module] :applications:rxlogic:apps:mobile

#### `MainActivity.kt`
- Entry point with Navigation 3 setup.

#### `RxLogicApplication.kt`
- Hilt Application class.

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
