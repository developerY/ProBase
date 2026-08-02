# Interfacial Chemistry & Layering Compatibility Engine

## Preventing Cosmetic Pilling Through Computational Chemistry

One of the primary objectives of the Glow Archive is to model not only *what* cosmetic products are, but also **how they interact** when layered together.

Successful cosmetic application depends on the thermodynamic and chemical compatibility of adjacent product layers.

When incompatible formulations are applied together, users may experience:

- Cosmetic pilling
- Film fracture
- Phase separation
- Uneven adhesion
- Reduced wear time

The **Layering Compatibility Engine** evaluates these interactions before application, allowing KoColor to provide proactive guidance rather than reactive troubleshooting. :contentReference[oaicite:0]{index=0}

---

# The Three Base Chemistry Types

Every cosmetic product belongs to one of three primary chemical phases.

| Chemistry | Description |
|-----------|-------------|
| **Water** | Hydrophilic formulations built on aqueous film formers |
| **Silicone** | Hydrophobic siloxane polymer systems |
| **Oil** | Lipid-based formulations using natural or synthetic oils |

These chemistry classifications become part of each product's Professional Facets.

---

# Why Pilling Occurs

Cosmetic pilling occurs when adjacent product layers exhibit incompatible physical or chemical properties.

Common causes include:

- Surface energy mismatch
- Solvent incompatibility
- Polymer disruption
- Uneven evaporation
- Mechanical shear during application

Rather than blending together, incompatible layers detach and roll into visible particles.

```text
Compatible Layers

Foundation
──────────────
Primer
──────────────
Skin

↓

Smooth Continuous Film


Incompatible Layers

Foundation
xxxxxxx
Primer
──────────────
Skin

↓

Film Separation
↓

Visible Pilling
```

---

# Compatibility Matrix

The Layering Compatibility Engine evaluates transitions between adjacent cosmetic layers.

| Base Layer → Next Layer | Compatibility | Behavior | Recommended Action |
|--------------------------|---------------|----------|--------------------|
| Silicone → Silicone | Optimal | Compatible polymer network | Proceed normally |
| Water → Silicone | High Risk | Surface tension mismatch | Allow flash-off or use transition layer |
| Oil → Water | Moderate Risk | Swelling of drying polymers | Increase drying time |
| Silicone → Oil | Poor | Lipid dissolution and slippage | Reorder application |
| Water → Water | High | Uniform film formation | Verify polymer loading |
| Oil → Oil | High | Stable emollient blending | Monitor pigment movement |

These compatibility rules form the basis of KoColor's layering recommendations. :contentReference[oaicite:1]{index=1}

---

# Computational Pipeline

The engine evaluates cosmetic layers sequentially.

```text
Routine

Primer
     │
Foundation
     │
Concealer
     │
Contour
     │
Blush
```

↓

```text
Compatibility Engine

Primer
     │
Chemistry
     ▼
Foundation
     │
Chemistry
     ▼
Evaluate

↓

Next Pair

Foundation
     │
Concealer

↓

Continue Until Complete
```

Every adjacent pair is evaluated independently.

---

# Layer Evaluation Algorithm

The compatibility engine performs a deterministic comparison between each neighboring product.

```text
Layer N
     │
     ▼
Extract Chemistry
     │
     ▼
Layer N + 1
     │
     ▼
Extract Chemistry
     │
     ▼
Compatibility Matrix
     │
     ▼
Risk Assessment
     │
     ▼
Recommendation
```

This approach avoids heuristic guessing and instead relies on structured metadata supplied by the Glow Archive Taxonomy.

---

# Intelligent Recommendations

When a high-risk chemistry transition is detected, KoColor can generate actionable recommendations.

Examples include:

### Flash-Off Period

> Allow the primer to dry for approximately 30 seconds before applying foundation.

---

### Drying Interval

> Wait until the water-based layer has completely set before introducing an oil-based product.

---

### Layer Reordering

> Apply the higher-volatility formulation first to improve adhesion.

---

### Alternative Products

> Recommend chemically compatible alternatives already present in the user's vanity.

---

# Integration with `CosmeticItem.kt`

The Layering Compatibility Engine relies directly on the structured metadata defined within `CosmeticItem.kt`.

Each product exposes:

```text
CosmeticItem
      │
      ▼
Chemistry
      │
      ▼
Compatibility Engine
      │
      ▼
Layer Validation
```

Because Chemistry is represented as a strongly typed Professional Facet, compatibility evaluation requires no runtime parsing or inference.

---

# Architectural Benefits

The Layering Compatibility Engine provides several advantages:

## Prevents Cosmetic Pilling

Users receive guidance before incompatible products are combined.

---

## Deterministic Evaluation

Recommendations are based on structured chemical metadata rather than subjective rules.

---

## Explainable AI

Every recommendation can be traced back to measurable product characteristics.

---

## Reusable Across Platforms

Because the engine depends only on domain models, it can be reused across:

- Android
- Wear OS
- Android XR
- Future desktop or web clients

---

# Summary

The Layering Compatibility Engine transforms cosmetic chemistry into computational intelligence.

By evaluating the interaction between adjacent cosmetic layers, KoColor can proactively identify incompatibilities and recommend corrective actions before application begins.

This architecture bridges cosmetic science and software engineering, enabling intelligent recommendations that improve both product performance and the overall user experience. :contentReference[oaicite:2]{index=2}