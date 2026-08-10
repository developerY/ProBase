# KoColor Canonical Product Schema (KCPS) v1

This document defines the strict data contract for KoColor Distribution Packages (`.kpkg`). Every item in a package MUST conform to this schema to be accepted by a KCPS-compliant package ingestion pipeline.

**KCPS Version**: 1  
**`schema_version`**: 1

After successful verification and decompression, every `.kpkg` package MUST produce exactly one JSON document conforming to this schema.

## Decompressed Payload Format

A compliant `.kpkg` payload MUST contain a JSON object with the following top-level structure:

```json
{
  "schema_version": 1,
  "cosmetics": [ ... ],
  "clothing": [ ... ]
}
```

### Payload Invariants

- **Exactly one root JSON object**.
- **`cosmetics` and `clothing` MUST exist** as top-level arrays.
- **Arrays MAY be empty**.
- **Unknown top-level fields MUST be ignored** by the consumer.
- **Duplicate `id` values MUST NOT exist anywhere** in the package, regardless of whether the items are in the cosmetics or clothing arrays.
- **Trailing Bytes**: The decompressed payload MUST contain no trailing non-whitespace bytes after the final closing brace.

---

## 1. General Invariants

### 1.1 String Encoding & Constraints
- **Encoding**: All strings MUST be UTF-8 encoded.
- **Identifiers (`id`)**: MUST be lowercase ASCII, hyphen-separated, immutable, and globally unique. 
    - **Max Length**: 128 UTF-8 bytes.
- **Commercial Strings (`name`, `brand`)**: MUST NOT be empty or whitespace only.
    - **Max Length (`name`)**: 512 UTF-8 bytes.
    - **Max Length (`brand`)**: 256 UTF-8 bytes.
- **URLs**: MUST use the HTTPS protocol and MUST NOT be empty.
- **Color Formatting**: All hex colors MUST exactly match the regex `^#[0-9A-Fa-f]{6}$` (e.g., `#FF0000`).

### 1.2 Numeric Constraints
- **Prices (`price`)**: MUST be `>= 0.0`. MUST NOT be `NaN` or `Infinity`.
- **Duration/Timestamps (`pao_months`, `expiry_date`)**: MUST be `> 0` when present.

### 1.3 Structural Rules
- **Enumerations**: Values MUST exactly match the uppercase names defined in this specification. Unknown or malformed enum values MUST cause the package to fail validation.
    - **Enum Philosophy**: Enum values are part of the wire protocol and are case-sensitive. Their serialized names MUST remain stable across compiler implementations.
- **Arrays**: MUST NOT contain null values.
- **Object Keys**: MUST be unique within a JSON object. Implementations MUST explicitly reject documents with duplicate keys.

---

## 2. Item Type Definitions

Items are discriminated by the top-level array they reside in. Each type has a specific allowed field set.

### 2.1 Common Fields (All Items)

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `id` | ✓ | `String` | Global unique identifier. |
| `name` | ✓ | `String` | Full commercial product name. |
| `brand` | ✓ | `String` | Manufacturer name. |
| `macro_category` | ✓ | `String` | High-level group. See [Reference Enums](#enums). |
| `micro_category` | ✓ | `String` | Specific item type. See [Reference Enums](#enums). |
| `color_hex` | ✓ | `String` | Canonical display color in `#RRGGBB` format. |
| `shade_name` | Optional | `String?` | The marketing color name. |
| `image_url` | ✓ | `String` | High-resolution product image URL (HTTPS). |
| `thumbnail_url` | ✓ | `String` | 256x256 optimized thumbnail URL (HTTPS). |
| `price` | Optional | `Double?` | MSRP price. |
| `notes` | Optional | `String?` | Short editorial description. |

### 2.2 Cosmetic Items

Reside in the `cosmetics` array. **Cloth# KoColor Canonical Product Schema (KCPS) v1

This document defines the strict data contract for KoColor Distribution Packages (`.kpkg`). Every item in a package MUST conform to this schema to be accepted by a KCPS-compliant package ingestion pipeline.

**KCPS Version**: 1  
**`schema_version`**: 1

After successful verification and decompression, every `.kpkg` package MUST produce exactly one JSON document conforming to this schema.

## Decompressed Payload Format

A compliant `.kpkg` payload MUST contain a JSON object with the following top-level structure:

```json
{
  "schema_version": 1,
  "cosmetics": [ ... ],
  "clothing": [ ... ]
}
```

### Payload Invariants

- **Exactly one root JSON object**.
- **`cosmetics` and `clothing` MUST exist** as top-level arrays.
- **Arrays MAY be empty**.
- **Unknown top-level fields MUST be ignored** by the consumer.
- **Duplicate `id` values MUST NOT exist anywhere** in the package, regardless of whether the items are in the cosmetics or clothing arrays.
- **Trailing Bytes**: The decompressed payload MUST contain no trailing non-whitespace bytes after the final closing brace.

---

## 1. General Invariants

### 1.1 String Encoding & Constraints
- **Encoding**: All strings MUST be UTF-8 encoded.
- **Identifiers (`id`)**: MUST be lowercase ASCII, hyphen-separated, immutable, and globally unique.
  - **Max Length**: 128 UTF-8 bytes.
- **Commercial Strings (`name`, `brand`)**: MUST NOT be empty or whitespace only.
  - **Max Length (`name`)**: 512 UTF-8 bytes.
  - **Max Length (`brand`)**: 256 UTF-8 bytes.
- **URLs**: MUST use the HTTPS protocol and MUST NOT be empty.
- **Color Formatting**: All hex colors MUST exactly match the regex `^#[0-9A-Fa-f]{6}$` (e.g., `#FF0000`).

### 1.2 Numeric Constraints
- **Prices (`price`)**: MUST be `>= 0.0`. MUST NOT be `NaN` or `Infinity`.
- **Duration/Timestamps (`pao_months`, `expiry_date`)**: MUST be `> 0` when present.

### 1.3 Structural Rules
- **Enumerations**: Values MUST exactly match the uppercase names defined in this specification. Unknown or malformed enum values MUST cause the package to fail validation.
  - **Enum Philosophy**: Enum values are part of the wire protocol and are case-sensitive. Their serialized names MUST remain stable across compiler implementations.
- **Arrays**: MUST NOT contain null values.
- **Object Keys**: MUST be unique within a JSON object. Implementations MUST explicitly reject documents with duplicate keys.

---

## 2. Item Type Definitions

Items are discriminated by the top-level array they reside in. Each type has a specific allowed field set.

### 2.1 Common Fields (All Items)

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `id` | ✓ | `String` | Global unique identifier. |
| `name` | ✓ | `String` | Full commercial product name. |
| `brand` | ✓ | `String` | Manufacturer name. |
| `macro_category` | ✓ | `String` | High-level group. See [Reference Enums](#enums). |
| `micro_category` | ✓ | `String` | Specific item type. See [Reference Enums](#enums). |
| `color_hex` | ✓ | `String` | Canonical display color in `#RRGGBB` format. |
| `shade_name` | Optional | `String?` | The marketing color name. |
| `image_url` | ✓ | `String` | High-resolution product image URL (HTTPS). |
| `thumbnail_url` | ✓ | `String` | 256x256 optimized thumbnail URL (HTTPS). |
| `price` | Optional | `Double?` | MSRP price. |
| `notes` | Optional | `String?` | Short editorial description. |

### 2.2 Cosmetic Items

Reside in the `cosmetics` array. **Clothing-only fields MUST NOT appear on cosmetic items.**

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `formulation` | Optional | `String?` | Physical form (e.g., `LIQUID`, `CREAM`). |
| `chemistry_base`| Optional | `String?` | Primary base (e.g., `WATER`, `SILICONE`). |
| `finish` | Optional | `String?` | Visual effect (e.g., `MATTE`, `SATIN`). |
| `coverage` | Optional | `String?` | Opacity level (e.g., `SHEER`, `MEDIUM`). |
| `temperature` | Optional | `String?` | Perceptual undertone (e.g., `WARM`, `COOL`). |
| `volume` | Optional | `String?` | Net weight/volume (e.g., "30ml"). |
| `pao_months` | Optional | `Integer?` | Period After Opening (months). |
| `expiry_date` | Optional | `Long?` | Hard expiration timestamp (Unix millis). |
| `instructions` | Optional | `String?` | Manufacturer usage instructions. |
| `ingredients` | ✓ | `Array<String>`| Ordered per the manufacturer's published list. |
| `allergens` | ✓ | `Array<String>`| Known safety triggers. |
| `is_vegan` | Optional | `Boolean?` | Contains no animal products. |
| `is_cruelty_free` | Optional | `Boolean?` | Not tested on animals. |
| `fda_data_verified`| ✓ | `Boolean` | Verified against clinical safety datasets. |

### 2.3 Clothing Items

Reside in the `clothing` array. **Cosmetic-only fields MUST NOT appear on clothing items.**

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `formality` | Optional | `String?` | Attire level (e.g., `CASUAL`, `PROFESSIONAL`). |
| `material` | Optional | `String?` | Primary fabric type (e.g., `SILK`, `COTTON`). |

---

## 3. Serialization & Compatibility

### 3.1 Canonical Serialization
The **Rust Normalization Compiler** is the sole authoritative KCPS serializer. Field ordering, UTF-8 encoding, escaping, numeric representation, and omission of optional fields MUST be deterministic to ensure stable `.kpkg` hashes.

### 3.2 Compatibility Rules
- **Forward Compatibility**: Adding optional fields is backward compatible. Clients MUST ignore unknown fields.
- **Breaking Changes**: Removing required fields or renaming existing fields requires a new schema version.
- **Versioning**:
  - `package_format_version`: Defines the `.kpkg` binary/container protocol.
  - `schema_version`: Defines the decompressed KCPS JSON data contract.

---

## 4. Validation & Rejection Rules

A package (`.kpkg`) MUST be rejected if:
1. A **Required** field is missing from any item.
2. A field type does not match the **Wire Type** specified.
3. An enum field contains a value not present in the [Reference Enums](#enums).
4. The `schema_version` of the package is unsupported by the client. Clients MUST reject packages whose `schema_version` exceeds the maximum supported version.
5. Duplicate `id` values exist anywhere in the package.
6. The `id` or `name` of any item is empty or whitespace only.
7. Any array contains `null` values.
8. JSON object keys are not unique.

---

<a name="enums"></a>
## Appendix A: Reference Enums

### Macro Categories (Complete List)
- **Cosmetic**: `PREP`, `COMPLEXION`, `DIMENSION`, `EYES`, `LIPS`, `NAILS`, `HAIR`, `HYGIENE`, `ORAL`, `FRAGRANCE`, `GROOMING`, `TOOLS`
- **Clothing**: `TOPS`, `BOTTOMS`, `SHOES`, `ACCESSORIES`, `OTHER`

### Micro Categories (Complete List)

**Cosmetic Categories:**
`CLEANSER`, `TONER`, `SERUM`, `MOISTURIZER`, `SPF`, `PRIMER`, `FACE_MASK`, `EXFOLIANT`, `EYE_CARE`, `LIP_CARE`, `FOUNDATION`, `BB_CC_CREAM`, `CONCEALER`, `COLOR_CORRECTOR`, `SETTING_POWDER`, `FACE_POWDER`, `SETTING_SPRAY`, `BLUSH`, `BRONZER`, `CONTOUR`, `HIGHLIGHTER`, `FRECKLE_TINT`, `EYESHADOW`, `EYELINER`, `MASCARA`, `LASH_PRIMER`, `BROW_PENCIL`, `BROW_GEL`, `FALSE_LASHES`, `LIPSTICK`, `LIP_GLOSS`, `LIP_LINER`, `LIP_TINT_STAIN`, `LIP_BALM`, `LIP_PLUMPER`

**Clothing Categories:**
`TOPS`, `BOTTOMS`, `SHOES`, `ACCESSORIES`, `OTHER`

### Professional Facets (Representative Examples)
- **Formulation**: `LIQUID`, `CREAM`, `POWDER`, `GEL`, `BALM`, `PENCIL`, `SPRAY`, `STICK`
- **Finish**: `MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT`, `GLOSSY`
- **Chemistry Base**: `WATER`, `SILICONE`, `OIL`, `WAX`, `ALCOHOL`
- **Coverage**: `SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE`
- **Temperature**: `WARM`, `COOL`, `NEUTRAL`, `OLIVE`
- **Formality**: `LOUNGE`, `CASUAL`, `SMART_CASUAL`, `PROFESSIONAL`, `FORMAL`, `GALA`
  ing-only fields MUST NOT appear on cosmetic items.**

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `formulation` | Optional | `String?` | Physical form (e.g., `LIQUID`, `CREAM`). |
| `chemistry_base`| Optional | `String?` | Primary base (e.g., `WATER`, `SILICONE`). |
| `finish` | Optional | `String?` | Visual effect (e.g., `MATTE`, `SATIN`). |
| `coverage` | Optional | `String?` | Opacity level (e.g., `SHEER`, `MEDIUM`). |
| `temperature` | Optional | `String?` | Perceptual undertone (e.g., `WARM`, `COOL`). |
| `volume` | Optional | `String?` | Net weight/volume (e.g., "30ml"). |
| `pao_months` | Optional | `Integer?` | Period After Opening (months). |
| `expiry_date` | Optional | `Long?` | Hard expiration timestamp (Unix millis). |
| `instructions` | Optional | `String?` | Manufacturer usage instructions. |
| `ingredients` | ✓ | `Array<String>`| Ordered per the manufacturer's published list. |
| `allergens` | ✓ | `Array<String>`| Known safety triggers. |
| `is_vegan` | Optional | `Boolean?` | Contains no animal products. |
| `is_cruelty_free` | Optional | `Boolean?` | Not tested on animals. |
| `fda_data_verified`| ✓ | `Boolean` | Verified against clinical safety datasets. |

### 2.3 Clothing Items

Reside in the `clothing` array. **Cosmetic-only fields MUST NOT appear on clothing items.**

| Field | Required | Wire Type | Description |
| :--- | :--- | :--- | :--- |
| `formality` | Optional | `String?` | Attire level (e.g., `CASUAL`, `PROFESSIONAL`). |
| `material` | Optional | `String?` | Primary fabric type (e.g., `SILK`, `COTTON`). |

---

## 3. Serialization & Compatibility

### 3.1 Canonical Serialization
The **Rust Normalization Compiler** is the sole authoritative KCPS serializer. Field ordering, UTF-8 encoding, escaping, numeric representation, and omission of optional fields MUST be deterministic to ensure stable `.kpkg` hashes.

### 3.2 Compatibility Rules
- **Forward Compatibility**: Adding optional fields is backward compatible. Clients MUST ignore unknown fields.
- **Breaking Changes**: Removing required fields or renaming existing fields requires a new schema version.
- **Versioning**: 
    - `package_format_version`: Defines the `.kpkg` binary/container protocol.
    - `schema_version`: Defines the decompressed KCPS JSON data contract.

---

## 4. Validation & Rejection Rules

A package (`.kpkg`) MUST be rejected if:
1. A **Required** field is missing from any item.
2. A field type does not match the **Wire Type** specified.
3. An enum field contains a value not present in the [Reference Enums](#enums).
4. The `schema_version` of the package is unsupported by the client. Clients MUST reject packages whose `schema_version` exceeds the maximum supported version.
5. Duplicate `id` values exist anywhere in the package.
6. The `id` or `name` of any item is empty or whitespace only.
7. Any array contains `null` values.
8. JSON object keys are not unique.

---

<a name="enums"></a>
## Appendix A: Reference Enums

### Macro Categories (Complete List)
- **Cosmetic**: `PREP`, `COMPLEXION`, `DIMENSION`, `EYES`, `LIPS`, `NAILS`, `HAIR`, `HYGIENE`, `ORAL`, `FRAGRANCE`, `GROOMING`, `TOOLS`
- **Clothing**: `TOPS`, `BOTTOMS`, `SHOES`, `ACCESSORIES`, `OTHER`

### Micro Categories (Complete List)

**Cosmetic Categories:**
`CLEANSER`, `TONER`, `SERUM`, `MOISTURIZER`, `SPF`, `PRIMER`, `FACE_MASK`, `EXFOLIANT`, `EYE_CARE`, `LIP_CARE`, `FOUNDATION`, `BB_CC_CREAM`, `CONCEALER`, `COLOR_CORRECTOR`, `SETTING_POWDER`, `FACE_POWDER`, `SETTING_SPRAY`, `BLUSH`, `BRONZER`, `CONTOUR`, `HIGHLIGHTER`, `FRECKLE_TINT`, `EYESHADOW`, `EYELINER`, `MASCARA`, `LASH_PRIMER`, `BROW_PENCIL`, `BROW_GEL`, `FALSE_LASHES`, `LIPSTICK`, `LIP_GLOSS`, `LIP_LINER`, `LIP_TINT_STAIN`, `LIP_BALM`, `LIP_PLUMPER`

**Clothing Categories:**
`TOPS`, `BOTTOMS`, `SHOES`, `ACCESSORIES`, `OTHER`

### Professional Facets (Representative Examples)
- **Formulation**: `LIQUID`, `CREAM`, `POWDER`, `GEL`, `BALM`, `PENCIL`, `SPRAY`, `STICK`
- **Finish**: `MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT`, `GLOSSY`
- **Chemistry Base**: `WATER`, `SILICONE`, `OIL`, `WAX`, `ALCOHOL`
- **Coverage**: `SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE`
- **Temperature**: `WARM`, `COOL`, `NEUTRAL`, `OLIVE`
- **Formality**: `LOUNGE`, `CASUAL`, `SMART_CASUAL`, `PROFESSIONAL`, `FORMAL`, `GALA`
