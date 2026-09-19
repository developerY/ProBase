# KoColor Product Capture Refactor Walkthrough

This document summarizes the comprehensive refactor of the cosmetic product capture system, shifting from a simple camera interface to an intelligent, multi-modal hybrid extraction pipeline.

## 1. Multi-Modal Capture Paths
The entry point in `StitchProductBuilder` now supports three distinct capture strategies tailored to the product type:

- **Bar Scan**: Rapid UPC lookup using the local database or Open Beauty Facts (OBF).
- **Box Scan**: For products with packaging. Includes a dynamic **3 / 7 shot toggle** inside the camera UI to switch between "Quick" (Front, Back, Ingredients) and "Pro" (All 6 sides + ingredients) modes.
- **No Box**: Optimized for loose containers. Uses a 2-pic AI approach (Front/Back) plus an optional barcode step.

## 2. Hybrid Data Intelligence
We implemented a "Confidence Threshold" logic that coordinates database lookups and AI analysis:

- **High Confidence**: If OBF returns the Brand, Name, and Ingredients, the AI is bypassed entirely for a rapid "instant add" experience.
- **Incomplete Hit (The Yellow State)**: If the database identifies the product but lacks ingredients, the app enters **Enrichment Mode**. The "Bar Scan" button turns yellow, and the user is guided to take photos of the ingredients panel.
- **Contextual Prompting**: The Gemini AI prompt is dynamically augmented with verified database info (e.g., "We already know this is NARS Radiant Creamy Concealer..."), allowing the LLM to focus strictly on Extracting the missing visual data.

## 3. Capture Review & Local OCR
A new "Review" screen acts as a staging area before final submission:

- **Visual Manifest**: Users can see all captured photos and the scanned barcode.
- **Local OCR Passes**: The app performs offline text extraction on the ingredients and instructions panels. The raw text is displayed for the user to verify and is passed to Gemini to improve accuracy for complex chemical names.
- **Photo Management**: Users can delete blurry or incorrect photos directly from the review grid and jump back into the camera to retake them.

## 4. Architectural Robustness (Singleton State)
To solve lifecycle wipes during the heavy transition from the external Google Barcode Scanner:

- **State Elevation**: Scan statuses (`ANALYZING`, `SUCCESS`, `FAILED`) were moved from the `CosmeticsViewModel` to the `@Singleton` `FashionSessionRepository`.
- **Race Condition Fix**: This ensures that even if the Android system kills the UI memory while the scanner is open, the lookup results are safely preserved in Application-scoped memory and restored instantly upon return.

## 5. Privacy & Community
- **Save & Add to Online DB**: A new user-controlled toggle allows users to opt-in to contributing their high-fidelity, AI-verified scans back to the Open Beauty Facts community.
- **Offline First**: The system remains zero-footprint and purely local by default.
# KoColor Product Capture Refactor Walkthrough

This document summarizes the comprehensive refactor of the cosmetic product capture system, shifting from a simple camera interface to an intelligent, multi-modal hybrid extraction pipeline.

## 1. Multi-Modal Capture Paths
The entry point in `StitchProductBuilder` now supports three distinct capture strategies tailored to the product type:

- **Bar Scan**: Rapid UPC lookup using the local database or Open Beauty Facts (OBF).
- **Box Scan**: For products with packaging. Includes a dynamic **3 / 7 shot toggle** inside the camera UI to switch between "Quick" (Front, Back, Ingredients) and "Pro" (All 6 sides + ingredients) modes.
- **No Box**: Optimized for loose containers. Uses a 2-pic AI approach (Front/Back) plus an optional barcode step.

## 2. Hybrid Data Intelligence
We implemented a "Confidence Threshold" logic that coordinates database lookups and AI analysis:

- **High Confidence**: If OBF returns the Brand, Name, and Ingredients, the AI is bypassed entirely for a rapid "instant add" experience.
- **Incomplete Hit (The Yellow State)**: If the database identifies the product but lacks ingredients, the app enters **Enrichment Mode**. The "Bar Scan" button turns yellow, and the user is guided to take photos of the ingredients panel.
- **Contextual Prompting**: The Gemini AI prompt is dynamically augmented with verified database info (e.g., "We already know this is NARS Radiant Creamy Concealer..."), allowing the LLM to focus strictly on Extracting the missing visual data.

## 3. Capture Review & Local OCR
A new "Review" screen acts as a staging area before final submission:

- **Visual Manifest**: Users can see all captured photos and the scanned barcode.
- **Local OCR Passes**: The app performs offline text extraction on the ingredients and instructions panels. The raw text is displayed for the user to verify and is passed to Gemini to improve accuracy for complex chemical names.
- **Photo Management**: Users can delete blurry or incorrect photos directly from the review grid and jump back into the camera to retake them.

## 4. Architectural Robustness (Singleton State)
To solve lifecycle wipes during the heavy transition from the external Google Barcode Scanner:

- **State Elevation**: Scan statuses (`ANALYZING`, `SUCCESS`, `FAILED`) were moved from the `CosmeticsViewModel` to the `@Singleton` `FashionSessionRepository`.
- **Race Condition Fix**: This ensures that even if the Android system kills the UI memory while the scanner is open, the lookup results are safely preserved in Application-scoped memory and restored instantly upon return.

## 5. Privacy & Community
- **Save & Add to Online DB**: A new user-controlled toggle allows users to opt-in to contributing their high-fidelity, AI-verified scans back to the Open Beauty Facts community.
- **Offline First**: The system remains zero-footprint and purely local by default.


## 6. AI-Driven Color Calibration**
To combat inaccurate color sampling caused by environmental lighting and shadows, the color extraction pipeline has been shifted from a deterministic to a probabilistic model:

- **Guided Sampling:** The manual UI color picker (sampling reticle) now functions strictly as a "User Color Hint" rather than a hardcoded truth.
- **The Discrepancy Rule:** The system utilizes Gemini's crossmodal reasoning to evaluate the user's color hint against the physical packaging and embedded industry knowledge. If a discrepancy is detected (e.g., the reticle sampled a shadow), the AI overrides the user input and returns the true, accurate cosmetic hex code.
- **Consistent Persona:** The LLM is anchored as a "Professional Cosmetic Analyzer" to prioritize technical accuracy over simply validating the user's manual input.

---

### Modified Components:

- [CosmeticsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/CosmeticsViewModel.kt): Unified state coordination and confidence logic.
- [FashionSessionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/repository/FashionSessionRepository.kt): Singleton source of truth for capture sessions.
- [BoxCaptureViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureViewModel.kt): AI prompt orchestration and local OCR management.
- [BoxCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureScreen.kt): Multi-step camera UI and review staging.
- [StitchProductBuilder.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/StitchProductBuilder.kt): Consolidated capture entry points and feedback dialogs.

---

### Modified Components:

- [CosmeticsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/CosmeticsViewModel.kt): Unified state coordination and confidence logic.
- [FashionSessionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/repository/FashionSessionRepository.kt): Singleton source of truth for capture sessions.
- [BoxCaptureViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureViewModel.kt): AI prompt orchestration and local OCR management.
- [BoxCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/boxcapture/src/main/java/com/zoewave/probase/kocolor/features/boxcapture/ui/BoxCaptureScreen.kt): Multi-step camera UI and review staging.
- [StitchProductBuilder.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/cosmetics/src/main/java/com/zoewave/probase/kocolor/features/cosmetics/ui/StitchProductBuilder.kt): Consolidated capture entry points and feedback dialogs.
