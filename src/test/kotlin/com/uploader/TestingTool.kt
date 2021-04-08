package com.uploader

import java.security.MessageDigest
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
object TestingTool : KoinComponent {
    fun downloadFromResource(path: String): ByteArray =
        this::class.java
            .classLoader.getResourceAsStream(path)
            ?.readBytes() ?: error("Resource $path was not found")

    fun sha256(byteArray: ByteArray) =
        MessageDigest
            .getInstance("SHA-256")
            .digest(byteArray)
            .fold("", { str, it -> str + "%02x".format(it) })
}
