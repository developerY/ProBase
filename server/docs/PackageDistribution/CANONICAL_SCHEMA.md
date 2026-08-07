# KoColor Canonical Product Schema (KCPS) v2

This document defines the strict data contract for KoColor Distribution Packages (`.kpkg`). Every item in a package MUST adhere to this schema to be successfully verified and ingested by the Android mobile hub.

## General Invariants

- **String Encoding**: All strings MUST be UTF-8 encoded.
- **Identifiers (`id`)**: MUST be lowercase ASCII, hyphen-separated, immutable, and globally unique.
- **URLs**: MUST use the HTTPS protocol.
- **Color Formatting**: All hex colors MUST use the `#RRGGBB` format (e.g., `#FF0000`).
- **Enumerations**: Values MUST exactly match the uppercase names defined in the schema. Unknown or malformed enum values MUST cause the package to fail validation.

## Compatibility Rules

- **Forward Compatibility**: Adding optional fields is backward compatible. Clients MUST ignore unknown fields.
- **Breaking Changes**: Removing required fields or renaming existing fields requires a new schema version.
- **Enum Evolution**: New enum values MAY only be added in a new schema version.

---

## 1. Identity & Classification

| Field | Required | Wire Type | Android Type | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | ✓ | `String` | `String` | Unique identifier (e.g., `kc-mac-ruby-woo`). |
| `name` | ✓ | `String` | `String` | Full commercial product name. |
| `brand` | ✓ | `String` | `String` | Manufacturer name. |
| `macro_category` | ✓ | `String` | `MacroCategory` | High-level group. See [Enums](#enums). |
| `micro_category` | ✓ | `String` | `MicroCategory` | Specific item type. See [Enums](#enums). |

## 2. Professional Facets (AI Styling Engine)

These fields are **strictly validated**. Values must exactly match the uppercase Enum names in the domain layer.

| Field | Required | Enum Type | Valid Values (Illustrative) |
| :--- | :--- | :--- | :--- |
| `formulation` | ✓ | `Formulation` | `LIQUID`, `CREAM`, `POWDER`, `GEL`, `STICK` |
| `chemistry_base`| ✓ | `ChemistryBase` | `WATER`, `SILICONE`, `OIL`, `WAX`, `ALCOHOL` |
| `finish` | ✓ | `Finish` | `MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT` |
| `coverage` | ✓ | `Coverage` | `SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE` |
| `temperature` | ✓ | `Temperature` | `WARM`, `COOL`, `NEUTRAL`, `OLIVE` |

## 3. Visuals & UI

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `color_hex` | ✓ | `String` | Canonical display color in `#RRGGBB` format. |
| `shade_name` | Optional | `String?` | The marketing color name (e.g., "Ruby Woo"). |
| `image_url` | ✓ | `String` | High-resolution product image URL (HTTPS). |
| `thumbnail_url` | ✓ | `String` | 256x256 optimized thumbnail for list views (HTTPS). |

## 4. Professional Inventory & Logistics

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `price` | Optional | `Double?` | MSRP price for Cost-Per-Use calculation. |
| `volume` | Optional | `String?` | Net weight/volume (e.g., "30ml", "15g"). |
| `pao_months` | Optional | `Integer?` | Period After Opening (months). |
| `expiry_date` | Optional | `Long?` | Hard expiration timestamp (Unix millis). |

## 5. Algorithmic & AI Insights

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `hero_ingredient` | Optional | `String?` | Primary active component (e.g., "Hyaluronic Acid"). |
| `skin_compatibility`| Optional | `String?` | Target skin type (e.g., "Sensitive", "Oily"). |
| `ingredients` | ✓ | `Array<String>`| Ordered per the manufacturer's published list. |
| `allergens` | ✓ | `Array<String>`| Known safety triggers (e.g., "Gluten", "Soy"). |

## 6. Safety & Ethics

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `is_vegan` | Optional | `Boolean?` | Contains no animal products. |
| `is_cruelty_free` | Optional | `Boolean?` | Not tested on animals. |
| `is_fda_checked` | ✓ | `Boolean` | Verified against clinical safety datasets. |

---

## Validation & Rejection Rules

A package (`.kpkg`) MUST be rejected if:
1. A **Required** field is missing from any item.
2. A field type does not match the **Wire Type** specified.
3. An enum field contains a value not present in the [Reference Enums](#enums).
4. The `schema_version` of the package is unsupported by the client.
5. Duplicate `id` values exist within the same package.
6. The `id` or `name` of any item is empty or whitespace only.

---

<a name="enums"></a>
## Appendix A: Reference Enums

### Macro Categories (Complete List)
`PREP`, `COMPLEXION`, `DIMENSION`, `EYES`, `LIPS`, `NAILS`, `HAIR`, `HYGIENE`, `ORAL`, `FRAGRANCE`, `GROOMING`, `TOOLS`

### Micro Categories (Illustrative Examples)
`CLEANSER`, `SERUM`, `SPF`, `PRIMER`, `FOUNDATION`, `CONCEALER`, `BLUSH`, `EYESHADOW`, `MASCARA`, `LIPSTICK`

---

## Appendix B: Rust Compiler Notes

When defining products in your inventory modules (e.g., `lips.rs`), ensure you use the `.to_string()` method on the uppercase Enum values to match the expected wire format exactly.

**Correct Example:**
```rust
CosmeticItem {
    macro_category: "LIPS".to_string(),
    micro_category: "LIPSTICK".to_string(),
    formulation: "STICK".to_string(),
    // ...
}
```
