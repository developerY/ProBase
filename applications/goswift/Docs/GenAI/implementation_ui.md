# Visual Overhaul for Caffeine and Hydration

This plan outlines the steps to add advanced visual components to GoSwift: a "Caffeine Clock" with decay arcs and a "Wavy Water Level" visualization for hydration.

## User Review Required

- **Decay Physics**: The caffeine arcs will represent the concentration over time using a standard half-life model (ADME). Multiple shots will sum their concentrations.
- **UI Performance**: These visualizations will use custom drawing (`Canvas`) and animations which are performant in Jetpack Compose.

## Proposed Changes

### GoSwift Feature Shots Module (`applications/goswift/apps/mobile/features/shots`)

#### [NEW] [CaffeineClock.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/shots/src/main/java/com/zoewave/probase/goswift/mobile/shots/ui/components/CaffeineClock.kt)
- Custom Composable using `Canvas`.
- Draws a clock face.
- Draws blue arcs starting from the time of each shot, with the arc's angular width or opacity representing the remaining caffeine level over a 24-hour period.

#### [ShotsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/shots/src/main/java/com/zoewave/probase/goswift/mobile/shots/ui/ShotsUiRoute.kt)
- Integrate `CaffeineClock` at the top of the screen.

---

### GoSwift Feature Hydration Module (`applications/goswift/apps/mobile/features/hydration`)

#### [NEW] [WavyWaterLevel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/hydration/src/main/java/com/zoewave/probase/goswift/mobile/hydration/ui/components/WavyWaterLevel.kt)
- Custom Composable using `Canvas` and `Animatable`.
- Implements a wave effect using a Sine wave path.
- Vertical level corresponds to the percentage of the daily hydration goal reached.

#### [HydrationUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/hydration/src/main/java/com/zoewave/probase/goswift/mobile/hydration/ui/HydrationUiRoute.kt)
- Replace the static progress bar with the `WavyWaterLevel` component.

---

## Verification Plan

### Manual Verification
- **Caffeine Arcs**: Log multiple shots at different times and verify that the arcs appear on the clock at the correct positions and correctly visualize the decay over time.
- **Wavy Water**: Log water intake and verify the water level rises smoothly with a continuous wave animation.
- **Composition**: Ensure the new components fit well within the 3-tab layout.
