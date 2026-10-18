# Walkthrough - Starter Pack Upgrade (Selective Sync & Search)

The **Starter Pack** system in KoColor has been upgraded to a high-performance **Glow Sync Hub** with global search and selective item ingestion.

## Key Features Implemented

### 1. Sync Hub (Global Search)
- **Search Bar**: A Material 3 `DockedSearchBar` at the top of the hub allows users to search across all available packs instantly.
- **Efficient Filtering**: The search index is debounced (300ms) and filtered in-memory for zero-latency feedback.
- **Deep Linking**: Clicking a search result navigates directly to the specific pack and item.

### 2. Selective Ingestion (The Picker)
- **Preview First**: Users can now preview the contents of a pack before downloading anything.
- **Selective Picker**: A `LazyColumn` with checkboxes allows users to "opt-in" to specific items.
- **Bulk Actions**: "Select All" and "Clear" actions enable rapid management of large packs.
- **Efficiency**: Only lightweight thumbnails are loaded in the picker. Full-resolution assets are fetched asynchronously only after the user confirms the import.

### 3. Smart Navigation (Scroll-to-Item)
- **Auto-Scroll**: When navigating from a search result, the app automatically scrolls to the target item using `animateScrollToItem`.
- **Visual Highlight**: The target item briefly pulses with a background highlight to help the user locate it in a long list.

## Technical Details
- **Architecture**: MVVM with Clean Architecture principles.
- **Data Layer**: Retrofit for API calls, Room for local persistence, and Coil for image loading.
- **State Management**: Kotlin StateFlow with `combine`, `debounce`, and `distinctUntilChanged` operators.
- **UI**: 100% Jetpack Compose with Material 3 components.

## Verification Results
- **Unit Tests**: 8 tests passed covering ViewModel logic (search debouncing, selection state, and repository interactions).
- **Build**: Successfully built the `:applications:kocolor:apps:mobile` module.
