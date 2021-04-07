package com.uploader

import com.uploader.TestingTool.DOWNLOAD_PYCHARM_URL
import com.uploader.container.TestDatabase
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.BuildDownloader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
    private val buildDownloader by inject<BuildDownloader>()
    private val databaseProvider by inject<DatabaseProvider>()

    private lateinit var app: TestApp
    private lateinit var db: TestDatabase

    private val mockedHttp = MockedHttp()

    @BeforeEach
    fun setup() {
        db = TestDatabase()
        app = TestApp()
        loadKoinModules(module { single(override = true) { mockedHttp.client } })
    }

    @Test
    fun test() {
        // given
        val toSave = BuildDto(
            productName = "CLion",
            fullNumber = "211.6693.66",
            channelId = "test_channel_id",
            version = "2021.3",
            state = CREATED
        )

        runBlocking { databaseProvider.dbQuery { buildRepository.insert(toSave) } }
        val saved = runBlocking {
            databaseProvider.dbQuery { buildRepository.getBy(toSave.fullNumber, toSave.channelId) }
        } ?: error("")

        // when
        val countDownLatch = CountDownLatch(4)
        repeat(5) {
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

        await()
            .atMost(30, SECONDS)
            .untilAsserted {
                val updated = runBlocking {
                    databaseProvider.dbQuery { buildRepository.getBy(saved.fullNumber, saved.channelId) }
                }

                assertThat(updated?.state, equalTo(DOWNLOADED))
            }

        // then
        assertThat(mockedHttp.invocations[DOWNLOAD_PYCHARM_URL]?.get(), equalTo(1))
    }

    private companion object : KLogging()
}
