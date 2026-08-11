package com.zoewave.probase.kocolor.features.starterpack.domain.security

import com.google.crypto.tink.subtle.Ed25519Verify
import com.zoewave.probase.core.util.HashUtils
import com.zoewave.probase.kocolor.features.starterpack.data.SecurityConstants
import okio.Source
import okio.buffer
import javax.inject.Inject
import javax.inject.Singleton

interface SignatureVerifier {
    /**
     * @param payloadBytes The exact raw JSON byte array of the 'data' field.
     * @param signatureHex The hex string signature from the envelope.
     * @param expectedSha256 The SHA-256 hash provided in the manifest. If empty, hash check is skipped.
     */
    fun verify(payloadBytes: ByteArray, signatureHex: String, expectedSha256: String): Boolean

    /**
     * Overload for streaming verification to protect the heap.
     * @param source The Okio Source containing the raw binary payload.
     * @param signatureHex The hex string signature from the manifest.
     */
    fun verify(source: Source, signatureHex: String): Boolean
}

@Singleton
class KoColorEd25519Verifier @Inject constructor() : SignatureVerifier {

    // Hardcoded compiler public key for Zero-Trust verification
    private val publicKeyHex = SecurityConstants.KOCOLOR_ROOT_PUBLIC_KEY 

    private val verifier by lazy {
        Ed25519Verify(publicKeyHex.decodeHex())
    }

    override fun verify(payloadBytes: ByteArray, signatureHex: String, expectedSha256: String): Boolean {
        // 1. Fast-Fail Integrity Check (SHA-256)
        if (expectedSha256.isNotEmpty()) {
            val calculatedHash = HashUtils.calculateSha256(payloadBytes)
            if (!calculatedHash.equals(expectedSha256, ignoreCase = true)) {
                return false // Fails integrity
            }
        }

        // 2. Cryptographic Authenticity Check (Ed25519 via Tink)
        return try {
            val signatureBytes = signatureHex.decodeHex()
            verifier.verify(signatureBytes, payloadBytes)
            true
        } catch (e: Exception) {
            false // Invalid signature or malformed data
        }
    }

    override fun verify(source: Source, signatureHex: String): Boolean {
        // Tink's Ed25519Verify requires the full message to verify.
        // Since .kpkg files are relatively small (<32MB), we read into memory.
        return try {
            val payloadBytes = source.buffer().readByteArray()
            val signatureBytes = signatureHex.decodeHex()
            verifier.verify(signatureBytes, payloadBytes)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
