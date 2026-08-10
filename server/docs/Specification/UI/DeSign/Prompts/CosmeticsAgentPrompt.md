# Prompt: High-Fidelity Starter Pack Generation

This document contains the engineering prompt used with the **Expert Cosmetics Agent** to generate 100% complete, "No Field Left Empty" starter packs for the KoColor ecosystem.

---

## 🤖 The Prompt

**Role**: You are a Senior Cosmetic Chemist and Luxury Fashion Editor specializing in the "Glow Archive" taxonomy.

**Task**: Generate a high-fidelity `starter-pack.json` payload for the KoColor ecosystem. You must provide a dataset for **5 signature cosmetic items** and **2 essential clothing items**.

**Strict Requirements**:
1.  **Zero Empty Fields**: Every single field in the JSON schema below must be populated with realistic, expert-grade data (typical ingredients, FDA active ingredients, PAO months, eco-scores, etc.).
2.  **Enum Compliance**: You must strictly use the allowed values for technical facets (listed below).
3.  **JSON Format**: Output ONLY the raw JSON object matching the provided DTO structure.

---

### 🎨 Allowed Technical Facets (Enums)

*   **MacroCategory**: `PREP`, `COMPLEXION`, `DIMENSION`, `EYES`, `LIPS`, `NAILS`, `HAIR`, `HYGIENE`, `TOOLS`
*   **MicroCategory**: `CLEANSER`, `TONER`, `SERUM`, `SPF`, `PRIMER`, `FOUNDATION`, `CONCEALER`, `BLUSH`, `EYESHADOW`, `LIPSTICK`, `MASCARA`, etc.
*   **Formulation**: `LIQUID`, `CREAM`, `POWDER`, `GEL`, `BALM`, `PENCIL`, `SPRAY`, `STICK`
*   **ChemistryBase**: `WATER`, `SILICONE`, `OIL`, `ALCOHOL`, `WAX`
*   **Finish**: `MATTE`, `SATIN`, `NATURAL`, `DEWY`, `RADIANT`, `METALLIC`, `GLITTER`
*   **Coverage**: `SHEER`, `LIGHT`, `MEDIUM`, `FULL`, `BUILDABLE`
*   **Temperature**: `WARM`, `COOL`, `NEUTRAL`, `OLIVE`
*   **ClothingCategory**: `TOPS`, `BOTTOMS`, `SHOES`, `ACCESSORIES`

---

### 📋 Expected JSON Schema (DTO Structure)

```json
{
  "version": 1,
  "cosmetics": [
    {
      "id": "string (unique)",
      "name": "string",
      "brand": "string",
      "macro_category": "string (The Display Name, e.g., 'Lips')",
      "micro_category": "string (The Enum name, e.g., 'LIPSTICK')",
      "formulation": "string (Enum)",
      "chemistry_base": "string (Enum)",
      "finish": "string (Enum)",
      "coverage": "string (Enum)",
      "temperature": "string (Enum)",
      "color_hex": "#HEX",
      "shade_name": "string",
      "image_url": "https://cdn.kocolor.com/inventory/assets/[filename].webp",
      "notes": "string (Editor's note)",
      "instructions": "string (How to use)",
      "batch_code": "string",
      "pao_months": 12,
      "price": 0.0,
      "volume": "string (e.g. 30ml)",
      "expiry_date": 1798761600000,
      "hero_ingredient": "string",
      "skin_compatibility": "string",
      "contains_fragrance": false,
      "ingredients": ["string", "string"],
      "allergens": [],
      "eco_score": "string (A-E)",
      "is_vegan": true,
      "is_cruelty_free": true,
      "recycling_instructions": "string",
      "ritual_placement": "string",
      "fda_recall_status": "Clear",
      "fda_adverse_event_count": 0,
      "fda_clinical_warnings": [],
      "fda_top_reactions": [],
      "fda_active_ingredients": ["string"],
      "is_fda_checked": true
    }
  ],
  "clothing": [
    {
      "id": "string",
      "name": "string",
      "brand": "string",
      "macro_category": "string (Enum, e.g. TOPS)",
      "micro_category": "string (Enum, e.g. TOPS)",
      "formality": "PROFESSIONAL",
      "color_hex": "#HEX",
      "size": "M",
      "material": "string",
      "price": 0.0,
      "image_url": "string",
      "dominant_hex": "#HEX",
      "vibrant_hex": "#HEX",
      "muted_hex": "#HEX",
      "palette_hexes": ["#HEX"],
      "color_temperature": "COOL",
      "seasonal_palette": "WINTER",
      "contrast_level": "HIGH",
      "ko_color_group": "string"
    }
  ]
}
```

---

## 🛠️ Usage Instructions

1.  **Generate**: Copy the prompt above and paste it into the **Expert Cosmetics Agent**.
2.  **Paste**: Once you have the raw JSON response, paste it into `server/kocolor/starter-pack.json`.
3.  **Sync**: Run the Rust generator (`cargo run --bin generate_payload`) and push the result to your GitHub CDN.
4.  **Ingest**: Tap the **"Load Starter Pack"** button in the app settings to see the high-fidelity items.
