package com.zoewave.probase.seaweed.data.finance.crypto

import android.content.Context
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TinkStreamingFileEncryptor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        StreamingAeadConfig.register()
    }

    private val streamingAead: StreamingAead by lazy {
        AndroidKeysetManager.Builder()
            .withSharedPref(context, "finance_image_keys", "finance_crypto_prefs")
            .withKeyTemplate(com.google.crypto.tink.streamingaead.StreamingAeadKeyTemplates.AES256_GCM_HKDF_4KB)
            .withMasterKeyUri("android-keystore://finance_image_master_key")
            .build()
            .keysetHandle
            .getPrimitive(StreamingAead::class.java)
    }

    fun encryptFile(source: File, destination: File, documentId: String, pageNumber: Int, variant: String) {
        val associatedData = "$documentId:$pageNumber:$variant".encodeToByteArray()
        val tmpDest = File(destination.parentFile, "${destination.name}.tmp")

        try {
            source.inputStream().use { input ->
                streamingAead.newEncryptingStream(tmpDest.outputStream(), associatedData).use { output ->
                    input.copyTo(output)
                }
            }
            tmpDest.renameTo(destination)
        } catch (e: Exception) {
            tmpDest.delete()
            throw e
        }
    }

    fun decryptStream(encryptedFile: File, documentId: String, pageNumber: Int, variant: String): InputStream {
        val associatedData = "$documentId:$pageNumber:$variant".encodeToByteArray()
        return streamingAead.newDecryptingStream(encryptedFile.inputStream(), associatedData)
    }
}
