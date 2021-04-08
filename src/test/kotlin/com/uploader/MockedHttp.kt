package com.uploader

import com.uploader.TestingTool.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingTool.DOWNLOAD_WEBSTORM_URL
import com.uploader.TestingTool.SHA_CHECK_PYCHARM_URL
import com.uploader.TestingTool.SHA_CHECK_WEBSTORM_URL
import com.uploader.TestingTool.downloadFromResource
import com.uploader.TestingTool.sha256
import com.uploader.provider.Constants.UPDATES_URL
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.utils.io.ByteReadChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class MockedHttp {
    private val invocations = ConcurrentHashMap<String, AtomicInteger>()

    val client = HttpClient(MockEngine) {
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
                    DOWNLOAD_WEBSTORM_URL -> {
                        putIfAbsentOrIncrement(DOWNLOAD_WEBSTORM_URL)
                        respond(ByteReadChannel(webStormBuild))
                    }
                    SHA_CHECK_PYCHARM_URL -> respond(sha256(pyCharmBuild))
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
    private val webStormBuild = downloadFromResource("app/tars/webstorm.tar.gz")
    private val updates = downloadFromResource("updates/test_with_two_products.xml")
}
