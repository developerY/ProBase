# Implementation Plan: KoColor Asset Engineering Pipeline (Phase 4)

This document outlines the technical execution path for transitioning the KoColor distribution system to the **Sealed Specification v1.0**. We are moving to a pure "Compute-at-Compile-Time" (CCT) architecture that separates raw authoring from optimized mobile distribution.

---

## 🏗️ 1. Infrastructure Architecture

### Directory Reorganization
We will establish a strict directory hierarchy to separate inputs from outputs.

*   `raw_assets/`: The source of truth for product data (KPSS JSON + PNG).
*   `package_configs/`: Assortment logic (TOML).
*   `dist/`: Final signed artifacts (.kpkg, manifest, WebP assets).

### The In-Memory Canonical Index
The compiler will transition to a two-pass system:
1.  **Pass 1 (Indexing)**: Traverse `raw_assets/`, validate against KPSS v1, and build a global `HashMap<ProductId, ProductData>`.
2.  **Pass 2 (Composition)**: Traverse `package_configs/`, resolve IDs from the index, and generate signed `.kpkg` payloads.

---

## ⚙️ 2. Core Compiler Modules

### A. KPSS Validator & Normalizer
*   Implement strict validation for **KPSS v1**.
*   Standardize commercial names and semantic IDs.
*   **Asset Naming Rule**: Implement filename extraction to ensure `cleanser.png` authoring source maps to `cleanser.webp` in distribution.

### B. Asset Optimization Pipeline (`rayon` integrated)
*   **Hero Stream**: Process 1:1 PNGs -> Resize to 1024x1024 -> Lossy WebP (85%).
*   **Thumbnail Stream**: Resize to 256x256 -> Generate 4x4 Base83 BlurHash string.
*   **Parallelism**: Use `rayon` to process all product images concurrently, maximizing build machine throughput.

### C. Composition Engine (TOML)
*   Integrate `toml` crate to parse package assortment definitions.
*   Implement "Sealed Build" logic to ensure byte-identical output for identical inputs.

---

## 🧪 3. Data Transformation Logic (KPSS -> KCPS)

The compiler will perform a "Clean Purge" during the transformation from authoring to wire formats.

| Action | Target Field(s) | Purpose |
| :--- | :--- | :--- |
| **Inject** | `blurhash` | Instant UI placeholders. |
| **Inject** | `image_url`, `thumbnail_url` | CDN-ready WebP paths. |
| **Purge** | `raw_image_input` | Local path information leak prevention. |
| **Purge** | Internal RGB/Phase intermediates | Minimize payload weight. |

---

## 🛠️ 4. Implementation Checklist

- [ ] **Task 1: Workspace Initialization**
    - Create `raw_assets/` and `package_configs/` folders.
    - Setup `Cargo.toml` with `image`, `blurhash`, `rayon`, and `toml`.
- [ ] **Task 2: KPSS Indexing & Validation**
    - Implement directory walker for `raw_assets/`.
    - Build the `CanonicalProductIndex` HashMap.
- [ ] **Task 3: The Asset Stream**
    - Implement the concurrent WebP and BlurHash generation logic.
- [ ] **Task 4: TOML Assortment Logic**
    - Create the composition resolver to build packages by ID.
- [ ] **Task 5: Final Packaging & Signing**
    - Update the Ed25519 signing logic to include the newly enriched KCPS fields.
- [ ] **Task 6: Documentation Update**
    - Update the user walkthrough with the new "Drop JSON & Run" workflow.

---
**Status**: 🗓️ **PLANNING COMPLETE**
**Target**: Sealed Specification v1.0
**Lead**: Rust Systems Architect
