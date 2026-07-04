package com.zoewave.probase.core.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect

/**
 * Surgical cropping utility for the "12-Megapixel Diet".
 * Maps UI coordinates back to the raw high-res sensor space to reduce memory overhead.
 */
object SurgicalCropper {

    /**
     * Crops a raw Bitmap to the specific Region of Interest (ROI) seen on screen.
     * @param rawBitmap The 12MP source bitmap.
     * @param uiRect The bounds of the ScannerOverlay in Compose units.
     * @param previewWidth The total width of the PreviewView on screen.
     * @param previewHeight The total height of the PreviewView on screen.
     * @param rotationDegrees The sensor rotation (usually 90).
     */
    fun cropToROI(
        rawBitmap: Bitmap,
        uiRect: Rect,
        previewWidth: Int,
        previewHeight: Int,
        rotationDegrees: Int
    ): Bitmap {
        // 1. Map the UI bounds to the unrotated sensor space
        // Note: CameraX FILL_CENTER usually rotates AFTER scaling.
        // We determine the scale factor between the "Visible Area" and the "Raw Pixels"
        
        // If rotation is 90 or 270, the sensor's width/height are swapped relative to UI
        val isSwapped = rotationDegrees == 90 || rotationDegrees == 270
        val sensorWidth = if (isSwapped) rawBitmap.height else rawBitmap.width
        val sensorHeight = if (isSwapped) rawBitmap.width else rawBitmap.height

        // Calculate FILL_CENTER scaling (maximum scale to cover the preview)
        val scaleX = previewWidth.toFloat() / sensorWidth.toFloat()
        val scaleY = previewHeight.toFloat() / sensorHeight.toFloat()
        val scale = maxOf(scaleX, scaleY)

        val scaledVisibleWidth = sensorWidth * scale
        val scaledVisibleHeight = sensorHeight * scale

        // Calculate the overflow offsets (how much sensor area is invisible on screen)
        val offsetX = (scaledVisibleWidth - previewWidth) / 2f
        val offsetY = (scaledVisibleHeight - previewHeight) / 2f

        // Map UI Rect into scaled sensor space
        val mappedLeft = ((uiRect.left + offsetX) / scale).toInt()
        val mappedTop = ((uiRect.top + offsetY) / scale).toInt()
        val mappedWidth = (uiRect.width() / scale).toInt()
        val mappedHeight = (uiRect.height() / scale).toInt()

        // 2. Adjust mapping for sensor rotation BEFORE cropping to avoid 12MP rotation memory hit
        val finalCropRect = when (rotationDegrees) {
            90 -> Rect(mappedTop, rawBitmap.height - mappedLeft - mappedWidth, mappedTop + mappedHeight, rawBitmap.height - mappedLeft)
            180 -> Rect(rawBitmap.width - mappedLeft - mappedWidth, rawBitmap.height - mappedTop - mappedHeight, rawBitmap.width - mappedLeft, rawBitmap.height - mappedTop)
            270 -> Rect(rawBitmap.width - mappedTop - mappedHeight, mappedLeft, rawBitmap.width - mappedTop, mappedLeft + mappedWidth)
            else -> Rect(mappedLeft, mappedTop, mappedLeft + mappedWidth, mappedTop + mappedHeight)
        }

        // 3. Safe Clamping
        val safeLeft = finalCropRect.left.coerceIn(0, rawBitmap.width - 1)
        val safeTop = finalCropRect.top.coerceIn(0, rawBitmap.height - 1)
        val safeWidth = finalCropRect.width().coerceAtMost(rawBitmap.width - safeLeft)
        val safeHeight = finalCropRect.height().coerceAtMost(rawBitmap.height - safeTop)

        // 4. Perform the surgical crop on the high-res raw bitmap
        val croppedHighRes = Bitmap.createBitmap(rawBitmap, safeLeft, safeTop, safeWidth, safeHeight)

        // 5. Final Rotation on the small cropped result (High Speed, Low Memory)
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(croppedHighRes, 0, 0, croppedHighRes.width, croppedHighRes.height, matrix, true)
    }
}
