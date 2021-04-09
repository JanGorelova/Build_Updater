package com.uploader

import com.uploader.config.AppConfig
import com.uploader.config.AppConfig.JobConfig
import com.uploader.module.JobType
import com.uploader.module.JobType.BUILD_DOWNLOAD
import com.uploader.module.JobType.PERSIST_PRODUCT_INFO
import com.uploader.module.JobType.REFRESH_PRODUCT_INFORMATION
import java.io.IOException
import java.net.ServerSocket
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom

object TestConfigProvider {
    private val occupied = mutableSetOf<Int>()

    private val config1 = JobConfig(
        delay = Duration.ofSeconds(10),
        period = Duration.ofSeconds(30)
    )
    private val config2 = JobConfig(
        delay = Duration.ofSeconds(0),
        period = Duration.ofSeconds(30)
    )

    operator fun get(environment: String): AppConfig {
        val randomPort = 8080
        val randomDbPort = 5432

        return when (environment) {
            "test" -> appConfig(
                randomPort,
                randomDbPort,
                jobs = mapOf(
                    REFRESH_PRODUCT_INFORMATION to config1,
                    BUILD_DOWNLOAD to config2,
                    PERSIST_PRODUCT_INFO to config2
                )
            )
            "testWithoutRefreshJob" -> appConfig(
                randomPort,
                randomDbPort,
                mapOf(
                    BUILD_DOWNLOAD to config2,
                    PERSIST_PRODUCT_INFO to config2
                )
            )
            else -> error("")
        }
    }

    private fun port(): Int =
        generateSequence { ThreadLocalRandom.current().nextInt(5000, 0xFFFF) }
            .filter(occupied::add)
            .filter(this::available)
            .first()

    private fun available(port: Int): Boolean =
        try {
            ServerSocket(port).close()
            true
        } catch (e: IOException) {
            false
        }

    private fun appConfig(port: Int, dbPort: Int, jobs: Map<JobType, JobConfig>) =
        AppConfig(
            host = "localhost",
            port = port,
            dbHost = "localhost",
            dbPort = dbPort,
            dbUser = "test",
            dbPassword = "test",
            dbName = "test_uploader",
            rootBuildsPath = "${Paths.get("").toRealPath()}/src/test/resources/downloads/",
            jobs = jobs
        )
}
