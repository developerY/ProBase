package com.zoewave.probase.core.util

import java.security.MessageDigest

object HashUtils {
    fun calculateSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
