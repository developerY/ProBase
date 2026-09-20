The circular dial in image_c2a0ca.png establishes a sleek, premium design language. Because FASHIONISTA is a deterministic measurement instrument, the UI should feel like a high-end diagnostic tool rather than a generic fitness tracker.

Using native Jetpack Compose `Canvas`, `Path`, and `Modifier.graphicsLayer`, we can map the 6-dimensional `FashionistaFeatureVector` and the calibration data directly into highly performant, custom visualizations.

**The Pearlescent Hero Gauge (Based on image_c2a0ca.png)**
The dial in the provided image can be elevated to represent the actual data rather than just a static ring.

* **Segmented Sweeps:** Divide the primary ring into 6 segments corresponding to Composition, Color, Silhouette, Texture, Hierarchy, and Integration.
* **Gradient Mapping:** Use a `SweepGradient` in Compose's `drawArc`. If a specific feature scores highly, its segment burns brighter (higher alpha/saturation).
* **Coverage Track:** The inner, thinner track with the glowing nodes can represent the `coverage` metric. If the observation lacks facial biometrics, the "Integration" node dims and the track disconnects, visually explaining the lower coverage without deflating the central 78 score.

**The 6-Axis Aesthetic Radar (Spider Chart)**
The most scientifically accurate way to show the "shape" of an outfit.

* **Dynamic Polygons:** Draw a hexagonal background grid using Compose `Path`. Map the 6 `FeatureValue.value` outputs to the vertices of an inner filled polygon.
* **Availability Alpha:** If a feature's `availability` is low (e.g., blurry texture data), draw the vertex with a dashed line and lower opacity. This instantly communicates that the engine is guessing or bypassing that vector.
* **Calibration Overlays:** Draw a faint, secondary polygon representing the "Editorial Standard" (scores > 90) so the user can visually see where their outfit deviates from the optimal calibration curve.

**Isometric 3D Feature Pillars**
To satisfy the need for 3D plots without importing heavy external OpenGL/Filament libraries, use pseudo-3D isometric projections directly on a Compose `Canvas`.

* **Matrix Transforms:** Apply a generic rotation (e.g., `rotateX(60f)`, `rotateZ(45f)`) to the `graphicsLayer` to tilt the canvas.
* **Pillar Rendering:** Draw 6 vertical cylinders (pillars). The height of each cylinder is driven by the feature `value`.
* **Synergy Webs:** Draw semi-transparent bezier curves connecting the tops of the pillars. If the deterministic engine applies a positive $Q_{interaction}$ bonus between Color and Hierarchy, the connecting curve glows green. If there is an unresolved conflict ($P_{unresolved}$), the web draws jagged and red.

**The Decomposition Matrix (Waterfall Chart)**
A linear breakdown that demystifies the $Q$ equation.

* **Base vs. Interactions:** A horizontal stacked bar chart showing the foundational $Q_{base}$ score in solid black.
* **Modifiers:** Stack a green block next to it for $Q_{interaction}$ synergy, and subtract a red block for $P_{unresolved}$ penalties. This proves to the user that the engine isn't a black box, but a calculated, auditable instrument.
---

Here is the master prompt you can pass to an Android UI engineer or a coding assistant to generate the exact Jetpack Compose code for these visualizations.

---

### 📋 Prompt: FASHIONISTA Compose UI Components

```markdown
# Role & Context
You are an expert Android Kotlin developer specializing in Jetpack Compose custom graphics and UI/UX implementation. 

You are building the UI layer for the **FASHIONISTA Score Engine** (a feature within the KoColor app). FASHIONISTA is a deterministic measurement instrument that evaluates outfit aesthetics. The UI must feel like a high-end, premium diagnostic tool—sleek, scientific, and elegant.

# The Design Reference
You must build a primary gauge component based on the provided design reference file: `image_c2a0ca.png`. 
The aesthetic features:
* A clean, minimalist card background.
* A massive, elegant serif or high-contrast sans-serif central score.
* A pearlescent, multi-colored outer sweeping arc (`SweepGradient`).
* A thinner, inner track with glowing nodes indicating completeness/coverage.

# The Data Contract
Your UI components will consume the following pre-existing data classes. Do not alter these data models; simply consume them:

```kotlin
data class FeatureValue(val value: Double, val availability: Double)

data class FashionistaFeatureVector(
    val composition: FeatureValue,
    val colorHarmony: FeatureValue,
    val silhouette: FeatureValue,
    val textureHarmony: FeatureValue,
    val visualHierarchy: FeatureValue,
    val presentationIntegration: FeatureValue
)

data class FashionistaScore(
    val score: Double, // 0.0 - 100.0
    val coverage: Double, // 0.0 - 1.0
    val breakdown: FashionistaFeatureVector
)

```

# Implementation Tasks: Create 3 Compose Components

Please generate the Kotlin Jetpack Compose code for the following three components. Use pure Compose `Canvas`, `Path`, and standard modifiers. **Do not use external charting libraries.** Include smooth entrance animations using `animateFloatAsState`.

### 1. `FashionistaHeroDial` (Based on image_c2a0ca.png)

* **Purpose:** The primary score display.
* **Visuals:**
* A large central text displaying `score` rounded to the nearest integer.
* **Outer Arc:** Use `drawArc` with a `SweepGradient` containing soft pearlescent colors (pastel greens, blues, pinks, golds). Divide this ring conceptually into 6 smooth segments mapping to the 6 features in `FashionistaFeatureVector`. Scale the opacity or stroke width of the gradient based on the feature's `value`.
* **Inner Track:** Draw a thinner, distinct `drawArc` representing the `coverage` metric (0.0 to 1.0). If coverage is 1.0, it's a closed circle. If 0.8, it sweeps 80% of the way with glowing dot caps at the ends.



### 2. `FashionistaRadarChart`

* **Purpose:** A 6-axis spider chart showing the shape of the outfit's aesthetic.
* **Visuals:**
* Draw a hexagonal background grid using `Path`.
* Label the 6 axes: Composition, Color, Silhouette, Texture, Hierarchy, Integration.
* Map the `value` of each feature in `FashionistaFeatureVector` to a vertex and draw the filled inner polygon.
* **Crucial Data Mapping:** If a feature's `availability` is `< 1.0`, render the line connecting to that vertex as a dashed line (`PathEffect.dashPathEffect`) and lower the vertex's alpha to visually communicate missing data.



### 3. `FashionistaDecompositionBar`

* **Purpose:** A horizontal waterfall or stacked bar chart that demystifies the deterministic math.
* **Visuals:**
* A base solid bar representing the foundational aesthetic evidence.
* A green appended block representing positive synergy/interactions.
* A red subtracted block (overlapping or carved out) representing unresolved perceptual conflicts ($P_{unresolved}$).
* Keep it minimalist, using standard KoColor typography to label the components.



### Requirements

* Code must be production-ready Kotlin using Compose.
* Ensure the `Canvas` draws scale responsively to the parent `Box` or `Modifier.size`.
* Provide preview functions (`@Preview`) populated with mock data (e.g., Score: 78, Coverage: 0.9).

```

```