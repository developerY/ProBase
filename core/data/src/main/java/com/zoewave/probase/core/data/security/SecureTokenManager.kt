package com.zoewave.probase.core.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages secure storage of authentication tokens using Android Keystore with StrongBox support.
 * This class ensures that sensitive keys are kept in hardware-backed storage (Titan M / Knox).
 */
@Singleton
class SecureTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "CGM_TOKEN_ENCRYPTION_KEY"

    init {
        generateKeyIfMissing()
    }

    private fun generateKeyIfMissing() {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            val builder = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)

            // Attempt to use StrongBox if available
            try {
                builder.setIsStrongBoxBacked(true)
                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            } catch (e: Exception) {
                // Fallback to TEE if StrongBox is not available
                builder.setIsStrongBoxBacked(false)
                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            }
        }
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    /**
     * Encrypts a plaintext token and returns a Base64 encoded string containing IV + Ciphertext.
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        val combined = iv + encryptedData
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Decrypts a Base64 encoded string (IV + Ciphertext) back to plaintext.
     */
    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = combined.copyOfRange(0, 12) // GCM IV is 12 bytes
        val encryptedData = combined.copyOfRange(12, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }
}
