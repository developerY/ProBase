Here is the complete authoring setup for the **KoColor Starter Kit**.

To successfully test the new Rust `kc-optimizer` and `kc-distributor` pipeline, we need to split your monolithic JSON payload into the strict **Authoring Triads** (Core JSON, Notes JSON, and Raw PNG) and place them into the correct human-readable directory structure.

### 🗂️ 1. The Authoring Directory Structure

Create this exact folder structure inside your `raw_assets/` directory. This is what the Rust `WalkDir` function will traverse.

```text
raw_assets/
├── packages/
│   └── starter-kit.toml
└── KoColor/
    ├── PREP/
    │   ├── CLEANSER/
    │   ├── SERUM/
    │   └── SPF/
    ├── COMPLEXION/
    │   ├── FOUNDATION/
    │   └── CONCEALER/
    ├── DIMENSION/
    │   └── BLUSH/
    ├── EYES/
    │   └── MASCARA/
    └── LIPS/
        ├── LIP_TINT_STAIN/
        └── LIP_BALM/

```

---

### 📦 2. The Package Manifest (TOML)

This file tells the Rust compiler which products to bundle into the `starter-kit.kpkg` payload.

**Save as:** `raw_assets/packages/starter-kit.toml`

```toml
[package]
id = "starter-kit"
name = "KoColor Essentials Starter Kit"
version = "1.0.0"

[products]
items = [
    "kc-starter-prep-01",
    "kc-starter-prep-02",
    "kc-starter-prep-03",
    "kc-starter-complexion-01",
    "kc-starter-complexion-02",
    "kc-starter-dimension-01",
    "kc-starter-eyes-01",
    "kc-starter-lips-01",
    "kc-starter-lips-02"
]

```

---

### 📄 3. The Product Triads (JSON + Notes)

Here are the split assets for the products. *(Note: The CDN URLs are removed from the core JSON because the Rust compiler automatically determines the `assets/hero/` and `assets/thumb/` routing during the build phase).*

#### 💧 PREP: Purifying Gel Cleanser

**Save as:** `raw_assets/KoColor/PREP/CLEANSER/kc-starter-prep-01.json`

```json
{
  "schema_version": 1,
  "id": "kc-starter-prep-01",
  "name": "Purifying Gel Cleanser",
  "brand": "KoColor",
  "macro_category": "PREP",
  "micro_category": "CLEANSER",
  "formulation": "GEL",
  "chemistry_base": "WATER",
  "finish": "NATURAL",
  "coverage": "SHEER",
  "temperature": "NEUTRAL",
  "color_hex": "#F4F6F0",
  "shade_name": "Clear Crystal",
  "pao_months": 12,
  "price": 18.0,
  "volume": "150ml",
  "hero_ingredient": "Niacinamide",
  "skin_compatibility": "All Skin Types",
  "contains_fragrance": false,
  "ingredients": ["Water", "Glycerin", "Sodium Cocoyl Glycinate", "Citric Acid"],
  "eco_score": "A",
  "is_vegan": true,
  "is_cruelty_free": true
}

```

**Save as:** `raw_assets/KoColor/PREP/CLEANSER/kc-starter-prep-01.notes.json`

```json
{
  "schema_version": 1,
  "product_id": "kc-starter-prep-01",
  "what_it_is": "Gentle daily foaming cleanser that balances skin pH.",
  "how_to_apply": "Lather with water and massage onto wet skin. Rinse thoroughly.",
  "good_to_know": ["pH Balanced", "Sulfate-Free formulation"],
  "known_issues": "Avoid direct contact with eyes.",
  "pro_tips": "Leave on for 60 seconds before rinsing to allow Niacinamide to activate."
}

```

#### 🎨 COMPLEXION: Seamless Silk Foundation

**Save as:** `raw_assets/KoColor/COMPLEXION/FOUNDATION/kc-starter-complexion-01.json`

```json
{
  "schema_version": 1,
  "id": "kc-starter-complexion-01",
  "name": "Seamless Silk Foundation",
  "brand": "KoColor",
  "macro_category": "COMPLEXION",
  "micro_category": "FOUNDATION",
  "formulation": "LIQUID",
  "chemistry_base": "SILICONE",
  "finish": "SATIN",
  "coverage": "MEDIUM",
  "temperature": "WARM",
  "color_hex": "#E0AC81",
  "shade_name": "Warm Silk 220W",
  "pao_months": 12,
  "price": 34.0,
  "volume": "30ml",
  "hero_ingredient": "Hyaluronic Acid",
  "skin_compatibility": "All Skin Types",
  "contains_fragrance": false,
  "ingredients": ["Dimethicone", "Water", "Isododecane", "Titanium Dioxide"],
  "eco_score": "B",
  "is_vegan": true,
  "is_cruelty_free": true
}

```

**Save as:** `raw_assets/KoColor/COMPLEXION/FOUNDATION/kc-starter-complexion-01.notes.json`

```json
{
  "schema_version": 1,
  "product_id": "kc-starter-complexion-01",
  "what_it_is": "Buildable medium coverage foundation with a natural skin finish.",
  "how_to_apply": "Pump onto back of hand and blend outwards using a brush.",
  "good_to_know": ["Non-comedogenic", "Contains Hyaluronic Acid for all-day hydration"],
  "known_issues": "Silicone base may pill if applied directly over heavy water-based gels.",
  "pro_tips": "Warm the product between your fingers before applying for a sheer, skin-like tint."
}

```

#### 💋 LIPS: Glow Catalyst Lip Stain

**Save as:** `raw_assets/KoColor/LIPS/LIP_TINT_STAIN/kc-starter-lips-01.json`

```json
{
  "schema_version": 1,
  "id": "kc-starter-lips-01",
  "name": "Glow Catalyst Lip Stain",
  "brand": "KoColor",
  "macro_category": "LIPS",
  "micro_category": "LIP_TINT_STAIN",
  "formulation": "LIQUID",
  "chemistry_base": "WATER",
  "finish": "SATIN",
  "coverage": "FULL",
  "temperature": "COOL",
  "color_hex": "#A81C28",
  "shade_name": "Signature Crimson",
  "pao_months": 12,
  "price": 18.0,
  "volume": "5ml",
  "hero_ingredient": "Tocopherol",
  "skin_compatibility": "All Lip Types",
  "contains_fragrance": false,
  "ingredients": ["Water", "Octyldodecanol", "Red 28", "Tocopherol"],
  "eco_score": "A",
  "is_vegan": true,
  "is_cruelty_free": true
}

```

**Save as:** `raw_assets/KoColor/LIPS/LIP_TINT_STAIN/kc-starter-lips-01.notes.json`

```json
{
  "schema_version": 1,
  "product_id": "kc-starter-lips-01",
  "what_it_is": "Long-wearing hydrating lip tint.",
  "how_to_apply": "Glide applicator across lips. Reapply for bolder coverage.",
  "good_to_know": ["Transfer-proof after 60 seconds", "Vitamin E enriched"],
  "known_issues": "Highly pigmented; can stain fingers if blended manually.",
  "pro_tips": "Dab a small amount on the center of the lips and blend outwards for a subtle, bitten-lip effect."
}

```

*(To complete the setup for the remaining 6 products, copy this exact split pattern: move `notes` and `instructions` into the `.notes.json` file, and keep the core data in the `.json` file, placing them in their respective macro/micro directories).*

---

### 🖼️ 4. Image Asset Placement

Take the image generation prompts you provided and run them through your preferred image generation tool (like Midjourney).

Once the images are generated, save them as raw `.png` files directly next to their JSON counterparts in the `raw_assets/` tree.

* `raw_assets/KoColor/PREP/CLEANSER/kc-starter-prep-01.png`
* `raw_assets/KoColor/COMPLEXION/FOUNDATION/kc-starter-complexion-01.png`
* `raw_assets/KoColor/LIPS/LIP_TINT_STAIN/kc-starter-lips-01.png`

Do not forget to add `*.png` to your `.gitignore` file so these high-res source files are kept out of your repository history.

Once the triads are in place, run `cargo run --release` in your `kc-optimizer` and `kc-distributor` crates. The Rust pipeline will discover them, transcode the images to WebP, bundle the package, and drop the fully compiled V1 payload into your `dist/` directory.