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
    val brand: String?
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

## ⚙️ 2. The Rust Package Compiler (`kocolor-compiler`)

The backend has transitioned from a simple generator to a **Strict Validation Compiler**.

### Key Logic:
1.  **Strict Type Check**: Validates the source JSON against Rust structs before compilation.
2.  **Deterministic Serialization**: Instead of hashing raw text, we serialize the Rust struct to a byte vector. This ensures that two JSON files with the same data but different whitespace result in the exact same `.kpkg` hash.
3.  **Filenaming**: Implements the `com.kocolor.pack.{id}-{sha256}.kpkg` standard.
4.  **Manifest Auto-Update**: Every build outputs a complete manifest entry ready for the CDN.

**Usage**: `cargo run --bin kocolor-compiler -- build starter-pack ./source.json`

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
