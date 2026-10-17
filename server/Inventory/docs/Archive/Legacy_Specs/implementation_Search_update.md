# Implementation Plan - Starter Pack Upgrade (Selective Sync Hub & Search)

Upgrade the existing Starter Pack system in KoColor to support global search via a build-time index and selective item ingestion with high-fidelity previews.

## User Review Required

> [!IMPORTANT]
> The search index and pack data are assumed to be hosted on a CDN (e.g., Cloudflare for near-instant propagation). This plan focuses on the client-side implementation of fetching, caching, and interacting with these resources.

## Technical Refinements

- **Search Debouncing**: The `StarterPackViewModel` will apply a `debounce(300L)` to the `searchQuery` StateFlow to optimize UI re-rendering and performance.
- **Scroll-to-Item Logic**: `PackPreviewScreen` will map the incoming `targetItemId` to its corresponding integer index in the `LazyColumn` and use `animateScrollToItem(index)` for a smooth user transition.
- **Visual Highlighting**: Items scrolled to via search will briefly display a background highlight to provide immediate visual feedback.
- **Asynchronous Image Pre-fetching**: Post-import, `StarterPackRepository` will use `Dispatchers.IO` and Coil's `ImageRequest` builder to pre-fetch full-resolution `imageUrl` assets without blocking the main thread.

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
    - Persistence logic: Inserting selected `PackItem`s into the Room database.
    - **Refinement**: Triggering asynchronous pre-loading of full-resolution images using Coil's `ImageRequest` on `Dispatchers.IO`.

---

### [ViewModel Layer]

#### [MODIFY] [StarterPackViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/StarterPackViewModel.kt)
- Update to manage `SyncHub` state.
- **Refinement**: Implement `debounce(300L)` on the search query StateFlow before filtering the `searchIndex`.
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
- **Refinement**: Add `LaunchedEffect` to map `targetItemId` to an index and call `animateScrollToItem(index)`.
- **Refinement**: Implement temporary row highlighting for items targeted via search.
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
- Unit tests for `StarterPackViewModel` search filtering logic (including debouncing verification).
- Unit tests for `PackPreviewViewModel` multi-selection and import triggers.

### Manual Verification
- Launch the app and navigate to **Glow Archive Sync Hub**.
- Verify that typing in the search bar filters results from the global index with a noticeable debounce.
- Click a search result and verify it navigates to the correct pack and smoothly scrolls/highlights the item.
- Test "Select All" / "Deselect All" in the picker.
- Click "Import Selected" and verify items appear in the local inventory.
- Verify full-resolution images are fetched asynchronously after import (inspecting network/cache activity).
