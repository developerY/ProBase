# Implementation Plan - KoColor Multi-Tab Adaptive Navigation

Implement a three-tab navigation system (Home, Color, Settings) for KoColor using Compose Nav3 and adaptive layout strategies to support foldable devices.

## Proposed Changes

### Model & Navigation

#### [KoColorRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/KoColorRoute.kt)
- Add icon and label properties to top-level routes for the bottom bar.
- Define a list of `topLevelRoutes`.

### Feature: Color

#### [NEW] [applications/kocolor/features/color](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/color)
- Create a new module for the 'Color' tab (blank for now).
- Include `ColorScreen` and `ColorUiRoute`.

### UI Components

#### [NEW] [KoColorBottomBar.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/components/KoColorBottomBar.kt)
- Implement a `NavigationBar` with items for Home, Color, and Settings.

#### [KoColorMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorMainScreen.kt)
- Integrate `NavigationSuiteScaffold` (or custom adaptive logic) to support folding phones/tablets.
- Use `NavigationRail` for expanded layouts and `NavigationBar` for compact layouts.

#### [MainViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/MainViewModel.kt)
- Update navigation logic to handle top-level tab switching (resetting backstack when switching tabs).

---

## Verification Plan

### Automated Tests
- Run `:applications:kocolor:apps:mobile:assembleDebug` to verify build.

### Manual Verification
- Verify the bottom bar appears in compact mode.
- Verify a navigation rail appears in expanded mode (foldable/tablet).
- Verify switching between Main, Color (blank), and Settings tabs works correctly.
- Verify the backstack behavior when switching tabs (standard Nav3 behavior).
