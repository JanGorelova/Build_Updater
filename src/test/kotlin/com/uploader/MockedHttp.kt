package com.uploader

import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_2_URL
import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingConstants.DOWNLOAD_WEBSTORM_URL
import com.uploader.TestingConstants.SHA_CHECK_PYCHARM_2_URL
import com.uploader.TestingConstants.SHA_CHECK_PYCHARM_URL
import com.uploader.TestingConstants.SHA_CHECK_WEBSTORM_URL
import com.uploader.TestingData.productsUpdates
import com.uploader.TestingTool.downloadFromResource
import com.uploader.TestingTool.sha256
import com.uploader.lifecycle.Constants.UPDATES_URL
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.HttpTimeout
import io.ktor.utils.io.ByteReadChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
object MockedHttp {
    private val invocations = ConcurrentHashMap<String, AtomicInteger>()
    val client: HttpClient

    init {
        client = HttpClient(MockEngine) {
            install(HttpTimeout) { requestTimeoutMillis = 10000 }
            engine {
                addHandler { request ->
                    when (request.url.toString()) {
                        UPDATES_URL -> {
                            respond(productsUpdates()).also { putIfAbsentOrIncrement(UPDATES_URL) }
                        }
                        DOWNLOAD_PYCHARM_URL -> {
                            respond(ByteReadChannel(pyCharmBuild)).also {
                                putIfAbsentOrIncrement(DOWNLOAD_PYCHARM_URL)
                            }
                        }
                        DOWNLOAD_PYCHARM_2_URL -> {
                            respond(ByteReadChannel(pyCharm2Build)).also {
                                putIfAbsentOrIncrement(DOWNLOAD_PYCHARM_2_URL)
                            }
                        }
                        DOWNLOAD_WEBSTORM_URL -> {
                            respond(ByteReadChannel(webStormBuild)).also {
                                putIfAbsentOrIncrement(DOWNLOAD_WEBSTORM_URL)
                            }
                        }
                        SHA_CHECK_PYCHARM_URL -> respond(sha256(pyCharmBuild))
                        SHA_CHECK_PYCHARM_2_URL -> respond(sha256(pyCharm2Build))
                        SHA_CHECK_WEBSTORM_URL -> respond(sha256(webStormBuild))
                        else -> error("Unhandled ${request.url}")
                    }
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

    fun reset() {
        invocations.clear()
    }
}
