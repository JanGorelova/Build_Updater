package com.uploader.provider

import java.io.File
import java.security.MessageDigest
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class ChecksumVerifier : KoinComponent {
    fun isIntegral(file: File, expectedCheckSum: String): Boolean {
        val current = hashFile(file)

        return expectedCheckSum == current
    }

    private fun hashFile(file: File): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(file.inputStream().readBytes())
            .fold("", { str, it -> str + "%02x".format(it) })
    }
}
