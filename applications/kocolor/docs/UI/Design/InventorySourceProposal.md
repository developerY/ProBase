# Proposal: Unified Inventory Source Tracking & Multi-Pack Management

This proposal outlines the transformation of our data layer into a source-aware inventory system. Every item in the **Glow Archive** will now carry its "Ancestry," and the **Sync Screen** will evolve into a dynamic catalog of curated style payloads.

---

## 🏗️ 1. Data Layer: The "Ancestry" Model

We will replace the current nullable `sourcePackId` with a robust, mandatory `ItemSource` model.

### New Enum: `InventorySource`
```kotlin
enum class InventorySource {
    USER_SCAN,      // Manually captured by user
    STARTER_PACK,   // Official KoColor foundational data
    SAMPLE_PACK,    // Seasonal or trend-based miniatures
    PROMO_PACK,     // Brand-partnered limited collections
    GIFTED          // Special unlocks or rewards
}
```

### Entity Update
Both `CosmeticItemEntity` and `ClothingItemEntity` will be updated to include:
*   `sourceType: InventorySource`
*   `sourceName: String` (e.g., "Winter 2026 Trend Kit")
*   `sourcePackId: String?` (The technical ID for batch wiping)

---

## 📦 2. Pack Management: The "Sync Hub"

The **Glow Archive Sync** screen will transition from a single-button ingestion to a **Dynamic Pack Catalog**.

### New Entity: `InstalledPackEntity`
This table will track which packs are currently living in the local database:
*   `packId: String` (Primary Key)
*   `version: Int`
*   `status: PackStatus` (AVAILABLE, DOWNLOADING, INSTALLED)
*   `itemCount: Int`

### Pack Discovery Flow
1.  **Manifest Fetch**: The app pings `cdn.kocolor.com/inventory/manifest.json`.
2.  **Catalog Render**: The `StarterPackScreen` lists available packs (e.g., "Essential Base," "Midnight Berry Collection").
3.  **Status Sync**: The UI compares the manifest against `InstalledPackEntity` to show **"INSTALLED"** badges.

---

## 🎨 3. UI/UX: Transparency & Provenance

### Inventory Source Badges
Whenever an item is shown (Grid, List, or Detail), a small **Source Tag** will appear:
*   **User Item**: No badge (clean look) or a subtle "Personal Archive" tag.
*   **Pack Item**: A pill-style badge matching the pack's theme (e.g., a "Winter Pack" badge on a blue background).

### Pack Detail View
Clicking a pack in the Sync screen will open a **Preview Screen**:
*   **Inventory List**: Shows all products contained in that JSON payload *before* you download it.
*   **Compatibility Summary**: "This pack adds 12 items compatible with your Cool Winter profile."

---

## 🛠️ Implementation Roadmap

### Phase 1: Persistence Upgrade
*   Update `CosmeticItemEntity` and `ClothingItemEntity` with the new source fields.
*   Implement `InstalledPackEntity` and DAO.

### Phase 2: Ingestion Logic
*   Refactor `StarterPackRepository` to accept any `packId` and download its specific JSON.
*   Tag all items during the mapping phase with the correct `sourceType`.

### Phase 3: The Multi-Pack UI
*   Update `StarterPackScreen` to use a `LazyColumn` of `SyncPackCard` components.
*   Implement the "Wipe" logic to be pack-specific.

---

## ✅ Benefits
*   **User Trust**: Users know exactly which data is "theirs" and which was "suggested."
*   **Clean Slate**: Users can experiment with a "Promotion Pack" and delete it without fear of losing their manual logs.
*   **Scalability**: We can launch infinite packs (Influencer collabs, seasonal kits) without changing the Android codebase.

**Does this tiered approach to source tracking and cataloging meet your vision for the inventory system?**
