package com.uploader

import com.fasterxml.jackson.databind.ObjectMapper
import com.uploader.DatabaseTool.getAllBuildInfos
import com.uploader.DatabaseTool.getAllBuilds
import com.uploader.TestingTool.APP_URL
import com.uploader.TestingTool.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingTool.DOWNLOAD_WEBSTORM_URL
import com.uploader.TestingTool.PYCHARM_EXPECTED_INFO_JSON
import com.uploader.TestingTool.WEBSTORM_CHANNEL
import com.uploader.TestingTool.WEBSTORM_EXPECTED_INFO_JSON
import com.uploader.TestingTool.WEBSTORM_FULL_NUMBER
import com.uploader.TestingTool.WEBSTORM_VERSION
import com.uploader.TestingTool.downloadFromResource
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.dto.BuildInfoDto
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.Constants.PYCHARM
import com.uploader.provider.Constants.UPDATES_URL
import com.uploader.provider.Constants.WEBSTORM
import io.ktor.client.HttpClient
import io.ktor.client.request.patch
import io.ktor.client.statement.HttpResponse
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.runBlocking
import net.javacrumbs.jsonunit.JsonAssert
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class ProductInfoRefresherResourceSpec : KoinTest {
    private val buildInfoRepository by inject<BuildInfoRepository>()
    private val buildRepository by inject<BuildRepository>()
    private val databaseProvider by inject<DatabaseProvider>()
    private val jsonMapper by inject<ObjectMapper>()

    private lateinit var app: TestApp

    private val mockedHttp = MockedHttp()
    private val client = HttpClient()

    @BeforeEach
    fun setup() {
        app = TestApp("testWithoutRefreshJob")
        loadKoinModules(module { single(override = true) { mockedHttp.client } })
    }

    @Test
    fun `should init refresh process`() {
        // when
        val response = runBlocking { client.patch<HttpResponse>("$APP_URL/refresh") }

        assertThat(response.status.value, equalTo(200))
        await()
            .atMost(100, SECONDS)
            .untilAsserted {
                val builds = getAllBuilds()
                assertThat(builds, hasSize(2))

                val buildInfos = getAllBuildInfos()
                assertThat(buildInfos, hasSize(2))
            }

        // then
        assertThat(mockedHttp.numberOfInvocations(UPDATES_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_WEBSTORM_URL), equalTo(1))
    }

    @Test
    fun `should init refresh process for specified product code`() {
        // given
        val toSave = BuildDto(
            productName = WEBSTORM,
            fullNumber = WEBSTORM_FULL_NUMBER,
            channelId = WEBSTORM_CHANNEL,
            version = WEBSTORM_VERSION,
            state = CREATED
        )

        runBlocking {
            databaseProvider.dbQuery {
                val saved = buildRepository.insert(toSave)
                buildRepository.processing(saved, CREATED)
                buildRepository.downloaded(saved, PROCESSING, "test/path")

                val buildInfoDto = BuildInfoDto(
                    buildId = saved,
                    info = downloadFromResource(WEBSTORM_EXPECTED_INFO_JSON).decodeToString()
                )
                buildInfoRepository.insert(buildInfoDto)
            }
        }

        // when
        val response = runBlocking { client.patch<HttpResponse>("$APP_URL/refresh/PYA") }

        assertThat(response.status.value, equalTo(200))
        await()
            .atMost(100, SECONDS)
            .untilAsserted {
                val builds = getAllBuilds()
                assertThat(builds, hasSize(2))

                val buildInfos = getAllBuildInfos()
                assertThat(buildInfos, hasSize(2))
            }

        // then
        val actualWebStormBuildInfo = runBlocking {
            databaseProvider.dbQuery { buildInfoRepository.findAllByProductName(PYCHARM).toList().first() }
        }
        JsonAssert.assertJsonEquals(
            actualWebStormBuildInfo.second,
            jsonMapper.readTree(downloadFromResource(PYCHARM_EXPECTED_INFO_JSON))
        )

        assertThat(mockedHttp.numberOfInvocations(UPDATES_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_URL), equalTo(1))
        assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_WEBSTORM_URL), nullValue())
    }

    @AfterEach
    fun close() {
        app.close()
    }
}
