# Canonical KoColor Product Schema (v2)

This document defines the strict data contract for KoColor Distribution Packages (`.kpkg`). Every item in a package MUST adhere to this schema to be successfully verified and ingested by the Android mobile hub.

## 1. Identity & Classification

| Field | Rust Type | Android Type | Description |
| :--- | :--- | :--- | :--- |
| `id` | `String` | `String` | Unique identifier slug (e.g., `kc-mac-001`). |
| `name` | `String` | `String` | Full commercial product name. |
| `brand` | `String` | `String` | Manufacturer name. |
| `macro_category` | `String` | `MacroCategory` | High-level group. See [Enums](#enums). |
| `micro_category` | `String` | `MicroCategory` | Specific item type. See [Enums](#enums). |

## 2. Professional Facets (AI Styling Engine)

These fields are **strictly validated**. Values must exactly match the uppercase Enum names in the domain layer.

| Field | Enum Type | Valid Values (Examples) |
| :--- | :--- | :--- |
| `formulation` | `Formulation` | `LIQUID`, `CREAM`, `POWDER`, `GEL`, `STICK` |
| `chemistry_base`| `ChemistryBase` | `WATER`, `SILICONE`, `OIL`, `WAX`, `ALCOHOL` |
| `finish` | `Finish` | `MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT` |
| `coverage` | `Coverage` | `SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE` |
| `temperature` | `Temperature` | `WARM`, `COOL`, `NEUTRAL`, `OLIVE` |

## 3. Visuals & UI

| Field | Type | Description |
| :--- | :--- | :--- |
| `color_hex` | `String` | Primary marketing color in hex (e.g., `#FF0000`). |
| `shade_name` | `Option<String>` | The color name (e.g., "Ruby Woo"). |
| `image_url` | `String` | High-resolution product image URL. |
| `thumbnail_url` | `String` | **(REQUIRED)** 256x256 optimized thumbnail for list views. |

## 4. Professional Inventory & Logistics

| Field | Type | Description |
| :--- | :--- | :--- |
| `price` | `Option<f64>` | MSRP price for Cost-Per-Use calculation. |
| `volume` | `Option<String>` | Net weight/volume (e.g., "30ml", "15g"). |
| `pao_months` | `Option<u32>` | Period After Opening (months). |
| `expiry_date` | `Option<u64>` | Hard expiration timestamp (Unix millis). |

## 5. Algorithmic & AI Insights

| Field | Type | Description |
| :--- | :--- | :--- |
| `hero_ingredient` | `Option<String>` | Primary active component (e.g., "Hyaluronic Acid"). |
| `skin_compatibility`| `Option<String>` | Target skin type (e.g., "Sensitive", "Oily"). |
| `ingredients` | `Vec<String>` | Full list of INCI ingredients. |
| `allergens` | `Vec<String>` | Safety triggers. |

## 6. Safety & Ethics

| Field | Type | Description |
| :--- | :--- | :--- |
| `is_vegan` | `Option<bool>` | Contains no animal products. |
| `is_cruelty_free` | `Option<bool>` | Not tested on animals. |
| `is_fda_checked` | `bool` | Verified against clinical safety datasets. |

---

<a name="enums"></a>
## Appendix: Reference Enums

### Macro Categories
`PREP`, `COMPLEXION`, `DIMENSION`, `EYES`, `LIPS`, `NAILS`, `HAIR`, `HYGIENE`, `ORAL`, `FRAGRANCE`, `GROOMING`, `TOOLS`

### Micro Categories (Partial List)
`CLEANSER`, `SERUM`, `SPF`, `PRIMER`, `FOUNDATION`, `CONCEALER`, `BLUSH`, `EYESHADOW`, `MASCARA`, `LIPSTICK`

---

## 🚀 Implementation Tip (Rust)

When defining products in your inventory modules (e.g., `lips.rs`), ensure you use the `.to_string()` method on the uppercase Enum values to match the Android expectations exactly.

**Correct Example:**
```rust
CosmeticItem {
    macro_category: "LIPS".to_string(),
    micro_category: "LIPSTICK".to_string(),
    formulation: "STICK".to_string(),
    // ...
}
```
