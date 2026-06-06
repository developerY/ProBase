# Implementation Plan - Glass Silhouette & Goal Navigation

I will further refine the Hydration component by morphing the container into a realistic "glass" shape and adding a direct navigation link to settings for goal management.

## 1. Research & Design Analysis
- **Glass Silhouette**: A standard drinking glass often has a slightly tapered base and curved shoulders. I will implement a custom `Shape` that provides this specific profile.
- **Goal Navigation**: The "of L goal" text should be actionable, encouraging users to personalize their targets.

## 2. Technical Steps

### UI Refinement (`StyleHealthDashboard.kt`)
- [ ] **Custom `GlassShape`**:
    - Implement a `GenericShape` that:
        - Maintains wide straight edges at the top.
        - Gently tapers inward towards the base.
        - Has rounded corners on the bottom for a "weighted glass" feel.
- [ ] **Hydration Container Update**:
    - Apply the `GlassShape` to the `HydrationVisualRefined` card.
    - Adjust the `WavyBackground` to ensure the liquid clipping matches the new tapered silhouette.
- [ ] **Interactive Goal Link**:
    - Wrap the goal text (`"of %.1fL goal"`) in a `TextButton` or a clickable `Box`.
    - Style it with a subtle underline or a "Settings" chevron to indicate interactivity.
    - Trigger `navTo(KoColorRoute.Settings)` on click.

## 3. Visual Standards
- **Clarity**: The "glass" shape must be distinct but subtle enough to not distract from the primary data.
- **Accessibility**: The navigation link must have a sufficient touch target size.

## 4. Verification
- [ ] Verify the "Glass" silhouette renders correctly on various screen sizes.
- [ ] Test the navigation from the goal text to the Settings screen.
- [ ] Build and run `:applications:kocolor:apps:mobile`.

---
<!-- feedback_request -->
I've updated the plan to include the tapered glass shape and the settings navigation. This will make the hydration tracking both more metaphorical and more functional.

**Should I proceed with the glass shape and navigation updates?**
