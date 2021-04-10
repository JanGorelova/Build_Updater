package com.uploader

import com.uploader.config.AppConfig
import java.nio.file.Paths

object TestingConstants {
    const val ROOT_DOWNLOAD_URL = "https://download.jetbrains.com"
    const val AWAIT_AT_MOST_SECONDS = 100L

    const val PYCHARM_FULL_NUMBER = "211.6693.115"

    const val PYCHARM_VERSION = "2021.1"
    const val PYCHARM_CHANNEL = "PC-PY-RELEASE-licensing-RELEASE"

    const val PYCHARM_2_FULL_NUMBER = "211.6693.77"
    const val PYCHARM_2_VERSION = "2021.1 RC"
    const val PYCHARM_2_CHANNEL = "PC-PY-EAP-licensing-RELEASE"
    const val WEBSTORM_VERSION = "2021.1"

    const val WEBSTORM_FULL_NUMBER = "211.6693.108"
    const val WEBSTORM_CHANNEL = "WS-RELEASE-licensing-RELEASE"
    const val DOWNLOAD_PYCHARM_URL = "$ROOT_DOWNLOAD_URL/python/pycharm-professional-$PYCHARM_VERSION.tar.gz"

    const val DOWNLOAD_PYCHARM_2_URL = "$ROOT_DOWNLOAD_URL/python/pycharm-professional-$PYCHARM_2_FULL_NUMBER.tar.gz"
    const val DOWNLOAD_WEBSTORM_URL = "$ROOT_DOWNLOAD_URL/webstorm/WebStorm-$WEBSTORM_VERSION.tar.gz"

    const val SHA_CHECK_PYCHARM_URL = "$DOWNLOAD_PYCHARM_URL.sha256"
    const val SHA_CHECK_PYCHARM_2_URL = "$DOWNLOAD_PYCHARM_2_URL.sha256"
    const val SHA_CHECK_WEBSTORM_URL = "$DOWNLOAD_WEBSTORM_URL.sha256"

    const val PYCHARM_EXPECTED_INFO_JSON = "app/tars/infos/pycharm-product-info.json"
    const val PYCHARM_2_EXPECTED_INFO_JSON = "app/tars/infos/pycharm-2-product-info.json"
    const val WEBSTORM_EXPECTED_INFO_JSON = "app/tars/infos/webstorm-product-info.json"

    private val rootBuildsPath = "${Paths.get("").toRealPath()}/src/test/resources/downloads/"

    fun pycharmPath() =
        "${rootBuildsPath}PyCharm/$PYCHARM_FULL_NUMBER.tar.gz"

    fun pycharm2Path() =
        "${rootBuildsPath}PyCharm/$PYCHARM_2_FULL_NUMBER.tar.gz"

    fun webstormPath() =
        "${rootBuildsPath}WebStorm/$WEBSTORM_FULL_NUMBER.tar.gz"

    fun AppConfig.appUrl() =
        "http://$host:$port/"
}
