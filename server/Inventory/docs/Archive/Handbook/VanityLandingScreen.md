# `VanityLandingScreen.kt` Architecture

## User Interface Integration with the Glow Archive Taxonomy

`VanityLandingScreen.kt` serves as the primary presentation layer for the Glow Archive.

Its responsibility is to expose the three-tier taxonomy through an intuitive user interface while maintaining a direct mapping to the underlying domain model.

Rather than displaying a simple product grid, the screen functions as an intelligent navigation hub that translates user interactions into structured taxonomy queries.

The architecture bridges:

- User interface navigation
- Repository filtering
- Domain models
- Recommendation engines

through a deterministic filtering pipeline. :contentReference[oaicite:0]{index=0}

---

# Architectural Overview

```text
                VanityLandingScreen

                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
 Macro Categories  Micro Categories  Professional Facets
        │              │              │
        └──────────────┼──────────────┘
                       ▼
              Filtering Pipeline
                       ▼
              Cosmetic Repository
                       ▼
              Filtered Product Grid
```

Every user interaction updates the active taxonomy state, which is then applied to the cosmetic repository.

---

# Three-Tier Navigation

The presentation layer mirrors the Glow Archive Taxonomy.

## Level 1 — Macro Categories

The primary navigation consists of anatomical product groupings.

Examples include:

- Skincare & Prep
- Complexion
- Color & Dimension
- Eyes & Brows
- Lips
- Tools & Hygiene

Selecting a Macro Category limits the available dataset before additional filtering occurs.

---

## Level 2 — Micro Categories

Once a Macro Category has been selected, users can refine the product list using specific product types.

Examples include:

- Primer
- Foundation
- Concealer
- Lipstick
- Contour

This secondary layer dramatically reduces search complexity while keeping navigation intuitive.

---

## Level 3 — Professional Facets

Professional Facets expose advanced filtering options for experienced users.

Examples include:

- Formulation
- Chemistry
- Finish
- Coverage
- Temperature

These facets allow users to filter products based on their physical and optical characteristics rather than brand or marketing terminology. :contentReference[oaicite:1]{index=1}

---

# Progressive Disclosure

Rather than overwhelming users with every available option, the UI reveals information progressively.

```text
Macro Category
        │
        ▼
Micro Category
        │
        ▼
Professional Facets
        │
        ▼
Filtered Products
```

This layered interaction keeps the interface approachable while still exposing powerful filtering capabilities.

---

# UI Components

Each taxonomy layer maps directly to a UI component.

| UI Component | Taxonomy Layer | Purpose |
|--------------|----------------|---------|
| `VanityLandingScreen.kt` | Level 1 | Primary navigation |
| `MicroCategoryFilterBar.kt` | Level 2 | Product-type filtering |
| `FacetFilterDrawer.kt` | Level 3 | Professional facet filtering |
| `CosmeticGridAdapter.kt` | All Levels | Displays filtered cosmetic products |

This one-to-one mapping keeps the presentation layer aligned with the domain model. :contentReference[oaicite:2]{index=2}

---

# Filtering Pipeline

Each user interaction contributes to a deterministic filtering pipeline.

```text
User Selection
        │
        ▼
Macro Category
        │
        ▼
Micro Category
        │
        ▼
Facet Filters
        │
        ▼
Predicate Evaluation
        │
        ▼
Filtered Product List
```

Every active filter must be satisfied before a product appears in the results.

---

# Predicate Evaluation

The filtering engine evaluates products using a logical sequence.

```text
For Each Product

      │
      ▼

Macro Category Match?

      │
      ▼

Micro Category Match?

      │
      ▼

Facet Match?

      │
      ▼

Yes
      │
      ▼

Display Product
```

If any condition fails, the product is excluded from the active view.

This deterministic approach ensures predictable search behavior regardless of catalog size.

---

# Reactive State Management

The UI maintains reactive state for each taxonomy level.

```text
selectedMacroCategory
            │
            ▼
activeMicroCategorySet
            │
            ▼
activeFacetMap
            │
            ▼
filteredCosmeticItemList
```

Each state update automatically triggers recomputation of the visible product list, keeping the interface synchronized with user selections.

---

# Architectural Benefits

The presentation architecture offers several advantages.

## Predictable Navigation

Each level narrows the search space before the next level is evaluated.

---

## High Performance

Filtering operates on structured domain models rather than expensive text searches.

---

## Scalable Design

New Macro Categories, Micro Categories, or Professional Facets can be introduced without redesigning the UI architecture.

---

## Consistent User Experience

Every platform can present the same taxonomy while using platform-specific controls.

Examples include:

- Android
- Wear OS
- Android XR
- Desktop
- Web

---

# Summary

`VanityLandingScreen.kt` is more than a cosmetic catalog—it is the presentation gateway into the Glow Archive Taxonomy.

By mirroring the three-tier taxonomy in the user interface, the screen enables intuitive navigation while preserving the structured metadata required by KoColor's computational engines.

The result is a scalable architecture that connects user interactions directly to deterministic filtering, recommendation, and compatibility workflows, ensuring a consistent experience across the entire KoColor ecosystem. :contentReference[oaicite:3]{index=3}