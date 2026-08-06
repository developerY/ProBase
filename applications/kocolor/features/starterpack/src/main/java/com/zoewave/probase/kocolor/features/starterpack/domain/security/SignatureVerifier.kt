package com.zoewave.probase.kocolor.features.starterpack.domain.security

import com.zoewave.probase.kocolor.features.starterpack.data.SecurityConstants
import com.zoewave.probase.core.util.HashUtils
import okio.Source
import okio.buffer
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
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
    // This is standard practice for root public keys
    private val publicKeyHex = SecurityConstants.KOCOLOR_ROOT_PUBLIC_KEY 

    private val publicKeyParams by lazy {
        Ed25519PublicKeyParameters(publicKeyHex.decodeHex(), 0)
    }

    override fun verify(payloadBytes: ByteArray, signatureHex: String, expectedSha256: String): Boolean {
        // 1. Fast-Fail Integrity Check (SHA-256)
        if (expectedSha256.isNotEmpty()) {
            val calculatedHash = HashUtils.calculateSha256(payloadBytes)
            if (!calculatedHash.equals(expectedSha256, ignoreCase = true)) {
                return false // Fails integrity
            }
        }

        // 2. Cryptographic Authenticity Check (Ed25519)
        val signer = Ed25519Signer().apply {
            init(false, publicKeyParams)
            update(payloadBytes, 0, payloadBytes.size)
        }

        return try {
            val signatureBytes = signatureHex.decodeHex()
            signer.verifySignature(signatureBytes)
        } catch (e: Exception) {
            false // Malformed signature string
        }
    }

    override fun verify(source: Source, signatureHex: String): Boolean {
        val signer = Ed25519Signer().apply {
            init(false, publicKeyParams)
        }

        val buffer = ByteArray(8192) // 8KB chunks
        source.buffer().use { bufferedSource ->
            while (true) {
                val bytesRead = bufferedSource.read(buffer)
                if (bytesRead == -1) break
                signer.update(buffer, 0, bytesRead)
            }
        }

        return try {
            val signatureBytes = signatureHex.decodeHex()
            signer.verifySignature(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }

    // --- Hex Utility Extensions ---
    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
