# KoColor Product Source Schema (KPSS) v1

This document defines the minimalist authoring contract for raw product data. The **KoColor Normalization Compiler** ingests KPSS-compliant JSON files and high-res PNGs to generate the final distribution artifacts.

**KPSS Version**: 1  
**`schema_version`**: 1

---

## 1. Design Philosophy

The KPSS is designed for **Human/AI Authors**. It contains only authoritative semantic identity and raw asset paths. It MUST NOT contain any generated artifacts (BlurHashes, CDN URLs, or pre-calculated math).

---

## 2. Authoring Structure

Every product is authored as a standalone JSON object. The compiler traverses the `raw_assets/` directory to discover these files.

### 2.1 Cosmetic Source Item

| Field | Required | Type | Description |
| :--- | :--- | :--- | :--- |
| `schema_version` | ✓ | `Integer` | Must be 1. |
| `id` | ✓ | `String` | Semantic ID (e.g., `kc-prep-01`). |
| `brand` | ✓ | `String` | Commercial brand name. |
| `macro_category` | ✓ | `String` | Upper-case category (e.g., `LIPS`). |
| `micro_category` | ✓ | `String` | Upper-case product type (e.g., `LIPSTICK`). |
| `color_hex` | ✓ | `String` | Exact hex code (e.g., `#A81C28`). |
| `shade_name` | ✓ | `String` | Commercial shade name. |
| `raw_image_input` | ✓ | `Path` | Relative path to the raw high-res PNG. |

### 2.2 Clothing Source Item

| Field | Required | Type | Description |
| :--- | :--- | :--- | :--- |
| `schema_version` | ✓ | `Integer` | Must be 1. |
| `id` | ✓ | `String` | Semantic ID. |
| `brand` | ✓ | | Authoritative brand name. |
| `macro_category` | ✓ | `String` | e.g., `TOPS`. |
| `micro_category` | ✓ | `String` | e.g., `TOPS`. |
| `color_hex` | ✓ | `String` | Primary display color. |
| `raw_image_input` | ✓ | `Path` | Relative path to raw asset. |

---

## 3. Directory Authority Rule

The physical directory structure (e.g., `raw_assets/KoColor/LIPS/`) is for **organization only**. 
*   The compiler **MUST NOT** infer categories or brands from folder names.
*   The JSON payload is the **only** authoritative source for product metadata.

---

## 4. Transformation Loop

The compiler performs the following on every KPSS input:
1.  **Validate**: Ensure all KPSS required fields are present.
2.  **Enrich**: Calculate CIELAB, Chemistry Phases, and Safety Flags.
3.  **Optimize**: Transform `raw_image_input` (PNG) into WebP Hero/Thumbnail.
4.  **Inject**: Populate `blurhash` and `image_url` fields.
5.  **Output**: Write the canonical **KCPS v1** object to the package.
