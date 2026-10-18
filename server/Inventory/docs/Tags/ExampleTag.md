Here is the standardized **KoColor Wardrobe Metadata Taxonomy (`metadataVersion: 1`)** record for **`kc-active-805` (Arcadia Blue Track Jacket)**, including exact CIELAB color coordinates calculated from `#252C58` and deterministic construction rules derived from its 100% tricot polyester composition.

---

### 1. Structured Facet Breakdown (`kc-active-805`)

| Dimension | Assigned Taxonomy Tags | Derivation & Grounding Rationale |
| --- | --- | --- |
| **Category & Role** | `OUTERWEAR` / `TRACK_JACKET` | Zip-front athletic jacket designed to be worn over foundational base layers. |
| **Occasion** | `ACTIVE`, `CASUAL`, `EVERYDAY`, `TRAVEL` | Engineered for warmups, athletic transit, and retro casual styling. |
| **Formality** | `CASUAL` | Athletic tricot construction with ribbed cuffs. |
| **Thermal Suitability** | `MILD` (14–20°C), `COOL` (7–14°C) | Uninsulated midweight knit; suited for cool/mild conditions or athletic warmups, excluded in `HOT` weather. |
| **Weather Protection** | `WIND_RESISTANT` | Dense tricot polyester weave and ribbed cuffs block light wind chill. |
| **Garment Construction** | Weight: `MIDWEIGHT` · Breathability: `MEDIUM` · Insulation: `NONE` | *Provenance: `DERIVED_FROM_CONSTRUCTION*` (100% synthetic tricot knit). |
| **Physical Hardware / Details** | `RIBBED_CUFFS`, `ZIP_POCKETS` | Preserved explicitly so the Gemini Style Architect can reference verified details under Strict Grounding Rule #1. |
| **Layering Role** | Primary: `OUTER_LAYER` · Secondary: `MID_LAYER` | Worn over a base t-shirt/tank, or layered under a heavy shell in colder climates. |
| **Fit & Silhouette** | `FITTED` | "Sleek, ribbed-cuff" profile that Taper-locks at the wrists and waist. |
| **Activity** | `EXERCISE`, `WALKING`, `CYCLING`, `LOUNGE` | High mobility synthetic construction with secure zip storage. |
| **Season** | `SPRING`, `AUTUMN`, `WINTER` | Ideal transitional outer layer; winter indoor/warmup layer. |
| **Color Intelligence** | `#252C58` · `BLUE` · `COOL` | Deep indigo/navy ($L^*: 19.64, a^*: 11.55, b^*: -27.62, C^*: 29.94, h^\circ: 292.7^\circ$). Low lightness, moderate chroma. |

---

### 2. Canonical CDN Metadata Record (JSON)

This JSON object is ready to be embedded into your `.kpkg` manifest compiler:

```json
{
  "itemId": "kc-active-805",
  "name": "Arcadia Blue Track Jacket",
  "category": "OUTERWEAR",
  "productType": "TRACK_JACKET",
  "commercial": {
    "priceUsd": 85.0,
    "volumeOrSize": "Size M",
    "sustainabilityGrade": "GRADE_A"
  },
  "occasionTags": [
    "ACTIVE",
    "CASUAL",
    "EVERYDAY",
    "TRAVEL"
  ],
  "formality": "CASUAL",
  "thermalSuitability": [
    "MILD",
    "COOL"
  ],
  "thermalProvenance": "DERIVED_FROM_CONSTRUCTION",
  "weatherProtection": [
    "WIND_RESISTANT"
  ],
  "construction": {
    "thermalWeight": "MIDWEIGHT",
    "breathability": "MEDIUM",
    "insulation": "NONE",
    "features": [
      "RIBBED_CUFFS",
      "ZIP_POCKETS"
    ],
    "provenance": "DERIVED_FROM_CONSTRUCTION"
  },
  "layeringRole": "OUTER_LAYER",
  "compatibleLayeringRoles": [
    "OUTER_LAYER",
    "MID_LAYER"
  ],
  "silhouette": "FITTED",
  "seasons": [
    "SPRING",
    "AUTUMN",
    "WINTER"
  ],
  "activities": [
    "EXERCISE",
    "WALKING",
    "CYCLING",
    "LOUNGE"
  ],
  "color": {
    "hex": "#252C58",
    "colorName": "Arcadia Blue",
    "hueFamily": "BLUE",
    "temperature": "COOL",
    "lStar": 19.64,
    "aStar": 11.55,
    "bStar": -27.62,
    "cStar": 29.94,
    "hDeg": 292.7
  },
  "material": {
    "composition": [
      {
        "material": "TRICOT_POLYESTER",
        "percentage": 100
      }
    ]
  },
  "metadataVersion": 1
}

```

### Schema Note for the Compiler

Notice two small additions in this record that help your Gemini prompt grounding rule (*"Describe items strictly using the physical materials and attributes listed in the manifest"*):

1. **`construction.features`**: Captures `["RIBBED_CUFFS", "ZIP_POCKETS"]` as structured enums so the compact manifest serializer can pass them to Gemini without relying on unstructured prose.
2. **`ProductType.TRACK_JACKET` & `MaterialFiber.TRICOT_POLYESTER**`: Added to the controlled enum vocabulary to distinguish knit athletic polyester from woven windbreaker polyester.

Drop the next item (or a batch of items) whenever you're ready, and we'll tag the rest of the catalog.