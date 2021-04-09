package com.uploader

import com.fasterxml.jackson.databind.ObjectMapper
import com.uploader.TestingConstants.AWAIT_AT_MOST_SECONDS
import com.uploader.TestingConstants.PYCHARM_2_CHANNEL
import com.uploader.TestingConstants.PYCHARM_2_EXPECTED_INFO_JSON
import com.uploader.TestingConstants.PYCHARM_2_FULL_NUMBER
import com.uploader.TestingConstants.PYCHARM_2_VERSION
import com.uploader.TestingConstants.PYCHARM_CHANNEL
import com.uploader.TestingConstants.PYCHARM_FULL_NUMBER
import com.uploader.TestingConstants.PYCHARM_VERSION
import com.uploader.TestingConstants.WEBSTORM_CHANNEL
import com.uploader.TestingConstants.WEBSTORM_EXPECTED_INFO_JSON
import com.uploader.TestingConstants.WEBSTORM_FULL_NUMBER
import com.uploader.TestingConstants.WEBSTORM_VERSION
import com.uploader.TestingConstants.pycharm2Path
import com.uploader.TestingConstants.pycharmPath
import com.uploader.TestingConstants.webstormPath
import com.uploader.TestingTool.downloadFromResource
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.entity.Build
import com.uploader.dao.entity.BuildInfo
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.lifecycle.Constants.PYCHARM
import com.uploader.lifecycle.Constants.WEBSTORM
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.runBlocking
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object DatabaseTool : KoinComponent {
    private val provider by inject<DatabaseProvider>()
    private val buildRepository by inject<BuildRepository>()
    private val buildInfoRepository by inject<BuildInfoRepository>()

    private val jsonMapper = ObjectMapper()

    fun getAllBuilds() = runBlocking { provider.dbQuery { Build.selectAll().toList() } }

    fun getAllBuildInfos() = runBlocking { provider.dbQuery { BuildInfo.selectAll().toList() } }

    fun doInitialSetup(): List<BuildDto> {
        await()
            .atMost(AWAIT_AT_MOST_SECONDS, SECONDS)
            .untilAsserted {
                val buildInfos = getAllBuildInfos()
                assertThat(buildInfos, Matchers.hasSize(3))
            }

        val pyCharmBuild = runBlocking {
            provider.dbQuery {
                buildRepository.getByFullNumberAndChannel(PYCHARM_FULL_NUMBER, PYCHARM_CHANNEL)
            }
        } ?: error("Build with $PYCHARM_FULL_NUMBER and $PYCHARM_CHANNEL was not found")

        val pyCharmBuild2 = runBlocking {
            provider.dbQuery {
                buildRepository.getByFullNumberAndChannel(PYCHARM_2_FULL_NUMBER, PYCHARM_2_CHANNEL)
            }
        } ?: error("Build with $PYCHARM_2_FULL_NUMBER and $PYCHARM_2_CHANNEL was not found")

        val webstormBuild = runBlocking {
            provider.dbQuery {
                buildRepository.getByFullNumberAndChannel(WEBSTORM_FULL_NUMBER, WEBSTORM_CHANNEL)
            }
        } ?: error("Build with $WEBSTORM_FULL_NUMBER and $WEBSTORM_CHANNEL was not found")

        return listOf(pyCharmBuild, pyCharmBuild2, webstormBuild)
    }

    fun compareBuilds() {
        val pyCharmBuild = runBlocking {
            provider.dbQuery { buildRepository.getByFullNumberAndChannel(PYCHARM_FULL_NUMBER, PYCHARM_CHANNEL) }
        }

        val pyCharmBuild2 = runBlocking {
            provider.dbQuery {
                buildRepository.getByFullNumberAndChannel(
                    PYCHARM_2_FULL_NUMBER,
                    PYCHARM_2_CHANNEL
                )
            }
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

        val expectedPyCharmBuild2 = BuildDto(
            productName = PYCHARM,
            fullNumber = PYCHARM_2_FULL_NUMBER,
            channelId = PYCHARM_2_CHANNEL,
            version = PYCHARM_2_VERSION,
            state = DOWNLOADED,
            path = pycharm2Path(),
            id = pyCharmBuild2?.id,
            dateUpdated = pyCharmBuild2?.dateUpdated,
            dateCreated = pyCharmBuild2?.dateCreated
        )

        val webStormBuild = runBlocking {
            provider.dbQuery { buildRepository.getByFullNumberAndChannel(WEBSTORM_FULL_NUMBER, WEBSTORM_CHANNEL) }
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

        assertThat(pyCharmBuild, Matchers.equalTo(expectedPyCharmBuild))
        assertThat(pyCharmBuild2, Matchers.equalTo(expectedPyCharmBuild2))
        assertThat(webStormBuild, Matchers.equalTo(expectedWebstormBuild))
    }

    fun compareBuildInfos() {
        comparePyCharmBuildInfos()

        val actualWebStormBuildInfo = runBlocking {
            provider.dbQuery { buildInfoRepository.findAllByProductName(WEBSTORM).toList().first() }
        }
        assertJsonEquals(
            actualWebStormBuildInfo.second,
            jsonMapper.readTree(downloadFromResource(WEBSTORM_EXPECTED_INFO_JSON))
        )
    }

    fun comparePyCharmBuildInfos() {
        val actualPyCharmBuildInfos = runBlocking {
            provider.dbQuery { buildInfoRepository.findAllByProductName(PYCHARM).toList() }
        }

        actualPyCharmBuildInfos.forEach { (fullNumber, info) ->
            val expected = when (fullNumber) {
                PYCHARM_FULL_NUMBER -> jsonMapper.readTree(downloadFromResource(TestingConstants.PYCHARM_EXPECTED_INFO_JSON))
                else -> jsonMapper.readTree(downloadFromResource(PYCHARM_2_EXPECTED_INFO_JSON))
            }
            assertJsonEquals(info, expected)
        }
    }
}
