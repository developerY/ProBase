package com.zoewave.probase.kocolor.features.starterpack.data

sealed class PackException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class SignatureException(message: String) : PackException(message)
    class SchemaException(message: String) : PackException(message)
    class ManifestException(message: String) : PackException(message)
    class VersionMismatchException(message: String) : PackException(message)
    class DownloadException(message: String, cause: Throwable) : PackException(message, cause)
}
