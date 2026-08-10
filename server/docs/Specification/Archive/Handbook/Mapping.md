# Architectural Mapping in `CosmeticItem.kt`

## Strongly Typed Domain Modeling

The **Glow Archive Taxonomy** is implemented through a strongly typed domain model centered around `CosmeticItem.kt`.

Rather than relying on free-form strings or loosely structured metadata, every cosmetic product is represented using enumerations and domain objects that enforce consistency throughout the system.

This approach provides:

- Compile-time safety
- Consistent product definitions
- Elimination of runtime string parsing
- Deterministic AI processing
- Reliable filtering and compatibility analysis

Every cosmetic item carries both its user-facing taxonomy and its engine-facing physical attributes. :contentReference[oaicite:0]{index=0}

---

# Core Entity Structure

Each `CosmeticItem` combines three levels of taxonomy into a single domain model.

```text
CosmeticItem
    │
    ├── Identity
    │      ├── Item ID
    │      └── Display Name
    │
    ├── Level 1
    │      └── Macro Category
    │
    ├── Level 2
    │      └── Micro Category
    │
    └── Level 3
           ├── Formulation
           ├── Chemistry
           ├── Finish
           ├── Coverage
           └── Temperature
```

This unified structure enables every product to expose a complete computational profile. :contentReference[oaicite:1]{index=1}

---

# Identity

Each cosmetic product begins with immutable identifying information.

| Field | Purpose |
|--------|---------|
| Item ID | Unique system identifier |
| Display Name | User-facing product name |

These values uniquely identify products throughout the ecosystem.

---

# Level 1 — Macro Category

The Macro Category determines where a product belongs within the application.

Examples include:

- Skincare & Prep
- Complexion
- Color & Dimension
- Eyes & Brows
- Lips

This layer primarily supports:

- Navigation
- Inventory organization
- Anatomical grouping

---

# Level 2 — Micro Category

The Micro Category specifies the exact product type.

Examples include:

- Primer
- Foundation
- Concealer
- Contour
- Lipstick

This enables:

- Product filtering
- Catalog indexing
- Functional grouping
- Search optimization

---

# Level 3 — Professional Facets

Professional Facets provide the structured metadata required by KoColor's computational engines.

Each product must define all five facets.

| Facet | Example Values |
|--------|----------------|
| Formulation | Liquid, Cream, Powder, Gel, Balm |
| Chemistry | Water, Silicone, Oil |
| Finish | Matte, Satin, Radiant, Metallic, Glitter |
| Coverage | Sheer, Light, Medium, Full, Buildable |
| Temperature | Warm, Cool, Neutral, Olive |

Because every field is mandatory, downstream algorithms never need to guess or infer missing product characteristics. :contentReference[oaicite:2]{index=2}

---

# Architectural Benefits

Using strongly typed enumerations provides several advantages.

## Compile-Time Validation

Invalid values cannot enter the system accidentally.

```text
String Model
────────────
"Matt"
"Matte"
"MATTE"

↓

Enum Model
──────────
Finish.MATTE
```

The compiler guarantees consistency.

---

## Deterministic Processing

AI and recommendation engines receive normalized values instead of unpredictable text.

This simplifies:

- Filtering
- Compatibility analysis
- Layering evaluation
- Recommendation generation

---

## Complete Product Definitions

Every cosmetic item must provide all required facets.

This guarantees that algorithms always have sufficient information to perform:

- Chemical compatibility checks
- Shade matching
- Finish analysis
- Layer ordering
- Product recommendations

without relying on null handling or fallback logic.

---

# Data Flow

```text
User Product
      │
      ▼
CosmeticItem
      │
      ├── Macro Category
      ├── Micro Category
      └── Professional Facets
              │
              ▼
Repositories
              │
              ▼
Compatibility Engine
Shade Matching
Recommendation Engine
Filtering
Analytics
```

---

# Why Strong Typing Matters

The Glow Archive is designed to serve as a computational foundation rather than a simple inventory database.

By encoding taxonomy into strongly typed models, the platform gains:

- Predictable data contracts
- Faster processing
- Safer refactoring
- Better developer tooling
- Cross-platform consistency

This design allows every module—from UI screens to AI recommendation engines—to operate on the same trusted representation of a cosmetic product. :contentReference[oaicite:3]{index=3}
