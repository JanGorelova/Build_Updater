package com.uploader.spec

import com.uploader.DatabaseTool.compareBuildInfos
import com.uploader.DatabaseTool.compareBuilds
import com.uploader.DatabaseTool.getAllBuildInfos
import com.uploader.DatabaseTool.getAllBuilds
import com.uploader.MockedHttp
import com.uploader.MockedHttp.numberOfInvocations
import com.uploader.TestApp
import com.uploader.TestingConstants.AWAIT_AT_MOST_SECONDS
import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_2_URL
import com.uploader.TestingConstants.DOWNLOAD_PYCHARM_URL
import com.uploader.TestingConstants.DOWNLOAD_WEBSTORM_URL
import java.util.concurrent.TimeUnit.SECONDS
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class MainFlowSpec : KoinTest {
    private lateinit var app: TestApp

    @BeforeEach
    fun setup() {
        app = TestApp("test")

        loadKoinModules(module { single(override = true) { MockedHttp.client } })
    }

    @Test
    fun `should save build, upload tar and retrieve product info`() {
        // then
        await()
            .atMost(AWAIT_AT_MOST_SECONDS, SECONDS)
            .untilAsserted {
                val builds = getAllBuilds()
                assertThat(builds, hasSize(3))

                val buildInfos = getAllBuildInfos()
                assertThat(buildInfos, hasSize(3))

                compareBuilds()
                compareBuildInfos()

                assertThat(numberOfInvocations(DOWNLOAD_PYCHARM_URL), equalTo(1))
                assertThat(numberOfInvocations(DOWNLOAD_PYCHARM_2_URL), equalTo(1))
                assertThat(numberOfInvocations(DOWNLOAD_WEBSTORM_URL), equalTo(1))
            }
    }

    @AfterEach
    fun close() {
        app.close()
        MockedHttp.reset()
    }
}
