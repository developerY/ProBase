# Implementation Plan - One-Way "View-Only" Sync for PhotoDo Wear App

The goal is to replace the current task-only sync with a full, one-way sync from the phone to the watch. The watch will become a "view-only" mirror of the phone's state, receiving a lightweight JSON payload. This eliminates the need for conflict resolution and reduces battery drain by avoiding Room operations on the watch for this feature.

## User Review Required

> [!IMPORTANT]
> - The existing Room database and repository in the Wear module will remain but will **not** be used for the current UI. They are preserved for future use as requested.
> - A new `SyncDataStore` will be introduced to persist the latest sync payload on the watch.

## Proposed Changes

### [photodo:model]

Add serializable DTOs for data transfer.

#### [SyncModels.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/model/src/main/java/com/zoewave/probase/photodo/model/sync/SyncModels.kt) [NEW]

- Define `SyncCategory`, `SyncProject`, and `SyncTask` with `kotlinx.serialization.Serializable`.

---

### [photodo:data]

Update the sync engine and listener service to handle full state broadcast and reception.

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/data/build.gradle.kts)

- Add `androidx.datastore:datastore-preferences` and `kotlinx-serialization-json` dependencies.

#### [PhotoDoSyncEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/data/src/main/java/com/zoewave/probase/photodo/data/PhotoDoSyncEngine.kt)

- Inject `PhotoDoRepo`.
- Implement `startSyncing()` to observe `photoDoRepo.getCategoriesWithProjectsAndTasks()`.
- Map database entities to `SyncCategory` DTOs.
- Encode to JSON and push to `/photodo/sync_state` path.

#### [PhotoDoSyncListenerService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/data/src/main/java/com/zoewave/probase/photodo/data/PhotoDoSyncListenerService.kt)

- Inject `SyncDataStore`.
- Update `onDataChanged` to handle `/photodo/sync_state`.
- Save raw JSON payload to `SyncDataStore`.

#### [SyncDataStore.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/data/src/main/java/com/zoewave/probase/photodo/data/SyncDataStore.kt) [NEW]

- Provide a wrapper around Jetpack DataStore to store and retrieve the latest sync payload.

---

### [photodo:apps:mobile]

Initialize the sync broadcaster.

#### [PhotoDoApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/PhotoDoApp.kt)

- Inject `PhotoDoSyncEngine` and call `startSyncing()`.

#### [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/AndroidManifest.xml)

- Ensure proper Data Layer permissions and service declarations if needed (usually only needed for listener).

---

### [photodo:apps:wear]

Update ViewModels and UI to use the synced state and enforce "view-only" mode.

#### [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/AndroidManifest.xml)

- Update `intent-filter` for `PhotoDoSyncListenerService` to watch `/photodo/sync_state` instead of `/photodo/task_update/`.

#### [HomeViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/home/HomeViewModel.kt)

- Replace `PhotoDoRepo` dependency with `SyncDataStore`.
- Observe `latestPayloadFlow`, decode JSON, and update `uiState`.

#### [ProjectListViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/project/ProjectListViewModel.kt)

- Replace `PhotoDoRepo` dependency with `SyncDataStore`.
- Observe `latestPayloadFlow`, decode JSON, and filter projects by `categoryId`.

#### [TaskDetailViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/task/TaskDetailViewModel.kt)

- Replace `PhotoDoRepo` dependency with `SyncDataStore`.
- Observe `latestPayloadFlow`, decode JSON, and filter tasks/photoCount by `projectId`.
- **Remove `onToggleTask` implementation.**

#### [TaskDetailRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/task/TaskDetailRoute.kt)

- Remove `onToggleTask` from `TaskDetailScreen` and `TaskItem`.
- Make `CheckboxButton` non-clickable or replace it with a read-only icon.
- Remove `PhotoItem` (photos) and just display the `photoCount`.

## Verification Plan

### Automated Tests
- Run `gradlew :applications:photodo:data:test` to verify JSON mapping and serialization.
- Run `gradlew :applications:photodo:apps:wear:unitTest` to verify ViewModels correctly decode and filter JSON payloads.

### Manual Verification
1.  **Phone Side**:
    *   Add a project or task on the phone.
    *   Observe (via logs) that `PhotoDoSyncEngine` broadcasts the updated state.
2.  **Watch Side**:
    *   Observe (via logs) that `PhotoDoSyncListenerService` receives the payload and saves it to `DataStore`.
    *   Verify the Watch UI updates automatically with the new data.
    *   Verify that clicking checkboxes or other interactive elements on the watch does nothing (or is disabled).
    *   Verify that photos are gone and only the count is shown.
