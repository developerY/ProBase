# Task - Hydration UI Refinement: Glass Silhouette & Custom Slider

Refining the Hydration dashboard to feature a tapered glass silhouette and an interactive custom volume slider.

## Status
- [ ] Implement tapered glass silhouette using `glassShape`.
- [ ] Add state management for custom volume selection.
- [ ] Implement expandable `Slider` for custom water entry.
- [ ] Style all new elements with frosted glass aesthetics.
- [ ] Verify functionality and visual alignment.

## Technical Details
- **Shape**: Use `GenericShape` to taper the bottom of the hydration card by ~8%.
- **Liquid Clipping**: Ensure the wavy animation is clipped exactly to the tapered glass boundary.
- **Slider**: Range 50ml to 1000ml with 10ml steps.
- **Glassmorphism**: Use `Surface` with `white(0.3f)` alpha and `blur` for interaction layers.
