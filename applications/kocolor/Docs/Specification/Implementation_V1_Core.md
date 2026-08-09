# Implementation: V1 Core Ingestion & Boutique UI

This document provides the technical specification for the baseline (Version 1) inventory ingestion pipeline and the premium "Boutique" user interface.

---

## 🏗️ 1. Data Contract (KCPS v1)

We have standardized on the **KoColor Canonical Product Schema (KCPS) Version 1**. This is the source of truth for all data exchange between the Rust backend and the Android app.

### Sealed DTO Hierarchy
We use a sealed interface to handle polymorphic collections of cosmetics and clothing within a single package.

```kotlin
@Serializable
sealed interface PackItemDto {
    val id: String
    val name: String
    val brand: String
    val macroCategory: String
    val microCategory: String
    val colorHex: String
    val shadeName: String?
    val imageUrl: String
    val thumbnailUrl: String
    val price: Double?
    val notes: String?
}

@Serializable
data class CosmeticItemDto(...) : PackItemDto

@Serializable
data class ClothingItemDto(...) : PackItemDto
```

---

## ⚙️ 2. The Rust Data-Driven Compiler (`generate_payload`)

The backend has transitioned into a pure, data-driven Command Line Interface (CLI). Hardcoded registries have been removed in favor of a flexible directory-based workflow.

### Key Logic:
1.  **Directory Iteration**: The compiler automatically scans the `input_packs/` directory for any `.json` files.
2.  **Dynamic Metadata**: `pack_id` and `name` are dynamically derived from the physical filename (e.g., `winter_2026.json` -> `com.kocolor.pack.winter-2026`).
3.  **Strict Type Check**: Validates every JSON source against KCPS v1 Rust structs before compilation.
4.  **Deterministic Serialization**: Serializes to a canonical byte vector to ensure stable SHA-256 hashes.
5.  **Unified Artifacts**: Automatically builds a single `manifest.json` and a global `search_index.json` covering all detected packs.

**Usage**: Simply drop a JSON file into `server/package/KoColor/input_packs/` and run `./runMe.sh`.

---

## 🎨 3. Boutique Hub UI (Android)

The `SelectItemsScreen` has been refactored for high-fidelity rendering and efficient data management.

### Technical UI Constraints:
*   **Opaque Sticky Headers**: Category headers use a 100% opaque surface. This prevents "text bleedthrough" where list items remain visible behind the header during scroll.
*   **Safe String Construction**: Prevents the common "null" string bug. If a product lacks a shade name, the UI renders only the brand.
*   **Safe Drawing Insets**: All top-level scaffolds apply `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`, ensuring compatibility with modern edge-to-edge displays and punch-hole cameras.
*   **State-Aware Selection**: The `Import Selected (X)` button is reactively enabled and updates its count as users tap items across different categories.

---

## 🛠️ Global Reset Policy
*   **Database Version**: Permanently set to **1** for all pre-release development.
*   **Migration Ban**: No manual or auto-migrations are allowed. The schema is iterated by wiping and recreating the Version 1 database.
*   **Protocol Lock**: Only `schema_version = 1` payloads are accepted by the repository.
