# Detailed Walkthrough: On-Device Color & Contrast Calibration

This document provides a comprehensive technical walkthrough of the "Color & Contrast Calibration" system implemented for KoColor. This feature establishes a user's mathematical aesthetic baseline (12-Season Color Profile) while guaranteeing absolute biometric privacy via on-device Edge AI.

---

## 1. Core Philosophy: Zero-Cloud Privacy
The system is designed as a **Zero-Cloud feature**.
- **In-Memory Analysis**: No photos are ever saved to storage or uploaded to a server.
- **Immediate Disposal**: Camera frames are processed as `ImageProxy` objects in RAM, converted to temporary Bitmaps for sampling, and immediately garbage collected.
- **Edge AI**: All facial landmark detection is performed locally using Google ML Kit.

---

## 2. Hardware Layer: Environmental Validation
To ensure the accuracy of the color analysis, I implemented a strict hardware validation gate.

### Lighting Validation (`LightingValidator.kt`)
Bad lighting (fluorescent, low light) ruins color season detection.
- **Sensor Integration**: Monitors the device's `Sensor.TYPE_LIGHT` (Ambient Light Sensor).
- **Daylight Threshold**: Only permits the scan if ambient light is between **300 and 10,000 lux** (the range of natural daylight).
- **Real-Time Feedback**: Communicates status via a `LightingStatus` state (Optimal, Poor, Unknown).

---

## 3. Edge AI Layer: Computer Vision Pipeline
The extraction of aesthetic data happens in a high-performance CameraX pipeline.

### Color Extraction Analyzer (`ColorExtractionAnalyzer.kt`)
This class implements the `ImageAnalysis.Analyzer` interface.
- **Native RGBA Configuration**: Configured the analyzer with `OUTPUT_IMAGE_FORMAT_RGBA_8888`. This allows for direct `imageProxy.toBitmap()` conversion.
- **ML Kit Integration**: Since ML Kit's `fromMediaImage` does not support RGBA, the analyzer converts the frame to a `Bitmap` first and uses `InputImage.fromBitmap()` to ensure compatibility and stability.
- **Landmark Detection**: Uses **ML Kit Face Detection** to locate:
    - `LEFT_CHEEK` / `RIGHT_CHEEK`: For base skin tone and warm/cool undertone estimation.
    - `LEFT_EYE` / `RIGHT_EYE`: For iris luminance.
    - `Forehead Region`: Calculated relative to the face bounding box for hair root luminance.
- **Luminance Sampling**: Uses the standard Rec. 709 weighted formula:
  `Y = 0.2126R + 0.7152G + 0.0722B`

---

## 4. Aesthetic Intelligence: 12-Season Framework
The raw mathematical vectors are mapped to the high-fashion "12-Season" color theory.

### Models (`ColorProfileModels.kt`)
- **`ColorSeason`**: Enum covering the full spectrum (e.g., `BRIGHT_SPRING`, `TRUE_WINTER`).
- **`FacialContrastVector`**: Captures the delta between skin, hair, and eye luminance.
- **`ColorProfile`**: The final result, which includes a mapper to the core `FashionProfile` used by the styling engine.

### Classifier (`ColorSeasonClassifier.kt`)
- **Undertone Logic**: Estimates warmth/coolness by comparing R and B channels on sampled skin pixels.
- **Contrast Logic**: Determines the "Season" based on the `contrastDelta`. High delta (e.g., > 0.6) leads to Winter/Autumn seasons, while low delta leads to Spring/Summer.

---

## 5. Presentation Layer: Premium Compose Experience
The UI is built to feel like a high-end medical or beauty tool.

### Calibration Camera UI (`CalibrationCameraScreen.kt`)
- **Immersive Overlay**: A Compose `Canvas` draws a dashed face reticle and applies a semi-transparent dark mask outside the focus area to guide the user.
- **Modern Permissions**: Uses `rememberLauncherForActivityResult` with `ActivityResultContracts.RequestPermission` to handle camera access strictly within the Composable lifecycle.
- **Feedback Pill**: A floating status component that changes color based on the `LightingValidator` output.

### Orchestration (`CalibrationViewModel.kt`)
- **State Machine**: Manages the flow from `Idle` -> `Scanning` -> `Success`.
- **Repository Integration**: Automatically persists the resulting `ColorProfile` to the `FashionRepository` upon a successful scan, immediately updating the app's global styling context.

---

## 6. Verification & Stability
- **Unit Tested**: 
    - `ColorSeasonClassifierTest` verifies all 12 seasons map correctly to their mathematical definitions.
    - `LightingValidatorTest` ensures the lux thresholds are strictly enforced.
- **Regression Fixes**: Fixed several legacy unit tests in the `:analyzer` module that were failing due to recent model changes, ensuring a 100% green build.
- **Hilt Integration**: Implemented `SensorModule.kt` to provide the `SensorManager` system service, resolving dependency injection errors.

---

### Artifacts Implemented
- [ColorProfileModels.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/calibration/ColorProfileModels.kt)
- [ColorExtractionAnalyzer.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ColorExtractionAnalyzer.kt)
- [ColorSeasonClassifier.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ColorSeasonClassifier.kt)
- [LightingValidator.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/LightingValidator.kt)
- [CalibrationCameraScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ui/CalibrationCameraScreen.kt)
- [CalibrationViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ui/CalibrationViewModel.kt)
- [SensorModule.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/di/SensorModule.kt)
