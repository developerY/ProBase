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
    private val isEnabled: () -> Boolean,
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
        if (!isEnabled()) {
            imageProxy.close()
            return
        }

        // We only process frames if a result is actually needed.
        val bitmap = try {
            imageProxy.toBitmap()
        } catch (e: Exception) {
            imageProxy.close()
            return
        }
        imageProxy.close() 

        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    Log.d("ColorExtractionAnalyzer", "Face detected: ${faces.size} faces")
                    val face = faces[0]
                    
                    // 1. Skin (Cheek Patch)
                    val cheekLandmark = face.getLandmark(FaceLandmark.LEFT_CHEEK) ?: face.getLandmark(FaceLandmark.RIGHT_CHEEK)
                    val skinLuminance = cheekLandmark?.let { samplePatchLuminance(bitmap, it.position.x.toInt(), it.position.y.toInt()) } ?: 0.5f

                    // 2. Iris (Eye Circle)
                    val eyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE) ?: face.getLandmark(FaceLandmark.RIGHT_EYE)
                    val eyeLuminance = eyeLandmark?.let { sampleIrisLuminance(bitmap, it.position.x.toInt(), it.position.y.toInt(), face.boundingBox.width()) } ?: 0.35f

                    // 3. Hair Root (Above forehead)
                    val hairLuminance = samplePatchLuminance(bitmap, face.boundingBox.centerX(), (face.boundingBox.top - 20).coerceAtLeast(0))

                    val contrastDelta = abs(skinLuminance - hairLuminance)
                    
                    // 4. Undertone Estimation
                    val undertone = estimateUndertone(bitmap, cheekLandmark?.position?.x?.toInt() ?: face.boundingBox.centerX(), cheekLandmark?.position?.y?.toInt() ?: face.boundingBox.centerY())

                    Log.d("ColorExtractionAnalyzer", "Established -> Skin: $skinLuminance, Eye: $eyeLuminance, Hair: $hairLuminance, Delta: $contrastDelta, Undertone: $undertone")

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

    private fun samplePatchLuminance(bitmap: Bitmap, cx: Int, cy: Int, radius: Int = 3): Float {
        var totalLuminance = 0f
        var count = 0

        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val px = cx + dx
                val py = cy + dy
                if (px in 0 until bitmap.width && py in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(px, py)
                    val lum = (0.2126f * Color.red(pixel) + 0.7152f * Color.green(pixel) + 0.0722f * Color.blue(pixel)) / 255f
                    totalLuminance += lum
                    count++
                }
            }
        }
        return if (count > 0) (totalLuminance / count).coerceIn(0.0f, 1.0f) else 0.5f
    }

    private fun sampleIrisLuminance(bitmap: Bitmap, cx: Int, cy: Int, faceWidth: Int): Float {
        val irisOffset = (faceWidth * 0.035f).toInt().coerceAtLeast(3)
        var totalLuminance = 0f
        var count = 0

        val offsets = listOf(
            Pair(irisOffset, 0),
            Pair(-irisOffset, 0),
            Pair(0, irisOffset),
            Pair(0, -irisOffset),
            Pair(irisOffset, irisOffset),
            Pair(-irisOffset, -irisOffset),
            Pair(irisOffset, -irisOffset),
            Pair(-irisOffset, irisOffset)
        )

        for ((dx, dy) in offsets) {
            val px = cx + dx
            val py = cy + dy
            if (px in 0 until bitmap.width && py in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(px, py)
                val lum = (0.2126f * Color.red(pixel) + 0.7152f * Color.green(pixel) + 0.0722f * Color.blue(pixel)) / 255f
                totalLuminance += lum
                count++
            }
        }
        return if (count > 0) (totalLuminance / count).coerceIn(0.0f, 1.0f) else 0.35f
    }

    private fun estimateUndertone(bitmap: Bitmap, cx: Int, cy: Int, radius: Int = 4): Float {
        var totalR = 0f
        var totalG = 0f
        var totalB = 0f
        var count = 0

        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val px = cx + dx
                val py = cy + dy
                if (px in 0 until bitmap.width && py in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(px, py)
                    totalR += Color.red(pixel)
                    totalG += Color.green(pixel)
                    totalB += Color.blue(pixel)
                    count++
                }
            }
        }
        if (count == 0) return 0f
        val avgR = totalR / count
        val avgG = totalG / count
        val avgB = totalB / count

        val rbDiff = (avgR - avgB) / 255f
        val gbDiff = (avgG - avgB) / 255f
        val warmMetric = (rbDiff * 0.6f + gbDiff * 0.4f) - 0.28f
        return warmMetric.coerceIn(-1.0f, 1.0f)
    }
}
