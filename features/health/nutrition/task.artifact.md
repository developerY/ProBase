# Task - Total Unsplash Localization for KoColor

Localizing all remaining Unsplash image references across the application modules.

## Status
- [ ] Create XML placeholders for:
    - [ ] `vanity_skincare`, `vanity_complexion`, `vanity_color`, `vanity_eyes`, `vanity_lips` in `:features:cosmetics`.
    - [ ] `wardrobe_shoes`, `wardrobe_accessories` in `:features:inventory`.
    - [ ] `routine_hero_fallback` in `:features:routines`.
    - [ ] `advice_clothes_fallback`, `advice_makeup_fallback` in `:home`.
- [ ] Update `VanityLandingScreen.kt` in `:features:cosmetics`.
- [ ] Update `WardrobeLandingScreen.kt` in `:features:inventory`.
- [ ] Update `StepHeroPage.kt` in `:features:routines`.
- [ ] Update `CollectionDetailScreen.kt` in `:home`.
- [ ] Update `ClothingDefaults.kt` and `CosmeticDefaults.kt` in `:db`.
- [ ] Provide list of Unsplash URLs for manual asset addition.
- [ ] Verify build status.

## Technical Details
- **Resource Naming**: Consistent lowercase snake_case.
- **Fallbacks**: Temporary XML shapes to allow compilation.
