package com.uploader.spec

import com.uploader.MockedHttp
import com.uploader.TestApp
import com.uploader.TestingConstants.AWAIT_AT_MOST_SECONDS
import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingConstants.PYCHARM_CHANNEL
import com.uploader.TestingConstants.PYCHARM_FULL_NUMBER
import com.uploader.TestingConstants.PYCHARM_VERSION
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.BuildDownloader
import com.uploader.provider.Constants.PYCHARM
import com.uploader.provider.Constants.UPDATES_URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class BuildUploadRaceConditionSpec : KoinTest {
    private val buildRepository by inject<BuildRepository>()
    private val buildInfoRepository by inject<BuildInfoRepository>()
    private val buildDownloader by inject<BuildDownloader>()
    private val databaseProvider by inject<DatabaseProvider>()

    private lateinit var app: TestApp
    private lateinit var mockedHttp: MockedHttp

    @BeforeEach
    fun setup() {
        mockedHttp = MockedHttp()
        app = TestApp("testWithoutRefreshJob")
        loadKoinModules(module { single(override = true) { mockedHttp.client } })
    }

    @Test
    fun `file should be downloaded only one time`() {
        // given
        val toSave = BuildDto(
            productName = PYCHARM,
            fullNumber = PYCHARM_FULL_NUMBER,
            channelId = PYCHARM_CHANNEL,
            version = PYCHARM_VERSION,
            state = CREATED
        )

        runBlocking { databaseProvider.dbQuery { buildRepository.insert(toSave) } }
        val saved = runBlocking {
            databaseProvider.dbQuery { buildRepository.getBy(toSave.fullNumber, toSave.channelId) }
        } ?: error("Build $toSave was not saved")

        // when
        val countDownLatch = CountDownLatch(4)
        repeat(10) {
            GlobalScope.launch {
                countDownLatch.countDown()
                countDownLatch.await()
                try {
                    buildDownloader.download(saved)
                } catch (e: Exception) {
                    logger.error { e }
                }
            }
        }

        // then
        await()
            .atMost(AWAIT_AT_MOST_SECONDS, SECONDS)
            .untilAsserted {
                val updated = runBlocking {
                    databaseProvider.dbQuery { buildRepository.getBy(saved.fullNumber, saved.channelId) }
                }

                assertThat(updated?.state, equalTo(DOWNLOADED))

                val info = runBlocking {
                    databaseProvider.dbQuery {
                        buildInfoRepository.findByBuildId(saved.id ?: error(""))
                    }
                }

                assertThat(info, notNullValue())
                assertThat(mockedHttp.numberOfInvocations(DOWNLOAD_PYCHARM_URL), equalTo(1))
                assertThat(mockedHttp.numberOfInvocations(UPDATES_URL), nullValue())
            }
    }

    @AfterEach
    fun close() {
        app.close()
        mockedHttp.client.close()
    }

    private companion object : KLogging()
}
