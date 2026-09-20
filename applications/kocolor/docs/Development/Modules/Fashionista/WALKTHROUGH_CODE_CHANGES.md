# Comprehensive Technical Walkthrough of All Code Changes

This document provides an end-to-end technical walkthrough of all architectural refactorings, camera computer vision fixes, ML Kit face telemetry overlays, color science algorithms, and UI improvements implemented across KoColor.

---

## 1. Camera & ViewPort Cropping Parity

### Files Modified:
* [`CalibrationCameraScreen.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ui/CalibrationCameraScreen.kt)
* [`CameraScreen.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/features/camera/src/main/java/com/zoewave/probase/features/camera/ui/components/CameraScreen.kt)

### The Problem:
When `ImageCapture` took a photo without an attached `ViewPort`, CameraX saved the uncropped $4032 \times 3024$ ($4:3$) raw camera sensor image to disk. Meanwhile, the live `PreviewView` center-cropped the camera stream to fill the phone screen ($19.5:9$). As a result, the saved photo contained extra ceiling/background space above the head, causing ML Kit landmarks and face bounding boxes to shift upward relative to the live reticle framing.

### The Solution:
Bound `Preview` and `ImageCapture` use cases inside a `ViewPort` (`ViewPort.FILL_CENTER`) wrapped in `view.post { ... }` after layout measurement:

```kotlin
// In CalibrationCameraScreen.kt:
AndroidView(
    factory = { previewView },
    modifier = Modifier.fillMaxSize()
) { view ->
    view.post {
        if (view.width <= 0 || view.height <= 0) return@post

        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }

        val display = view.display
        val rotation = display?.rotation ?: Surface.ROTATION_0
        val viewPort = ViewPort.Builder(
            Rational(view.width, view.height),
            rotation
        ).setScaleType(ViewPort.FILL_CENTER).build()

        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageCapture)
            .setViewPort(viewPort)
            .build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
    }
}
```

---

## 2. EXIF Orientation & Off-Thread Bitmap Decoding

### Files Modified:
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`AnalyzerViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/ui/AnalyzerViewModel.kt)

### The Problem:
Raw camera JPEG files were decoded using `BitmapFactory.decodeStream(inputStream)` without inspecting EXIF orientation tags (`ExifInterface.TAG_ORIENTATION`). The bitmap was passed to ML Kit as a $90^\circ$ sideways image, causing face detection to fail (`faces.isEmpty()`) or return unrotated landscape coordinates. Additionally, decoding executed on the main thread, causing frame drops during Compose recomposition.

### The Solution:
Wrapped image loading in `withContext(Dispatchers.IO)` and inspected EXIF rotation metadata:

```kotlin
// In StyleSimulatorViewModel.kt:
private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    try {
        var rotationDegrees = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exifInterface = ExifInterface(inputStream)
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        } catch (e: Exception) { Log.w("StyleSimulatorVM", e.message) }

        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return@withContext null

        if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }
    } catch (e: Exception) { null }
}
```

---

## 3. Iris Ring Color Sampling & Calibrated Skin Undertones

### Files Modified:
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`ColorExtractionAnalyzer.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/calibration/ColorExtractionAnalyzer.kt)

### The Problem:
1. ML Kit's `FaceLandmark.LEFT_EYE` returns the dead-center pupil coordinate, which is pitch-black (`0.0384`).
2. Single-pixel sampling on cheek coordinates was susceptible to pore/shadow noise and misclassified warm golden/brown skin complexions into cool summer types.

### The Solution:
1. Implemented `sampleIrisLuminance` & `sampleIrisColorHex`, sampling an 8-point ring offset by $3.5\%$ face width around the pupil center to sample the true colored iris.
2. Implemented $5 \times 5$ patch sampling (`samplePatchLuminance` & `samplePatchColorHex`) with `coerceIn(0, bitmap.width - 1)` boundary checks to prevent array index overflow crashes.
3. Calibrated undertone calculation:
   $$\text{warmMetric} = 2.2 \cdot \left( 0.7 \cdot (R - B) + 0.3 \cdot (G - B) - 0.12 \right)$$

```kotlin
private fun sampleIrisLuminance(bitmap: Bitmap, cx: Int, cy: Int, faceWidth: Int): Float {
    val irisOffset = (faceWidth * 0.035f).toInt().coerceAtLeast(3)
    var totalLuminance = 0f
    var count = 0

    val offsets = listOf(
        Pair(irisOffset, 0), Pair(-irisOffset, 0), Pair(0, irisOffset), Pair(0, -irisOffset),
        Pair(irisOffset, irisOffset), Pair(-irisOffset, -irisOffset),
        Pair(irisOffset, -irisOffset), Pair(-irisOffset, irisOffset)
    )

    for ((dx, dy) in offsets) {
        val px = (cx + dx).coerceIn(0, bitmap.width - 1)
        val py = (cy + dy).coerceIn(0, bitmap.height - 1)
        val pixel = bitmap.getPixel(px, py)
        val lum = (0.2126f * Color.red(pixel) + 0.7152f * Color.green(pixel) + 0.0722f * Color.blue(pixel)) / 255f
        totalLuminance += lum
        count++
    }
    return if (count > 0) (totalLuminance / count).coerceIn(0.0f, 1.0f) else 0.35f
}
```

---

## 4. ML Face Telemetry & Visualizer Overlays

### Files Modified:
* [`FaceTelemetryVisualizer.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FaceTelemetryVisualizer.kt)
* [`FindingsDialog.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FindingsDialog.kt)
* [`FaceDetectionResultsCard.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FaceDetectionResultsCard.kt)

### The Problem:
`FindingsDialog` was wrapping `FaceTelemetryVisualizer` inside `uiState.faceTelemetry?.let { ... }`. When opening the dialog immediately after capturing a photo, `faceTelemetry` was initially null while ML Kit finished processing, causing the image box to be skipped entirely.

### The Solution:
1. Updated `FindingsDialog` to render `FaceTelemetryVisualizer` as long as `userPortraitUri != null`.
2. Applied uniform 2D coordinate scaling (`scale = minOf(size.width / imgW, size.height / imgH)`).
3. Added fallback landmark position generators if ML Kit detects `faceBoundingBox` but misses individual cheek/eye landmarks.
4. Rendered high-visibility target nodes:
   * **Yellow Dashed Rect**: Forehead / Hair region
   * **Cyan Dashed Rect**: Face bounding box
   * **Cyan Target Circle**: Left cheek
   * **Blue Target Circle**: Right cheek
   * **Magenta Target Circle**: Eye / Iris

---

## 5. Interactive Swatches & Real-Time Re-Analysis

### Files Modified:
* [`FindingsDialog.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/FindingsDialog.kt)
* [`UserPortraitSlot.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/UserPortraitSlot.kt)
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)

### The Problem:
Users had no way to manually adjust sampled feature colors if camera lighting altered their natural skin/eye/hair tones.

### The Solution:
1. Integrated `ColorPickerDialog` into the **SAMPLED FEATURE COLORS** row (`Skin / Cheek`, `Eye / Iris`, `Hair / Root`).
2. Added `OnManualSkinColorSelected`, `OnManualEyeColorSelected`, and `OnManualHairColorSelected` events to `StyleSimulatorViewModel`.
3. Updating any color swatch instantly recalculates skin/eye/hair luminance, contrast delta, and undertones, re-evaluating the seasonal category (`Summer`, `Spring`, `Autumn`, `Winter`) and updating the UI state in real time.
4. Updated `UserPortraitSlot` uninitialized state to display **`ACTIVE: NOT ESTABLISHED`** with guidance text when no portrait photo exists.

---

## 6. Continuous 2D Seasonal Quadrant Map Plotting

### Files Modified:
* [`SeasonalQuadrantMap.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/messaging/SeasonalQuadrantMap.kt)

### The Problem:
`clampedX` and `clampedY` were hard-clamping coordinates into sub-quadrant ranges (`0.05..0.45` vs. `0.55..0.95`), causing the target dot to jump discretely rather than glide smoothly when colors were edited.

### The Solution:
Implemented continuous 2D coordinate plotting:

```kotlin
val normX = ((undertoneScore + 1f) / 2f).coerceIn(0.05f, 0.95f)
val normY = (hairLuminance * 0.5f + eyeLuminance * 0.5f).coerceIn(0.05f, 0.95f)

val plotX = normX * canvasW
val plotY = (1f - normY) * canvasH
val userPoint = Offset(plotX, plotY)
```

The target dot glides continuously across Cool $\leftrightarrow$ Warm ($X$-axis) and Light $\leftrightarrow$ Deep ($Y$-axis) in real time.

---

## 7. Outfit Geometry Invariants & Slot Deduplication

### Files Modified:
* [`GreedyRehydrator.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/GreedyRehydrator.kt)
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`FaceBlueprintView.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/simulator/ui/components/graphics/FaceBlueprintView.kt)
* [`HandBlueprintView.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/simulator/ui/components/graphics/HandBlueprintView.kt)

### The Problem:
In certain fallback scenarios, `GreedyRehydrator` assigned two items of category `BOTTOMS` (e.g. `Khaki Cargo` + `Ivory Culottes`) to top and bottom slots while leaving shoes as `None`.

### The Solution:
1. Enforced category deduplication (`distinctBy { it.category }`). An outfit is strictly constrained to **at most 1 TOP**, **1 BOTTOM**, **1 SHOES**, and **1 OUTERWEAR**.
2. Added wardrobe inventory fallback for shoes so `SHOES` is never left as `None` when shoes exist in the closet.
3. Replaced `"Not required"` labels in visual blueprints with active harmonic cosmetic recommendations (`"Warm Nude Definition"`, `"Natural Flush Glow"`, `"Velvet Amber Tint"`, `"Sheer Nude Gel"`).

---

## 8. Permissions, Light Theme & Production R8 Rules

### Files Modified:
* [`AndroidManifest.xml`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/AndroidManifest.xml)
* [`KoColorSettings.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/KoColorSettings.kt)
* [`KoColorMainScreen.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/src/main/java/com/zoewave/probase/kocolor/mobile/ui/KoColorMainScreen.kt)
* [`proguard-rules.pro`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/apps/mobile/proguard-rules.pro)

### Changes:
1. Fixed `READ_HEART_RATE` string typo in `AndroidManifest.xml`.
2. Stripped unneeded Health Connect write permissions via `tools:node="remove"`, keeping `WRITE_HYDRATION` and `READ_SLEEP`.
3. Changed default app theme preference from `"SYSTEM"` to **`"LIGHT"`**.
4. Added R8 rules to `proguard-rules.pro`:
   ```proguard
   -maximumremovedandroidloglevel 3

   -assumenosideeffects class android.util.Log {
       public static *** v(...);
       public static *** d(...);
   }

   -keepattributes SourceFile,LineNumberTable
   -renamesourcefileattribute SourceFile
   ```

---

## 9. FASHIONISTA Architecture Specification

### Files Created:
* [`FASHIONISTA_Vs_Recommendation_Architecture.md`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/docs/Fashionista/FASHIONISTA_Vs_Recommendation_Architecture.md)

### Key Architectural Invariants Established:
* **Dependency Firewall**: FASHIONISTA must NOT depend on Repositories, Database entities (`WardrobeItem`), User Profiles, Weather, Occasion, User Intent, LLM SDKs, or UI state.
* **Input / Output Contract**: Receives `FashionistaObservation` (`ImmutableList` of visual properties) + `FashionistaCalibration` and returns `FashionistaResult` (`aestheticScore`, `coverage`, `radarBreakdown`, `calibrationVersion`).
* **Determinism Invariant**:
  $$\text{FashionistaObservation} + \text{FashionistaCalibration} \longrightarrow \text{FashionistaResult}$$
  Logical determinism is enforced across platforms with fixed collection iteration ordering and zero random seeds/timestamps.
* **Score vs. Coverage Disambiguation**: Missing face/wearer telemetry reduces `coverage`, not the intrinsic `aestheticScore`.

---

## 10. Build & Test Verification Results

* **Unit Tests**: Executed `:applications:kocolor:features:analyzer:testDebugUnitTest` and `:applications:kocolor:data:testDebugUnitTest`. **31 out of 31 unit tests passed 100% green**.
* **Debug Build**: `:applications:kocolor:apps:mobile:assembleDebug` built successfully with 0 errors.
* **Release Build**: `:applications:kocolor:apps:mobile:assembleRelease` compiled with R8 log stripping enabled and assembled successfully with **0 errors**.
