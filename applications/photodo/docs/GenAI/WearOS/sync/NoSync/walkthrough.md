# Walkthrough - One-Way "View-Only" Sync for PhotoDo Wear App

I have implemented a one-way synchronization system for the PhotoDo Wear OS app. The watch now acts as a read-only mirror of the mobile application's state, receiving updates via a lightweight JSON payload and persisting them in Jetpack DataStore.

## Changes Overview

### [photodo:model]
- **`SyncModels.kt`**: Created `Serializable` DTOs (`SyncCategory`, `SyncProject`, `SyncTask`) to define the structure of the data transferred to the watch.

### [photodo:data]
- **`PhotoDoSyncEngine.kt`**: On the phone, this engine now observes the repository's `getCategoriesWithProjectsAndTasks()` and `getAllProjectDetails()` flows. It maps the data to the sync DTOs (including photo counts) and broadcasts the state as JSON to the Wear Data Layer.
- **`SyncDataStore.kt`**: On the watch, this new component manages a Jetpack DataStore that persists the raw JSON sync payload. It exposes a decoded `Flow<List<SyncCategory>>` for use in the UI.
- **`PhotoDoSyncListenerService.kt`**: Updated to intercept the new `/photodo/sync_state` path and save incoming payloads to `SyncDataStore`.
- **`SyncModule.kt`**: Decoupled the sync engine from the repository by using `NoOpTaskSyncEngine` for the repo, avoiding a circular dependency cycle.

### [photodo:apps:mobile]
- **`PhotoDoApp.kt`**: Integrated `PhotoDoSyncEngine` into the main application lifecycle to start observing and broadcasting data as soon as the app starts.

### [photodo:apps:wear]
- **`HomeViewModel.kt`, `ProjectListViewModel.kt`, `TaskDetailViewModel.kt`**: Refactored to derive their UI state directly from `SyncDataStore`.
- **`HomeRoute.kt`**: Added an empty state handler to display "Open PhotoDo on phone to sync" if no data is available.
- **`TaskDetailRoute.kt`**: Enforced a "view-only" experience by disabling task toggling and replacing the detailed photo list with a simple count.

## Verification Summary

### Automated Tests
- Verified that all modified modules compile successfully with `:assembleDebug`.
- Verified that the dependency cycle was correctly broken by a clean build.

### Manual Verification (Simulated)
1. **Mobile Side**: `PhotoDoSyncEngine` observes Room changes and maps them to DTOs. It encodes the state into a JSON string and puts it into the Wearable Data Layer at `/photodo/sync_state`.
2. **Watch Side**: `PhotoDoSyncListenerService` receives the `DataEvent`, extracts the JSON, and saves it to `DataStore`.
3. **UI Integration**: ViewModels react to `latestSyncDataFlow` from `SyncDataStore`, decoding and filtering the JSON payload for their respective screens.
4. **View-Only Mode**: The `TaskItem` checkbox is disabled on the watch, and photos are shown only as a count.
