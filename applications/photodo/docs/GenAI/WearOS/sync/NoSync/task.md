# Task: One-Way "View-Only" Sync for PhotoDo Wear App

## Research & Planning
- [/] Research existing mobile and wear modules [/]
    - [x] List files in mobile module
    - [x] List files in wear module
    - [ ] Locate `PhotoDoSyncEngine` in mobile module
    - [ ] Locate `PhotoDoSyncListenerService` in wear module
    - [ ] Identify shared models or decide where to put DTOs
    - [ ] Analyze Wear UI to identify elements to be locked down (FABs, swipe-to-delete, checkboxes)
- [ ] Create Implementation Plan

## Implementation - Shared / DTOs
- [ ] Create/Update DTOs for sync (`SyncCategory`, `SyncProject`, `SyncTask`)

## Implementation - Mobile (Broadcaster)
- [ ] Update `PhotoDoSyncEngine` to observe Room and broadcast JSON payload
- [ ] Ensure `kotlinx.serialization` is available

## Implementation - Wear (Mirror)
- [ ] Create `SyncDataStore` for storing raw JSON payload
    - [ ] Handle internal JSON decoding and expose `Flow<List<SyncCategory>>`
- [ ] Update `PhotoDoSyncListenerService` to save JSON to `DataStore`
- [ ] Update Wear ViewModels to observe `DataStore`
- [ ] Lock down Wear UI (View-Only mode)
    - [ ] Update `HomeScreen` with empty sync message: "Open PhotoDo on phone to sync"
    - [ ] Remove/Disable FABs
    - [ ] Disable swipe-to-delete
    - [ ] Disable checkbox interactions

## Verification
- [ ] Build mobile and wear apps
- [ ] Verify sync from phone to watch
- [ ] Verify watch UI is view-only
- [ ] Verify Room is not used on the watch for this feature
