# Walkthrough - Health Data Seeding Feature

I have implemented a comprehensive health data seeding tool to help developers quickly populate the app with varied health data for testing.

## Changes

### 1. New "Seed Comprehensive Data" Button
I have added a new button to the **Settings** tab under the **Debug Tools** section.
- Clicking this button triggers a comprehensive data generation process that covers multiple health categories.
- The button uses a distinct secondary color to differentiate it from the single "Test Ride" button.

### 2. Comprehensive Seeding Logic
The seeding logic in `HealthViewModel` (triggered by the new `SeedData` event) performs the following actions:
- **Sleep Data**: Generates 7 days of sleep sessions with varying stages using `healthSessionManager.generateSleepData()`.
- **Exercise Sessions**: Inserts a variety of workouts, including:
    - A specific bike ride from yesterday.
    - A test run session from 2 days ago.
- **Aggregated Activity**: Adds 7 days of steps, distance, and calories using `writeExerciseSessionNotUse`.
- **Automatic Refresh**: The UI automatically reloads the latest health data once the seeding process is complete.

### 3. Resource & Event Updates
- **HealthEvent.kt**: Added `data object SeedData` to the `HealthEvent` interface.
- **strings.xml**: Added `features_health_core_action_seed_data` string resource.
- **HealthViewModel.kt**: Implemented `seedHealthData()` with proper permission checks.

## Verification Summary

### Automated Tests
- Successfully ran `:features:health:core:assembleDebug` to ensure all code changes, resource additions, and event handling are correctly compiled.

### Manual Verification
- Verified the appearance of the "Seed Comprehensive Data (Last 7 Days)" button in the `SettingsTab`.
- Verified the `seedHealthData()` logic correctly orchestrates multiple `HealthSessionManager` calls for varied data types.
- Confirmed that the seeding process includes a final call to `onEvent(HealthEvent.LoadHealthData)` to ensure the UI stays in sync.
