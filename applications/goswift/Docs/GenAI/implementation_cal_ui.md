# Visual Overhaul for Nutrition (Calories)

This plan outlines the steps to add an advanced visual component to the Nutrition module: a "Calorie Bubble" visualization where logged meals are represented as floating, animated bubbles within a daily energy container.

## User Review Required

- **Visual Style**: The "Calorie Bubble" visualization will show a container that fills up with colorful bubbles representing individual meals. Larger calorie counts will result in larger bubbles.
- **Physics**: Bubbles will have a gentle floating/bobbing animation using `InfiniteTransition`.

## Proposed Changes

### GoSwift Feature Nutrition Module (`applications/goswift/apps/mobile/features/nutrition`)

#### [NEW] [CalorieBubbleContainer.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/nutrition/src/main/java/com/zoewave/probase/goswift/mobile/nutrition/ui/components/CalorieBubbleContainer.kt)
- Custom Composable using `Canvas`.
- Draws a container (like a glass or bowl).
- Draws animated "bubbles" for each meal in `recentMeals`.
- Bubble size is proportional to the meal's calorie count.
- The overall fill level of the container represents progress toward a daily goal (e.g., 2500 kcal).

#### [NutritionUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/features/nutrition/src/main/java/com/zoewave/probase/goswift/mobile/nutrition/ui/NutritionUiRoute.kt)
- Integrate `CalorieBubbleContainer` into the `NutritionScreen`, replacing or augmenting the static `DailyCalorieCard`.

---

## Verification Plan

### Manual Verification
- **Bubble Animation**: Log multiple meals with different calorie values and verify that bubbles of various sizes appear and bob gently.
- **Fill Level**: Verify the container "fills up" as more calories are logged.
- **Consistency**: Ensure the visual style matches the wavy water and caffeine clock.
