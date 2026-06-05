# Implementation Plan - Frosted Glass Hydration UI

I will refine the Hydration tracking component to simulate the aesthetic of "water inside a frosted glass," using procedural gradients, layered wave paths, and advanced Compose graphics modifiers.

## 1. Research & Design Analysis
- **Frosted Effect**: Requires a semi-transparent surface with a subtle white border and high-gloss specular highlights.
- **Liquid Physics**: Instead of a single flat wave, I will use layered `Path` animations with varying frequencies and opacities to create depth.
- **Color Palette**: Transition from a static blue to a `Brush.verticalGradient` representing deep water to a clear surface.

## 2. Technical Steps

### UI Refinement (`StyleHealthDashboard.kt`)
- [ ] **Container Refactor**:
    - Update the `HydrationVisualRefined` card to use a `Box` with a `Brush.linearGradient` background (Light Blue -> White).
    - Apply a white inner border to simulate the glass edge.
- [ ] **Liquid "Water" Engine**:
    - Refactor `WavyBackground` to draw **multiple overlapping paths**.
    - Implement a `verticalGradient` on the water path itself.
    - Add a "gloss" layer—a thin, high-opacity white path at the top of the water level to simulate light hitting the surface.
- [ ] **Frosted Buttons**:
    - Update `HydrationButton` and other utility buttons to use a more pronounced "Glassmorphism" effect.
    - Combine `Color.White.copy(alpha = 0.2f)` with a `1.dp` solid white border and a subtle shadow.

## 3. Visual Standards
- **Wave Geometry**: Sinusoidal paths with a "shimmer" effect achieved by offsetting the X-phase of the wave over time (if possible) or by stacking static paths with different periods.
- **Transparency**: Maintain a high-alpha "clean" look consistent with the Atelier design language.

## 4. Verification
- [ ] Ensure the liquid level matches the `current / goal` progress accurately.
- [ ] Verify that text is perfectly sharp and legible against the new complex background.
- [ ] Build and run `:applications:kocolor:apps:mobile`.

---
<!-- feedback_request -->
I've planned a procedural "liquid engine" for your hydration card that uses layered gradients and wave paths to create that "water in a glass" look without needing static assets.

**Should I proceed with the liquid engine implementation?**
