package com.uploader.lifecycle

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
        val instance = MessageDigest.getInstance("SHA-256")

        val buffer = ByteArray(bufferSize)
        val stream = file.inputStream()
        var sizeRead: Int

        stream.use {
            while (stream.read(buffer).also { readNumber -> sizeRead = readNumber } != -1) {
                instance.update(buffer, 0, sizeRead)
            }
        }

        return instance
            .digest()
            .fold("", { str, it -> str + "%02x".format(it) })
    }

    private companion object {
        const val bufferSize = 1024
    }
}
