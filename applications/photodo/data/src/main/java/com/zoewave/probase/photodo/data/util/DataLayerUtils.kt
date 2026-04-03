package com.zoewave.probase.photodo.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

import android.util.Log

import com.google.android.gms.wearable.PutDataRequest

private const val TAG = "PhotoDoSync_Watch"

/**
 * Utility to load a Wearable Asset from the Data Layer as a Bitmap.
 */
suspend fun loadAssetAsBitmap(context: Context, path: String, assetKey: String): Bitmap? {
    Log.d(TAG, "Loading asset: $assetKey from path: $path")
    return try {
        val dataClient = Wearable.getDataClient(context)
        
        // Use getDataItems to find the item by path (ignoring the host node ID)
        val uri = Uri.Builder()
            .scheme("wear")
            .path(path)
            .build()
            
        val dataItemBuffer = dataClient.getDataItems(uri).await()
        val dataItem = if (dataItemBuffer.count > 0) dataItemBuffer.get(0) else null
        
        if (dataItem == null) {
            Log.w(TAG, "DataItem not found for path: $path")
            dataItemBuffer.release()
            return null
        }

        val asset = DataMapItem.fromDataItem(dataItem).dataMap.getAsset(assetKey)
        dataItemBuffer.release() // Important to release the buffer

        if (asset == null) {
            Log.w(TAG, "Asset not found for key: $assetKey in DataItem: $path")
            return null
        }
            
        val fd = dataClient.getFdForAsset(asset).await()
        if (fd == null) {
            Log.w(TAG, "Failed to get FileDescriptor for asset: $assetKey")
            return null
        }

        val inputStream = fd.inputStream
        val bitmap = BitmapFactory.decodeStream(inputStream)
        if (bitmap != null) {
            Log.d(TAG, "Successfully decoded bitmap for asset: $assetKey (${bitmap.width}x${bitmap.height})")
        } else {
            Log.w(TAG, "Failed to decode bitmap for asset: $assetKey")
        }
        bitmap
    } catch (e: Exception) {
        Log.e(TAG, "Error loading asset: $assetKey", e)
        null
    }
}
