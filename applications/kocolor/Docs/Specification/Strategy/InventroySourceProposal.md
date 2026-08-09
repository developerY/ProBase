# Inventory Provenance & Dynamic Pack Management

## Evolving Starter Packs into a Professional Inventory Platform

The proposed architecture transforms Starter Packs from a one-time import mechanism into a fully managed inventory ecosystem.

Rather than treating imported products as anonymous records, every item maintains a complete history of its origin, lifecycle, and synchronization state.

This creates a transparent, scalable inventory system capable of supporting curated collections, partner packs, and future marketplace integrations.

---

# 🌟 Proposal Highlights

## 1. The Ancestry Model

### Every Product Has a Known Origin

Every inventory item includes a mandatory `InventorySource` field that records where the item originated.

Example:

```kotlin
enum class InventorySource {
    USER_SCAN,
    STARTER_PACK,
    SAMPLE_PACK,
    INFLUENCER_PACK,
    PARTNER_PACK,
    IMPORTED,
    MANUAL_ENTRY
}
```

This metadata becomes part of every inventory record.

### Benefits

- Complete product provenance
- Better analytics
- Smarter filtering
- Easier synchronization
- Improved debugging

---

## Inventory Flow

```text
          Product Added
                │
                ▼
        InventorySource
                │
      ┌─────────┼─────────┐
      ▼         ▼         ▼
 User Scan  Starter Pack  Sample Pack
      │         │         │
      └─────────┼─────────┘
                ▼
         Cosmetic Inventory
```

Every product carries its origin throughout its lifecycle.

---

# 2. Installed Pack Tracking

## Managing Pack Lifecycle

Introduce a dedicated database table:

```text
InstalledPackEntity
```

This table records which packs are currently installed on the device.

Example fields:

| Field | Purpose |
|--------|---------|
| Pack ID | Unique identifier |
| Version | Installed version |
| Install Date | When the pack was added |
| Source | Publisher or provider |
| Status | Installed, Available, Update Available |

---

## UI Benefits

Instead of guessing installation state, the application can dynamically display:

```text
Core Starter Pack

✓ INSTALLED
```

or

```text
Winter Essentials

⬇ AVAILABLE
```

The interface always reflects the actual synchronization state.

---

# 3. Visual Provenance

## Source Badges

Every product card can display a small visual badge indicating its origin.

Examples:

```text
Lipstick

[Starter Pack]
```

```text
Foundation

[User Scan]
```

```text
Blush

[Influencer Pack]
```

### Benefits

Users can immediately distinguish:

- Personal products
- Curated samples
- Downloaded packs
- Partner collections
- Imported inventory

without opening product details.

---

# 4. Dynamic Sync Hub

## From Download Screen to Content Catalog

The existing Starter Pack screen evolves into a centralized content hub.

Instead of supporting a single downloadable pack, the application displays an expandable catalog.

Example:

```text
Available Collections

✓ Core Starter Pack

⬇ Winter Essentials

⬇ Sephora Favorites

⬇ Summer Glow Collection

⬇ Influencer Collection
```

All available packs are retrieved from a centralized manifest.

---

## Architecture

```text
Manifest
     │
     ▼
Pack Catalog
     │
     ▼
Available Packs
     │
     ▼
Download
     │
     ▼
Install
     │
     ▼
InstalledPackEntity
```

This design allows new collections to appear automatically without requiring application updates.

---

# 5. Targeted Wiping

## Granular Inventory Management

Rather than deleting all imported content at once, users can remove individual collections.

Example:

```text
Installed Packs

✓ Core Starter Pack

✓ Summer Collection

✓ Winter Essentials
```

User action:

```text
Delete

Summer Collection
```

Result:

```text
Remaining

✓ Core Starter Pack

✓ Winter Essentials
```

Personal inventory remains untouched.

---

## Deletion Flow

```text
User Selects Pack
        │
        ▼
InstalledPackEntity
        │
        ▼
Find Associated Products
        │
        ▼
Delete Only Matching Items
        │
        ▼
Update Installed Packs
```

This targeted approach prevents accidental data loss while providing users with greater control over their inventory.

---

# Overall Architecture

```text
                  Central Manifest
                         │
                         ▼
                 Dynamic Pack Catalog
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
 Core Starter     Winter Pack     Sephora Pack
         │               │               │
         ▼               ▼               ▼
      Download       Download       Download
         │               │               │
         └───────────────┼───────────────┘
                         ▼
              InstalledPackEntity
                         │
                         ▼
              Cosmetic Inventory
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
 Source Badge      InventorySource     Targeted Delete
```

---

# Architectural Benefits

## Complete Provenance

Every inventory item has a traceable origin throughout its lifecycle.

---

## Dynamic Content Delivery

New cosmetic collections can be published without requiring application updates.

---

## Transparent User Experience

Visual badges communicate product origins at a glance.

---

## Flexible Inventory Management

Users can install, update, or remove individual collections without affecting unrelated inventory.

---

## Scalable Partner Ecosystem

The architecture naturally supports:

- Influencer collections
- Retail partnerships
- Seasonal releases
- Sponsored product packs
- Community-created collections

---

# Conclusion

This proposal transforms the existing Starter Pack implementation into a professional inventory management platform.

By introducing **InventorySource**, **InstalledPackEntity**, **Visual Provenance**, a **Dynamic Sync Hub**, and **Targeted Wiping**, the architecture evolves from a simple import mechanism into a transparent, scalable ecosystem.

The result is an inventory engine that supports rich content distribution, precise lifecycle management, and future monetization opportunities while giving users complete visibility into where every product originated and how it is managed.

---

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

