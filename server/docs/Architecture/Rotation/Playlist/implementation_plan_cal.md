# Implementation Plan - On-Device Color & Contrast Calibration

This plan details the implementation of a Zero-Cloud facial scanning feature to establish a user's mathematical aesthetic baseline (12-Season Color Profile).

## Proposed Changes

### Domain & Data Models
I will implement the core mathematical and aesthetic models in the `model` module.

#### [NEW] [PhenotypeModels.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/calibration/PhenotypeModels.kt)
- Define `ColorSeason` enum: `BRIGHT_SPRING`, `TRUE_SPRING`, `LIGHT_SPRING`, `LIGHT_SUMMER`, `TRUE_SUMMER`, `SOFT_SUMMER`, `SOFT_AUTUMN`, `TRUE_AUTUMN`, `DEEP_AUTUMN`, `DEEP_WINTER`, `TRUE_WINTER`, `BRIGHT_WINTER`.
- Define `FacialContrastVector` data class: `skinLuminance`, `hairLuminance`, `eyeLuminance`, `contrastDelta`.
- Define `PhenotypeProfile` data class: `season`, `undertone`, `contrastVector`, `optimalPaletteHexCodes`.

### Hardware & Edge AI Layer
I will implement the sensors and image analysis logic in the `analyzer` module.

#### [NEW] [LightingValidator.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/LightingValidator.kt)
- Monitor `Sensor.TYPE_LIGHT`.
- Implement `isLightingOptimal(lux: Float)` returning true for daylight range (300-10,000 lux).

#### [NEW] [ColorExtractionAnalyzer.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ColorExtractionAnalyzer.kt)
- Implement `ImageAnalysis.Analyzer`.
- **CRITICAL**: Configure the `ImageAnalysis.Builder` with `setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)` so that `imageProxy.toBitmap()` works natively without manual YUV conversion scripts.
- Integrate ML Kit Face Detection to locate landmarks (cheeks, eyes, forehead).
- Perform RGB/Luminance sampling on in-memory Bitmaps.
- Fire callbacks with `FacialContrastVector` and `undertone`.

#### [NEW] [ColorSeasonClassifier.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ColorSeasonClassifier.kt)
- Implement mapping logic from `FacialContrastVector` and `undertone` to a `ColorSeason`.

### Presentation Layer
I will build the premium camera UI and state management.

#### [NEW] [CalibrationCameraScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ui/CalibrationCameraScreen.kt)
- Stateless Composable with CameraX `PreviewView`.
- **Permissions**: Use `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` for handling the Camera permission strictly within the Compose lifecycle.
- `Canvas` overlay for dashed face reticle and focused dark regions.
- Floating status pill for real-time lighting feedback.

#### [NEW] [CalibrationViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ui/CalibrationViewModel.kt)
- Orchestrate UI states (`PermissionsGranted`, `LightingStatus`, `Scanning`, `Success`, `Error`).
- Manage `ColorExtractionAnalyzer` lifecycle.

### Build Configuration
#### [MODIFY] [analyzer/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/build.gradle.kts)
- Add ML Kit Face Detection: `com.google.mlkit:face-detection`.
- **CRITICAL**: Add CameraX dependencies (`camera-camera2`, `camera-lifecycle`, `camera-view`) to support the analyzer and preview UI.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `ColorSeasonClassifierTest`: Verify mathematical mapping for all 12 seasons.
    - `LightingValidatorTest`: Verify lux threshold logic.
- **Integration Tests**:
    - Verify `CalibrationViewModel` state transitions during a simulated scan.

### Manual Verification
- Deploy to a physical device.
- Test lighting feedback pill by moving between dark and daylight environments.
- Verify face reticle alignment and "Scan Profile" button enablement.
- Confirm "Zero-Cloud" by verifying no network requests are fired during the scan.
