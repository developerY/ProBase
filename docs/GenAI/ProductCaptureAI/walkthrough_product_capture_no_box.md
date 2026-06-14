# Walkthrough - Front & Back Product Capture

I have implemented a streamlined **Front & Back Product Capture** sequence as a faster alternative for users who don't have the original product box.

## New Feature: Streamlined 2-Photo Mode

The AI scanning system now supports two distinct capture modes:

1.  **Box Mode (7 Photos)**: Comprehensive scan of all sides of the packaging. Best for maximum data accuracy when the box is available.
2.  **Product Mode (2 Photos)**: Quick scan of just the **Front** and **Back** of the product container. Ideal for products already out of their box.

### UI Integration
- **[StitchProductBuilder](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/StitchProductBuilder.kt)**: Added a new **"Scan Product"** icon (`PhotoCamera`) next to the "Scan Box" icon in the barcode section.
- **Improved Failover**: When a barcode scan fails, the "Product Not Found" dialog now offers both **"SCAN FULL BOX"** and **"SCAN FRONT/BACK ONLY"** as recovery options.

### Intelligent Camera Sequencing
- **[BoxCaptureScreen](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureScreen.kt)**: The camera UI automatically adapts its step indicators (e.g., "STEP 1/2" for Product mode) based on the selected sequence.
- **[BoxCaptureViewModel](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureViewModel.kt)**: Refactored to handle mode-specific sequencing and AI prompt tailoring.

## Technical Details
- Updated **[KoColorRoute](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/KoColorRoute.kt)** to pass the capture mode via navigation.
- Tailored the Gemini Vision prompt to account for the reduced context in Product mode while still aiming for high-fidelity extraction of ingredients and brand details.

## Verification
- **Module Build**: Successfully built `:applications:kocolor:features:boxcapture`.
- **App Integration**: Both "Scan Box" and "Scan Product" buttons correctly launch the camera with their respective sequences.
- **Failover Logic**: Verified the recovery dialog provides both options upon barcode failure.

> [!TIP]
> The Front/Back mode is highly effective for identifying ingredients from the back of containers even without the box context!
