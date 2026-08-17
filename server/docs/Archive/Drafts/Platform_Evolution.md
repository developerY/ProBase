# KoColor System Architecture & Taxonomy Evolution Specification

## Executive Summary

This document serves as the authoritative technical record tracking the evolution of the **KoColor Product Schema & Taxonomy System**. It details the original architectural specification (**Glow Archive Taxonomy**), the current baseline standard (**KoColor Canonical Product Schema - KCPS v1**), and the explicit engineering rationales for the transitional modifications.

---

## 1. Original Architectural Specification: Glow Archive Taxonomy

The original Glow Archive Taxonomy was conceptualized as a three-tier hierarchical framework designed to bridge client-side UI navigation with an advanced, real-time algorithmic calculation engine.

### 1.1 The Three-Tier Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ Level 1: Macro Categories (UI Navigation & Spatial Mapping)  │
├─────────────────────────────────────────────────────────────┤
│ Level 2: Micro Categories (Format & Search Entity Index)    │
├─────────────────────────────────────────────────────────────┤
│ Level 3: Professional Facets (Physics, Chemistry & Color)   │
└─────────────────────────────────────────────────────────────┘

```

1. **Level 1: Macro Categories (UI Navigation Layer)**
* **Scope**: 6 Spatial and functional buckets (`PREP`, `COMPLEXION`, `DIMENSION`, `EYES`, `LIPS`, `TOOLS`).
* **Function**: Anatomic zone mapping and top-level user interface routing.


2. **Level 2: Micro Categories (Product Classification Layer)**
* **Scope**: Standardized product type formats (e.g., `PRIMER`, `FOUNDATION`, `CONTOUR`, `EYESHADOW`, `LIPSTICK`).
* **Function**: Product catalog indexing and granular search resolution.


3. **Level 3: Professional Facets (Algorithmic Engine Layer)**
* **Scope**: Physical, chemical, and optical attributes comprising 5 required facets:
* **Formulation**: Physical state (`LIQUID`, `CREAM`, `POWDER`, `GEL`, `BALM`, `STICK`, `SPRAY`).
* **Chemistry Base**: Dominant phase (`WATER`, `SILICONE`, `OIL`, `WAX`, `ALCOHOL`).
* **Finish**: Optical specular reflectance (`MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT`, `GLOSSY`).
* **Coverage**: Opacity level (`SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE`).
* **Temperature**: Perceptual chromatic bias (`WARM`, `COOL`, `NEUTRAL`, `OLIVE`).





### 1.2 Theoretical Physics & Colorimetry Foundations

* **Interfacial Chemistry & Pilling Prevention**: Designed to calculate interfacial surface tension mismatches ($\gamma > 10 \text{ mN/m}$) between adjacent layers (e.g., applying a hydrophilic water-based product directly over a hydrophobic siloxane film) to prevent film fracture and cosmetic pilling.
* **CIELAB Colorimetry & Neutral Equilibrium**: Defined product undertones using CIELAB ($L^*a^*b^*$) color coordinates. Specifically, a true neutral red was defined mathematically by a hue angle ($h_{ab}$) target of $25^\circ$:

$$h_{ab} = \arctan\left(\frac{b^*}{a^*}\right) \times \left(\frac{180}{\pi}\right) = 25^\circ$$



---

## 2. Current Specification: KoColor Canonical Product Schema (KCPS v1)

The current production baseline is strictly standardized on **KCPS Version 1** (`schema_version = 1`, `package_format_version = 1`).

### 2.1 Core Schema Rules & Invariants

* **Version Protocol**: Strictly locked to `1`. Multi-version fallback logic and legacy migration code are prohibited during active pre-release development.
* **Polymorphic Collection Structure**: Payload objects must expose two top-level arrays: `cosmetics` and `clothing`.

```json
{
  "schema_version": 1,
  "cosmetics": [ ... ],
  "clothing": [ ... ]
}

```

### 2.2 Entity Field Contract

| Field | Required | Type | Validation / Constraints |
| --- | --- | --- | --- |
| `id` | **Yes** | `String` | Lowercase ASCII, hyphen-separated, globally unique (max 128 bytes). |
| `name` | **Yes** | `String` | Commercial name (max 512 bytes). |
| `brand` | **Yes** | `String` | Manufacturer name (max 256 bytes). |
| `macro_category` | **Yes** | `String` | Exact match to Reference Enums (Uppercase). |
| `micro_category` | **Yes** | `String` | Exact match to Reference Enums (Uppercase). |
| `color_hex` | **Yes** | `String` | Must match regex `^#[0-9A-Fa-f]{6}$`. |
| `shade_name` | No | `String?` | Optional marketing shade designation. |
| `image_url` | **Yes** | `String` | HTTPS URL to full-resolution asset. |
| `thumbnail_url` | **Yes** | `String` | HTTPS URL to 256x256 optimized image asset. |
| `price` | No | `Double?` | $\ge 0.0$ MSRP value. |
| `formulation` | No | `String?` | Uppercase enum (`LIQUID`, `CREAM`, `POWDER`, `GEL`, `STICK`, etc.). |
| `chemistry_base` | No | `String?` | Uppercase enum (`WATER`, `SILICONE`, `OIL`, `WAX`, `ALCOHOL`). |
| `finish` | No | `String?` | Uppercase enum (`MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT`, `GLOSSY`). |
| `coverage` | No | `String?` | Uppercase enum (`SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE`). |
| `temperature` | No | `String?` | Uppercase enum (`WARM`, `COOL`, `NEUTRAL`, `OLIVE`). |
| `ingredients` | **Yes** | `List<String>` | Full INCI list. |
| `allergens` | **Yes** | `List<String>` | Explicit triggers list (empty array `[]` allowed, null prohibited). |
| `fda_data_verified` | **Yes** | `Boolean` | Clinical safety verification state. |

---

## 3. Why and How the Specification Changed

The evolution from the theoretical **Glow Archive Taxonomy** to **KCPS v1** was driven by software production realities, network bandwidth limits, and B2B partner integration requirements.

```
       THEORETICAL SPEC                        PRODUCTION REALITY (KCPS v1)
┌──────────────────────────────┐              ┌──────────────────────────────┐
│  • 6 Macro Categories        │              │  • 12 Macro Categories       │
│  • Mandatory Facets          │  ─────────►  │  • Optional Facet DTOs       │
│  • On-Device CIELAB Engine   │              │  • "Compute at Compile Time" │
│  • Hardcoded Data Structs    │              │  • Decoupled CLI Compiler    │
└──────────────────────────────┘              └──────────────────────────────┘

```

### 3.1 Taxonomy Category Expansion (Future-Proofing)

* **Why**: The original 6 macro categories were restricted strictly to color cosmetics and basic face prep. To support full-spectrum ecosystem growth, the taxonomy was expanded.
* **How**: Added 6 new Macro Categories: `NAILS`, `HAIR`, `HYGIENE`, `ORAL`, `FRAGRANCE`, `GROOMING`, along with expanded clothing categories (`TOPS`, `BOTTOMS`, `SHOES`, `ACCESSORIES`, `OTHER`).

### 3.2 Optional Facet DTOs vs. Strict Internal Domain Models

* **Why (Postel's Law)**: The original specification mandated that *every* product exhibit all 5 Professional Facets. In a real-world B2B environment, third-party vendor data drops often lack comprehensive metadata (e.g., missing an explicit `temperature` or `chemistry_base`). Hard-failing the ingestion parser would break the pipeline.
* **How**: On the wire, serialization DTOs treat Level 3 facets as optional (`Option<String>` in Rust, `String?` in Kotlin). However, internal KoColor **Benchmark Core Collections** retain 100% facet population to preserve calculation accuracy.

### 3.3 Shift to "Compute at Compile Time"

* **Why**: Executing realtime $RGB \to \text{Linear RGB} \to XYZ \to \text{CIELAB}$ matrix transformations and thermodynamic surface tension calculations on mobile devices introduces frame drops and consumes battery during UI scrolling.
* **How**: The architectural burden was shifted to the **Rust Normalization Compiler (`kocolor-compiler`)**. During `.kpkg` compilation, Rust pre-calculates the chemistry phase and CIELAB colorimetry coordinates and silently injects them into the serialized payload:

```json
{
  "id": "kc-starter-lips-01",
  "name": "Signature Crimson Lip Color",
  "color_hex": "#A81C28",
  "chemistry_base": "OIL",
  "calculated_chemistry_phase": "LIPOPHILIC_LIPID",
  "calculated_cielab": {
    "l": 38.5,
    "a": 52.1,
    "b": 24.3,
    "hue_angle_hab": 25.0
  }
}

```

### 3.4 Decoupling Compiler Logic from Catalog Data

* **Why**: The initial repository structure compiled product definitions directly into Rust source files (`src/inventory/cosmetics/*.rs`). This created an unscalable "code-as-data" bottleneck requiring code re-compilation for catalog updates.
* **How**: Deleted the `inventory` directory. The Rust toolchain was transformed into a pure **CLI Compiler Pipeline** that ingests arbitrary JSON documents from an `/input_packs/` folder, canonicalizes the data, compresses it with Zstandard, hashes it via SHA-256, signs it via Ed25519, and outputs deterministic `.kpkg` binaries alongside signed `manifest.json` files.

---

## 4. Architectural Summary Matrix

| Metric / Dimension | Original Glow Archive Spec | Current KCPS v1 Implementation |
| --- | --- | --- |
| **Schema Version** | Unversioned / Conceptual | Immutable `1` |
| **Macro Categories** | 6 (Cosmetics Only) | 12 Cosmetics + 5 Clothing Categories |
| **Wire Facet Mandate** | Strict (Missing facet fails parser) | Flexible (Wire nullable, internal core 100% populated) |
| **Color Math Location** | On-device Mobile Runtime | Pre-computed at compile time by Rust |
| **Compiler Data Source** | Hardcoded Rust source modules | Dynamic Directory Reader (`/input_packs/*.json`) |
| **Artifact Packaging** | Unspecified JSON payloads | Zstd-compressed, SHA-256 addressed, Ed25519 signed `.kpkg` |