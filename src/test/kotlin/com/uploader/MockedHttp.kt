package com.uploader

import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_2_URL
import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingConstants.DOWNLOAD_WEBSTORM_URL
import com.uploader.TestingConstants.SHA_CHECK_PYCHARM_2_URL
import com.uploader.TestingConstants.SHA_CHECK_PYCHARM_URL
import com.uploader.TestingConstants.SHA_CHECK_WEBSTORM_URL
import com.uploader.TestingTool.downloadFromResource
import com.uploader.TestingTool.sha256
import com.uploader.provider.Constants.UPDATES_URL
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.HttpTimeout
import io.ktor.utils.io.ByteReadChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class MockedHttp {
    private val invocations = ConcurrentHashMap<String, AtomicInteger>()

    val client = HttpClient(MockEngine) {
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }
        engine {
            addHandler { request ->
                when (request.url.toString()) {
                    UPDATES_URL -> {
                        putIfAbsentOrIncrement(UPDATES_URL)
                        respond(updates)
                    }
                    DOWNLOAD_PYCHARM_URL -> {
                        putIfAbsentOrIncrement(DOWNLOAD_PYCHARM_URL)
                        respond(ByteReadChannel(pyCharmBuild))
                    }
                    DOWNLOAD_PYCHARM_2_URL -> {
                        putIfAbsentOrIncrement(DOWNLOAD_PYCHARM_2_URL)
                        respond(ByteReadChannel(pyCharm2Build))
                    }
                    DOWNLOAD_WEBSTORM_URL -> {
                        putIfAbsentOrIncrement(DOWNLOAD_WEBSTORM_URL)
                        respond(ByteReadChannel(webStormBuild))
                    }
                    SHA_CHECK_PYCHARM_URL -> respond(sha256(pyCharmBuild))
                    SHA_CHECK_PYCHARM_2_URL -> respond(sha256(pyCharm2Build))
                    SHA_CHECK_WEBSTORM_URL -> respond(sha256(webStormBuild))
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

    fun numberOfInvocations(key: String): Int? =
        invocations[key]?.get()

    private fun putIfAbsentOrIncrement(key: String) {
        invocations.putIfAbsent(key, AtomicInteger(1))
            ?.let { invocations[key]?.incrementAndGet() }
    }

    private val pyCharmBuild = downloadFromResource("app/tars/pycharm.tar.gz")
    private val pyCharm2Build = downloadFromResource("app/tars/pycharm-2.tar.gz")
    private val webStormBuild = downloadFromResource("app/tars/webstorm.tar.gz")
    private val updates = downloadFromResource("updates/test_with_two_products.xml")
}
