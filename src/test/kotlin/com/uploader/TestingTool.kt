package com.uploader

import java.security.MessageDigest

object TestingTool {
    const val PYCHARM_VERSION = "211.6693.115"
    const val PYCHARM_CHANNEL = "PC-PY-RELEASE-licensing-RELEASE"
    const val WEBSTORM_VERSION = "211.6693.108"
    const val WEBSTORM_CHANNEL = "WS-RELEASE-licensing-RELEASE"

    const val DOWNLOAD_PYCHARM_URL =
        "https://download.jetbrains.com/python/pycharm-professional-$PYCHARM_VERSION.tar.gz"
    const val DOWNLOAD_WEBSTORM_URL = "https://download.jetbrains.com/webstorm/WebStorm-$WEBSTORM_VERSION.tar.gz"

    const val SHA_CHECK_PYCHARM_URL = "$DOWNLOAD_PYCHARM_URL.sha256"
    const val SHA_CHECK_WEBSTORM_URL = "$DOWNLOAD_WEBSTORM_URL.sha256"

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
