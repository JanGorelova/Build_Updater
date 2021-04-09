package com.uploader

import com.uploader.config.AppConfig
import com.uploader.config.AppConfig.JobConfig
import com.uploader.module.JobType
import com.uploader.module.JobType.BUILD_DOWNLOAD
import com.uploader.module.JobType.PERSIST_PRODUCT_INFO
import com.uploader.module.JobType.REFRESH_PRODUCT_INFORMATION
import java.nio.file.Paths
import java.time.Duration

object TestConfigProvider {
    private val config1 = JobConfig(
        delay = Duration.ofSeconds(10),
        period = Duration.ofSeconds(30)
    )
    private val config2 = JobConfig(
        delay = Duration.ofSeconds(0),
        period = Duration.ofSeconds(30)
    )

    operator fun get(environment: String): AppConfig =
        when (environment) {
            "test" -> appConfig(
                jobs = mapOf(
                    REFRESH_PRODUCT_INFORMATION to config1,
                    BUILD_DOWNLOAD to config2,
                    PERSIST_PRODUCT_INFO to config2
                )
            )
            "testWithoutRefreshJob" -> appConfig(
                jobs = mapOf(
                    BUILD_DOWNLOAD to config2,
                    PERSIST_PRODUCT_INFO to config2
                )
            )
            "testWithoutJobs" -> appConfig()
            else -> error("")
        }

    private fun appConfig(jobs: Map<JobType, JobConfig> = mapOf()) =
        AppConfig(
            host = "localhost",
            port = 8080,
            dbHost = "localhost",
            dbPort = 5432,
            dbUser = "test",
            dbPassword = "test",
            dbName = "test_uploader",
            rootBuildsPath = "${Paths.get("").toRealPath()}/src/test/resources/downloads/",
            jobs = jobs
        )
}
