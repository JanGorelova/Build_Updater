package com.uploader

import com.fasterxml.jackson.databind.ObjectMapper
import com.uploader.DatabaseTool.getAllBuildInfos
import com.uploader.DatabaseTool.getAllBuilds
import com.uploader.TestingTool.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingTool.DOWNLOAD_WEBSTORM_URL
import com.uploader.TestingTool.PYCHARM_CHANNEL
import com.uploader.TestingTool.PYCHARM_EXPECTED_INFO_JSON
import com.uploader.TestingTool.PYCHARM_FULL_NUMBER
import com.uploader.TestingTool.PYCHARM_VERSION
import com.uploader.TestingTool.WEBSTORM_CHANNEL
import com.uploader.TestingTool.WEBSTORM_EXPECTED_INFO_JSON
import com.uploader.TestingTool.WEBSTORM_FULL_NUMBER
import com.uploader.TestingTool.WEBSTORM_VERSION
import com.uploader.TestingTool.downloadFromResource
import com.uploader.TestingTool.pycharmPath
import com.uploader.TestingTool.webstormPath
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.Constants.PYCHARM
import com.uploader.provider.Constants.WEBSTORM
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.runBlocking
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class MainFlowSpec : KoinTest {
    private val buildInfoRepository by inject<BuildInfoRepository>()
    private val buildRepository by inject<BuildRepository>()
    private val databaseProvider by inject<DatabaseProvider>()
    private val jsonMapper by inject<ObjectMapper>()

    private lateinit var app: TestApp

    private val mockedHttp = MockedHttp()

    @BeforeEach
    fun setup() {
        app = TestApp("test")
        loadKoinModules(module { single(override = true) { mockedHttp.client } })
    }

    @Test
    fun `should save build, upload tar and retrieve product info`() {
        // then
        await()
            .atMost(100, SECONDS)
            .untilAsserted {
                val builds = getAllBuilds()
                assertThat(builds, hasSize(2))

                val buildInfos = getAllBuildInfos()
                assertThat(buildInfos, hasSize(2))

                compareBuilds()
                compareBuildInfos()

                assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_URL), equalTo(1))
                assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_WEBSTORM_URL), equalTo(1))
            }
    }

    private fun compareBuilds() {
        val pyCharmBuild = runBlocking {
            databaseProvider.dbQuery { buildRepository.getBy(PYCHARM_FULL_NUMBER, PYCHARM_CHANNEL) }
        }

        val expectedPyCharmBuild = BuildDto(
            productName = PYCHARM,
            fullNumber = PYCHARM_FULL_NUMBER,
            channelId = PYCHARM_CHANNEL,
            version = PYCHARM_VERSION,
            state = DOWNLOADED,
            path = pycharmPath(),
            id = pyCharmBuild?.id,
            dateUpdated = pyCharmBuild?.dateUpdated,
            dateCreated = pyCharmBuild?.dateCreated
        )

        val webStormBuild = runBlocking {
            databaseProvider.dbQuery { buildRepository.getBy(WEBSTORM_FULL_NUMBER, WEBSTORM_CHANNEL) }
        }

        val expectedWebstormBuild = BuildDto(
            productName = WEBSTORM,
            fullNumber = WEBSTORM_FULL_NUMBER,
            channelId = WEBSTORM_CHANNEL,
            version = WEBSTORM_VERSION,
            state = DOWNLOADED,
            path = webstormPath(),
            id = webStormBuild?.id,
            dateUpdated = webStormBuild?.dateUpdated,
            dateCreated = webStormBuild?.dateCreated
        )

        assertThat(pyCharmBuild, equalTo(expectedPyCharmBuild))
        assertThat(webStormBuild, equalTo(expectedWebstormBuild))
    }

    private fun compareBuildInfos() {
        val actualPyCharmBuildInfo = runBlocking {
            databaseProvider.dbQuery { buildInfoRepository.findAllByProductName(PYCHARM).toList().first() }
        }
        assertJsonEquals(
            actualPyCharmBuildInfo.second,
            jsonMapper.readTree(downloadFromResource(PYCHARM_EXPECTED_INFO_JSON))
        )

        val actualWebStormBuildInfo = runBlocking {
            databaseProvider.dbQuery { buildInfoRepository.findAllByProductName(WEBSTORM).toList().first() }
        }
        assertJsonEquals(
            actualWebStormBuildInfo.second,
            jsonMapper.readTree(downloadFromResource(WEBSTORM_EXPECTED_INFO_JSON))
        )
    }

    @AfterEach
    fun close() {
        app.close()
    }
}
