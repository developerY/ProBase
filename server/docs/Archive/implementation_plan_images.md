# Implementation Plan: KoColor UI Landscape Refinement (Origin-Blind)

Update the cosmetic UI to natively support the new 4:3 landscape professional assets while ensuring a seamless, crop-safe experience for user-captured CameraX images.

## Proposed Changes

### 1. Cosmetic Product Grid
#### [MODIFY] [CosmeticProductGridCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/components/CosmeticProductGridCard.kt)
- Change `aspectRatio` from `0.75f` (portrait) to **`4f / 3f`** (landscape).
- Ensure `ContentScale.Crop` is used to prevent letterboxing for non-landscape images.

### 2. Cosmetic Detail Screen
#### [MODIFY] [CosmeticDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/CosmeticDetailScreen.kt)
- Update the hero image container `aspectRatio` from `1f` (square) to **`4f / 3f`**.
- Switch `ContentScale.Fit` to **`ContentScale.Crop`** to eliminate the checkerboard bars on all asset types.

## Verification Plan
- Deploy the app and verify the "Glow Catalyst Lip Stain" (CDN) fits perfectly.
- Take a portrait photo with the camera and verify it is center-cropped to the landscape container without empty gaps.
