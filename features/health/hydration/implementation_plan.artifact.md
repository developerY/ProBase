# Implementation Plan - Pure Crystal Glass (No Border)

I will overhaul the hydration buttons and cards to a "Pure Crystal" design, removing the visible gray/dark borders entirely and relying on soft light-based physics for depth.

## 1. Research & Analysis
- **Problem**: The current 1.5dp gradient border, when combined with shadows and transparency, creates a "muddy" or gray ring that clashes with the clean aesthetic.
- **Goal**:
    - Remove all explicit `BorderStroke` from functional components.
    - Achieving depth using only **Shadows** and **Internal Highlights**.
- **Aesthetic Refinements**:
    - **Surface**: Use a multi-layer approach:
        - Layer 1: Soft drop shadow for elevation.
        - Layer 2: Ultra-transparent white surface (`alpha 0.1f`).
        - Layer 3: A "Rim Light" effect—a thin white line drawn inside the top-left edge via `Canvas` to simulate light catching the glass.

## 2. Technical Steps

### UI Overhaul (`HydrationUiRoute.kt`)
- [ ] **`QuickHydrationButton` Refinement**:
    - Remove the `border` parameter from the `Surface`.
    - Increase `shadow` elevation slightly but use a very soft, low-alpha color to avoid a "dirty" look.
    - Add a `Canvas` overlay to draw a 1dp white arc at the top-left corner to simulate the "crystal rim."
- [ ] **Custom Amount Pill Refinement**:
    - Apply the same "no-border" logic.
    - Ensure the internal icon and text remain sharp.
- [ ] **Reminder Card Refinement**:
    - Sync the "Reminder" card to the same borderless standard.

## 3. Visual & Aesthetic Standards
- **Depth**: The buttons should feel like they are carved directly out of light.
- **Color Palette**: Zero Grays. Only pure White, transparent overlays, and the underlying Blue background gradients.

## 4. Verification
- [ ] Verify the "gray ring" is completely eliminated.
- [ ] Confirm the buttons look premium and "crystal-like" through highlights rather than outlines.
- [ ] Ensure the background waves remain clearly visible through the components.
- [ ] Build and run `:applications:kocolor:apps:mobile`.

---
<!-- feedback_request -->
I've designed a "Pure Crystal" overhaul that removes all outlines. We'll use internal light highlights (rim lighting) to define the shapes instead, creating a much cleaner and higher-end look.

**Should I proceed with the borderless crystal design?**
