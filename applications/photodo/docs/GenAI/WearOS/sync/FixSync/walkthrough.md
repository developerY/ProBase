# Walkthrough - Photodo Task Synchronization

I have implemented a robust, bidirectional synchronization system for checklist tasks between the Photodo mobile and Wear OS applications.

## How It Works

### Bidirectional Propogation
- **Local to Remote**: Whenever a task is updated (e.g., toggled as complete) on either the watch or the phone, the `PhotoDoRepo` triggers the `TaskSyncEngine`. This engine uses the **Wearable Data Layer API** to broadcast the task's state (ID, completion status, and modification timestamp).
- **Remote to Local**: A background `PhotoDoSyncListenerService` on the companion device detects these data changes. It validates the incoming update using a **timestamp-based conflict resolution** strategy—only applying the change if the received data is newer than what's stored locally.

### Key Components
- **`:applications:photodo:data`**: A new shared module containing the synchronization logic and Hilt bindings.
- **`PhotoDoSyncEngine`**: Handles the transmission of `TaskEntity` states via `PutDataMapRequest`.
- **`PhotoDoSyncListenerService`**: A `WearableListenerService` that keeps the local Room database in sync even when the app is not in the foreground.
- **Enhanced `PhotoDoRepo`**: Automatically orchestrates the sync flow during standard database updates.

## Technical Details
- **Consistent Identification**: Hardcoded `globalSyncId` values for onboarding tasks to ensure they match across all devices (Mobile and Wear).
- **Comprehensive Triggers**: The sync flow is now triggered on both `upsertTask` (creation) and `updateTask` (status changes).
- **Robust Resolution**: Uses a **timestamp-based conflict resolution** strategy (`lastModified`) to ensure data consistency.
- **Enhanced Logging**: Added detailed logs to `PhotoDoSyncListenerService` to facilitate debugging of the synchronization process.

> [!IMPORTANT]
> To see the fix for existing onboarding tasks, you may need to **Clear Data** or **Reinstall** the apps on both the phone and the watch. This ensures they both use the new consistent IDs instead of the previously generated random UUIDs.

## Verification
- **Build Integrity**: Verified that all modules (`:data`, `:apps:wear`, `:apps:mobile`) compile and link correctly.
- **Architectural Alignment**: Follows the same patterns as the `ashbike` module for consistency across the ProBase ecosystem.
