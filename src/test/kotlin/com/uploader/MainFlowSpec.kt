package com.uploader

import com.uploader.TestingTool.PYCHARM_CHANNEL
import com.uploader.TestingTool.PYCHARM_VERSION
import com.uploader.TestingTool.WEBSTORM_CHANNEL
import com.uploader.TestingTool.WEBSTORM_VERSION
import com.uploader.container.TestDatabase
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.Constants.PYCHARM
import com.uploader.provider.Constants.WEBSTORM
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
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
        // when
        // then
        await()
            .atMost(290, SECONDS)
            .untilAsserted {
                val pyCharmBuild = runBlocking {
                    databaseProvider.dbQuery {
                        buildRepository.getBy(PYCHARM_VERSION, PYCHARM_CHANNEL)
                    }
                }

                val webStormBuild = runBlocking {
                    databaseProvider.dbQuery {
                        buildRepository.getBy(WEBSTORM_VERSION, WEBSTORM_CHANNEL)
                    }
                }

                assertThat(pyCharmBuild, notNullValue())
                assertThat(webStormBuild, notNullValue())

                val pyCharmBuildInfo = runBlocking {
                    databaseProvider.dbQuery {
                        buildInfoRepository.findAllByProductName(PYCHARM).toList()
                    }
                }

                val webStormBuildInfo = runBlocking {
                    databaseProvider.dbQuery {
                        buildInfoRepository.findAllByProductName(WEBSTORM).toList()
                    }
                }

                assertThat(pyCharmBuildInfo, hasSize(1))
                assertThat(webStormBuildInfo, hasSize(1))
            }
    }
}
