package com.zoewave.probase.core.util

import java.security.MessageDigest

object HashUtils {
    fun calculateSha256(input: String): String = calculateSha256(input.toByteArray(Charsets.UTF_8))

    fun calculateSha256(input: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
