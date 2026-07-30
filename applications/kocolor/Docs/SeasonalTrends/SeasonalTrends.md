# Seasonal Trends: Immersive Editorial Module

The **Seasonal Trends** module is a premium, immersive feature within KoColor designed to bridge the gap between static color data and professional fashion storytelling. It transforms user color profiles into tactile, narrative experiences.

## 🌟 Vision
Move beyond mere "recommendations" by providing a high-end, editorial experience that inspires style shifts through:
1. **Dynamic Visuals**: Procedurally generated backgrounds that reflect material properties.
2. **AI Narratives**: Context-aware styling advice generated via local LLMs.
3. **Seamless Discovery**: Zero-friction transitions from the analytical dashboard to immersive content.

---

## 🧩 Technical Pillars

### 1. Frosted Glass Morph Shader (`FluidInspirationShader.kt`)
To create a high-fashion, "frosted glass" aesthetic, we use a morphing AGSL shader.
- **The Morph**: As the card expands, the `frostAmount` animates from 0.0 (sharp) to 1.0 (frosted), creating a seamless transition from a clear image to a tactile background.
- **Tactile Jitter**: Uses high-frequency noise to displace UVs, simulating the "etched" look of frosted glass.
- **Grainy Shimmer**: Adds a subtle, moving grit to the surface to give it physical presence and depth.
- **Milky Tint**: Mixes in a soft white luminance to achieve the classic "milk glass" effect seen in premium UI.

### 2. Shared Element Transitions
We utilize the **SharedTransitionLayout** to maintain visual continuity.
- **The Flow**: A small preview card on the "Color Hub" dashboard expands into a full-screen immersive editorial.
- **User Benefit**: Reduces cognitive load by visually tracking the context shift from "Dashboard" to "Inspiration."

### 3. Gemini AI Curation
The editorial copy is generated dynamically based on the user's specific **Seasonal Type** and **Undertone**.
- **The Prompt**: Uses a "High-End Fashion Editor" persona to explain the biological reason behind specific trends (e.g., Deep Plum velvet for Winter profiles).
- **Infinite Freshness**: The content is unique to each user and updates as trends or user profiles shift.

---

## 🏗️ Architecture

- **Module**: `:applications:kocolor:features:seasonal_trends`
- **Layering**: Strictly follows the **Unidirectional Data Flow (UDF)** pattern.
- **Dependencies**: Uses `:features:colors` for data and `:core:ui` for component styling.

---

## 🗺️ Roadmap
- **Tactile Integration**: Shader variations for specific fabrics (Wool vs. Silk).
- **Direct Commerce**: Deep-linking trend items to the KoColor Store.
- **Wellness Overlay**: Connecting seasonal trends to skincare preparation rituals.
