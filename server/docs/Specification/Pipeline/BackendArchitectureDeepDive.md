# Deep Dive: The Glow Archive Manifest & Modular Backend

This document explains the high-performance, "Static-First" backend architecture powering the KoColor inventory ecosystem. It details how we use Rust to generate a dynamic product catalog served via a global CDN.

---

## 🏗️ 1. The Architectural Strategy

The KoColor backend is designed for **Zero Latency** and **Infinite Scalability**. We achieve this by treating the backend not as a live server, but as a **Build-Time Compiler**.

### The Flow:
1.  **Definitions**: Items are defined in strictly-typed Rust modules.
2.  **Composition**: Specific items are grouped into "Packs" (e.g., Core Starter, Winter 2026).
3.  **Generation**: A Rust binary compiles these definitions into static JSON files.
4.  **Deployment**: These files (and images) are hosted on GitHub Pages (our "CDN").
5.  **Ingestion**: The mobile app pings a master manifest to discover and download these packs.

---

## 📄 2. The Master Manifest (`manifest.json`)

The `manifest.json` is the entry point for the mobile application. It tells the app which curated libraries are available for download.

### Schema Example:
```json
{
  "packs": [
    {
      "id": "starter_pack_v1",
      "name": "Core Collection",
      "description": "Foundational high-fidelity product library.",
      "version": 1,
      "type": "STARTER_PACK",
      "endpoint": "starter-pack.json",
      "item_count": 86
    }
  ]
}
```

*   **Endpoint**: The specific filename on the CDN.
*   **Version**: Used by the app to trigger "UPDATE" badges if a pack is revised.
*   **Type**: Maps to the `InventorySource` enum in the database (e.g., `PROMO_PACK`).

---

## 🧩 3. Modular Inventory Registry

Instead of managing a giant, error-prone JSON file, we define items in Rust code. This provides **Compile-Time Validation** of our taxonomy.

### Directory Structure:
*   `server/kocolor/src/inventory/cosmetics/`: Contains `prep.rs`, `lips.rs`, `complexion.rs`, etc.
*   `server/kocolor/src/inventory/clothing/`: Contains `tops.rs`, `accessories.rs`.
*   `server/kocolor/src/inventory/mod.rs`: The master `InventoryRegistry` that provides the `compose_pack` utility.

### Pack Composition Logic:
In Rust, we can create a new seasonal pack in seconds by simply listing IDs:
```rust
let (winter_items, _) = InventoryRegistry::compose_pack(
    vec!["kc-cosm-005", "kc-cosm-014"], // ID references
    vec!["kc-cloth-001"]
);
```

---

## ⚙️ 4. The Payload Generator (`generate_payload.rs`)

The `generate_payload` binary is the orchestration tool. When run, it:
1.  Loads all items from the `InventoryRegistry`.
2.  Creates the full `starter-pack.json`.
3.  Creates curated seasonal packs (e.g., `winter-essentials.json`).
4.  Calculates item counts and generates the `manifest.json`.

**Command**: `cd server/kocolor && cargo run --bin generate_payload`

---

## 📱 5. Mobile Ingestion & Source Tracking

The Android application is now **Source-Aware**. 

### The "Ancestry" Persistence:
Every item ingested from the backend is tagged with its `sourcePackId`.
*   **Selective Wiping**: If a user deletes the "Winter Pack," the app runs `DELETE FROM items WHERE sourcePackId = 'winter_2026_kit'`.
*   **Personal Data Safety**: Manual scans have a `sourceType` of `USER_SCAN` and are excluded from pack wipes.
*   **Data Fidelity**: The app maps 40+ fields, including **Base Chemistry** (Water/Silicone/Oil) and **FDA Status**, ensuring the AI has a professional dataset to work with.

---

## ✅ Summary of Benefits
*   **Cost**: $0 hosting (Static JSON on GitHub).
*   **Speed**: Instant download from CDN edge locations.
*   **Security**: No database ports exposed; only signed JSON payloads.
*   **Maintainability**: Adding an item to a category file automatically updates all relevant packs and the manifest.

---
**Status**: 🚀 **Production Ready**
**Current Version**: 1.0.0
**Backend Engine**: Rust (Cargo)
