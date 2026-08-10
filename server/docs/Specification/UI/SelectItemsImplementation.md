# Implementation: Collapsible Category Hub for Pack Ingestion

This document details the architectural and UI implementation of the **Collapsible Select Items** screen. This upgrade transforms the pack preview into a professional boutique-style interface with granular control over product categories.

---

## 🏗️ 1. Data & State Architecture

### Grouped State Model
The `PackPreviewUiState` was expanded to support category-based grouping and persistence of the collapse/expand states.

```kotlin
data class PackPreviewUiState(
    val items: List<PackItem> = emptyList(),
    val groupedItems: Map<String, List<PackItem>> = emptyMap(), // Key: Macro Category
    val selectedIds: Set<String> = emptySet(),
    val collapsedCategories: Set<String> = emptySet(), // Tracking UI state
    val isLoading: Boolean = false,
    val targetItemId: String? = null
)
```

### ViewModel Logic
The `PackPreviewViewModel` now performs a deterministic grouping of incoming items. It also exposes new handlers for section-level bulk actions:
*   `onToggleCategoryCollapse(category: String)`
*   `onSelectCategoryAll(category: String)`
*   `onClearCategory(category: String)`

---

## 🎨 2. UI Component Breakdown

### A. Sticky Category Headers (`PackPreviewCategoryHeader.kt`)
A lightweight, high-fidelity header that provides both visual structure and functional controls.
*   **Sticky Behavior**: Leverages `stickyHeader` in `LazyColumn` to keep category context visible during long scrolls.
*   **Granular Actions**: Integrated "Select All" and "Clear" text buttons directly into the header surface.
*   **Visual Cues**: Uses `MaterialTheme.colorScheme.surfaceVariant` with alpha transparency to create a distinct section boundary.

### B. Grouped LazyColumn (`PackPreviewScreen.kt`)
The main layout orchestrator now iterates over the `groupedItems` map instead of a flat list.

```kotlin
LazyColumn(state = listState) {
    uiState.groupedItems.forEach { (category, items) ->
        // 1. Render Header
        stickyHeader {
            PackPreviewCategoryHeader(category, ...)
        }

        // 2. Render Items (if expanded)
        if (!isCollapsed) {
            itemsIndexed(items) { index, item ->
                PackPreviewItemRow(item, ...)
            }
        }
    }
}
```

---

## 🌟 3. UX Features & Benefits

| Feature | Technical Implementation | User Benefit |
| :--- | :--- | :--- |
| **Collapse/Expand** | `collapsedCategories` set in state | Reduces visual clutter; allows users to "skip" categories like 'Nails' or 'Hair'. |
| **Boutique Branding** | `PackPreviewTopAppBar` serif titles | Reinforces the "Atelier" luxury aesthetic of the KoColor ecosystem. |
| **Selection Tracking** | `selectedIds.size` in Bottom Bar | Real-time feedback on exactly how many items will be added to the local DB. |
| **Animated Entry** | `animateColorAsState` on Target IDs | When a user clicks a search result, the app scrolls to the item and performs a subtle highlight pulse. |

---

## 🛠️ Integration Checklist
- [x] **Module Isolation**: Components moved to `ui.packpreview` package.
- [x] **Registry Integration**: Updated `KoColorNavEntryProvider` with new event lambdas.
- [x] **Preview Stability**: Added isolated previews for `CategoryHeader` and `ItemRow`.
- [x] **Data Integrity**: Ensure `macroCategory` mapping in `PackSyncRepositoryImpl` matches the UI keys.

**Status**: ✅ **UI IMPLEMENTED & VERIFIED**
**Design System**: Material 3 + KoColor Serif
