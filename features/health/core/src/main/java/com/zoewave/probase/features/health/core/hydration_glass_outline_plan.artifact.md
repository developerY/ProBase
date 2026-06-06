# Implementation Plan - Glass Silhouette Outline & Custom Hydration Slider

I will refine the Hydration UI to emphasize the "glass" silhouette with a visible outline and implement an interactive slider for custom water entry, all while maintaining the premium frosted glass aesthetic.

## 1. Research & Visual Analysis
- **Glass Outline**: The current implementation clips the liquid but the outer container (Card) is still mostly rectangular. I will apply the tapered `glassShape` to the border and container itself to make the silhouette "pop".
- **Custom Entry**: Replace the static "+ Custom Amount" button with an expandable section containing a `Slider` for precise volume selection (e.g., 50ml to 1000ml).

## 2. Technical Steps

### UI Refinement (`StyleHealthDashboard.kt`)
- [ ] **Glass Outline Enhancement**:
    - Refactor `HydrationVisualRefined` to use a `Surface` or `Box` with the custom `glassShape` as its primary boundary.
    - Apply a high-specular white-to-transparent gradient border (`2.dp`) to this shape to simulate the glass edge.
    - Add a "weighted base" effect at the bottom of the glass.
- [ ] **Expandable Custom Slider**:
    - Introduce `var showCustomSlider by remember { mutableStateOf(false) }`.
    - Create an `AnimatedVisibility` section that reveals a `Slider` when "Custom Amount" is tapped.
    - Include a "Log [Value]ml" confirm button with a frosted glass look.
- [ ] **Interactive States**:
    - Ensure the slider thumb and track follow the blue/transparent glass theme.
    - Update `onAdd` to handle the custom values from the slider.

## 3. Visual & Aesthetic Standards
- **Refraction**: Use subtle `blur` and `alpha` layers to ensure the "liquid" looks like it's inside the frosted glass.
- **Micro-interactions**: Smooth transitions for the slider expansion.

## 4. Verification
- [ ] Verify the tapered glass outline is clearly visible on the background.
- [ ] Test the slider's range and logging precision.
- [ ] Build and run `:applications:kocolor:apps:mobile`.

---
<!-- feedback_request -->
I've designed the "Glass Silhouette" update to make the hydration card actually look like a tapered drinking glass with a visible rim. I'll also add the custom slider you requested for precise logging.

**Should I proceed with the glass outline and custom slider implementation?**
