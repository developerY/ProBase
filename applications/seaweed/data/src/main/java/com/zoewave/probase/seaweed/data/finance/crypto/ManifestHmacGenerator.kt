package com.zoewave.probase.seaweed.data.finance.crypto

import android.content.Context
import com.google.crypto.tink.Mac
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.mac.MacConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManifestHmacGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        MacConfig.register()
    }

    private val hmacMac: Mac by lazy {
        AndroidKeysetManager.Builder()
            .withSharedPref(context, "finance_hmac_keys", "finance_crypto_prefs")
            .withKeyTemplate(com.google.crypto.tink.mac.MacKeyTemplates.HMAC_SHA256_256BITTAG)
            .withMasterKeyUri("android-keystore://finance_manifest_hmac_key")
            .build()
            .keysetHandle
            .getPrimitive(Mac::class.java)
    }

    fun generateHmac(documentId: String, evidenceId: String, semanticRole: String, normalizedValue: String): String {
        val payload = "$documentId|$evidenceId|$semanticRole|$normalizedValue".encodeToByteArray()
        val tagBytes = hmacMac.computeMac(payload)
        return tagBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyHmac(documentId: String, evidenceId: String, semanticRole: String, normalizedValue: String, expectedHex: String): Boolean {
        val payload = "$documentId|$evidenceId|$semanticRole|$normalizedValue".encodeToByteArray()
        val expectedBytes = expectedHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return try {
            hmacMac.verifyMac(expectedBytes, payload)
            true
        } catch (e: Exception) {
            false
        }
    }
}
