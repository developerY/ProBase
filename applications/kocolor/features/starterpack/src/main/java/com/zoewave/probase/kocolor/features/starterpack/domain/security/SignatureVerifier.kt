package com.zoewave.probase.kocolor.features.starterpack.domain.security

import android.util.Base64
import android.util.Log
import com.zoewave.probase.kocolor.features.starterpack.BuildConfig
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

interface SignatureVerifier {
    /**
     * Verifies that the provided [jsonPayload] was signed by the [signatureBase64].
     * Uses SHA256withECDSA algorithm.
     */
    suspend fun verify(jsonPayload: String, signatureBase64: String): Boolean
}

@Singleton
class SignatureVerifierImpl @Inject constructor() : SignatureVerifier {

    private val publicKeyBase64 = BuildConfig.CDN_PUBLIC_KEY

    override suspend fun verify(jsonPayload: String, signatureBase64: String): Boolean {
        return try {
            val publicKey = loadPublicKey(publicKeyBase64)
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initVerify(publicKey)
            signature.update(jsonPayload.toByteArray(Charsets.UTF_8))
            
            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            Log.e("SignatureVerifier", "Verification failed", e)
            false
        }
    }

    private fun loadPublicKey(base64Key: String): PublicKey {
        val keyBytes = Base64.decode(base64Key, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("EC")
        return kf.generatePublic(spec)
    }
}
