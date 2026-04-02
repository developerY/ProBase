# Wearable Data Layer Sync for Photodo Tasks

Implement bidirectional synchronization of task completion status between the mobile app and the Wear OS app using the Wearable Data Layer API.

## Proposed Changes

### Shared Core & DB

#### [PhotoDoDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/PhotoDoDao.kt)
- Add `getTaskBySyncId` and `updateTaskStatusBySyncId` queries.

#### [PhotoDoRepo.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/PhotoDoRepo.kt)
- Add sync-related methods to the interface.

#### [PhotoDoRepoImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/PhotoDoRepoImpl.kt)
- Integrate `TaskSyncEngine` to trigger sync on `updateTask`.
- Implement `getTaskBySyncId` and `updateTaskStatusBySyncId`.

#### [NEW] [TaskSyncEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/sync/TaskSyncEngine.kt)
- Interface for pushing task updates to the Data Layer.

#### [NEW] [NoOpTaskSyncEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/sync/NoOpTaskSyncEngine.kt)
- Default implementation for contexts where sync is not needed.

---

### Data Module (Shared Sync Logic)

#### [NEW] [:applications:photodo:data](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/data/build.gradle.kts)
- New module for sync implementations.
- Dependencies: `play-services-wearable`, `gson`, `:applications:photodo:db`.

#### [NEW] [PhotoDoSyncEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/data/src/main/java/com/zoewave/probase/photodo/data/PhotoDoSyncEngine.kt)
- Implements `TaskSyncEngine` using `DataClient`.

#### [NEW] [PhotoDoSyncListenerService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/data/src/main/java/com/zoewave/probase/photodo/data/PhotoDoSyncListenerService.kt)
- Listens for `/photodo/task_update/` data events and updates the local database.

---

### Mobile App Integration

#### [mobile/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/build.gradle.kts)
- Add dependency on `:applications:photodo:data`.

#### [mobile/src/main/AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/AndroidManifest.xml)
- Register `PhotoDoSyncListenerService`.

---

### Wear App Integration

#### [wear/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/build.gradle.kts)
- Add dependency on `:applications:photodo:data`.

#### [wear/src/main/AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/AndroidManifest.xml)
- Register `PhotoDoSyncListenerService`.

## Verification Plan

### Automated Tests
- I'll verify the module builds successfully: `./gradlew :applications:photodo:data:assembleDebug`.

### Manual Verification
- This requires running both apps on real devices or paired emulators.
- I'll verify the code structure and service registrations.
- I'll double-check that `globalSyncId` is used consistently.
