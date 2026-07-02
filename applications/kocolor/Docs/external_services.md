# Cosmetics Inventory: External Web Services & Data Sources

This document outlines the primary external web services and data providers used to build and enrich the cosmetics inventory in KoColor.

## 1. Open Beauty Facts (OBF)
The backbone of the product identification system. It is a crowdsourced, open-source database of cosmetic products from around the world.

*   **Base URL:** `https://world.openbeautyfacts.org/`
*   **Purpose:**
    *   Primary barcode lookup (`api/v1/product/{barcode}.json`).
    *   Retrieval of basic product identity (Name, Brand, volume).
    *   Sourcing high-resolution product images (`image_front_url`).
    *   Providing raw ingredient lists and allergen tags.
*   **Key Mapping:** Categories like `en:lipsticks` are mapped to the app's internal `ClothingCategory` or `MicroCategory` enums.

## 2. OpenFDA (FDA Public API)
Used to provide official clinical safety data and regulatory status for products, especially those classified as Over-The-Counter (OTC) drugs in the US (e.g., SPF, acne treatments).

*   **Base URL:** `https://api.fda.gov/`
*   **Purpose:**
    *   **Enforcement Queries:** Checking for active product recalls (`drug/enforcement.json` and `food/enforcement.json`).
    *   **Adverse Events:** Retrieving counts of reported reactions and the "Top Reactions" list (`food/event.json`).
    *   **Drug Labels:** Extracting official "Warnings" and "Active Ingredients" from manufacturer filings (`drug/label.json`).
*   **Integration:** Powers the **FDA Clinical Safety Badge** (Green/Orange/Red status) in the product details.

## 3. PubChem (chemDB)
A comprehensive database of chemical molecules and their activities against biological assays.

*   **Base URL:** `https://pubchem.ncbi.nlm.nih.gov/rest/pug/`
*   **Purpose:**
    *   **Ingredient Intelligence:** Fetching detailed safety hazards and classification for specific chemical compounds found in product ingredients.
    *   **Molecular Data:** Retrieving molecular formulas, weights, and physical properties for advanced analysis.
*   **Integration:** Enriches the `CosmeticItem` with deep safety data beyond basic manufacturer warnings. Currently integrated into the `CosmeticsViewModel` to fetch safety hazards for the hero ingredient.

## 4. The Color API
A free, public REST API for color information and color theory mathematics.

*   **Base URL:** `https://www.thecolorapi.com/`
*   **Purpose:**
    *   **Shade Naming:** Translating raw hex codes (from Gemini or user sampling) into human-readable color names (e.g., "Mauve").
    *   **Harmonious Palettes:** Generating mathematically derived color schemes (Analogic, Complement, Triad) to power the "Works Well With" coordination engine.
*   **Integration:** Keyless GET requests within the `ColorRepository` in the `:kocolor:features:colors` module.

## 5. Open-Meteo (Keyless Weather & Environment)
A privacy-first, open-source weather API that requires zero API keys.

*   **Base URL:** `https://api.open-meteo.com/v1/`
*   **Purpose:**
    *   **UV Index & Sun Exposure:** Retrieving real-time UV levels to trigger dynamic skincare alerts (e.g., SPF reminders).
    *   **Humidity & Air Quality:** Providing environmental context (temperature, humidity) to refine product recommendations based on formulation (e.g., suggesting water-based vs. occlusive products).
*   **Integration:** Queried directly via `OpenMeteoService` in the `core:network` module, leveraging local device coordinates for maximum privacy.

## 6. Google Gemini AI (Generative AI)
A multimodal large language model used to bridge the gap between raw data (OCR/OBF) and a premium editorial experience.

*   **SDK:** `com.google.ai.client.generativeai` (Google AI SDK for Android)
*   **Purpose:**
    *   **Metadata Enrichment:** Extracting "Professional Facets" (Formulation, Chemistry Base, Finish, Coverage) from photos when they are missing from OBF.
    *   **Text Synthesis:** Cleaning and summarizing messy local OCR text from back panels and instructions.
    *   **Fashion Persona:** Specifically tuned for garment analysis in the `ClothingCapture` workflow.
    *   **Color Validation:** Correlating user-sampled hex codes with visual evidence to ensure high-fidelity color matching.

## 7. Google ML Kit (Local On-Device Intelligence)
While not a "web service" in the traditional sense, these libraries provide the intelligence layer for real-time interaction.

*   **Libraries:**
    *   `com.google.mlkit:text-recognition`
    *   `com.google.mlkit:barcode-scanning`
*   **Purpose:**
    *   **Local OCR:** Instantly reading ingredients lists and instructions before sending to Gemini.
    *   **Price Extraction:** Recognizing currency patterns on price tags for the budgeting features.
    *   **Barcode Scanning:** Handling the high-speed barcode detection used to trigger OBF lookups.

## 8. Local Analytics Engine (Heuristic Mapper)
A proprietary set of Kotlin classes (`ObfTaxonomyMapper`, `LocalProductAnalyzer`) that runs offline.

*   **Purpose:**
    *   Mapping OBF ingredient IDs (e.g., `en:dimethicone`) to clinical categories (e.g., `Silicone-Based`).
    *   Calculating "Cost Per Use" and "Remaining Value" based on local usage telemetry.
    *   Generating "Works Well With" recommendations by matching color palettes across the inventory.
