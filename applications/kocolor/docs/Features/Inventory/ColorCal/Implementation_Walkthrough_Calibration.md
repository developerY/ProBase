# Implementation Walkthrough: On-Device Color & Contrast Calibration

This document details the implementation of the Zero-Cloud "Color & Contrast Calibration" feature. This system establishes a user's mathematical aesthetic baseline (12-Season Color Profile) via local facial analysis using CameraX and ML Kit.

---

## 1. Zero-Cloud Privacy & Performance

The core constraint of this feature is that **no image data ever leaves the device**. 

- **In-Memory Processing**: Frames from CameraX are processed as `ImageProxy` objects, converted to in-memory Bitmaps for sampling, and immediately discarded.
- **Edge AI**: Uses Google ML Kit's Face Detection to locate key facial landmarks (cheeks, eyes, forehead) without uploading data to any cloud service.

---

## 2. Hardware & Edge AI Layer

I implemented a multi-stage validation and extraction pipeline:

### Lighting Validation (`LightingValidator.kt`)
To guarantee color accuracy, the scan is blocked unless ambient lighting is optimal.
- Monitors `Sensor.TYPE_LIGHT`.
- Enforces a "Natural Daylight" range of **300 - 10,000 lux**.

### Color Extraction (`ColorExtractionAnalyzer.kt`)
A custom CameraX `ImageAnalysis.Analyzer` that performs the mathematical sampling:
- **Skin Sampling**: Targets cheek landmarks for base skin tone and undertone estimation.
- **Iris Sampling**: Targets eye landmarks for eye luminance.
- **Hair Sampling**: Samples the region just above the detected face bounding box for hair luminance.
- **Contrast Vector**: Calculates the `FacialContrastVector` (delta between skin, hair, and eye luminance).

---

## 3. Aesthetic Intelligence

The raw mathematical vectors are converted into fashion-tech insights via the `ColorSeasonClassifier`.

### 12-Season Classification (`ColorSeasonClassifier.kt`)
Implements the mapping logic to determine the user's color season based on:
- **Undertone**: Warm vs. Cool estimation.
- **Contrast Delta**: High contrast (e.g., True Winter) vs. Low contrast (e.g., Light Spring).

---

## 4. Premium Presentation Layer

I built a high-fidelity camera experience using Jetpack Compose.

### Calibration Camera UI (`CalibrationCameraScreen.kt`)
- **CameraX Integration**: Uses `AndroidView` to host the `PreviewView` and binds the lifecycle to the Composable.
- **Custom Overlay**: A `Canvas`-based dashed face reticle with dark focused overlays to guide the user.
- **Real-Time Feedback**: A status pill provides immediate feedback on lighting conditions (e.g., "Lighting Optimal" vs. "Move to a window").
- **State Orchestration**: The `CalibrationViewModel` manages the permission state, lighting status, and final profile generation.

---

## 5. Verification & Stability

- **Native RGBA Output**: Configured `ImageAnalysis` for `RGBA_8888` to ensure direct `toBitmap()` compatibility.
- **Compose Permissions**: Implemented modern `ActivityResultContracts` for camera permission handling.
- **Build Verified**: Successfully integrated dependencies for ML Kit Face Detection and CameraX, with a clean module build.

---

### Artifacts Created:
- **Models**: `ColorProfileModels.kt`
- **Logic**: `LightingValidator.kt`, `ColorExtractionAnalyzer.kt`, `ColorSeasonClassifier.kt`
- **UI**: `CalibrationCameraScreen.kt`, `CalibrationViewModel.kt`
- **Config**: Updated `libs.versions.toml` and `analyzer/build.gradle.kts`.
