package com.uploader

import com.uploader.config.AppConfig
import java.security.MessageDigest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object TestingTool : KoinComponent {
    private val config by inject<AppConfig>()

    const val APP_URL = "http://localhost:8080/"

    const val PYCHARM_FULL_NUMBER = "211.6693.115"
    const val PYCHARM_VERSION = "2021.1"
    const val PYCHARM_CHANNEL = "PC-PY-RELEASE-licensing-RELEASE"
    fun pycharmPath() = "${config.rootBuildsPath}PyCharm/$PYCHARM_FULL_NUMBER.tar.gz"

    const val WEBSTORM_VERSION = "2021.1"
    const val WEBSTORM_FULL_NUMBER = "211.6693.108"
    const val WEBSTORM_CHANNEL = "WS-RELEASE-licensing-RELEASE"
    fun webstormPath() = "${config.rootBuildsPath}WebStorm/$WEBSTORM_FULL_NUMBER.tar.gz"

    const val DOWNLOAD_PYCHARM_URL =
        "https://download.jetbrains.com/python/pycharm-professional-$PYCHARM_FULL_NUMBER.tar.gz"
    const val DOWNLOAD_WEBSTORM_URL = "https://download.jetbrains.com/webstorm/WebStorm-$WEBSTORM_FULL_NUMBER.tar.gz"

    const val SHA_CHECK_PYCHARM_URL = "$DOWNLOAD_PYCHARM_URL.sha256"
    const val SHA_CHECK_WEBSTORM_URL = "$DOWNLOAD_WEBSTORM_URL.sha256"

    const val PYCHARM_EXPECTED_INFO_JSON = "app/tars/infos/pycharm-product-info.json"
    const val WEBSTORM_EXPECTED_INFO_JSON = "app/tars/infos/webstorm-product-info.json"

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
