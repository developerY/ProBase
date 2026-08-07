# KoColor Canonical Product Schema (KCPS) v2

This document defines the strict data contract for KoColor Distribution Packages (`.kpkg`). Every item in a package MUST conform to this schema to be accepted by a KCPS-compliant package ingestion pipeline.

**KCPS Version**: 2  
**`schema_version`**: 2

## 🏗 Package Layout

A compliant `.kpkg` binary, when decompressed, MUST contain a JSON object with the following top-level structure:

```json
{
  "version": 1,
  "cosmetics": [ ... ],
  "clothing": [ ... ]
}
```

---

## 1. General Invariants

- **String Encoding**: All strings MUST be UTF-8 encoded.
- **Identifiers (`id`)**: MUST be lowercase ASCII, hyphen-separated, immutable, and globally unique.
- **URLs**: MUST use the HTTPS protocol.
- **Color Formatting**: All hex colors MUST use the `#RRGGBB` format (e.g., `#FF0000`).
- **Enumerations**: Values MUST exactly match the uppercase names defined in this specification. Unknown or malformed enum values MUST cause the package to fail validation.
- **Arrays**: MUST NOT contain null values.
- **Object Keys**: MUST be unique within a JSON object.

## 2. Compatibility Rules

- **Forward Compatibility**: Adding optional fields is backward compatible. Clients MUST ignore unknown fields.
- **Breaking Changes**: Removing required fields or renaming existing fields requires a new schema version.
- **Enum Evolution**: Existing enum values MUST NOT change meaning. New enum values MAY only be added in a new schema version.

---

## 3. Data Domains

### 3.1 Identity & Classification

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `id` | ✓ | `String` | Unique identifier (e.g., `kc-mac-ruby-woo`). |
| `name` | ✓ | `String` | Full commercial product name. |
| `brand` | ✓ | `String` | Manufacturer name. |
| `macro_category` | ✓ | `String` | High-level group. See [Reference Enums](#enums). |
| `micro_category` | ✓ | `String` | Specific item type. See [Reference Enums](#enums). |

### 3.2 Professional Facets (AI Styling)

These fields are **strictly validated** for Cosmetic items.

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `formulation` | Optional | `String?` | Physical form (e.g., `LIQUID`, `CREAM`). |
| `chemistry_base`| Optional | `String?` | Primary base (e.g., `WATER`, `SILICONE`). |
| `finish` | Optional | `String?` | Visual effect (e.g., `MATTE`, `SATIN`). |
| `coverage` | Optional | `String?` | Opacity level (e.g., `SHEER`, `MEDIUM`). |
| `temperature` | Optional | `String?` | Perceptual undertone (e.g., `WARM`, `COOL`). |

### 3.3 Visuals & UI

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `color_hex` | ✓ | `String` | Canonical display color in `#RRGGBB` format. |
| `shade_name` | Optional | `String?` | The marketing color name (e.g., "Ruby Woo"). |
| `image_url` | ✓ | `String` | High-resolution product image URL (HTTPS). |
| `thumbnail_url` | ✓ | `String` | 256x256 optimized thumbnail URL (HTTPS). |

### 3.4 Logistics & Metadata

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `price` | Optional | `Double?` | MSRP price for Cost-Per-Use calculation. |
| `volume` | Optional | `String?` | Net weight/volume (e.g., "30ml", "15g"). |
| `pao_months` | Optional | `Integer?` | Period After Opening (months). |
| `expiry_date` | Optional | `Long?` | Hard expiration timestamp (Unix millis). |
| `is_fda_checked` | ✓ | `Boolean` | Verified against clinical safety datasets. |

### 3.5 Algorithmic & AI Insights

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `hero_ingredient` | Optional | `String?` | Primary active component. |
| `skin_compatibility`| Optional | `String?` | Target skin type. |
| `ingredients` | ✓ | `Array<String>`| Ordered per the manufacturer's published list. |
| `allergens` | ✓ | `Array<String>`| Known safety triggers. |
| `is_vegan` | Optional | `Boolean?` | Contains no animal products. |
| `is_cruelty_free` | Optional | `Boolean?` | Not tested on animals. |

### 3.6 Wardrobe Metadata (Clothing Only)

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `formality` | Optional | `String?` | Attire level (e.g., `CASUAL`, `PROFESSIONAL`). |
| `material` | Optional | `String?` | Primary fabric type (e.g., `SILK`, `COTTON`). |

---

## 4. Validation & Rejection Rules

A package (`.kpkg`) MUST be rejected if:
1. A **Required** field is missing from any item.
2. A field type does not match the **Wire Type** specified.
3. An enum field contains a value not present in the [Reference Enums](#enums).
4. The `schema_version` of the package is unsupported by the client.
5. Duplicate `id` values exist within the same package.
6. The `id` or `name` of any item is empty or whitespace only.
7. Any array contains `null` values.
8. JSON object keys are not unique.

---

<a name="enums"></a>
## Appendix A: Reference Enums

### Macro Categories (Complete List)
- **Cosmetic**: `PREP`, `COMPLEXION`, `DIMENSION`, `EYES`, `LIPS`, `NAILS`, `HAIR`, `HYGIENE`, `ORAL`, `FRAGRANCE`, `GROOMING`, `TOOLS`
- **Clothing**: `TOPS`, `BOTTOMS`, `OUTERWEAR`, `DRESSES`, `ACCESSORIES`

### Professional Facets (Representative Examples)
- **Formulation**: `LIQUID`, `CREAM`, `POWDER`, `GEL`, `BALM`, `PENCIL`, `SPRAY`, `STICK`
- **Finish**: `MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT`, `GLOSSY`
- **Chemistry Base**: `WATER`, `SILICONE`, `OIL`, `WAX`, `ALCOHOL`
- **Coverage**: `SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE`
- **Temperature**: `WARM`, `COOL`, `NEUTRAL`, `OLIVE`
- **Formality**: `LOUNGE`, `CASUAL`, `SMART_CASUAL`, `PROFESSIONAL`, `FORMAL`, `GALA`
