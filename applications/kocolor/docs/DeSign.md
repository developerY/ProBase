Here is the exact markdown content for your repository. You can select it right from the chat and it will keep all the formatting intact when you paste it.

# KoColor: Advanced Cosmetic & Wardrobe Intelligence Architecture

**KoColor** is a privacy-first, on-device styling intelligence platform within the ProBase ecosystem. It bridges the gap between professional cosmetic taxonomy and personal wardrobe analytics without relying on external cloud AI.

By combining deterministic color science, local image processing, and crowdsourced cosmetic data, KoColor acts as an automated, offline personal stylist.

---

## 1. Monorepo Structure Integration

KoColor strictly adheres to the ProBase clean architecture. Domain-specific logic is isolated within the `applications/kocolor/features/` directory to prevent cross-contamination with other modular applications.

```text
applications/kocolor/
├── features/
│   ├── color/               # Wardrobe Color Engine (Local image analysis, color science)
│   ├── inventory/           # Cosmetic Inventory (OBF integration, Taxonomy mapping)
│   ├── routines/            # Chronobiological beauty trackers (AM/PM)
│   ├── analyzer/            # Personal FaceLab / Skin-tone intelligence
│   └── suggestions/         # Comprehensive Look Builder orchestration

```

---

## 2. The Wardrobe Color Engine (Offline Analytics)

Unlike cosmetics, garments lack standardized global barcodes (UPCs) and chemical manifests. Therefore, a centralized "Open Clothing Database" does not exist.

To solve this, KoColor relies on the **Wardrobe Color Engine**: a fully local pipeline that uses on-device APIs to analyze garment pixels and classify them into semantic fashion intelligence.

### The 3-Stage Pipeline

1. **Pre-Processing:** Efficiently downsizes captured garment bitmaps to prevent Out-Of-Memory (OOM) errors. Integrates with ML Kit Object Detection to automatically categorize the item (e.g., "Dress", "Pants").
2. **Palette Extraction:** Utilizes the Android Palette API to extract the `dominant`, `vibrant`, and `muted` hex color signatures based on pixel weight.
3. **Semantic Signature Generation:** Converts RGB/Hex values into HSL to mathematically deduce `ColorTemperature` (WARM, COOL, NEUTRAL) and map the garment to a `SeasonalPalette` (SPRING, SUMMER, AUTUMN, WINTER).

### The Data Model

```kotlin
data class ClothingItem(
    val id: String,
    val imageUri: String,
    
    // Extracted Visual Signatures
    val dominantHex: String?,
    val paletteHexes: List<String>,
    
    // Calculated Semantic Metadata
    val colorTemperature: String?, // WARM, COOL, NEUTRAL
    val seasonalPalette: String?   // SPRING, SUMMER, AUTUMN, WINTER
)

```

---

## 3. The Cosmetic Inventory System

To prevent data entry fatigue, KoColor integrates with the **Open Beauty Facts (OBF)** REST API via an ML Kit Barcode Scanner. Because OBF taxonomy is crowdsourced and often messy, the architecture employs a strict mapping layer.

### Hierarchical Taxonomy

The platform uses "Progressive Disclosure" to bridge the gap between everyday consumers and professional makeup artists.

1. **Macro Categories (UI Level):** Body-zone mapping (e.g., *Complexion, Eyes & Brows, Lips, Prep*).
2. **Micro Categories (System Logic):** Exact product types (e.g., *Foundation, Concealer, Mascara*).
3. **Professional Facets (Algorithmic Metadata):** Crucial tags required for formulation conflict detection (e.g., *Water-based vs. Silicone-based, Matte vs. Dewy*).

### The Auto-Fetch Pipeline

When a user scans a cosmetic barcode:

1. `CosmeticInventoryRepository` pings the OBF JSON API.
2. `ObfTaxonomyMapper` sanitizes the data, extracts the INCI ingredient list, and maps the messy tags to strict KoColor `MicroCategory` enums.
3. A draft item is presented to the user for validation before persisting to the local Room database.

---

## 4. The Comprehensive Look Builder

The ultimate objective of the KoColor ecosystem is the **Comprehensive Look Builder**.

Instead of relying on generative AI, KoColor uses a **Deterministic Heuristic Algorithm** that runs entirely locally. It queries the `ClothingDao` for a specific garment's semantic signature and cross-references it with the user's `CosmeticDao` inventory.

### Harmony Scoring Rules

* **Temperature Matching (Primary):** A WARM garment strongly prefers WARM cosmetics. Mismatched temperatures are mathematically penalized. NEUTRAL acts as a safe bridge.
* **Seasonal Alignment (Secondary):** A SPRING garment searches for SPRING cosmetics to ensure lightness and saturation harmony.
* **Layering Integrity:** The engine scans INCI ingredients to prevent recommending a water-based primer with a silicone-based foundation.

### Example Output

```kotlin
data class HarmonizedLook(
    val targetGarment: ClothingItem,
    val recommendedLip: CosmeticItem?,   // e.g., "Warm Autumn" Brick Red Lipstick
    val recommendedEye: CosmeticItem?,   // e.g., "Warm" Copper Eyeshadow
    val recommendedCheek: CosmeticItem?  // e.g., "Warm" Peach Blush
)

```

---

## 5. Security & Privacy Philosophy

* **Local-First Processing:** Image analysis (Palette API / ML Kit) happens strictly on-device. Wardrobe photos are never uploaded to a cloud server.
* **Deterministic Intelligence:** Styling suggestions are hard-coded in color science, ensuring explainable, repeatable, and cost-free recommendations.
* **Offline Capability:** Once cosmetics are fetched from OBF and cached, the entire Wardrobe Engine and Look Builder function without internet connectivity.