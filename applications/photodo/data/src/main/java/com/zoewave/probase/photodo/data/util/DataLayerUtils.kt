package com.zoewave.probase.photodo.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Utility to load a Wearable Asset from the Data Layer as a Bitmap.
 */
suspend fun loadAssetAsBitmap(context: Context, path: String, assetKey: String): Bitmap? {
    return try {
        val dataClient = Wearable.getDataClient(context)
        val uri = Uri.Builder()
            .scheme("wear")
            .path(path)
            .build()
            
        val dataItem = dataClient.getDataItem(uri).await() ?: return null
        val asset = DataMapItem.fromDataItem(dataItem).dataMap.getAsset(assetKey)
            
        if (asset != null) {
            val inputStream = dataClient.getFdForAsset(asset).await().inputStream
            BitmapFactory.decodeStream(inputStream)
        } else null
    } catch (e: Exception) {
        null
    }
}
