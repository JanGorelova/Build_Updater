package com.uploader.spec

import com.uploader.DatabaseTool.compareBuildInfos
import com.uploader.DatabaseTool.compareBuilds
import com.uploader.DatabaseTool.comparePyCharmBuildInfos
import com.uploader.DatabaseTool.getAllBuildInfos
import com.uploader.DatabaseTool.getAllBuilds
import com.uploader.MockedHttp
import com.uploader.TestApp
import com.uploader.TestingConstants.AWAIT_AT_MOST_SECONDS
import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_2_URL
import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingConstants.DOWNLOAD_WEBSTORM_URL
import com.uploader.TestingConstants.appUrl
import com.uploader.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.patch
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode.Companion.OK
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class ProductInfoRefresherResourceSpec : KoinTest {
    private lateinit var app: TestApp
    private lateinit var mockedHttp: MockedHttp
    private lateinit var config: AppConfig

    private lateinit var client: HttpClient

    @BeforeEach
    fun setup() {
        app = TestApp("testWithoutRefreshJob")
        mockedHttp = MockedHttp()
        config = app.config
        client = HttpClient()

        loadKoinModules(module { single(override = true) { mockedHttp.client } })
    }

    @Test
    fun `should init refresh process`() {
        // when
        val response = runBlocking { client.patch<HttpResponse>("${config.appUrl()}/refresh") }

        // then
        assertThat(response.status, equalTo(OK))
        await()
            .atMost(AWAIT_AT_MOST_SECONDS, SECONDS)
            .untilAsserted {
                val builds = getAllBuilds()
                assertThat(builds, hasSize(3))

                val buildInfos = getAllBuildInfos()
                assertThat(buildInfos, hasSize(3))

                compareBuilds()
                compareBuildInfos()
            }

        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_2_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_WEBSTORM_URL), equalTo(1))
    }

    @Test
    fun `should init refresh process for specified product code`() {
        // when
        val response = runBlocking { client.patch<HttpResponse>("${config.appUrl()}/refresh/PYA") }

        assertThat(response.status, equalTo(OK))
        await()
            .atMost(AWAIT_AT_MOST_SECONDS, SECONDS)
            .untilAsserted {
                val builds = getAllBuilds()
                assertThat(builds, hasSize(2))

                val buildInfos = getAllBuildInfos()
                assertThat(buildInfos, hasSize(2))
            }

        // then
        comparePyCharmBuildInfos()

        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_2_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_WEBSTORM_URL), nullValue())
    }

    @AfterEach
    fun close() {
        app.close()
        client.close()
        mockedHttp.client.close()
    }
}
