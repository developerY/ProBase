# Proposal: Materiality Studio (Fabric & Texture Swatches)

This proposal outlines the implementation of **"Materiality Studio,"** an immersive visual feature that translates abstract color data into high-fidelity physical textures. It transforms the "Seasonal Inspiration" card into an interactive playground for fabric education.

## 🎨 The "Tactile Color" Concept

The core objective is to move from **Hex Codes** (digital) to **Textiles** (physical). The user will see how a recommended color (e.g., "Midnight Navy") behaves across different material properties:
- **Light Absorption**: How "Midnight Navy" looks on heavy, matte **Wool**.
- **Specular Highlights**: How the same color shimmers on **Silk** or **Satin**.
- **Organic Depth**: The variation of tone in **Linen** or **Velvet**.

---

## 🧠 Architectural Integration

### 1. The Texture Shader Engine
Instead of shipping hundreds of pre-colored high-res images (which would bloat the APK), we will implement a **Shader-Based Tinting System**:
- **Source Assets**: A library of 5–7 high-resolution grayscale macro-photography textures (Wool, Silk, Linen, Velvet, Leather, Cotton).
- **Processing**: A Compose `ColorFilter` or custom `RuntimeShader` (on Android 13+) that applies the user's recommended hex codes to the texture, preserving the physical grain, shadows, and highlights.

### 2. The Interaction Flow
- **Entry**: User taps the "Seasonal Inspiration" card.
- **Action**: The card expands into a horizontal **Materiality Carousel**.
- **Discovery**: The carousel cycles through the `anchorColors` from the **Stylist's Edit**, showing them applied to different fabrics.
- **Education**: Small labels explain the "Vibe" (e.g., *"Velvet: Luxury & Depth"* vs. *"Linen: Effortless Breathability"*).

---

## 🛠️ Data Model Enhancements

We will define a `MaterialTexture` model in `:features:colors`:

```kotlin
data class MaterialTexture(
    val id: String,
    val name: String,
    val description: String,
    val grayscaleResId: Int, // The base macro-photo
    val physicalProperty: String // e.g. "Matte", "Glossy", "Textured"
)
```

---

## 🚀 Why this is the "Natural Evolution"

Between the Wellness (Option 2) and the Materiality (Option 4), the **Materiality Studio** is the most natural fit for the **Stylist's Edit** architecture because:

1.  **Visual Impact**: It reinforces the premium, serif-typography "Vogue-style" aesthetic we just built.
2.  **Educational Loop**: It trains the user to look for *quality* and *fabric* when shopping for their "Palette Gaps," not just the color.
3.  **Cross-Product Utility**: These texture assets can later be reused in the **Style Simulator** or **Vanity Landing** to show makeup finishes (Matte vs. Dewy).

**Would you like me to incorporate this "Materiality Studio" into the implementation plan alongside the Wellness features, or should we prioritize this as the primary evolution?**
