# Glow Archive Taxonomy

## Architectural Specification and Taxonomic Integration

The **Glow Archive Taxonomy** provides the foundational classification system for the KoColor ecosystem.

Its purpose is to transform the physical characteristics of cosmetic products into structured computational attributes that can be consumed consistently by user interfaces, repositories, recommendation engines, and AI pipelines.

Rather than serving as a simple product catalog, the taxonomy establishes a deterministic framework that enables:

- Product discovery
- Inventory management
- AI recommendations
- Shade analysis
- Compatibility evaluation
- Cross-platform consistency

The taxonomy bridges the presentation layer (such as `VanityLandingScreen.kt`) with the underlying data model (`CosmeticItem.kt`), creating predictable contracts between the UI and the engine. :contentReference[oaicite:0]{index=0}

---

# Three-Tier Taxonomy

The Glow Archive is organized into three abstraction layers.

Each layer serves a distinct architectural purpose.

| Layer | Purpose | Primary Consumers |
|--------|----------|-------------------|
| **Level 1 – Macro Categories** | High-level UI navigation and anatomical grouping | Navigation, Inventory Views |
| **Level 2 – Micro Categories** | Product type classification | Search, Catalog, Filtering |
| **Level 3 – Professional Facets** | Physical and computational metadata | AI Engines, Compatibility Analysis |

Together, these layers allow the interface to remain intuitive while preserving detailed technical metadata for downstream processing. :contentReference[oaicite:1]{index=1}

---

# Level 1 — Macro Categories

## The User Interface Layer

Macro Categories represent broad functional groupings based on where products are used on the body.

Examples include:

- Skincare & Prep
- Complexion (Base)
- Color & Dimension
- Eyes & Brows
- Lips
- Tools & Hygiene

These categories primarily support:

- Navigation
- Inventory segmentation
- Anatomical organization
- Visual discovery

---

# Level 2 — Micro Categories

## Product Classification

Micro Categories provide a searchable catalog of specific product types.

Examples include:

- Primer
- Foundation
- Concealer
- Contour
- Eyeshadow
- Lipstick

This layer enables:

- Search filtering
- Catalog indexing
- Product comparisons
- Inventory organization

---

# Level 3 — Professional Facets

## The Computational Engine Layer

Professional Facets describe the physical and optical properties required for intelligent analysis.

Each cosmetic product exposes five core facets.

| Facet | Purpose |
|--------|---------|
| **Formulation** | Physical product form (Liquid, Cream, Powder, Gel, Balm) |
| **Chemistry** | Base chemistry (Water, Silicone, Oil) |
| **Finish** | Visual appearance (Matte, Satin, Radiant, Metallic, Glitter) |
| **Coverage** | Pigment opacity (Sheer, Light, Medium, Full, Buildable) |
| **Temperature** | Undertone classification (Warm, Cool, Neutral, Olive) |

These facets enable deterministic compatibility calculations without relying on runtime heuristics. :contentReference[oaicite:2]{index=2}

---

# Architectural View

```text
                     Glow Archive Taxonomy

        ┌─────────────────────────────────────────────┐
        │         Level 1 – Macro Categories          │
        │  Skincare • Lips • Eyes • Complexion • etc. │
        └─────────────────────────────────────────────┘
                           │
                           ▼
        ┌─────────────────────────────────────────────┐
        │         Level 2 – Micro Categories          │
        │ Primer • Foundation • Lipstick • Contour    │
        └─────────────────────────────────────────────┘
                           │
                           ▼
        ┌─────────────────────────────────────────────┐
        │      Level 3 – Professional Facets          │
        │ Formulation • Chemistry • Finish            │
        │ Coverage • Temperature                      │
        └─────────────────────────────────────────────┘
                           │
                           ▼
             AI • Filtering • Compatibility • Search
```

---

# Why This Architecture Matters

Separating the taxonomy into three layers provides several architectural advantages:

- **Consumers** interact with simple, intuitive categories.
- **Developers** work with strongly typed domain models.
- **AI engines** receive structured metadata for deterministic analysis.
- **Future platforms** can reuse the taxonomy without redesigning the data model.

This layered approach establishes a scalable foundation for product discovery, cosmetic intelligence, compatibility analysis, and AI-driven recommendations throughout the KoColor ecosystem. :contentReference[oaicite:3]{index=3}
