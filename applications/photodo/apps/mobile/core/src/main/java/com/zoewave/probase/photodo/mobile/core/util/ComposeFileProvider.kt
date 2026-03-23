package com.zoewave.probase.photodo.mobile.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ComposeFileProvider {
    fun getImageUri(context: Context): Uri {
        // 1. Create a temporary file in the app's cache directory
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File.createTempFile(
            "selected_image_",
            ".jpg",
            directory
        )
        // 2. Wrap it securely using your FileProvider
        val authority = context.packageName + ".fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}