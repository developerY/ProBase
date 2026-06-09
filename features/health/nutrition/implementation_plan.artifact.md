# Implementation Plan - Total Unsplash Localization for KoColor

I will localize all remaining Unsplash image calls across the KoColor application to ensure a spectacular, high-performance, and offline-capable user experience.

## 1. Research & Asset Audit
I have identified the following categories of remote images:
- **Vanity Categories**: 5 images for skincare, complexion, color, eyes, and lips.
- **Wardrobe Categories**: 2 images for shoes and accessories.
- **Routine Hero Fallbacks**: Default image for ritual stages.
- **Advice & Suggestions**: Fallback images for AI fashion advice.
- **Database Defaults**: Initial images for default clothing and cosmetic items.

## 2. Technical Steps

### Asset Placeholder Generation
- [ ] Create XML color placeholders for all new local resources in the following modules:
    - `:applications:kocolor:features:cosmetics`
    - `:applications:kocolor:features:inventory`
    - `:applications:kocolor:features:routines`
    - `:applications:kocolor:apps:mobile:features:home`
    - `:applications:kocolor:db` (for default data)

### Code Refactoring
- [ ] **Cosmetics**: Update `VanityLandingScreen.kt` to use local drawable IDs for category sections.
- [ ] **Inventory**: Update `WardrobeLandingScreen.kt` for shoes and accessories.
- [ ] **Routines**: Update `StepHeroPage.kt` fallback image.
- [ ] **Home Hub**: Update `CollectionDetailScreen.kt` advice fallbacks.
- [ ] **Database Defaults**: Update `ClothingDefaults.kt` and `CosmeticDefaults.kt` to use local resource paths or IDs if supported (will use a helper or string mapping).

### Asset Integration Guide
- [ ] Provide the user with the complete list of Unsplash URLs and their corresponding local resource names for manual download.

## 3. Visual & Aesthetic Standards
- **Atelier Consistency**: Ensure that even as placeholders, the local resources use a soft palette consistent with the "Atelier" design language.
- **High-Fidelity**: All localized images will be intended for high-resolution display.

## 4. Verification
- [ ] Build and verify that all `AsyncImage` calls compile correctly with `R.drawable.*`.
- [ ] Verify that the app functions perfectly offline with these localized elements.
- [ ] Build and run `:applications:kocolor:apps:mobile`.

---
<!-- feedback_request -->
I've designed a plan to remove all external network dependencies for your primary visual assets. This will make the app significantly faster and more spectacular.

**Should I proceed with the total asset localization?**
