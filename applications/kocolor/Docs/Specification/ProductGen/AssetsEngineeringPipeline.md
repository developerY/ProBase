# KoColor Assets Engineering Pipeline Documentation (Sealed Specification v1.0)

## 1. Executive Summary & Architectural Philosophy

This document defines the asset engineering pipeline for the KoColor mobile boutique ecosystem. The architecture utilizes a **Compute-at-Compile-Time (CCT)** strategy to protect mobile CPU/GPU performance and network bandwidth, ensuring a zero-latency visual experience during high-velocity grid scrolling in the mobile client.

Computationally expensive operations (image optimization, BlurHash generation, payload normalization, signing) are executed *before* CDN upload using stable, high-performance compilation logic.

### Core Architectural Principle

Adding a new product, brand, image asset, or mixed assortment configuration must require **zero changes** to the stable Rust compiler logic. The logic remains locked while the data inventory iterates.

---

## 2. High-Level Canonical Architecture

The standard data and asset flow follows this deterministic boundary:

```text
                 DATA AUTHORING GRID (raw_assets/)
                               │
                               │ JSON (KPSS v1 Source Schema)
                               │ + source PNG images
                               ▼
                    ┌─────────────────────────┐
                    │      RUST COMPILER      │
                    │   STABLE LOGIC LOOP     │
                    │                         │
                    │ 1. Validate (vs KPSS v1)│
                    │ 2. Normalize Data       │
                    │ 3. Compute (HEX, Phase) │
                    │ 4. Optimize Images(WebP)│
                    │ 5. Generate BlurHash    │
                    │ 6. Canonicalize(KCPS v1)│
                    │ 7. Purge Intermediates  │
                    │ *Enforce Determinism*   │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │   CANONICAL CATALOG     │
                    │                         │
                    │ INDEXED PRODUCT DATABASE│
                    │   (In-Memory HashMap)   │
                    └────────────┬────────────┘
                                 ▲
                   composition   │
                   query loop    │
                                 │
                    ┌─────────────────────────┐
                    │     PACKAGE CONFIG      │
                    │                         │
                    │   ASSORTMENTS (TOML)    │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │    KCPS v1 .kpkg       │
                    │                         │
                    │   Signed + Compressed  │
                    │     CDN Artifacts       │
                    └────────────┬────────────┘
                                 │
                                 ▼
                            CDN DELIVERY
                                 │
                                 ▼
                         MOBILE ROUTINE APP

```

---

## 3. Physical Directory Hierarchy and Authoritative Metadata

This directory structure is the strictly defined input for the compiler.

> **Architecture Safeguard:** The directory path (`Brand/MacroCat/MicroCat`) is non-authoritative organizational metadata used for file traversal only. The semantic product identity (`brand`, `macro_category`) is defined *authoritatively* within the KPSS JSON file.

```text
kocolor-asset-engine/
├── Cargo.toml
├── src/                    <-- Locked Rust compiler source
├── package_configs/        <-- Composition (Assortment) definitions (TOML)
│   ├── starter_prep.toml
│   └── dermatological_routines.toml
└── raw_assets/             <-- KCPS Authoring Source Input (Normalized JSON + PNG)
    ├── AuraSkin/           <- metadata folder
    │   └── PREP/           <- metadata folder
    │       └── Cleanser/   <- metadata folder
    │           ├── as-gel.json     <-- KPSS v1 JSON (Source)
    │           └── as-gel.png      <-- Raw High-Res Asset
    └── KoColor/
        └── PREP/
            └── Cleanser/
                ├── kc-prep-01.json <-- KPSS v1 JSON (Source)
                └── kc-prep-01.png  <-- Raw High-Res Asset

```

---

## 4. Schema Contracts: KPSS vs. KCPS

The system is explicitly bounded by two independent schema contracts, coincidently both at Version 1 for pre-release development. The compiler is the stable transformation boundary between these contracts.

### 4.1. Raw Product Source Schema (The KPSS v1 Contract)

This is the minimal, canonical authoring data object. It only contains inputs, never calculated intermediates or generated artifacts.

| Field | Type | Description |
| --- | --- | --- |
| `schema_version` | Integer | Source Schema Version (Must be 1). |
| `id` | String | Authoritative semantic product ID (e.g., `kc-prep-01`). |
| `brand` | String | Authoritative brand name. |
| `macro_category` | String | Authoritative macro-category (e.g., `PREP`). |
| `micro_category` | String | Authoritative micro-category (e.g., `CLEANSER`). |
| `shade_name` | String | Commercial shade name (e.g., `Clear Crystal`). |
| `color_hex` | String | exact hexadecimal color code (e.g., `#F4F6F0`). |
| `raw_image_input` | Path | Relative path to the raw high-res PNG asset. |

### Example KPSS V1 Input

```json
{
  "schema_version": 1,
  "id": "kc-prep-01",
  "brand": "KoColor",
  "macro_category": "PREP",
  "micro_category": "CLEANSER",
  "shade_name": "Clear Crystal",
  "color_hex": "#F4F6F0",
  "raw_image_input": "./kc-prep-01.png"
}

```

### 4.2. Optimized Canonical CDN Object (The KCPS v1 Wire Contract)

The Rust compiler transforms the KPSS source into this complete, wire-compliant object used by mobile clients. Intermediates ( intermediate chemistry phase data, calculated RGB intermediates) are **PURGED** before distribution.

> **Correction Checklist Note:** Malformed fields like ` Contains_Fragrance` (previous conversation) are **fixed or purged** in the wire payload, not normalized into malformed JSON.

### Illustrative KCPS V1 Output Excerpt (Complete object listed)

```json
{
  "schema_version": 1, // Wire/Distribution Schema Version
  
  // SEMANTIC IDENTITY (Propagated from KPSS)
  "id": "kc-prep-01",
  "name": "Purifying Gel Cleanser",
  "brand": "KoColor",
  "macro_category": "PREP",
  "micro_category": "CLEANSER",
  "shade_name": "Clear Crystal",
  "color_hex": "#F4F6F0",
  
  // GENERATED CCT ARTIFACTS (Required by Mobile Engine)
  "blurhash": "LEHV6nWB2yk8pyo0adRj00WBof%M",
  "image_url": "https://cdn.kocolor.com/assets/kc-prep-01.webp",
  "thumbnail_url": "https://cdn.kocolor.com/assets/kc-prep-01_thumb.webp",
  
  // OTHER REQUIRED CONTRACT FIELDS (Ingested/Normalized by compiler)
  "notes": "Gentle daily foaming cleanser.",
  "hero_ingredient": "Niacinamide",
  "price": 18.0,
  "volume": "150ml",
  "eco_score": "A",
  "is_vegan": true,
  " Contains_Fragrance": false, // Formally normalized from KPSS source
  "recycling_instructions": null,
  "fda_data_verified": true
}

```

---

## 5. Asset Generation and Optimization Loop

The compilation phase implements the CCT philosophy to generate optimized WebP visual assets.

### 5.1 Raw Asset Ingestion Specifications

The compiler ingests high-resolution input PNGs generated sequentially by Gemini.

**Gemini Asset Generation Specification:**

* **Format:** PNG (Native full resolution).
* **Ratio:** 1:1 Square.
* **View:** Orthographic flatlay (direct top-down view).
* **Content:** Minimalist commercial photography. All text (e.g., brand typography) must be sharp and legible.
* **Color/Texture:** Must strictly match the `color_hex` and formulation from KPSS.
* **Background:** Seamless, pure white.

### 5.2. Rust Optimization Pipeline

The stable compiler uses Rayon for multi-threaded processing, performing the following on each raw asset sequentially:

#### Stream A: High-Fidelity Hero Assets (Detail Screen)

* **Processing:** Tight crop -> Resize to **1024x1024**.
* **Optimization:** Convert PNG input to Optimized Lossy **WebP (Quality 85%)** for superior mobile bandwidth performance.
* **Result:** Pristine product image for mobile detail screens.

#### Stream B: Performance Thumbnail Assets (Scroller Grids)

* **Input:** High-Fidelity Hero (WebP).
* **Processing:** gaussian downscale precisely to **256x256**.
* **BlurHash Encoding:** This thumbnail is the final input for the Rust BlurHash generator, encoding the small visual data into the stable Base83 string injected into the KCPS Wire Object.
* **Result:** The foundational asset required for fast-scrolling LazyGrid visual placeholders.

---

## 6. Composition and Mixed Packages (TOML Assortments)

The pipeline separates **Product existence** (Canonical Product Index) from **Product assortment** (Mixed Packages).

Package TOML manifestations in `package_configs/*.toml` are the *only* authoritative composition definitions. This allows composition without data duplication (e.g., same cleanser can exist in 'Starter Kit' and 'Dry Skin Routine').

The IDs used in TOML must be semantically clean and canonical. We do not use casual ID versioning (e.g., `starter-prep-v1`).

### Example Package Manifest: `starter_prep_ kit_a.toml`

```toml
[package_metadata]
id = "starter-prep-kit-a" # Semantic ID only, no casual ID versioning
name = "Initial Prep Routine A"
description = "Best-of-both routine mixing normalized products."
# is_standard_assortment_v1 = true # Casual versioning removed

[assortment]
includes = [
    "kc-prep-01",     # KoColor Cleanser (resolved from index)
    "as-vit-c",       # Aura Skin Serum (resolved from index)
    "kc-spf-invisible" # KoColor Sunscreen (resolved from index)
]

```

---

## 7. Determinism Requirement

To Ground reproducibility, hash verification, and security across distributed builder hardware, we enforce **Strict Determinism**. The architecture is now considered structurally locked, while the determinism guarantees are an implementation requirement.

**Phase 4 Sealed Build Requirement:**

> The compiler MUST produce byte-identical canonical payloads (`.kpkg`) for identical authoring source inputs (KPSS JSON + PNG), compiler configuration, and signing key.

Determinism must be enforced across all sub-processes: image processing math, BlurHash generation, hashing, and cryptography.

---

### Phase 4 Specification Locked v1.0 (Implementation Phase)