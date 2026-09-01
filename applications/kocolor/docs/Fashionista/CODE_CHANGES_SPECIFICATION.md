# KoColor & FASHIONISTA Implementation & Code Changes Specification

This document summarizes all technical code changes, architectural refactorings, and bug fixes implemented across the KoColor codebase.

---

## 1. Camera & ViewPort Cropping Parity

### Modified Files:
* [`CalibrationCameraScreen.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ui/CalibrationCameraScreen.kt)
* [`CameraScreen.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/features/camera/src/main/java/com/zoewave/probase/features/camera/ui/components/CameraScreen.kt)

### Changes:
* Deferred CameraX use-case binding using `view.post { ... }` until `PreviewView` layout measurements completes (`width > 0 && height > 0`).
* Configured explicit CameraX `ViewPort`:
  ```kotlin
  val viewPort = ViewPort.Builder(
      Rational(view.width, view.height),
      rotation
  ).setScaleType(ViewPort.FILL_CENTER).build()

  val useCaseGroup = UseCaseGroup.Builder()
      .addUseCase(preview)
      .addUseCase(imageCapture)
      .setViewPort(viewPort)
      .build()

  cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
  ```
* **Result**: Captured JPEG image files saved to disk match the exact `9:16` aspect ratio and framing of the live camera viewfinder preview screen.

---

## 2. EXIF Orientation & Bitmap Decoding

### Modified Files:
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`AnalyzerViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/AnalyzerViewModel.kt)

### Changes:
* Replaced unrotated `BitmapFactory.decodeStream` with EXIF-aware bitmap loading:
  ```kotlin
  private fun loadBitmapFromUri(uri: Uri): Bitmap? {
      return try {
          var rotationDegrees = 0
          try {
              context.contentResolver.openInputStream(uri)?.use { inputStream ->
                  val exifInterface = android.media.ExifInterface(inputStream)
                  val orientation = exifInterface.getAttributeInt(
                      android.media.ExifInterface.TAG_ORIENTATION,
                      android.media.ExifInterface.ORIENTATION_NORMAL
                  )
                  rotationDegrees = when (orientation) {
                      android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                      android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                      android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                      else -> 0
                  }
              }
          } catch (e: Exception) { Log.w("BitmapLoader", e.message) }

          val rawBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
              BitmapFactory.decodeStream(inputStream)
          } ?: return null

          if (rotationDegrees != 0) {
              val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
              Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
          } else {
              rawBitmap
          }
      } catch (e: Exception) { null }
  }
  ```
* Removed premature `bitmap.recycle()` calls to prevent Coil image loader memory invalidation errors.
* Passed upright bitmaps directly to ML Kit via `InputImage.fromBitmap(bitmap, 0)`.

---

## 3. ML Kit Face Telemetry & Visualizer Overlays

### Modified Files:
* [`FaceTelemetryVisualizer.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FaceTelemetryVisualizer.kt)
* [`FindingsDialog.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FindingsDialog.kt)
* [`FaceDetectionResultsCard.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FaceDetectionResultsCard.kt)

### Changes:
* Applied uniform 2D coordinate scaling (`scale = minOf(size.width / imgW, size.height / imgH)`) and centering offsets (`offsetX`, `offsetY`).
* Implemented front-camera horizontal $X$-axis mirror mapping (`mapX = size.width - scaledX`).
* Added fallback landmark generators so that if ML Kit detects `faceBoundingBox` but misses specific cheek/eye landmarks, default target nodes are generated based on face box geometry.
* Updated `FindingsDialog.kt` to display `FaceTelemetryVisualizer` unconditionally whenever `userPortraitUri != null`.

---

## 4. Region Patch Sampling & Calibrated Undertones

### Modified Files:
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`ColorExtractionAnalyzer.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ColorExtractionAnalyzer.kt)

### Changes:
* Replaced single-pixel sampling with $5 \times 5$ patch sampling (`samplePatchLuminance`, `samplePatchColorHex`).
* Implemented `sampleIrisLuminance` / `sampleIrisColorHex` by sampling an 8-point ring offset by $3.5\%$ face width around the pupil center to bypass the black pupil (`0.0384`).
* Calibrated warm undertone calculation:
  $$\text{warmMetric} = 2.2 \cdot \left( 0.7 \cdot (R - B) + 0.3 \cdot (G - B) - 0.12 \right)$$
  ensuring golden/chestnut brown complexions classify into **AUTUMN** / **SPRING**.

---

## 5. Interactive Swatches & Real-Time Re-analysis

### Modified Files:
* [`FindingsDialog.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FindingsDialog.kt)
* [`UserPortraitSlot.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/UserPortraitSlot.kt)
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)

### Changes:
* Added `OnManualSkinColorSelected(hex)`, `OnManualEyeColorSelected(hex)`, and `OnManualHairColorSelected(hex)` to `SimulatorEvent`.
* Integrated `ColorPickerDialog` on skin, eye, and hair color swatches.
* Editing any color swatch immediately recalculates luminance, contrast, and undertone, reclassifying the seasonal profile in real time.
* Updated `UserPortraitSlot` uninitialized state to display **`ACTIVE: NOT ESTABLISHED`** with guidance text when no portrait photo exists.

---

## 6. Continuous 2D Seasonal Quadrant Map

### Modified Files:
* [`SeasonalQuadrantMap.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/SeasonalQuadrantMap.kt)

### Changes:
* Removed rigid sub-quadrant clamping (`clampedX`, `clampedY`).
* Implemented continuous 2D coordinate plotting:
  $$\text{normX} = \text{coerce}\left(\frac{\text{undertone} + 1}{2}, 0.05, 0.95\right)$$
  $$\text{normY} = \text{coerce}\left(\frac{\text{hairLum} + \text{eyeLum}}{2}, 0.05, 0.95\right)$$
* The target dot glides continuously across the 4 quadrants ($X, Y$) in real time as colors are edited.

---

## 7. Clothing & Cosmetic Slot Allocation Invariants

### Modified Files:
* [`GreedyRehydrator.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/GreedyRehydrator.kt)
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`FaceBlueprintView.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/graphics/FaceBlueprintView.kt)
* [`HandBlueprintView.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/graphics/HandBlueprintView.kt)

### Changes:
* Enforced strict category deduplication (`distinctBy { it.category }`). An outfit is constrained to **at most 1 TOP**, **1 BOTTOM**, **1 SHOES**, and **1 OUTERWEAR**, eliminating 2-bottom outfits.
* Added wardrobe inventory fallback for shoes if missing in AI selection.
* Replaced `"Not required"` labels in visual blueprints with active harmonic cosmetic names (e.g. `"Warm Nude Definition"`, `"Natural Flush Glow"`, `"Velvet Amber Tint"`, `"Sheer Nude Gel"`).

---

## 8. Permissions, R8 & Theme Configuration

### Modified Files:
* [`AndroidManifest.xml`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/AndroidManifest.xml)
* [`KoColorSettings.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/KoColorSettings.kt)
* [`KoColorMainScreen.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorMainScreen.kt)
* [`proguard-rules.pro`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/proguard-rules.pro)

### Changes:
* Fixed `READ_HEART_RATE` typo in `AndroidManifest.xml`.
* Stripped unneeded Health Connect write permissions via `tools:node="remove"`, keeping `WRITE_HYDRATION` and `READ_SLEEP`.
* Changed default app theme preference from `"SYSTEM"` to **`"LIGHT"`**.
* Added R8 rule `-maximumremovedandroidloglevel 3` to automatically strip debug (`Log.d`) and verbose (`Log.v`) logs in production release builds.
