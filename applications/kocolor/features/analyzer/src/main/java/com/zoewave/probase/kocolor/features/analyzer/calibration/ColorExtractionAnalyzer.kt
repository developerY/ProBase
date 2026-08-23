package com.zoewave.probase.kocolor.features.analyzer.calibration

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.zoewave.probase.kocolor.model.calibration.FacialContrastVector
import kotlin.math.abs

class ColorExtractionAnalyzer(
    private val onResult: (FacialContrastVector, Float) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    @OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Since we configured CameraX to output RGBA_8888, we convert to Bitmap.
        // toBitmap() creates a new Bitmap object (copy), so we can close the proxy immediately.
        val bitmap = try {
            imageProxy.toBitmap()
        } catch (e: Exception) {
            Log.e("ColorExtractionAnalyzer", "Failed to convert image to bitmap", e)
            imageProxy.close()
            return
        }
        imageProxy.close() // Release buffer back to camera

        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    Log.d("ColorExtractionAnalyzer", "Face detected: ${faces.size} faces")
                    val face = faces[0]
                    
                    // 1. Skin (Cheek)
                    val cheekLandmark = face.getLandmark(FaceLandmark.LEFT_CHEEK) ?: face.getLandmark(FaceLandmark.RIGHT_CHEEK)
                    val skinLuminance = cheekLandmark?.let { sampleLuminance(bitmap, it.position.x.toInt(), it.position.y.toInt()) } ?: 0.5f

                    // 2. Iris (Eye)
                    val eyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE) ?: face.getLandmark(FaceLandmark.RIGHT_EYE)
                    val eyeLuminance = eyeLandmark?.let { sampleLuminance(bitmap, it.position.x.toInt(), it.position.y.toInt()) } ?: 0.2f

                    // 3. Hair Root (Above forehead)
                    val hairLuminance = sampleLuminance(bitmap, face.boundingBox.centerX(), (face.boundingBox.top - 20).coerceAtLeast(0))

                    val contrastDelta = abs(skinLuminance - hairLuminance)
                    
                    // 4. Undertone Estimation (Simple R vs B comparison for warm/cool)
                    val undertone = estimateUndertone(bitmap, cheekLandmark?.position?.x?.toInt() ?: face.boundingBox.centerX(), cheekLandmark?.position?.y?.toInt() ?: face.boundingBox.centerY())

                    Log.d("ColorExtractionAnalyzer", "Established -> Skin: $skinLuminance, Hair: $hairLuminance, Delta: $contrastDelta, Undertone: $undertone")

                    onResult(
                        FacialContrastVector(
                            skinLuminance = skinLuminance,
                            hairLuminance = hairLuminance,
                            eyeLuminance = eyeLuminance,
                            contrastDelta = contrastDelta
                        ),
                        undertone
                    )
                } else {
                    Log.v("ColorExtractionAnalyzer", "Landmark scanning... no face found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ColorExtractionAnalyzer", "Face detection failed", e)
            }
    }

    private fun sampleLuminance(bitmap: Bitmap, x: Int, y: Int): Float {
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) return 0.5f
        val pixel = bitmap.getPixel(x, y)
        return (0.2126f * Color.red(pixel) + 0.7152f * Color.green(pixel) + 0.0722f * Color.blue(pixel)) / 255f
    }

    private fun estimateUndertone(bitmap: Bitmap, x: Int, y: Int): Float {
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) return 0f
        val pixel = bitmap.getPixel(x, y)
        val r = Color.red(pixel)
        val b = Color.blue(pixel)
        // Warm tones have more red relative to blue
        return ((r - b).toFloat() / 255f).coerceIn(-1.0f, 1.0f)
    }
}
