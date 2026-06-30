# KoColor Generative AI Integration Contract

This document outlines the interaction patterns and data contracts between the KoColor Android application and the Gemini Multimodal AI.

## 1. Persona & System Instructions
The AI is instructed to act as a **"Professional Cosmetic Analyzer for the KoColor Boutique app."** This persona ensures that the output prioritizes technical accuracy, industry-standard terminology, and professional database integrity over generic descriptions.

## 2. The Prompt Architecture
The system uses a **Hybrid Intelligence** model. The prompt is dynamically constructed based on the following inputs:

*   **Visual Input**: A list of captured bitmaps (front, back, ingredients, product color).
*   **Database Context (OBF)**: Verified information from Open Beauty Facts (Brand, Name, Categories).
*   **User Hints**: Manual color sampling provided by the user.
*   **Local OCR**: Offline text extracted from the ingredients and instructions panels.

### Prompt Logic Example:
> "Analyze these photos of a product container. The confirmed database info is: Brand: NARS, Name: Radiant Concealer. The user sampled the color #E0B0FF. Focus your visual analysis on extracting the MISSING details like full ingredients and specific instructions."

## 3. Data Contract (JSON)
Gemini is configured to return a strict JSON object. This ensures predictable parsing into the Kotlin `CosmeticItem` model.

### JSON Schema:
```json
{
  "name": "Product Name",
  "brand": "Brand Name",
  "macroCategory": "PREP|COMPLEXION|DIMENSION|EYES|LIPS|HAIR|HYGIENE|ORAL|FRAGRANCE|GROOMING|TOOLS",
  "microCategory": "FOUNDATION|SPF|SERUM|CLEANSER|MOISTURIZER|TONER|LIPSTICK|etc",
  "formulation": "LIQUID|CREAM|POWDER|GEL|STICK|PENCIL|BALM|OIL|SPRAY|FOAM|LOOSE_POWDER|PRESSED_POWDER|UNKNOWN",
  "chemistryBase": "WATER|SILICONE|OIL|ALCOHOL|MINERAL|WAX|HYBRID|UNKNOWN",
  "finish": "MATTE|SATIN|NATURAL|DEWY|GLOSSY|SHIMMER|METALLIC|SHEER|VELVET|UNKNOWN",
  "coverage": "SHEER|LIGHT|MEDIUM|FULL|BUILDABLE|NOT_APPLICABLE",
  "shadeName": "Shade name",
  "colorHex": "#RRGGBB",
  "instructions": "Usage instructions extracted from the back panel",
  "batchCode": "Barcode or manufacturer batch code",
  "paoMonths": 12,
  "volume": "30ml",
  "ingredients": ["Water", "Glycerin", "Phenoxyethanol", "..."],
  "heroIngredient": "Main active ingredient",
  "skinCompatibility": "e.g. Sensitive, Oily, All Skin Types",
  "containsFragrance": true,
  "ecoScore": "A|B|C|D|E",
  "isVegan": true,
  "isCrueltyFree": true,
  "recyclingInstructions": "Packaging disposal guide",
  "printedWarnings": ["Warning 1", "..."],
  "activeIngredients": ["Active 1", "..."],
  "userColorOverridden": true
}
```

## 4. Intelligent Color Validation Rule
A critical architectural constraint in KoColor is the **Validation Rule**:
*   **The Intent**: The AI treats user color samples as a **Hint**, not a Command.
*   **The Rule**: *"If the user's hex code appears incorrect due to lighting or sampling error, you MUST provide the true, accurate hex code for this makeup shade based on your internal knowledge of the product line."*
*   **Tracking**: If the AI provides a hex code different from the user hint, it sets `userColorOverridden` to `true`.

## 5. Architectural Safeguards
*   **Safe Parsing**: The application layer uses a `try/catch` and `uppercase()` normalization strategy for all Enum fields to handle minor AI stubbornness or hallucinations.
*   **FDA Data Separation**: The AI is strictly limited to extracting text **printed on the box** (using `printedWarnings` and `activeIngredients`). Real-time regulatory safety data and recalls are handled by a dedicated `FdaRepository` via an external API to ensure 100% compliance.
