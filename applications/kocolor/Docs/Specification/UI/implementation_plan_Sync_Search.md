# Implementation Plan - Starter Pack Upgrade (Selective Sync Hub & Search)

Upgrade the existing Starter Pack system in KoColor to support global search via a build-time index and selective item ingestion with high-fidelity previews.

## User Review Required

> [!IMPORTANT]
> The search index and pack data are assumed to be hosted on a CDN. This plan focuses on the client-side implementation of fetching, caching, and interacting with these resources.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [KocolorApiService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/KocolorApiService.kt)
- Add endpoints for `getSearchIndex()` and `getPackItems(packId: String)`.

#### [NEW] [SearchIndexEntry.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/model/SearchIndexEntry.kt)
- Define `SearchIndexEntry` data class: `id`, `term`, `brand`, `packId`.

#### [NEW] [PackItem.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/model/PackItem.kt)
- Define `PackItem` data class: `id`, `name`, `shade`, `brand`, `hexColor`, `thumbnailUrl`, `imageUrl`.

#### [NEW] [StarterPackRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt)
- Create a repository to handle:
    - Fetching and in-memory caching of the global search index.
    - Fetching individual pack contents.
    - Persistence logic: Inserting selected `PackItem`s into the Room database and triggering full-res image pre-loading.

---

### [ViewModel Layer]

#### [MODIFY] [StarterPackViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/StarterPackViewModel.kt)
- Update to manage `SyncHub` state.
- Add logic to filter `searchIndex` based on a `searchQuery` StateFlow.
- Trigger initial fetch of `manifest.json` and `search_index.json`.

#### [NEW] [PackPreviewViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/PackPreviewViewModel.kt)
- Manage `PackPreview` state.
- Handle multi-selection using `StateFlow<Set<String>>`.
- Implement `selectAll()`, `deselectAll()`, and `importSelected()` actions.

---

### [UI Layer (Compose)]

#### [MODIFY] [StarterPackScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/StarterPackScreen.kt)
- Refactor into `SyncHubScreen`.
- Add `DockedSearchBar` at the top.
- Implement search results dropdown showing filtered index items.

#### [NEW] [PackPreviewScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/PackPreviewScreen.kt)
- Implement the selective picker UI.
- Use `LazyColumn` with `LazyListState`.
- Add `LaunchedEffect` to scroll to `targetItemId` if provided.
- Row items with `Checkbox` and `AsyncImage` (Coil) for thumbnails.
- Sticky bottom bar with "Import Selected (X)" button.

#### [MODIFY] [KoColorRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/KoColorRoute.kt)
- Add `PackPreview(val packId: String, val targetItemId: String? = null)` destination.

---

### [Navigation & Wiring]

#### [MODIFY] [KoColorNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorNavEntryProvider.kt)
- Wire up the new `PackPreview` route in the navigation graph.

## Verification Plan

### Automated Tests
- Unit tests for `StarterPackViewModel` search filtering logic.
- Unit tests for `PackPreviewViewModel` multi-selection and import triggers.

### Manual Verification
- Launch the app and navigate to **Glow Archive Sync Hub**.
- Verify that typing in the search bar filters results from the global index.
- Click a search result and verify it navigates to the correct pack and scrolls to the item.
- Test "Select All" / "Deselect All" in the picker.
- Click "Import Selected" and verify items appear in the local inventory.
- Verify full-resolution images are fetched only after import.
