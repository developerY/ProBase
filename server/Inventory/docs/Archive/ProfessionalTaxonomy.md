# KoColor Professional Taxonomy

This document outlines the three-tier classification system used to organize the **Glow Archive**. This taxonomy ensures algorithmic synergy between color analysis, product layering, and inventory management.

---

## 🟢 Level 1: Macro Categories (The UI Layer)
These are intuitive "buckets" designed for quick body-zone mapping and navigation within the app.

*   **Skincare & Prep**: Applied before pigment to prepare the canvas.
*   **Complexion (Base)**: Products designed to unify and even out skin tone.
*   **Color & Dimension**: Adds life, shadow, and light to the face.
*   **Eyes & Brows**: Defines the upper face and eye area.
*   **Lips**: Specialized care and color for the lip zone.
*   **Tools & Hygiene**: Equipment for application and professional sanitization.

---

## 🔵 Level 2: Micro Categories (Product Type)
Technical classifications that ensure a clean, searchable database of specific product formats.

*   **Skincare**: Cleanser, Toner, Serum, SPF, Primer.
*   **Complexion**: Foundation, Concealer, Setting Powder.
*   **Dimension**: Blush, Bronzer, Contour, Highlighter.
*   **Eyes**: Eyeshadow, Eyeliner, Mascara, Brow Gel.
*   **Lips**: Lipstick, Gloss, Liner, Stain, Balm.

---

## 🟣 Level 3: Professional Facets (The Engine Layer)
Expert-status attributes used by the AI engine for filtering and calculating product compatibility.

*   **Formulation**: The physical state of the product (Liquid, Cream, Powder, Gel, Balm).
*   **Chemistry**: The base ingredient profile (Water, Silicone, or Oil). *Critical for preventing layering pilling.*
*   **Finish**: The visual result on the skin (Matte, Satin, Radiant, Metallic, Glitter).
*   **Coverage**: The opacity level (Sheer, Light, Medium, Full, Buildable).
*   **Temperature**: Engine-aligned color bias (Warm, Cool, Neutral, Olive).

---
**Status**: Implementation Finalized
**Usage**: Data mapping in `CosmeticItem.kt` and UI filtering in `VanityLandingScreen.kt`.
