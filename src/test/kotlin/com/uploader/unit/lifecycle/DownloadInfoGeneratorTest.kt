package com.uploader.unit.lifecycle

import com.uploader.TestingConstants.PYCHARM_2_FULL_NUMBER
import com.uploader.TestingConstants.PYCHARM_VERSION
import com.uploader.TestingConstants.ROOT_DOWNLOAD_URL
import com.uploader.TestingData.py1BuildDto
import com.uploader.TestingData.py2BuildDto
import com.uploader.lifecycle.DownloadInfoGenerator
import com.uploader.lifecycle.DownloadInfoGenerator.DownloadInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.HttpTimeout
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

@KoinApiExtension
class DownloadInfoGeneratorTest : KoinTest {
    private lateinit var client: HttpClient

    private val py1CheckSum = "checkSumPY1"
    private val py2CheckSum = "checkSumPY2"
    private val productPath = "python/pycharm-professional"

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { client }
            }
        )
    }

    @BeforeEach
    fun setup() {
        client = clientMock()
    }

    @Test
    fun `should provide download info for product with version without empty spaces`() {
        // when
        val downloadInfo = runBlocking { DownloadInfoGenerator()[py1BuildDto] }

        // then
        val expected = DownloadInfo(
            downloadLink = "$ROOT_DOWNLOAD_URL/$productPath-$PYCHARM_VERSION.tar.gz",
            checkSum = py1CheckSum
        )
        assertThat(downloadInfo, equalTo(expected))
    }

    @Test
    fun `should provide information about products`() {
        // when
        val downloadInfo = runBlocking { DownloadInfoGenerator()[py2BuildDto] }

        // then
        val expected = DownloadInfo(
            downloadLink = "$ROOT_DOWNLOAD_URL/$productPath-$PYCHARM_2_FULL_NUMBER.tar.gz",
            checkSum = py2CheckSum
        )
        assertThat(downloadInfo, equalTo(expected))
    }

    private fun clientMock() = HttpClient(MockEngine) {
        install(HttpTimeout) { requestTimeoutMillis = 10000 }
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/$productPath-$PYCHARM_VERSION.tar.gz.sha256" -> respond(py1CheckSum)
                    "/$productPath-$PYCHARM_2_FULL_NUMBER.tar.gz.sha256" -> respond(py2CheckSum)
                    else -> error("Unknown url")
                }
            }
        }
    }
}
