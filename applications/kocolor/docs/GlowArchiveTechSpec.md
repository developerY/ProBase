# KoColor Glow Archive: Technical Architecture & Taxonomy Specification

This document codifies the "Offline-First" distributed architecture and the "Cosmetic Intelligence" engine powering the KoColor ecosystem.

---

## 🏛️ 1. Distributed Hybrid Architecture
To achieve zero-cost, high-performance, and 100% uptime, KoColor utilizes a split-responsibility infrastructure.

### A. Static Metadata CDN (GitHub Pages)
*   **Host:** `cdn.kocolor.com` (GitHub Pages)
*   **Role:** Serves the **Starter Pack** (metadata) and optimized image assets.
*   **Key Artifacts:** 
    *   `starter-pack.json`: The versioned JSON blueprint.
    *   `/assets/*.webp`: Compressed 512x512 visual assets.
*   **Benefit:** Zero cold-start latency; instant payload delivery via global edge locations.

### B. Dynamic Intelligence Node (Hugging Face)
*   **Host:** Hugging Face Docker Spaces (Rust/Axum)
*   **Role:** Orchestrates dynamic AI workflows (Gemini integration, advanced layering simulations).
*   **Benefit:** Secure API key management and off-device heavy compute.

### C. Local SSOT (Android Room)
*   **Role:** The "Single Source of Truth."
*   **Persistence:** All items, whether from the CDN or local scans, are stored as `file://` URIs in internal storage and indexed in Room.

---

## 🟣 2. The Glow Archive Taxonomy
The system uses a deterministic three-tier classification to resolve physical products into computational attributes.

### Tier 1: Macro Categories (UI Layer)
Organizes the app into anatomical zones: `Skincare & Prep`, `Complexion`, `Color & Dimension`, `Eyes & Brows`, `Lips`, `Tools & Hygiene`.

### Tier 2: Micro Categories (Product Type)
Defines specific product types: `Primer`, `Foundation`, `Contour`, `Lipstick`, `Serum`, etc.

### Tier 3: Professional Facets (Engine Layer)
The metadata used by the **Layering Compatibility Engine**:
*   **Formulation:** Gel, Cream, Liquid, Powder, Balm.
*   **Chemistry Base:** Water, Silicone, Oil. *(Critical for pilling prevention)*.
*   **Finish:** Matte, Satin, Radiant, Metallic, Glitter.
*   **Coverage:** Sheer, Light, Medium, Full, Buildable.
*   **Temperature:** Warm, Cool, Neutral, Olive.

---

## ⚙️ 3. Backend Implementation: Rust Payload Generator
The backend logic executes locally to generate a validated, static payload.

*   **Tool:** `generate_payload.rs`
*   **Workflow:**
    1.  Define domain models matching the Android `core:model`.
    2.  Seed "Classic" reference items (e.g., *Signature Crimson Lip Color*).
    3.  Export to `starter-pack.json` with a specific `version` header.
    4.  Deploy to `kc-cdn` repository.

---

## 📱 4. Mobile Implementation: Data & Persistence

### The Ingestion Pipeline
The Android app utilizes a robust transformation pipeline to ensure data integrity:

1.  **Remote DTOs**: `StarterPackResponse` captures the CDN payload.
2.  **Safe Mapping**: The repository (`CosmeticInventoryRepositoryImpl`) maps API strings to strongly-typed Enums with defensive try-catch fallbacks.
3.  **Room Persistence**: `CosmeticItemEntity` stores the full facet set.
4.  **Converters**: `FashionConverters` handle the serialization of enums like `Temperature` and `ChemistryBase`.

### "Origin-Blind" Asset Storage
*   The app downloads images from the CDN via **Coil**.
*   Physical bytes are written to `Context.filesDir`.
*   The database stores a local `file:///` URI, ensuring the app works 100% offline after the initial seed.

---

## 🚀 5. Deployment Lifecycle

> [!TIP]
> **To update the Taxonomy:**
> 1. Modify `ProfessionalTaxonomy.md`.
> 2. Update the Rust generator `generate_payload.rs` and increment the `version`.
> 3. Run `cargo run --bin generate_payload`.
> 4. Push the new `starter-pack.json` to GitHub.
> 5. The Android app will detect the version bump and trigger `ingestStarterPack()`.

---

**Status:** Implementation Finalized
**Engineers:** Android Agent & Rust Sub-Agent
**Reference:** `DeployArch.md` | `GitHubServer.md`
